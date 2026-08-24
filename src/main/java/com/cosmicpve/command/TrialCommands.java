package com.cosmicpve.command;

import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.trial.TrialOperationResult;
import com.cosmicpve.trial.TrialRuntime;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class TrialCommands {
    private TrialCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("trial")
                .then(Commands.literal("portal").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> givePortal(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> givePortal(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "count")))))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("complete-room").executes(ctx -> send(ctx.getSource(),
                                TrialRuntime.sessions().completeRoom(ctx.getSource().getServer()))))
                        .then(Commands.literal("continue").executes(ctx -> send(ctx.getSource(),
                                TrialRuntime.sessions().continueRoom(ctx.getSource().getServer()))))
                        .then(Commands.literal("exit").executes(ctx -> send(ctx.getSource(),
                                TrialRuntime.sessions().exit(ctx.getSource().getPlayerOrException()))))
                        .then(Commands.literal("abort").executes(ctx -> send(ctx.getSource(),
                                TrialRuntime.sessions().abort(ctx.getSource().getServer(), "Trial aborted by operator."))))
                        .then(Commands.literal("restore").then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> restore(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))))
                        .then(Commands.literal("sound")
                                .then(Commands.literal("countdown").executes(ctx -> sound(ctx.getSource(), false)))
                                .then(Commands.literal("start").executes(ctx -> sound(ctx.getSource(), true))))
                        .then(Commands.literal("progress").then(Commands.literal("set")
                                .then(Commands.argument("completed", IntegerArgumentType.integer(0, 8))
                                        .executes(ctx -> send(ctx.getSource(), TrialRuntime.sessions().debugSetCompletedRooms(
                                                ctx.getSource().getServer(), IntegerArgumentType.getInteger(ctx, "completed")))))))
                        .then(Commands.literal("force-room").then(Commands.argument("room", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        new String[]{"raiding_rainbow","circuit_circus","fire_colony","zero_g"}, builder))
                                .executes(ctx -> forceRoom(ctx.getSource(), StringArgumentType.getString(ctx, "room")))))
                        .then(Commands.literal("timer")
                                .then(Commands.literal("set").then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(ctx -> timer(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "seconds"), true))))
                                .then(Commands.literal("add").then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(ctx -> timer(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "seconds"), false))))
                                .then(Commands.literal("remove").then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                        .executes(ctx -> timer(ctx.getSource(), -IntegerArgumentType.getInteger(ctx, "seconds"), false))))));
    }

    private static int givePortal(net.minecraft.commands.CommandSourceStack source, ServerPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            ItemStack stack = new ItemStack(ModItems.TRIAL_PORTAL.get());
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.literal("Gave " + count + " Trial Portal(s) to " + player.getName().getString() + "."), true);
        return count;
    }

    private static int status(net.minecraft.commands.CommandSourceStack source) {
        var active = TrialRuntime.sessions().active(source.getServer());
        if (active.isEmpty()) { source.sendSuccess(() -> Component.literal("No active Trial session."), false); return 0; }
        var session = active.orElseThrow();
        String players = session.participants().stream().map(id -> {
            var online = source.getServer().getPlayerList().getPlayer(id);
            return online == null ? id.toString() : online.getName().getString();
        }).collect(java.util.stream.Collectors.joining(", "));
        source.sendSuccess(() -> Component.literal("Trial " + session.sessionId() + " state=" + session.state()
                + " participants=[" + players + "] timer=" + session.timerTicks() + "t room="
                + session.currentRoom().map(Object::toString).orElse("none") + " transition=" + session.transitionSerial()
                + " phase=" + session.progress().phase() + " completed=" + session.progress().completedRooms()
                + " pot=" + session.progress().pot().size()), false);
        if (!session.progress().encounter().hiddenSequence().isEmpty())
            source.sendSuccess(() -> Component.literal("  hidden sequence=" + session.progress().encounter().hiddenSequence()
                    + " progress=" + session.progress().encounter().sequenceProgress()), false);
        if (!session.progress().encounter().pillarMaterials().isEmpty())
            source.sendSuccess(() -> Component.literal("  pillars=" + session.progress().encounter().pillarMaterials()
                    + " completed circuits=" + session.progress().encounter().completedCircuits()), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.ZERO_G::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  objectives="
                    + session.progress().encounter().completedObjectives().size() + "/10 encounter entities="
                    + session.progress().encounter().encounterEntities().size() + " completed positions="
                    + session.progress().encounter().completedObjectives()), false);
        for (var id : session.participants()) {
            var player = source.getServer().getPlayerList().getPlayer(id);
            String snapshot = player == null ? "offline/preserved" : String.valueOf(player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE));
            source.sendSuccess(() -> Component.literal("  " + id + " snapshot=" + snapshot), false);
        }
        return 1;
    }

    private static int restore(net.minecraft.commands.CommandSourceStack source, ServerPlayer player) {
        boolean restored = TrialRuntime.sessions().emergencyRestore(player);
        if (restored) source.sendSuccess(() -> Component.literal("Restored pending Trial snapshot for " + player.getName().getString() + "."), true);
        else source.sendFailure(Component.literal("No safe pending Trial snapshot exists for that player."));
        return restored ? 1 : 0;
    }

    private static int sound(net.minecraft.commands.CommandSourceStack source, boolean start) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        TrialRuntime.sessions().debugSound(source.getPlayerOrException(), start);
        source.sendSuccess(() -> Component.literal("Played Trial " + (start ? "room-start" : "countdown") + " sound."), false);
        return 1;
    }

    private static int forceRoom(net.minecraft.commands.CommandSourceStack source, String room) {
        return send(source, TrialRuntime.sessions().debugForceRoom(source.getServer(),
                com.cosmicpve.CosmicPVE.id("trial/" + room)));
    }

    private static int timer(net.minecraft.commands.CommandSourceStack source, int seconds, boolean absolute) {
        long ticks = (long)seconds * 20L;
        int safe = (int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, ticks));
        return send(source, TrialRuntime.sessions().adjustTimer(source.getServer(), safe, absolute));
    }

    private static int send(net.minecraft.commands.CommandSourceStack source, TrialOperationResult result) {
        if (result.success()) source.sendSuccess(() -> Component.literal(result.message()), true);
        else source.sendFailure(Component.literal(result.message()));
        return result.success() ? 1 : 0;
    }
}
