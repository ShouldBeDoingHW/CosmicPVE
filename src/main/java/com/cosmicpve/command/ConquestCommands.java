package com.cosmicpve.command;

import com.cosmicpve.conquest.ConquestEventService;
import com.cosmicpve.conquest.ConquestOrigin;
import com.cosmicpve.conquest.ConquestRuntime;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.RewardDeliveryService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ConquestCommands {
    private ConquestCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("conquest")
                .then(Commands.literal("spawn")
                        .then(Commands.literal("natural").executes(context -> spawn(context.getSource(), ConquestOrigin.NATURAL)))
                        .then(Commands.literal("flare").executes(context -> spawn(context.getSource(), ConquestOrigin.FLARE))))
                .then(Commands.literal("spawn-here").executes(context -> spawnHere(context.getSource())))
                .then(Commands.literal("list").executes(context -> list(context.getSource())))
                .then(Commands.literal("inspect").then(eventArgument().executes(context -> inspect(
                        context.getSource(), StringArgumentType.getString(context, "event")))))
                .then(Commands.literal("expire").then(eventArgument().executes(context -> expire(
                        context.getSource(), StringArgumentType.getString(context, "event")))))
                .then(Commands.literal("flare").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> giveFlare(context.getSource(),
                                        EntityArgument.getPlayer(context, "player"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> giveFlare(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "count")))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack, String> eventArgument() {
        return Commands.argument("event", StringArgumentType.word()).suggests((context, builder) ->
                SharedSuggestionProvider.suggest(ConquestRuntime.events().active(context.getSource().getServer())
                        .stream().map(event -> event.id().toString()), builder));
    }

    private static int spawn(net.minecraft.commands.CommandSourceStack source, ConquestOrigin origin) {
        var level = source.getLevel();
        BlockPos center = BlockPos.containing(source.getPosition());
        var result = ConquestRuntime.events().spawnNear(level, origin, center,
                ConquestEventService.FLARE_RADIUS, level.getRandom(), origin == ConquestOrigin.NATURAL);
        if (result.isEmpty()) {
            source.sendFailure(Component.literal("Unable to find and create a valid Conquest Chest after "
                    + ConquestEventService.PLACEMENT_ATTEMPTS + " attempts."));
            return 0;
        }
        var event = result.orElseThrow();
        source.sendSuccess(() -> Component.literal("Created " + origin + " Conquest event " + event.id()
                + " at " + event.chestPosition().toShortString() + "."), true);
        return 1;
    }

    private static int spawnHere(net.minecraft.commands.CommandSourceStack source) {
        var result = ConquestRuntime.events().spawnNearest(source.getLevel(),
                BlockPos.containing(source.getPosition()), 16);
        if (result.isEmpty()) {
            source.sendFailure(Component.literal("Unable to create a Conquest Chest near the command source after "
                    + ConquestEventService.PLACEMENT_ATTEMPTS + " deterministic attempts."));
            return 0;
        }
        var event = result.orElseThrow();
        source.sendSuccess(() -> Component.literal("Created FLARE Conquest event " + event.id()
                + " at " + event.chestPosition().toShortString() + " through the production pipeline."), true);
        return 1;
    }

    private static int list(net.minecraft.commands.CommandSourceStack source) {
        var events = ConquestRuntime.events().active(source.getServer());
        source.sendSuccess(() -> Component.literal("Active Conquest events: " + events.size()), false);
        events.forEach(event -> source.sendSuccess(() -> Component.literal(event.id() + " " + event.origin()
                + " " + event.chestPosition().toShortString()), false));
        return events.size();
    }

    private static int inspect(net.minecraft.commands.CommandSourceStack source, String text) {
        try {
            var event = ConquestRuntime.events().find(source.getServer(), UUID.fromString(text));
            if (event.isEmpty()) { source.sendFailure(Component.literal("Unknown Conquest event.")); return 0; }
            var value = event.orElseThrow();
            long age = Math.max(0L, source.getLevel().getGameTime() - value.createdGameTime());
            String remaining = value.origin() == ConquestOrigin.NATURAL && !value.interacted()
                    ? ConquestEventService.remainingMinutes(age) + "m" : "disabled";
            var level = source.getServer().overworld();
            var actual = level.getBlockState(value.chestPosition());
            var blockEntity = level.getBlockEntity(value.chestPosition());
            source.sendSuccess(() -> Component.literal("Conquest " + value.id() + ": type=" + value.origin()
                    + " pos=" + value.chestPosition().toShortString() + " interacted=" + value.interacted()
                    + " pirates=" + value.piratesSpawned() + " ageTicks=" + age + " expiry=" + remaining
                    + " bounds=" + value.bounds() + " chunkLoaded=" + level.hasChunkAt(value.chestPosition())
                    + " block=" + actual.getBlock().builtInRegistryHolder().getRegisteredName()
                    + " expectedBlock=" + actual.is(com.cosmicpve.registry.ModBlocks.CONQUEST_CHEST.get())
                    + " blockEntity=" + (blockEntity == null ? "none" : blockEntity.getClass().getSimpleName())), false);
            return 1;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid event UUID.")); return 0;
        }
    }

    private static int expire(net.minecraft.commands.CommandSourceStack source, String text) {
        try {
            boolean removed = ConquestRuntime.events().expire(source.getLevel(), UUID.fromString(text));
            if (!removed) { source.sendFailure(Component.literal("Unknown active Conquest event.")); return 0; }
            source.sendSuccess(() -> Component.literal("Removed Conquest event " + text + "."), true);
            return 1;
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Invalid event UUID.")); return 0;
        }
    }

    private static int giveFlare(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, int count) {
        new RewardDeliveryService().deliver(player, List.of(new ItemStack(ModItems.CONQUEST_CHEST_FLARE.get(), count)));
        source.sendSuccess(() -> Component.literal("Gave " + count + " Conquest Chest Flare(s)."), true);
        return count;
    }
}
