package com.cosmicpve.equipment.accessory;

import com.cosmicpve.CosmicPVE;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public enum BeltDefinition {
    SHOCK_THERAPY("shock_therapy", "Belt: Shock Therapy", 0x123B07,
            "All outgoing lightning effects deal +1 true damage and heal you for 0.25 HP."),
    BANDOLIER("bandolier", "Belt: Bandolier", 0x5C4D04,
            "Deal +12% outgoing damage every fourth hit with a sword."),
    JELLY_ROLL("jelly_roll", "Belt: Jelly Roll", 0xC999FF,
            "Take 2% less incoming damage and Devour procs increase saturation by 20% more.");

    private final Identifier id;
    private final String displayName;
    private final int color;
    private final String effect;
    BeltDefinition(String path, String displayName, int color, String effect) {
        this.id = CosmicPVE.id(path); this.displayName = displayName; this.color = color; this.effect = effect;
    }
    public Identifier id() { return id; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public String effect() { return effect; }
    public static Optional<BeltDefinition> find(Identifier id) {
        return Arrays.stream(values()).filter(value -> value.id.equals(id)).findFirst();
    }
}
