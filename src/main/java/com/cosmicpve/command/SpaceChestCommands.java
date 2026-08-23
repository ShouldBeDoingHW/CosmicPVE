package com.cosmicpve.command;

import com.cosmicpve.spacechest.SpaceChestSessionService;
import com.cosmicpve.spacechest.SpaceChestTier;
import com.cosmicpve.spacechest.SpaceChests;
import com.cosmicpve.reward.RewardDeliveryService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public final class SpaceChestCommands {
    private static final RewardDeliveryService DELIVERY = new RewardDeliveryService();
    private SpaceChestCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("space-chest")
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.Arrays.stream(SpaceChestTier.values()).map(SpaceChestTier::serializedName), builder))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> give(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        StringArgumentType.getString(context, "tier"),
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(Commands.literal("session")
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("player", EntityArgument.player()).executes(context -> inspect(
                                        context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player()).executes(context -> reset(
                                        context.getSource(), EntityArgument.getPlayer(context, "player"))))));
    }
    private static int give(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            String rawTier, int count) {
        SpaceChestTier tier;
        try { tier = SpaceChestTier.valueOf(rawTier.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { source.sendFailure(Component.literal("Unknown Space Chest tier.")); return 0; }
        DELIVERY.deliver(player, SpaceChests.createMany(tier, count));
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + tier + " Space Chest(s) to "
                + player.getName().getString() + "."), true);
        return count;
    }
    private static int inspect(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player) {
        source.sendSuccess(() -> Component.literal(SpaceChestSessionService.INSTANCE.inspect(player)), false);
        return 1;
    }
    private static int reset(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player) {
        SpaceChestSessionService.INSTANCE.reset(player);
        source.sendSuccess(() -> Component.literal("Recovered and reset the Space Chest session."), true);
        return 1;
    }
}
