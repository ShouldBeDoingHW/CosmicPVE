package com.cosmicpve.data.component;

import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

/** Synchronized presentation snapshot; gameplay resolves {@link #setId()} through the repository. */
public record ArmorSetIdentity(
        int dataVersion,
        Identifier setId,
        Component displayName,
        int color,
        List<Component> fullSetBonus) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<ArmorSetIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(ArmorSetIdentity::dataVersion),
            Identifier.CODEC.fieldOf("set_id").forGetter(ArmorSetIdentity::setId),
            ComponentSerialization.CODEC.fieldOf("display_name").forGetter(ArmorSetIdentity::displayName),
            Codec.intRange(0, 0xFFFFFF).fieldOf("color").forGetter(ArmorSetIdentity::color),
            ComponentSerialization.CODEC.listOf().fieldOf("full_set_bonus").forGetter(ArmorSetIdentity::fullSetBonus)
    ).apply(instance, ArmorSetIdentity::new));

    public ArmorSetIdentity {
        fullSetBonus = List.copyOf(fullSetBonus);
        if (dataVersion < 1 || color < 0 || color > 0xFFFFFF || fullSetBonus.isEmpty()) {
            throw new IllegalArgumentException("Invalid armor-set identity snapshot");
        }
    }

    public static ArmorSetIdentity from(ArmorSetDefinition definition) {
        return new ArmorSetIdentity(CURRENT_DATA_VERSION, definition.id(), definition.displayName(),
                definition.presentationColor(), definition.fullSetBonus());
    }

    /** Legacy synchronized Yjiki snapshots retain identity and mechanics, but use the revised white presentation. */
    @Override public int color() {
        return setId.equals(com.cosmicpve.equipment.armor.ArmorSetIds.YJIKI) ? 0xFFFFFF : color;
    }
}
