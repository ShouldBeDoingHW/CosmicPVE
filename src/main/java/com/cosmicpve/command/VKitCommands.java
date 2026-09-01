package com.cosmicpve.command;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.vkit.VKitDefinition;
import com.cosmicpve.vkit.VKitEquipmentGenerator;
import com.cosmicpve.vkit.VKitEquipmentType;
import com.cosmicpve.vkit.VKitProgressionService;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class VKitCommands {
    private static final com.mojang.brigadier.exceptions.DynamicCommandExceptionType INVALID_ARGUMENT =
            new com.mojang.brigadier.exceptions.DynamicCommandExceptionType(value ->
                    Component.literal("Invalid V-Kit argument: " + value));
    private static final VKitProgressionService PROGRESSION = new VKitProgressionService();
    private static final VKitEquipmentGenerator GENERATOR = new VKitEquipmentGenerator();
    private static final RewardDeliveryService DELIVERY = new RewardDeliveryService();

    private VKitCommands() {}

    public static void registerPublic(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("vkit").executes(context -> {
            var player = context.getSource().getPlayerOrException();
            player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                    (id, inventory, ignored) -> new com.cosmicpve.vkit.VKitInfoMenu(id, inventory, player),
                    com.cosmicpve.vkit.VKitInfoMenu.TITLE));
            return 1;
        }));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("vkit")
                .then(Commands.literal("inspect")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> inspect(context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(kitArgument().then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
                                        .executes(context -> set(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"),
                                                definition(context), IntegerArgumentType.getInteger(context, "level")))))))
                .then(Commands.literal("reset")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> resetAll(context.getSource(),
                                        EntityArgument.getPlayer(context, "player")))
                                .then(kitArgument().executes(context -> set(context.getSource(),
                                        EntityArgument.getPlayer(context, "player"), definition(context), 0)))))
                .then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(kitArgument()
                                        .executes(context -> give(context.getSource(),
                                                EntityArgument.getPlayer(context, "player"), definition(context), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(context -> give(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"), definition(context),
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(Commands.literal("generate")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(kitArgument().then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                java.util.List.of("armor", "weapon"), builder))
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                                .executes(context -> generate(context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"), definition(context),
                                                        type(context), IntegerArgumentType.getInteger(context, "level"),
                                                        context.getSource().getLevel().getRandom()))
                                                .then(Commands.argument("seed", LongArgumentType.longArg())
                                                        .executes(context -> generate(context.getSource(),
                                                                EntityArgument.getPlayer(context, "player"), definition(context),
                                                                type(context), IntegerArgumentType.getInteger(context, "level"),
                                                                RandomSource.create(LongArgumentType.getLong(context, "seed"))))))))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<net.minecraft.commands.CommandSourceStack,
            net.minecraft.resources.Identifier> kitArgument() {
        return Commands.argument("kit", IdentifierArgument.id()).suggests((context, builder) ->
                SharedSuggestionProvider.suggestResource(VKitDefinition.ALL.stream().map(VKitDefinition::id), builder));
    }

    private static VKitDefinition definition(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var id = IdentifierArgument.getId(context, "kit");
        return VKitDefinition.find(id).orElseThrow(() -> INVALID_ARGUMENT.create(id));
    }

    private static VKitEquipmentType type(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        String value = StringArgumentType.getString(context, "type");
        for (VKitEquipmentType type : VKitEquipmentType.values()) {
            if (type.getSerializedName().equals(value)) return type;
        }
        throw INVALID_ARGUMENT.create(value);
    }

    private static int inspect(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        String levels = VKitDefinition.ALL.stream()
                .map(definition -> {
                    int level = PROGRESSION.level(player, definition);
                    return definition.displayName() + ": " + (level == 0
                            ? "Locked" : VKitEquipmentGenerator.roman(level) + " (" + level + ")");
                })
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal(player.getName().getString() + " V-Kits: " + levels), false);
        return 1;
    }

    private static int set(net.minecraft.commands.CommandSourceStack source, ServerPlayer player,
            VKitDefinition definition, int level) {
        PROGRESSION.set(player, definition, level);
        source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + "'s "
                + definition.displayName() + " V-Kit to " + level + "."), true);
        return 1;
    }

    private static int resetAll(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        VKitDefinition.ALL.forEach(definition -> PROGRESSION.set(player, definition, 0));
        source.sendSuccess(() -> Component.literal("Reset all V-Kits for " + player.getName().getString() + "."), true);
        return 1;
    }

    private static int give(net.minecraft.commands.CommandSourceStack source, ServerPlayer player,
            VKitDefinition definition, int count) {
        Item crystal = crystal(definition);
        DELIVERY.deliver(player, java.util.List.of(new ItemStack(crystal, count)));
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + definition.displayName()
                + " V-Kit Crystal(s)."), true);
        return count;
    }

    private static int generate(net.minecraft.commands.CommandSourceStack source, ServerPlayer player,
            VKitDefinition definition, VKitEquipmentType type, int level, RandomSource random) {
        ItemStack reward = GENERATOR.generate(definition, type, level,
                player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT), random);
        DELIVERY.deliver(player, java.util.List.of(reward));
        source.sendSuccess(() -> Component.literal("Generated " + definition.displayName() + " "
                + type.getSerializedName() + " at level " + level + "."), true);
        return 1;
    }

    private static Item crystal(VKitDefinition definition) {
        if (definition == VKitDefinition.PHOENIX) return ModItems.PHOENIX_VKIT_CRYSTAL.get();
        if (definition == VKitDefinition.OGRE) return ModItems.OGRE_VKIT_CRYSTAL.get();
        if (definition == VKitDefinition.JUDGEMENT) return ModItems.JUDGEMENT_VKIT_CRYSTAL.get();
        if (definition == VKitDefinition.SLAYER) return ModItems.SLAYER_VKIT_CRYSTAL.get();
        throw new IllegalArgumentException("Unknown V-Kit " + definition.id());
    }
}
