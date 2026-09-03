package com.cosmicpve.command;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.upgrade.PlayerUpgrade;
import com.cosmicpve.upgrade.PlayerUpgradeService;
import com.cosmicpve.upgrade.PlayerUpgradesMenu;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;

public final class PlayerUpgradeCommands {
    private static final PlayerUpgradeService SERVICE = new PlayerUpgradeService();

    private PlayerUpgradeCommands() {}

    public static void registerPublic(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("upgrades")
                .executes(context -> open(context.getSource().getPlayerOrException())));
    }

    private static int open(ServerPlayer player) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inventory, ignored) -> new PlayerUpgradesMenu(id, inventory, player),
                PlayerUpgradesMenu.TITLE));
        return 1;
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("upgrades")
                .then(Commands.literal("give-crystals")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(context -> giveCrystals(
                                                EntityArgument.getPlayer(context, "player"),
                                                IntegerArgumentType.getInteger(context, "count"))))))
                .then(Commands.literal("bank")
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> inspectBank(
                                                context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                .executes(context -> SERVICE.setBanked(
                                                        EntityArgument.getPlayer(context, "player"),
                                                        LongArgumentType.getLong(context, "amount")) ? 1 : 0))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes(context -> SERVICE.addBanked(
                                                        EntityArgument.getPlayer(context, "player"),
                                                        LongArgumentType.getLong(context, "amount")) ? 1 : 0)))))
                .then(Commands.literal("tier")
                        .then(Commands.literal("inspect")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> inspectTiers(
                                                context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("upgrade", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(PlayerUpgrade.values()).map(value -> value.id().getPath()),
                                                        builder))
                                                .then(Commands.argument("tier", IntegerArgumentType.integer(0, 5))
                                                        .executes(context -> setTier(
                                                                EntityArgument.getPlayer(context, "player"),
                                                                StringArgumentType.getString(context, "upgrade"),
                                                                IntegerArgumentType.getInteger(context, "tier"))))))));
    }

    private static int giveCrystals(ServerPlayer player, int count) {
        new RewardDeliveryService().deliver(player, java.util.List.of(new ItemStack(ModItems.UPGRADE_CRYSTAL.get(), count)));
        return count;
    }

    private static int inspectBank(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal(
                player.getName().getString() + " has " + SERVICE.banked(player) + " banked Upgrade Crystals."), false);
        return 1;
    }

    private static int inspectTiers(CommandSourceStack source, ServerPlayer player) {
        String tiers = Arrays.stream(PlayerUpgrade.values())
                .map(upgrade -> upgrade.id().getPath() + "=" + SERVICE.tier(player, upgrade))
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal(player.getName().getString() + ": " + tiers), false);
        return 1;
    }

    private static int setTier(ServerPlayer player, String rawUpgrade, int tier) {
        PlayerUpgrade upgrade = Arrays.stream(PlayerUpgrade.values())
                .filter(value -> value.id().getPath().equals(rawUpgrade))
                .findFirst()
                .orElse(null);
        return upgrade != null && SERVICE.setTier(player, upgrade, tier) ? 1 : 0;
    }
}
