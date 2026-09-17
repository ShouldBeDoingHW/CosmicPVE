package com.cosmicpve.trial.madness;

import com.cosmicpve.trial.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.execution.ExecutionCause;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import java.util.*;

/** Trial-scoped Java mechanics; clocks advance only during physical gameplay. */
public final class MadnessRuntime {
    public static final MadnessRuntime INSTANCE = new MadnessRuntime();
    private UUID sessionId;
    private String breezeRoom = "";
    private Vec3 roomBreeze = Vec3.ZERO;
    private final Map<UUID, PlayerState> players = new HashMap<>();
    private static final ExecutionCause STATUES = new ExecutionCause(CosmicPVE.id("trial_statues"));
    private static final class PlayerState {
        final Map<String,Integer> clocks = new HashMap<>();
        int owlWarning; int statueWarning; int stillness;
        Vec3 statueOrigin; Vec3 breeze = Vec3.ZERO; String room = "";
    }
    public void tick(MinecraftServer server, TrialSession session) {
        if (session == null) { sessionId = null; players.clear(); return; }
        if (!session.sessionId().equals(sessionId)) { players.clear(); sessionId = session.sessionId(); breezeRoom = ""; }
        players.keySet().removeIf(id -> !session.activeParticipant(id));
        if (session.state() != TrialLifecycleState.ROOM_ACTIVE) return;
        String actualRoom = session.currentRoom().map(r -> r + ":" + session.progress().appearances(r)).orElse("");
        if (!actualRoom.equals(breezeRoom)) {
            breezeRoom = actualRoom;
            double angle = net.minecraft.util.RandomSource.create().nextDouble()*Math.PI*2;
            roomBreeze = new Vec3(Math.cos(angle),0,Math.sin(angle));
        }
        for (UUID id : List.copyOf(session.participants())) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null || player.isDeadOrDying() || !player.level().dimension().equals(TrialRuntime.INSTANCE_DIMENSION)) continue;
            var state = players.computeIfAbsent(id, ignored -> new PlayerState());
            String room = session.currentRoom().map(r -> r + ":" + session.progress().appearances(r)).orElse("");
            if (!room.equals(state.room)) {
                state.room = room; state.stillness = 0; state.statueWarning = 0; state.owlWarning = 0;
                state.breeze = roomBreeze;
            }
            for (var definition : session.progress().madness().active()) apply(player,state,definition);
        }
    }
    private void apply(ServerPlayer player, PlayerState state, MadnessDefinition definition) {
        String handler = definition.handler().getPath();
        int tick = state.clocks.merge(handler,1,Integer::sum);
        switch (handler) {
            case "inventory_shuffle" -> {
                int interval = definition.interval(360);
                if (tick >= interval && player.containerMenu == player.inventoryMenu && player.containerMenu.getCarried().isEmpty()) {
                    shuffle(player); state.clocks.put(handler,0);
                }
            }
            case "owl_gene" -> {
                if (state.owlWarning > 0) {
                    --state.owlWarning;
                    if (state.owlWarning > 0 && state.owlWarning % 20 == 0) {
                        warn(player,"Owl Gene: " + state.owlWarning/20); MadnessSounds.countdown(player,state.owlWarning/20);
                    }
                    if (state.owlWarning == 0) player.connection.teleport(player.getX(),player.getY(),player.getZ(),
                            player.getYRot()+180,player.getXRot());
                } else if (tick >= definition.interval(400)) {
                    state.clocks.put(handler,0); state.owlWarning = boundedTicks(definition,"warning_ticks",60,200);
                    warn(player,"Owl Gene: 3");
                    MadnessSounds.countdown(player,3);
                }
            }
            case "statues" -> {
                if (state.stillness > 0) {
                    double epsilon = Math.max(0.001,Math.min(0.1,definition.parameter("movement_epsilon",0.03)));
                    if (moved(state.statueOrigin,player.position(),epsilon)) {
                        state.stillness = 0; penalty(player,definition);
                    } else --state.stillness;
                } else if (state.statueWarning > 0) {
                    --state.statueWarning;
                    if (state.statueWarning > 0 && state.statueWarning % 20 == 0) {
                        warn(player,"Statues: " + state.statueWarning/20); MadnessSounds.countdown(player,state.statueWarning/20);
                    }
                    if (state.statueWarning == 0) {
                        state.statueOrigin = player.position(); state.stillness = boundedTicks(definition,"stillness_ticks",15,60);
                        warn(player,"Statues: HOLD STILL");
                    }
                } else if (tick >= definition.interval(600)) {
                    state.clocks.put(handler,0); state.statueWarning = boundedTicks(definition,"warning_ticks",60,200);
                    warn(player,"Statues: 3");
                    MadnessSounds.statues(player); MadnessSounds.countdown(player,3);
                }
            }
            case "gentle_breeze" -> {
                if (player.isShiftKeyDown()) return;
                double acceleration = Math.min(0.02,definition.parameter("acceleration",0.004));
                double max = Math.min(0.2,definition.parameter("maximum_horizontal_speed",0.15));
                Vec3 velocity = player.getDeltaMovement();
                Vec3 next = breezeVelocity(velocity,state.breeze,false,acceleration,max);
                if (!next.equals(velocity)) {
                    player.setDeltaMovement(next);
                    player.hurtMarked = true;
                }
            }
            case "rocket_man" -> {
                if (tick >= definition.interval(500)) {
                    state.clocks.put(handler,0);
                    Vec3 velocity = player.getDeltaMovement();
                    player.setDeltaMovement(velocity.x,Math.max(velocity.y,Math.min(1.2,definition.parameter("vertical_velocity",1.2))),velocity.z);
                    player.hurtMarked = true;
                    MadnessSounds.rocket(player);
                }
            }
            default -> { } // Input, healing, and presentation use their semantic hooks below.
        }
    }
    private static int boundedTicks(MadnessDefinition definition,String key,int fallback,int max) {
        return (int)Math.max(1,Math.min(max,definition.parameter(key,fallback)));
    }
    private static void warn(ServerPlayer player,String text) {
        player.displayClientMessage(net.minecraft.network.chat.Component.literal(text).withColor(0x8C1708),true);
    }
    private static void penalty(ServerPlayer player,MadnessDefinition definition) {
        float amount = penaltyAmount(player.getMaxHealth(),definition.parameter("penalty_max_health_fraction",0.5));
        if (player.getHealth() <= amount) CosmicCombat.executions().execute(player,STATUES,null,null);
        else player.setHealth(player.getHealth()-amount);
    }
    public static void shuffle(net.minecraft.world.entity.player.Player player) {
        var inventory = player.getInventory();
        List<ItemStack> exact = new ArrayList<>();
        for (int i=0;i<36;i++) exact.add(inventory.getItem(i));
        for (int i=35;i>0;i--) Collections.swap(exact,i,player.getRandom().nextInt(i+1));
        for (int i=0;i<36;i++) inventory.setItem(i,exact.get(i));
        inventory.setChanged(); player.inventoryMenu.broadcastChanges();
    }
    private static TrialSession participant(ServerPlayer player) {
        return TrialRuntime.sessions().active(player.level().getServer()).filter(s -> s.activeParticipant(player.getUUID())
                && player.level().dimension().equals(TrialRuntime.INSTANCE_DIMENSION)).orElse(null);
    }
    public void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var session = participant(player); if (session == null) return;
        session.progress().madness().active().stream().filter(d -> d.handler().getPath().equals("cursed_life")).findFirst()
                .ifPresent(d -> event.setAmount(scaledHealing(event.getAmount(),d.parameter("healing_multiplier",0.7))));
    }
    /** Called before bespoke lever/control handlers can record activation. */
    public boolean rejectControl(ServerPlayer player, net.minecraft.core.BlockPos position) {
        var session = participant(player);
        if (session == null || session.state() != TrialLifecycleState.ROOM_ACTIVE) return false;
        var block = player.level().getBlockState(position).getBlock();
        if (!isControl(block)) return false;
        if (!TrialRuntime.sessions().allowsProtectedRoomUse(player,position)) return false;
        return session.progress().madness().active().stream().filter(d -> d.handler().getPath().equals("sticky_keys")).findFirst()
                .map(d -> failsControl(true,d.parameter("failure_chance",0.5),player.getRandom().nextDouble())).orElse(false);
    }
    public static boolean isControl(net.minecraft.world.level.block.Block block) {
        return block instanceof ButtonBlock || block instanceof LeverBlock;
    }
    public static boolean failsControl(boolean control,double chance,double roll) {
        return control && roll < Math.max(0,Math.min(1,chance));
    }
    public static float scaledHealing(float amount,double multiplier) {
        return amount*(float)Math.max(0,Math.min(1,multiplier));
    }
    public static boolean moved(Vec3 origin,Vec3 position,double epsilon) {
        return origin.distanceToSqr(position) > epsilon*epsilon;
    }
    public static float penaltyAmount(float maximumHealth,double fraction) {
        return maximumHealth*(float)Math.max(0,Math.min(1,fraction));
    }
    public static Vec3 breezeVelocity(Vec3 velocity,Vec3 direction,boolean crouching,double acceleration,double maximum) {
        if (crouching) return velocity;
        double along=velocity.x*direction.x+velocity.z*direction.z;
        return along >= maximum ? velocity : velocity.add(direction.scale(Math.min(acceleration,maximum-along)));
    }
    public static boolean timerReadable(TrialSession session,int serverTick) {
        return !session.progress().madness().has("time_glitch") || Math.floorDiv(serverTick,20)%15 == 0;
    }
}
