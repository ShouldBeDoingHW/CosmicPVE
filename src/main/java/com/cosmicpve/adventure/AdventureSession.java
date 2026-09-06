package com.cosmicpve.adventure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public record AdventureSession(UUID owner, UUID id, int minutes, Phase phase, ReturnPoint home,
        BlockPos entry, BlockPos exit, long deadline, int callSlot, int callCount) {
    public enum Phase { PREPARED, TRANSITION, ACTIVE, RETURNING, DEAD }
    public static final Codec<AdventureSession> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(AdventureSession::owner),
            UUIDUtil.CODEC.fieldOf("id").forGetter(AdventureSession::id),
            Codec.INT.fieldOf("minutes").forGetter(AdventureSession::minutes),
            Codec.STRING.xmap(Phase::valueOf,Phase::name).fieldOf("phase").forGetter(AdventureSession::phase),
            ReturnPoint.CODEC.fieldOf("home").forGetter(AdventureSession::home),
            BlockPos.CODEC.fieldOf("entry").forGetter(AdventureSession::entry),
            BlockPos.CODEC.fieldOf("exit").forGetter(AdventureSession::exit),
            Codec.LONG.fieldOf("deadline").forGetter(AdventureSession::deadline),
            Codec.INT.fieldOf("call_slot").forGetter(AdventureSession::callSlot),
            Codec.INT.fieldOf("call_count").forGetter(AdventureSession::callCount)
    ).apply(i,AdventureSession::new));
    public AdventureSession phase(Phase next, long time) {
        return new AdventureSession(owner,id,minutes,next,home,entry,exit,time,callSlot,callCount);
    }
    public AdventureSession activate(BlockPos arrival, BlockPos portal, long time) {
        return new AdventureSession(owner,id,minutes,Phase.ACTIVE,home,arrival,portal,time,callSlot,callCount);
    }
    public record ReturnPoint(ResourceKey<Level> dimension, Vec3 position, float yaw, float pitch, GameType mode) {
        public static final Codec<ReturnPoint> CODEC = RecordCodecBuilder.create(i -> i.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(ReturnPoint::dimension),
                Vec3.CODEC.fieldOf("position").forGetter(ReturnPoint::position),
                Codec.FLOAT.fieldOf("yaw").forGetter(ReturnPoint::yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(ReturnPoint::pitch),
                GameType.CODEC.fieldOf("mode").forGetter(ReturnPoint::mode)).apply(i,ReturnPoint::new));
    }
}
