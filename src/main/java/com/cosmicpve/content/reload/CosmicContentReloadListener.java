package com.cosmicpve.content.reload;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.ContentCandidateBuilder;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.scaling.ScalingProfileData;
import com.cosmicpve.content.definition.stack.StackDefinitionData;
import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class CosmicContentReloadListener
        extends SimplePreparableReloadListener<ValidationResult<ContentSnapshot>> {
    private static final FileToIdConverter SCALING_PROFILES =
            FileToIdConverter.json("cosmicpve/scaling_profiles");
    private static final FileToIdConverter STACK_DEFINITIONS =
            FileToIdConverter.json("cosmicpve/stack_definitions");

    private final CosmicContentRepository repository;

    public CosmicContentReloadListener(CosmicContentRepository repository) {
        this.repository = repository;
    }

    @Override
    protected ValidationResult<ContentSnapshot> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        ContentCandidateBuilder candidate = new ContentCandidateBuilder();
        loadDefinitions(
                resourceManager,
                SCALING_PROFILES,
                ScalingProfileData.CODEC,
                candidate,
                candidate::addScalingProfile);
        loadDefinitions(
                resourceManager,
                STACK_DEFINITIONS,
                StackDefinitionData.CODEC,
                candidate,
                candidate::addStackDefinition);
        return candidate.build();
    }

    @Override
    protected void apply(
            ValidationResult<ContentSnapshot> candidate,
            ResourceManager resourceManager,
            ProfilerFiller profiler) {
        candidate.diagnostics().forEach(CosmicContentReloadListener::logDiagnostic);
        if (!repository.publish(candidate)) {
            throw new IllegalStateException(
                    "CosmicPVE content reload rejected with " + candidate.diagnostics().size() + " diagnostic(s)");
        }

        ContentSnapshot active = repository.snapshot();
        CosmicPVE.LOGGER.info(
                "Published CosmicPVE content revision {}: {} scaling profile(s), {} stack definition(s)",
                active.revision(),
                active.scalingProfiles().size(),
                active.stackDefinitions().size());
        CosmicPVE.LOGGER.debug("Scaling profiles: {}", active.scalingProfiles().keySet());
        CosmicPVE.LOGGER.debug("Stack definitions: {}", active.stackDefinitions().keySet());
    }

    private static <T> void loadDefinitions(
            ResourceManager resourceManager,
            FileToIdConverter converter,
            Codec<T> codec,
            ContentCandidateBuilder candidate,
            BiConsumer<Identifier, T> consumer) {
        converter.listMatchingResources(resourceManager).entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().toString()))
                .forEach(entry -> decodeDefinition(converter, codec, candidate, consumer, entry));
    }

    private static <T> void decodeDefinition(
            FileToIdConverter converter,
            Codec<T> codec,
            ContentCandidateBuilder candidate,
            BiConsumer<Identifier, T> consumer,
            Map.Entry<Identifier, Resource> entry) {
        Identifier definitionId = converter.fileToId(entry.getKey());
        try (Reader reader = entry.getValue().openAsReader()) {
            JsonElement json = JsonParser.parseReader(reader);
            codec.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(message -> candidate.addDiagnostic(ContentDiagnostic.error(
                            entry.getKey().toString(),
                            "Could not decode " + definitionId + ": " + message)))
                    .ifPresent(decoded -> consumer.accept(definitionId, decoded));
        } catch (IOException | JsonParseException exception) {
            candidate.addDiagnostic(ContentDiagnostic.error(
                    entry.getKey().toString(),
                    "Could not read " + definitionId + ": " + exception.getMessage()));
        }
    }

    private static void logDiagnostic(ContentDiagnostic diagnostic) {
        switch (diagnostic.severity()) {
            case ERROR -> CosmicPVE.LOGGER.error("Cosmic content [{}]: {}", diagnostic.source(), diagnostic.message());
            case WARNING -> CosmicPVE.LOGGER.warn("Cosmic content [{}]: {}", diagnostic.source(), diagnostic.message());
        }
    }
}
