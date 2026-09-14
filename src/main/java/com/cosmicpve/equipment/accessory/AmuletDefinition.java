package com.cosmicpve.equipment.accessory;

import com.cosmicpve.CosmicPVE;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public enum AmuletDefinition {
    BLOOD_DIAMOND("blood_diamond", "Amulet: Blood Diamond", 0x5C0404,
            "Deal +1% damage for each active Bleed stack on both you and your target."),
    ICICLE("icicle", "Amulet: Icicle", 0xBDF0FF,
            "Hitting an enemy that has Slowness has a 10% chance to give them a Bleed stack."),
    BLACK_HEART("black_heart", "Amulet: Black Heart", 0x310082,
            "Gain +5% outgoing damage for 4s when a hostile Mastery or Heroic enchantment proc affects you.");

    private final Identifier id;
    private final String displayName;
    private final int color;
    private final String effect;
    AmuletDefinition(String path, String displayName, int color, String effect) {
        this.id = CosmicPVE.id(path); this.displayName = displayName; this.color = color; this.effect = effect;
    }
    public Identifier id() { return id; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public String effect() { return effect; }
    public static Optional<AmuletDefinition> find(Identifier id) {
        return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst();
    }
}
