package com.cosmicpve.command;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.execution.ExecutionCause;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.DevelopmentProcFixtures;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CosmicCommands {
    private CosmicCommands() {}

    public static void register(RegisterCommandsEvent event) {
        EconomyCommands.registerPublic(event);
        EnchanterCommands.registerPublic(event);
        FlashSaleCommands.registerPublic(event);
        MaskLimitCommands.register(event);
        TinkererCommands.registerPublic(event);
        PersonalVaultCommands.registerPublic(event);
        VKitCommands.registerPublic(event);
        PlayerUpgradeCommands.registerPublic(event);
        FameCommands.registerPublic(event);
        AdventureInfoCommands.registerPublic(event);
        var combat = Commands.literal("combat")
                .then(Commands.literal("trace")
                        .then(Commands.literal("on").executes(context -> setTrace(context.getSource(), true)))
                        .then(Commands.literal("off").executes(context -> setTrace(context.getSource(), false)))
                        .then(Commands.literal("last").executes(context -> showLast(context.getSource()))))
                .then(Commands.literal("execute")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> executeTarget(
                                        context.getSource(), EntityArgument.getEntity(context, "target")))))
                .then(Commands.literal("true-damage")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.001F))
                                        .executes(context -> trueDamageTarget(
                                                context.getSource(),
                                                EntityArgument.getEntity(context, "target"),
                                                FloatArgumentType.getFloat(context, "amount"))))))
                .then(Commands.literal("set-health")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("amount", FloatArgumentType.floatArg(0.001F))
                                        .executes(context -> setHealth(
                                                context.getSource(), EntityArgument.getEntity(context, "target"),
                                                FloatArgumentType.getFloat(context, "amount"))))));
        var proc = Commands.literal("proc")
                .then(Commands.literal("trace")
                        .then(Commands.literal("on").executes(context -> setProcTrace(context.getSource(), true)))
                        .then(Commands.literal("off").executes(context -> setProcTrace(context.getSource(), false)))
                        .then(Commands.literal("last").executes(context -> showLastProc(context.getSource()))))
                .then(Commands.literal("test")
                        .then(Commands.literal("hit").executes(context -> testProc(
                                context.getSource(), RecursionPolicy.NORMAL)))
                        .then(Commands.literal("no-procs").executes(context -> testProc(
                                context.getSource(), RecursionPolicy.NO_PROCS))));
        var cooldown = Commands.literal("cooldown")
                .then(Commands.literal("list").executes(context -> listCooldowns(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("key", IdentifierArgument.id())
                                .then(Commands.argument("ticks", IntegerArgumentType.integer(0))
                                        .executes(context -> setCooldown(
                                                context.getSource(),
                                                IdentifierArgument.getId(context, "key"),
                                                IntegerArgumentType.getInteger(context, "ticks"))))))
                .then(Commands.literal("clear")
                        .then(Commands.literal("all").executes(context -> clearAllCooldowns(context.getSource())))
                        .then(Commands.argument("key", IdentifierArgument.id())
                                .executes(context -> clearCooldown(
                                        context.getSource(), IdentifierArgument.getId(context, "key")))));
        event.getDispatcher().register(Commands.literal("cosmic")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .then(combat)
                .then(proc)
                .then(cooldown)
                .then(ArmorSetCommands.create())
                .then(EnchantingCommands.create())
                .then(WeaponSkinCommands.create())
                .then(AccessoryCommands.create())
                .then(HeroicCommands.create())
                .then(EconomyCommands.create())
                .then(FlashSaleCommands.create())
                .then(FoodDebugCommands.create())
                .then(RewardCommands.create())
                .then(SpaceChestCommands.create())
                .then(PremiumContainerCommands.create())
                .then(com.cosmicpve.adventure.AdventureCommands.create())
                .then(TrialCommands.create())
                .then(ConquestCommands.create())
                .then(TinkererCommands.create())
                .then(CombatStackCommands.create())
                .then(MaskCommands.create())
                .then(VKitCommands.create())
                .then(PersonalVaultCommands.create())
                .then(PlayerUpgradeCommands.create())
                .then(FameCommands.create()));




        event.getDispatcher().register(Commands.literal("feed")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .executes(context -> feed(context.getSource())));
        event.getDispatcher().register(Commands.literal("heal")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .executes(context -> heal(context.getSource())));
        event.getDispatcher().register(Commands.literal("restore")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .executes(context -> restore(context.getSource())));
    }

    private static int feed(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        PlayerUtilityService.feed(source.getPlayerOrException());
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.feed"), false);
        return 1;
    }

    private static int heal(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        PlayerUtilityService.heal(source.getPlayerOrException());
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.heal"), false);
        return 1;
    }

    private static int restore(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        PlayerUtilityService.restore(source.getPlayerOrException());
        source.sendSuccess(() -> Component.translatable("command.cosmicpve.restore"), false);
        return 1;
    }

    private static int trueDamageTarget(
            net.minecraft.commands.CommandSourceStack source,
            net.minecraft.world.entity.Entity entity,
            float amount) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity target)) {
            source.sendFailure(Component.literal("The true-damage target must be a living entity."));
            return 0;
        }
        var actor = source.getEntity() instanceof net.minecraft.world.entity.LivingEntity living ? living : null;
        var outcome = CosmicCombat.childActions().deliverTrueRoot(
                target, actor, TrueDamagePacket.standard(CosmicPVE.id("development_command"), amount),
                RecursionPolicy.NO_PROCS);
        source.sendSuccess(
                () -> Component.literal("True damage sequence " + outcome.sequenceId()
                        + " (parent " + outcome.parentSequenceId() + ") dealt "
                        + outcome.healthDamage() + " health damage."), true);
        return outcome.accepted() ? 1 : 0;
    }

    private static int setHealth(
            net.minecraft.commands.CommandSourceStack source,
            net.minecraft.world.entity.Entity entity,
            float amount) {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity target)) {
            source.sendFailure(Component.literal("The health target must be a living entity."));
            return 0;
        }
        target.setHealth(Math.min(amount, target.getMaxHealth()));
        source.sendSuccess(() -> Component.literal("Set " + target.getName().getString()
                + " to " + target.getHealth() + " health."), true);
        return 1;
    }

    private static int executeTarget(net.minecraft.commands.CommandSourceStack source, net.minecraft.world.entity.Entity entity)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity target)) {
            source.sendFailure(Component.literal("The execution target must be a living entity."));
            return 0;
        }
        var actor = source.getEntity() instanceof net.minecraft.world.entity.LivingEntity living ? living : null;
        var result = CosmicCombat.executions().execute(
                target, new ExecutionCause(CosmicPVE.id("development_command")), actor, null);
        source.sendSuccess(
                () -> Component.literal("Executed " + target.getName().getString()
                        + " (sequence " + result.sequenceId() + ")."), true);
        return result.executed() ? 1 : 0;
    }

    private static int setTrace(net.minecraft.commands.CommandSourceStack source, boolean enabled)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        CosmicCombat.traces().setEnabled(player.getUUID(), enabled);
        source.sendSuccess(() -> Component.literal("Cosmic combat tracing " + (enabled ? "enabled" : "disabled") + "."), false);
        return 1;
    }

    private static int showLast(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var result = CosmicCombat.traces().last(player.getUUID());
        if (result.isEmpty()) {
            source.sendFailure(Component.literal("No committed damaging hit has been traced yet."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(CosmicCombat.traces().format(result.orElseThrow())), false);
        return 1;
    }

    private static int setProcTrace(net.minecraft.commands.CommandSourceStack source, boolean enabled)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        CosmicCombat.procTraces().setEnabled(player.getUUID(), enabled);
        source.sendSuccess(() -> Component.literal("Cosmic proc tracing " + (enabled ? "enabled" : "disabled") + "."), false);
        return 1;
    }

    private static int showLastProc(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var result = CosmicCombat.procTraces().last(player.getUUID());
        if (result.isEmpty()) {
            source.sendFailure(Component.literal("No proc event has been traced yet."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(CosmicCombat.procTraces().format(result.orElseThrow())), false);
        return 1;
    }

    private static int testProc(net.minecraft.commands.CommandSourceStack source, RecursionPolicy policy)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var result = CosmicCombat.procEvents().dispatchDevelopment(
                player, java.util.List.of(DevelopmentProcFixtures.guaranteedHit()), policy, java.util.Set.of());
        String formatted = CosmicCombat.procTraces().format(result);
        source.sendSuccess(() -> Component.literal("Development proc: " + formatted), false);
        return result.activationCount() > 0 ? 1 : 0;
    }

    private static int listCooldowns(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        long tick = source.getServer().getTickCount();
        var active = CosmicCombat.cooldowns().active(player.getUUID(), tick);
        if (active.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No active Cosmic cooldowns."), false);
            return 0;
        }
        String output = active.stream()
                .map(entry -> entry.getKey() + "="
                        + CosmicCombat.cooldowns().remainingTicks(player.getUUID(), entry.getKey(), tick)
                        + "t(" + entry.getValue().scope() + ")")
                .collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal("Cosmic cooldowns: " + output), false);
        return active.size();
    }

    private static int setCooldown(
            net.minecraft.commands.CommandSourceStack source,
            net.minecraft.resources.Identifier key,
            int ticks)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        CosmicCombat.cooldowns().set(
                player.getUUID(), key, ticks, source.getServer().getTickCount(),
                CooldownScope.EPHEMERAL_COMBAT, java.util.Optional.empty());
        source.sendSuccess(() -> Component.literal("Set " + key + " to " + ticks + " ticks."), false);
        return 1;
    }

    private static int clearCooldown(
            net.minecraft.commands.CommandSourceStack source,
            net.minecraft.resources.Identifier key)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        boolean removed = CosmicCombat.cooldowns().clear(player.getUUID(), key);
        source.sendSuccess(() -> Component.literal((removed ? "Cleared " : "No active cooldown for ") + key + "."), false);
        return removed ? 1 : 0;
    }

    private static int clearAllCooldowns(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        int count = CosmicCombat.cooldowns().clearAll(player.getUUID());
        source.sendSuccess(() -> Component.literal("Cleared " + count + " Cosmic cooldown(s)."), false);
        return count;
    }
}
