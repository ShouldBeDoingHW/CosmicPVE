package com.cosmicpve.command;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.execution.ExecutionCause;
import com.cosmicpve.combat.api.RecursionPolicy;
import com.cosmicpve.combat.api.TrueDamagePacket;
import com.mojang.brigadier.arguments.FloatArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class CosmicCommands {
    private CosmicCommands() {}

    public static void register(RegisterCommandsEvent event) {
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
                                                FloatArgumentType.getFloat(context, "amount"))))));
        event.getDispatcher().register(Commands.literal("cosmic")
                .requires(Commands.hasPermission(new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER)))
                .then(combat));
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
}
