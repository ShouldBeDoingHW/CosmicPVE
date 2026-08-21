package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record HeroicIdentity(int dataVersion, HeroicEquipmentKind kind, int durabilityBonus) {
    public static final int CURRENT_VERSION = 1;
    public static final int DURABILITY_BONUS = 250;
    public static final Codec<HeroicIdentity> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("version").forGetter(HeroicIdentity::dataVersion),
            Codec.STRING.xmap(HeroicEquipmentKind::valueOf, HeroicEquipmentKind::name)
                    .fieldOf("kind").forGetter(HeroicIdentity::kind),
            Codec.INT.fieldOf("durability_bonus").forGetter(HeroicIdentity::durabilityBonus)
    ).apply(i, HeroicIdentity::new));

    public static HeroicIdentity of(HeroicEquipmentKind kind) {
        return new HeroicIdentity(CURRENT_VERSION, kind, DURABILITY_BONUS);
    }
}
