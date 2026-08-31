package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Stable authoritative identity for one signature set weapon. */
public record SignatureWeaponIdentity(int dataVersion, Identifier signatureId,
        Identifier matchingArmorSetId, Kind kind) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<SignatureWeaponIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(SignatureWeaponIdentity::dataVersion),
            Identifier.CODEC.fieldOf("signature_id").forGetter(SignatureWeaponIdentity::signatureId),
            Identifier.CODEC.fieldOf("matching_armor_set_id").forGetter(SignatureWeaponIdentity::matchingArmorSetId),
            Codec.STRING.xmap(Kind::valueOf, Kind::name).fieldOf("kind").forGetter(SignatureWeaponIdentity::kind)
    ).apply(instance, SignatureWeaponIdentity::new));

    public enum Kind { MELEE, RANGED }
}
