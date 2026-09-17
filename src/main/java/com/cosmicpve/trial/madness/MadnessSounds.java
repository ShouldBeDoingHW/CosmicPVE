package com.cosmicpve.trial.madness;

import com.cosmicpve.trial.TrialTitleService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;

/** Small common cue helper; the existing per-effect server clocks retain timing ownership. */
public final class MadnessSounds {
    private MadnessSounds() {}
    public static void countdown(ServerPlayer player,int seconds) {
        if(seconds>=1 && seconds<=3) TrialTitleService.playForPlayer(player,SoundEvents.NOTE_BLOCK_PLING.value(),1.0F,3.0F-seconds);
    }
    public static void statues(ServerPlayer player) { TrialTitleService.playForPlayer(player,SoundEvents.WITHER_SPAWN); }
    public static void rocket(ServerPlayer player) { TrialTitleService.playForPlayer(player,SoundEvents.FIREWORK_ROCKET_BLAST); }
}
