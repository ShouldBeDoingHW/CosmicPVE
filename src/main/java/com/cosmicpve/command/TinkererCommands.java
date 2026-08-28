package com.cosmicpve.command;

import com.cosmicpve.equipment.enchantment.CosmicDustService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.tinkerer.TinkererMenu;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;

public final class TinkererCommands {
    private TinkererCommands() {}

    public static void registerPublic(net.neoforged.neoforge.event.RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("tinkerer").executes(context -> {
            var player = context.getSource().getPlayerOrException();
            player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new TinkererMenu(id, inventory),
                    Component.translatable("menu.cosmicpve.tinkerer")));
            return 1;
        }));
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("tinkerer").then(Commands.literal("dust").then(Commands.literal("give")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        Arrays.stream(CosmicEnchantmentTier.values()).map(CosmicEnchantmentTier::serializedName), builder))
                                .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                        StringArgumentType.getString(context, "tier"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 640))
                                        .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                StringArgumentType.getString(context, "tier"),
                                                IntegerArgumentType.getInteger(context, "count"))))))));
    }

    private static int give(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String tierName, int count) {
        final CosmicEnchantmentTier tier;
        try { tier = CosmicEnchantmentTier.valueOf(tierName.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) {
            source.sendFailure(Component.literal("Unknown Cosmic rarity: " + tierName));
            return 0;
        }
        int remaining = count;
        while (remaining > 0) {
            int portion = Math.min(64, remaining);
            player.getInventory().placeItemBackInInventory(CosmicDustService.dust(tier, portion));
            remaining -= portion;
        }
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + tier.serializedName() + " Cosmic Dust."), true);
        return count;
    }
}
