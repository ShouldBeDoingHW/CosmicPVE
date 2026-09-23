package com.cosmicpve.command;

import com.cosmicpve.cosmiccrate.CosmicCrateSeason;
import com.cosmicpve.cosmiccrate.CosmicCrateSide;
import com.cosmicpve.cosmiccrate.SeasonalCosmicCrates;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Operator fixtures for validating the placeholder premium-container milestone. */
public final class PremiumContainerCommands {
    private PremiumContainerCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var season = Commands.argument("season", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        java.util.Arrays.stream(CosmicCrateSeason.values()).map(CosmicCrateSeason::id), builder));
        var side = Commands.argument("side", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(java.util.List.of("left", "right"), builder))
                .executes(context -> giveHalf(context.getSource(), EntityArgument.getPlayer(context, "player"),
                        StringArgumentType.getString(context, "season"), StringArgumentType.getString(context, "side")));
        var halfSeason = Commands.argument("season", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        java.util.Arrays.stream(CosmicCrateSeason.values()).map(CosmicCrateSeason::id), builder))
                .then(side);
        return Commands.literal("containers")
                .then(Commands.literal("memory-chest").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.MEMORY_CHEST.get())))))
                .then(Commands.literal("trials-creation-kit").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.TRIALS_CREATION_KIT.get())))))
                .then(Commands.literal("cosmic-swag-bag").then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.COSMIC_SWAG_BAG.get())))))
                .then(Commands.literal("cosmic-crate")
                        .then(Commands.literal("give").then(Commands.argument("player", EntityArgument.player())
                                .then(season.executes(context -> giveCrate(context.getSource(),
                                        EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "season"))))))
                        .then(Commands.literal("give-half").then(Commands.argument("player", EntityArgument.player())
                                .then(halfSeason))))
                .then(Commands.literal("higher-lore-orb").then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("armor-9").executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_9_LORE.get()))))
                        .then(Commands.literal("armor-10").executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_10_LORE.get()))))
                        .then(Commands.literal("weapon-11").executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_11_LORE.get()))))
                        .then(Commands.literal("weapon-12").executes(context -> give(EntityArgument.getPlayer(context, "player"),
                                new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_12_LORE.get()))))));
    }

    private static int giveCrate(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String value) {
        var season = season(source, value);
        return season == null ? 0 : give(player, SeasonalCosmicCrates.complete(season));
    }

    private static int giveHalf(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String seasonValue, String sideValue) {
        var season = season(source, seasonValue);
        if (season == null) return 0;
        try {
            return give(player, SeasonalCosmicCrates.half(season,
                    CosmicCrateSide.valueOf(sideValue.toUpperCase(Locale.ROOT))));
        } catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Unknown crate side: " + sideValue));
            return 0;
        }
    }

    private static CosmicCrateSeason season(net.minecraft.commands.CommandSourceStack source, String value) {
        try { return CosmicCrateSeason.valueOf(value.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Unknown Cosmic Crate season: " + value));
            return null;
        }
    }

    private static int give(net.minecraft.server.level.ServerPlayer player, ItemStack stack) {
        new com.cosmicpve.reward.RewardDeliveryService().deliver(player, java.util.List.of(stack));
        return 1;
    }
}
