package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Versioned, order-preserving set of distinct mask powers. */
public record MaskLoadout(int dataVersion, List<Identifier> maskIds, Optional<String> renderTexture,
        List<MaskPresentation> presentations) {
    public static final int DATA_VERSION = 1;
    private static final Codec<MaskLoadout> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(MaskLoadout::dataVersion),
            Identifier.CODEC.listOf().fieldOf("masks").forGetter(MaskLoadout::maskIds),
            Codec.STRING.optionalFieldOf("render_texture").forGetter(MaskLoadout::renderTexture),
            MaskPresentation.CODEC.listOf().optionalFieldOf("presentations", List.of()).forGetter(MaskLoadout::presentations)
    ).apply(instance, MaskLoadout::new));
    public static final Codec<MaskLoadout> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? DataResult.success(value) : DataResult.error(() -> "Mask loadout requires 1-5 distinct mask IDs"));

    public MaskLoadout {
        maskIds = List.copyOf(maskIds);
        renderTexture = renderTexture == null ? Optional.empty() : renderTexture;
        presentations = presentations == null ? List.of() : List.copyOf(presentations);
    }
    public MaskLoadout(List<Identifier> maskIds) { this(DATA_VERSION, maskIds, Optional.empty(), List.of()); }
    public MaskLoadout(List<Identifier> maskIds, String renderTexture) {
        this(DATA_VERSION, maskIds, Optional.of(renderTexture), List.of());
    }
    public MaskLoadout(List<Identifier> maskIds, String renderTexture, List<MaskPresentation> presentations) {
        this(DATA_VERSION, maskIds, Optional.of(renderTexture), presentations);
    }
    public boolean valid() {
        return dataVersion == DATA_VERSION && !maskIds.isEmpty() && maskIds.size() <= 5
                && new HashSet<>(maskIds).size() == maskIds.size()
                && (presentations.isEmpty() || presentations.size() == maskIds.size()
                && java.util.stream.IntStream.range(0, maskIds.size())
                        .allMatch(index -> maskIds.get(index).equals(presentations.get(index).id())));
    }
}
