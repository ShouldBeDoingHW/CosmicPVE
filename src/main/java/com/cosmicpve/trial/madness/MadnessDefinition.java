package com.cosmicpve.trial.madness;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import java.util.Map;
import java.util.Set;

/** Bounded configuration for fixed Java handlers, never executable data. */
public record MadnessDefinition(Identifier id, String name, String description, Identifier icon, Identifier handler,
        Map<String, Double> parameters, boolean enabled) {
    public static final Set<String> HANDLERS = Set.of("sticky_keys", "inventory_shuffle", "owl_gene",
            "cursed_life", "statues", "gentle_breeze", "rocket_man", "time_glitch", "wet_noodle", "thin_skin");
    public static final Codec<MadnessDefinition> CODEC = RecordCodecBuilder.<MadnessDefinition>create(i -> i.group(
            Identifier.CODEC.fieldOf("id").forGetter(MadnessDefinition::id),
            Codec.STRING.fieldOf("name").forGetter(MadnessDefinition::name),
            Codec.STRING.fieldOf("description").forGetter(MadnessDefinition::description),
            Identifier.CODEC.fieldOf("icon").forGetter(MadnessDefinition::icon),
            Identifier.CODEC.fieldOf("handler").forGetter(MadnessDefinition::handler),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("parameters", Map.of()).forGetter(MadnessDefinition::parameters),
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(MadnessDefinition::enabled)
    ).apply(i, MadnessDefinition::new)).validate(v -> v.valid()
            ? com.mojang.serialization.DataResult.success(v)
            : com.mojang.serialization.DataResult.error(() -> "Invalid Madness definition/unknown handler: " + v.id()));
    public MadnessDefinition { parameters = Map.copyOf(parameters); }
    public boolean valid() {
        return !name.isBlank() && name.length() <= 128 && !description.isBlank() && description.length() <= 1024
                && handler.getNamespace().equals("cosmicpve")
                && HANDLERS.contains(handler.getPath()) && parameters.size() <= 16
                && parameters.values().stream().allMatch(v -> Double.isFinite(v) && v >= 0 && v <= 86400);
    }
    public double parameter(String key, double fallback) { return parameters.getOrDefault(key, fallback); }
    public int interval(int fallback) { return (int)Math.max(20, Math.min(86400, parameter("interval_ticks", fallback))); }
}
