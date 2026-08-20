package com.cosmicpve.command;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.stack.ActiveCombatStack;
import com.cosmicpve.combat.stack.StackApplication;
import com.cosmicpve.content.definition.stack.StackPolarity;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

final class CombatStackCommands {
    private CombatStackCommands() {}

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("stack")
                .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("stack-id", IdentifierArgument.id())
                                        .executes(context -> add(context.getSource(), living(
                                                        context.getSource(), EntityArgument.getEntity(context, "target")),
                                                IdentifierArgument.getId(context, "stack-id"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                .executes(context -> add(context.getSource(), living(
                                                                context.getSource(), EntityArgument.getEntity(context, "target")),
                                                        IdentifierArgument.getId(context, "stack-id"),
                                                        IntegerArgumentType.getInteger(context, "count")))))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.argument("stack-id", IdentifierArgument.id())
                                        .executes(context -> remove(context.getSource(), living(
                                                        context.getSource(), EntityArgument.getEntity(context, "target")),
                                                IdentifierArgument.getId(context, "stack-id"), 1, false))
                                        .then(Commands.literal("all").executes(context -> remove(
                                                context.getSource(), living(
                                                        context.getSource(), EntityArgument.getEntity(context, "target")),
                                                IdentifierArgument.getId(context, "stack-id"), 0, true)))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                                .executes(context -> remove(context.getSource(), living(
                                                                context.getSource(), EntityArgument.getEntity(context, "target")),
                                                        IdentifierArgument.getId(context, "stack-id"),
                                                        IntegerArgumentType.getInteger(context, "count"), false))))))
                .then(Commands.literal("list")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> list(context.getSource(), living(
                                        context.getSource(), EntityArgument.getEntity(context, "target"))))))
                .then(Commands.literal("cleanse")
                        .then(Commands.argument("target", EntityArgument.entity())
                                .then(Commands.literal("positive").executes(context -> cleanse(
                                        context.getSource(), living(
                                                context.getSource(), EntityArgument.getEntity(context, "target")),
                                        StackPolarity.POSITIVE)))
                                .then(Commands.literal("negative").executes(context -> cleanse(
                                        context.getSource(), living(
                                                context.getSource(), EntityArgument.getEntity(context, "target")),
                                        StackPolarity.NEGATIVE)))))
                .then(Commands.literal("steal")
                        .then(Commands.argument("from", EntityArgument.entity())
                                .then(Commands.argument("to", EntityArgument.entity())
                                        .executes(context -> steal(
                                                context.getSource(),
                                                living(context.getSource(), EntityArgument.getEntity(context, "from")),
                                                living(context.getSource(), EntityArgument.getEntity(context, "to")))))));
    }

    private static int add(CommandSourceStack source, LivingEntity target, net.minecraft.resources.Identifier id, int count) {
        if (target == null) {
            return 0;
        }
        Optional<java.util.UUID> actor = source.getEntity() instanceof LivingEntity living
                ? Optional.of(living.getUUID()) : Optional.empty();
        Optional<java.util.UUID> credited = source.getEntity() instanceof ServerPlayer player
                ? Optional.of(player.getUUID()) : Optional.empty();
        var result = CosmicCombat.stacks().addStack(
                target, id, count, StackApplication.ephemeral(actor, credited), source.getServer().getTickCount());
        source.sendSuccess(() -> Component.literal(
                "Stack add " + id + ": status=" + result.status() + " added=" + result.added()
                        + " refreshed=" + result.refreshed() + " count=" + result.finalCount()), false);
        return result.added() + result.refreshed() > 0 ? 1 : 0;
    }

    private static int remove(
            CommandSourceStack source,
            LivingEntity target,
            net.minecraft.resources.Identifier id,
            int count,
            boolean all) {
        if (target == null) {
            return 0;
        }
        var result = all
                ? CosmicCombat.stacks().removeAll(target, id, source.getServer().getTickCount())
                : CosmicCombat.stacks().remove(target, id, count, source.getServer().getTickCount());
        source.sendSuccess(() -> Component.literal(
                "Stack remove " + id + ": status=" + result.status() + " removed=" + result.removed()
                        + " count=" + result.finalCount()), false);
        return result.removed();
    }

    private static int list(CommandSourceStack source, LivingEntity target) {
        if (target == null) {
            return 0;
        }
        long tick = source.getServer().getTickCount();
        var active = CosmicCombat.stacks().activeStacks(target, tick);
        if (active.isEmpty()) {
            source.sendSuccess(() -> Component.literal(target.getName().getString() + " has no active Cosmic stacks."), false);
            return 0;
        }
        String output = active.stream().map(stack -> format(stack, tick))
                .collect(java.util.stream.Collectors.joining("; "));
        source.sendSuccess(() -> Component.literal(target.getName().getString() + " stacks: " + output), false);
        return active.stream().mapToInt(ActiveCombatStack::count).sum();
    }

    private static int cleanse(CommandSourceStack source, LivingEntity target, StackPolarity polarity) {
        if (target == null) {
            return 0;
        }
        int removed = CosmicCombat.stacks().cleanseAll(target, polarity, source.getServer().getTickCount());
        source.sendSuccess(() -> Component.literal(
                "Cleansed " + removed + " eligible " + polarity + " stack(s) from "
                        + target.getName().getString() + "."), false);
        return removed;
    }

    private static int steal(CommandSourceStack source, LivingEntity from, LivingEntity to) {
        if (from == null || to == null) {
            return 0;
        }
        Optional<java.util.UUID> actor = source.getEntity() == null
                ? Optional.empty() : Optional.of(source.getEntity().getUUID());
        var result = CosmicCombat.stacks().stealOne(from, to, actor, source.getServer().getTickCount());
        String id = result.transferred().map(stack -> stack.definitionId().toString()).orElse("none");
        source.sendSuccess(() -> Component.literal(
                "Stack steal: status=" + result.status() + " transferred=" + id
                        + " sourceCount=" + result.sourceFinalCount()
                        + " recipientCount=" + result.recipientFinalCount()), false);
        return result.transferred().isPresent() ? 1 : 0;
    }

    private static String format(ActiveCombatStack active, long tick) {
        String remaining = active.instances().stream()
                .map(stack -> Math.max(0L, stack.expirationTick() - tick) + "t")
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
        return active.definition().map(definition -> active.definitionId()
                        + " count=" + active.count()
                        + " polarity=" + definition.polarity()
                        + " policy=" + definition.refreshPolicy()
                        + " remaining=" + remaining
                        + " transferable=" + definition.transferable()
                        + " cleansable=" + definition.cleansable()
                        + " revision=" + active.oldestDefinitionRevision())
                .orElseGet(() -> active.definitionId() + " count=" + active.count()
                        + " definition=UNKNOWN remaining=" + remaining
                        + " revision=" + active.oldestDefinitionRevision());
    }

    private static LivingEntity living(CommandSourceStack source, net.minecraft.world.entity.Entity entity) {
        if (entity instanceof LivingEntity living) {
            return living;
        }
        source.sendFailure(Component.literal("Stack targets must be living entities."));
        return null;
    }
}
