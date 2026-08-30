package com.cosmicpve.command;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.ArmorSetCrystalData;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.SharedSuggestionProvider;

public final class ArmorSetCommands {
    private ArmorSetCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        var success = Commands.argument("success-rate", IntegerArgumentType.integer(1, 100))
                .executes(context -> give(context.getSource(), EntityArgument.getPlayer(context, "player"),
                        IdentifierArgument.getId(context, "set-id"),
                        IntegerArgumentType.getInteger(context, "success-rate")));
        var setId = Commands.argument("set-id", IdentifierArgument.id()).then(success);
        var crystalPlayer = Commands.argument("player", EntityArgument.player()).then(setId);
        var crystal = Commands.literal("crystal").then(Commands.literal("give").then(crystalPlayer));
        var omni = Commands.literal("omni")
                        .then(Commands.literal("mark").executes(context -> setOmni(context.getSource(), true)))
                        .then(Commands.literal("unmark").executes(context -> setOmni(context.getSource(), false)));
        var activityName = Commands.argument("activity", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        java.util.List.of("none", "trial", "dungeon", "invasion"), builder))
                .executes(context -> setActivity(context.getSource(), EntityArgument.getPlayer(context, "player"),
                        StringArgumentType.getString(context, "activity")));
        var activity = Commands.literal("activity").then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player()).then(activityName)));
        var inspect = Commands.literal("inspect").then(Commands.argument("player", EntityArgument.player())
                .executes(context -> inspect(context.getSource(), EntityArgument.getPlayer(context, "player"))));
        return Commands.literal("armor").then(crystal).then(omni).then(activity).then(inspect);
    }

    private static int give(net.minecraft.commands.CommandSourceStack source, net.minecraft.server.level.ServerPlayer player,
            net.minecraft.resources.Identifier id, int successRate) {
        var definition = CosmicContent.repository().findArmorSetDefinition(id);
        if (definition.isEmpty()) {
            source.sendFailure(Component.translatable("command.cosmicpve.armor.unknown_set", id));
            return 0;
        }
        ItemStack stack = com.cosmicpve.equipment.armor.ArmorSetCrystals.create(definition.orElseThrow(), successRate);
        player.getInventory().placeItemBackInInventory(stack);
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.armor.given", id, successRate, player.getName()), true);
        return 1;
    }

    private static int setOmni(net.minecraft.commands.CommandSourceStack source, boolean enabled)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ItemStack held = source.getPlayerOrException().getMainHandItem();
        if (!com.cosmicpve.equipment.armor.ArmorSetResolver.isArmor(held)) {
            source.sendFailure(Component.literal("Hold an armor item in your main hand.")); return 0;
        }
        if (enabled) held.set(ModDataComponents.OMNI_ARMOR.get(), true);
        else held.remove(ModDataComponents.OMNI_ARMOR.get());
        source.sendSuccess(() -> Component.literal((enabled ? "Marked " : "Unmarked ")
                + held.getHoverName().getString() + " as Omni."), false);
        return 1;
    }

    private static int setActivity(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player, String value) {
        com.cosmicpve.activity.ActivityType type;
        try { type = com.cosmicpve.activity.ActivityType.valueOf(value.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException exception) { source.sendFailure(Component.literal("Unknown activity: " + value)); return 0; }
        com.cosmicpve.combat.CosmicCombat.activities().set(player.getUUID(), type);
        source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + " activity to " + type + "."), true);
        return 1;
    }

    private static int inspect(net.minecraft.commands.CommandSourceStack source,
            net.minecraft.server.level.ServerPlayer player) {
        var potential = com.cosmicpve.combat.CosmicCombat.armorSets().resolvePotential(player)
                .map(definition -> definition.id().toString()).orElse("none");
        var active = com.cosmicpve.combat.CosmicCombat.armorSets().resolve(player)
                .map(definition -> definition.id().toString()).orElse("none");
        double raw = com.cosmicpve.combat.CosmicCombat.movement().rawBonus(player);
        double effective = com.cosmicpve.equipment.armor.CosmicMovementBonusService.capped(raw);
        var activity = com.cosmicpve.combat.CosmicCombat.activities().current(player);
        source.sendSuccess(() -> Component.literal("Armor set for " + player.getName().getString()
                + ": potential=" + potential + ", active=" + active
                + ", suppressed=" + com.cosmicpve.combat.CosmicCombat.armorSets().isSuppressed(player)
                + ", activity=" + activity + ", cosmic_movement=" + Math.round(effective * 100)
                + "% (raw " + Math.round(raw * 100) + "%)."), false);
        return 1;
    }
}
