package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Stable typed identity carried by every V-Kit Crystal stack. */
public record VKitCrystalData(int dataVersion, Identifier kitId) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<VKitCrystalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("data_version").forGetter(VKitCrystalData::dataVersion),
            Identifier.CODEC.fieldOf("kit_id").forGetter(VKitCrystalData::kitId)
    ).apply(instance, VKitCrystalData::new));

    public boolean isCurrent() {
        return dataVersion == CURRENT_DATA_VERSION;
    }
}
