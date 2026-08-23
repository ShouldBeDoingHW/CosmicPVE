package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfile;
import com.cosmicpve.content.definition.stack.StackDefinition;
import com.cosmicpve.content.definition.armor.ArmorSetDefinition;
import com.cosmicpve.content.definition.reward.RewardTable;
import com.cosmicpve.content.definition.trial.TrialRoomDefinition;
import com.cosmicpve.content.validation.ValidationResult;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.Identifier;

public final class CosmicContentRepository {
    private final AtomicReference<ContentSnapshot> active = new AtomicReference<>(ContentSnapshot.EMPTY);

    public ContentSnapshot snapshot() {
        return active.get();
    }

    public Optional<ScalingProfile> findScalingProfile(Identifier id) {
        return Optional.ofNullable(snapshot().scalingProfiles().get(id));
    }

    public ScalingProfile requireScalingProfile(Identifier id) {
        return findScalingProfile(id)
                .orElseThrow(() -> new UnknownContentDefinitionException("scaling profile", id));
    }

    public Optional<StackDefinition> findStackDefinition(Identifier id) {
        return Optional.ofNullable(snapshot().stackDefinitions().get(id));
    }

    public StackDefinition requireStackDefinition(Identifier id) {
        return findStackDefinition(id)
                .orElseThrow(() -> new UnknownContentDefinitionException("stack", id));
    }

    public Optional<ArmorSetDefinition> findArmorSetDefinition(Identifier id) {
        return Optional.ofNullable(snapshot().armorSetDefinitions().get(id));
    }

    public ArmorSetDefinition requireArmorSetDefinition(Identifier id) {
        return findArmorSetDefinition(id)
                .orElseThrow(() -> new UnknownContentDefinitionException("armor set", id));
    }

    public Optional<RewardTable> findRewardTable(Identifier id) {
        return Optional.ofNullable(snapshot().rewardTables().get(id));
    }

    public RewardTable requireRewardTable(Identifier id) {
        return findRewardTable(id).orElseThrow(() -> new UnknownContentDefinitionException("reward table", id));
    }

    public Optional<TrialRoomDefinition> findTrialRoom(Identifier id) {
        return Optional.ofNullable(snapshot().trialRooms().get(id));
    }

    public TrialRoomDefinition requireTrialRoom(Identifier id) {
        return findTrialRoom(id).orElseThrow(() -> new UnknownContentDefinitionException("Trial room", id));
    }

    public synchronized boolean publish(ValidationResult<ContentSnapshot> candidate) {
        if (!candidate.isSuccess()) {
            return false;
        }
        ContentSnapshot previous = active.get();
        active.set(candidate.valueOrThrow().withRevision(previous.revision() + 1L));
        return true;
    }
}
