package com.cosmicpve.entity.spacepirate;

import java.util.List;

public record SpacePirateEquipmentPlan(
        SpacePirateVariant variant,
        List<ArmorRoll> armor,
        int poisonLevel,
        int executeLevel,
        boolean pummel,
        int silenceLevel) {
    public SpacePirateEquipmentPlan(SpacePirateVariant variant, List<ArmorRoll> armor,
            int poisonLevel, int executeLevel, boolean pummel) {
        this(variant, armor, poisonLevel, executeLevel, pummel, 0);
    }
    public enum ArmorMaterial { IRON, DIAMOND }
    public record ArmorRoll(ArmorMaterial material, int protectionLevel) {
        public ArmorRoll {
            if (protectionLevel < 1 || protectionLevel > 4) throw new IllegalArgumentException("Protection must be I-IV");
        }
    }
    public SpacePirateEquipmentPlan {
        armor = List.copyOf(armor);
        if (silenceLevel < 0 || silenceLevel > 4 || (variant != SpacePirateVariant.VARIANT_1 && silenceLevel != 0))
            throw new IllegalArgumentException("Only Variant 1 may have Silence I-IV");
    }
}
