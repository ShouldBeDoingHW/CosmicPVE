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
import com.cosmicpve.data.component.TrialPortalModifiers;
import com.cosmicpve.data.component.TrialTrinketType;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.trial.trinket.TrialTrinkets;

public final class TrialCommands {
    private TrialCommands() {}
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("trial")
                .then(Commands.literal("portal").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> givePortal(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 1))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> givePortal(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "count"))))))
                        .then(Commands.literal("give-modified").then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("time", IntegerArgumentType.integer(0, 5))
                                        .then(Commands.argument("skip", IntegerArgumentType.integer(0, 3))
                                                .then(Commands.argument("insurance", IntegerArgumentType.integer(0, 3))
                                                        .executes(ctx -> giveModifiedPortal(ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "player"),
                                                                IntegerArgumentType.getInteger(ctx, "time"),
                                                                IntegerArgumentType.getInteger(ctx, "skip"),
                                                                IntegerArgumentType.getInteger(ctx, "insurance"))))))))
                        .then(Commands.literal("inspect").executes(ctx -> inspectPortal(ctx.getSource()))))
                .then(Commands.literal("trinket").then(Commands.literal("give")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                                new String[]{"time", "skip", "insurance"}, builder))
                                        .then(Commands.argument("value", IntegerArgumentType.integer(1, 5))
                                                .executes(ctx -> giveTrinket(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                        StringArgumentType.getString(ctx, "type"),
                                                        IntegerArgumentType.getInteger(ctx, "value"), 1))
                                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                        .executes(ctx -> giveTrinket(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"),
                                                                StringArgumentType.getString(ctx, "type"),
                                                                IntegerArgumentType.getInteger(ctx, "value"),
                                                                IntegerArgumentType.getInteger(ctx, "count")))))))))
                .then(Commands.literal("debug")
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("perf").executes(ctx -> perf(ctx.getSource())))
                        .then(Commands.literal("corpse")
                                .then(Commands.literal("spawn").executes(ctx -> spawnCorpse(ctx.getSource())))
                                .then(Commands.literal("inspect").executes(ctx -> inspectCorpse(ctx.getSource()))))
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
                        .then(Commands.literal("pot").then(Commands.literal("fill")
                                .then(Commands.argument("count", IntegerArgumentType.integer(0, 1000))
                                        .executes(ctx -> send(ctx.getSource(), TrialRuntime.sessions().debugFillPot(
                                                ctx.getSource().getServer(), IntegerArgumentType.getInteger(ctx, "count")))))))
                        .then(Commands.literal("force-room").then(Commands.argument("room", StringArgumentType.word())
                                .suggests((ctx, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                        new String[]{"raiding_rainbow","circuit_circus","cold_snap","zero_g","fire_colony","bomb_squad","haze_seek","hidden_graveyard","deadeye","warzone_giants"}, builder))
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

    private static int giveModifiedPortal(net.minecraft.commands.CommandSourceStack source, ServerPlayer player,
                                          int time, int skip, int insurance) {
        TrialPortalModifiers modifiers = new TrialPortalModifiers(TrialPortalModifiers.DATA_VERSION, time, skip, insurance);
        if (!modifiers.valid()) {
            source.sendFailure(Component.literal("Time must be 0, 1, 3, or 5; Skip and Insurance must be 0-3."));
            return 0;
        }
        ItemStack stack = new ItemStack(ModItems.TRIAL_PORTAL.get());
        com.cosmicpve.trial.portal.TrialPortalItem.applyModifiers(stack, modifiers);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        source.sendSuccess(() -> Component.literal("Gave modified Trial Portal: time=" + time
                + " skip=" + skip + " insurance=" + insurance + "."), true);
        return 1;
    }

    private static int inspectPortal(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ItemStack held = source.getPlayerOrException().getMainHandItem();
        if (!held.is(ModItems.TRIAL_PORTAL.get())) {
            source.sendFailure(Component.literal("Hold a Trial Portal in your main hand.")); return 0;
        }
        var modifiers = held.getOrDefault(ModDataComponents.TRIAL_PORTAL_MODIFIERS.get(), TrialPortalModifiers.EMPTY);
        source.sendSuccess(() -> Component.literal("Trial Portal modifiers: time=" + modifiers.timeMinutes()
                + " skip=" + modifiers.skipRooms() + " insurance=" + modifiers.insuranceLevel()), false);
        return 1;
    }

    private static int giveTrinket(net.minecraft.commands.CommandSourceStack source, ServerPlayer player,
                                   String typeName, int value, int count) {
        TrialTrinketType type;
        try { type = TrialTrinketType.valueOf(typeName.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException exception) { source.sendFailure(Component.literal("Unknown Trinket type.")); return 0; }
        if (!type.validValue(value)) { source.sendFailure(Component.literal("Invalid value for " + typeName + " Trinket.")); return 0; }
        ItemStack stack = TrialTrinkets.create(type, value, count);
        if (!player.getInventory().add(stack)) player.drop(stack, false);
        source.sendSuccess(() -> Component.literal("Gave " + count + " " + typeName + " " + value + " Trial Trinket(s)."), true);
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
                + " pot=" + session.progress().pot().size() + " modifiers=" + session.progress().portalModifiers()
                + " skipProcessed=" + session.progress().initialSkipProcessed()), false);
        source.sendSuccess(() -> Component.literal("  eligible pool weights: "
                + TrialRuntime.sessions().productionPoolStatus(session)), false);
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
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.COLD_SNAP::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  cold snap door="
                    + (com.cosmicpve.trial.room.ColdSnapService.doorUnlocked(session.progress().encounter()) ? "unlocked" : "locked")
                    + " secret=" + (com.cosmicpve.trial.room.ColdSnapService.secretRevealed(session.progress().encounter()) ? "revealed" : "hidden")), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.BOMB_SQUAD::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  "
                    + TrialRuntime.sessions().bombSquadStatus(session.sessionId())), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.HIDDEN_GRAVEYARD::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  "
                    + TrialRuntime.sessions().hiddenGraveyardStatus(session.sessionId())), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.DEADEYE::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  "
                    + TrialRuntime.sessions().deadeyeStatus(session)), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.HAZE_AND_SEEK::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  " + TrialRuntime.sessions().hazeStatus(session.sessionId())), false);
        if (session.currentRoom().filter(com.cosmicpve.trial.TrialSessionService.WARZONE_GIANTS::equals).isPresent())
            source.sendSuccess(() -> Component.literal("  " + TrialRuntime.sessions().warzoneStatus(session.sessionId())), false);
        for (var id : session.participants()) {
            var player = source.getServer().getPlayerList().getPlayer(id);
            String snapshot = player == null ? "offline/preserved" : String.valueOf(player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE));
            source.sendSuccess(() -> Component.literal("  " + id + " snapshot=" + snapshot), false);
        }
        return 1;
    }

    private static int perf(net.minecraft.commands.CommandSourceStack source) {
        var snapshot = TrialRuntime.sessions().performanceSnapshot();
        source.sendSuccess(() -> Component.literal("Trial perf: samples=" + snapshot.samples()
                + " avg=" + snapshot.averageMicros() + "us max=" + snapshot.maximumMicros() + "us state="
                + snapshot.state() + " room=" + snapshot.room() + " participants=" + snapshot.participants()), false);
        return snapshot.samples();
    }

    private static int spawnCorpse(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var corpse = com.cosmicpve.registry.ModEntities.UNDEAD_CORPSE.get().create(
                (net.minecraft.server.level.ServerLevel)player.level(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (corpse == null) { source.sendFailure(Component.literal("Could not create Undead Corpse.")); return 0; }
        corpse.setPos(player.getX(), player.getY(), player.getZ() + 2.0D);
        com.cosmicpve.entity.undeadcorpse.UndeadCorpseEquipmentService.equipBase(
                corpse, player.registryAccess(), player.getRandom());
        if (!player.level().addFreshEntity(corpse)) { source.sendFailure(Component.literal("Server rejected Undead Corpse.")); return 0; }
        source.sendSuccess(() -> Component.literal("Spawned canonical Undead Corpse " + corpse.getUUID() + "."), true);
        return 1;
    }

    private static int inspectCorpse(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        var corpses = player.level().getEntitiesOfClass(com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity.class,
                player.getBoundingBox().inflate(16.0D));
        if (corpses.isEmpty()) { source.sendFailure(Component.literal("No Undead Corpse within 16 blocks.")); return 0; }
        var corpse = corpses.stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElseThrow();
        source.sendSuccess(() -> Component.literal("Corpse " + corpse.getUUID() + " health=" + corpse.getHealth()
                + " speed=" + corpse.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                + " axe=" + corpse.getMainHandItem().getEnchantments()), false);
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
