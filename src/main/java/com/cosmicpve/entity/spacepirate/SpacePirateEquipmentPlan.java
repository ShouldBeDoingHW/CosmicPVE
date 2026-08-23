package com.cosmicpve.entity.spacepirate;

import java.util.List;

public record SpacePirateEquipmentPlan(
        SpacePirateVariant variant,
        List<ArmorRoll> armor,
        int poisonLevel,
        int executeLevel,
        boolean pummel) {
    public enum ArmorMaterial { IRON, DIAMOND }
    public record ArmorRoll(ArmorMaterial material, int protectionLevel) {
        public ArmorRoll {
            if (protectionLevel < 1 || protectionLevel > 4) throw new IllegalArgumentException("Protection must be I-IV");
        }
    }
    public SpacePirateEquipmentPlan { armor = List.copyOf(armor); }
}
