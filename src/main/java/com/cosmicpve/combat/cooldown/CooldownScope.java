package com.cosmicpve.combat.cooldown;

/** Persistence ownership for a cooldown. Storage remains runtime-only in Step 4A. */
public enum CooldownScope {
    PERSISTENT_PLAYER,
    EPHEMERAL_COMBAT,
    INSTANCE_SESSION
}
