package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.attachment.PlayerProfileData;
import com.cosmicpve.combat.stack.CombatStackContainer;
import com.cosmicpve.spacechest.SpaceChestSessionAttachment;
import com.cosmicpve.trial.persistence.TrialPlayerState;
import com.cosmicpve.data.attachment.VKitProgressionData;
import com.cosmicpve.data.attachment.PersonalVaultData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, CosmicPVE.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerProfileData>> PLAYER_PROFILE =
            ATTACHMENTS.register(
                    "player_profile",
                    () -> AttachmentType.builder(PlayerProfileData::createDefault)
                            .serialize(PlayerProfileData.CODEC)
                            .copyOnDeath()
                            .build());

    /** Available on every entity; only persistent stack instances are serialized by the container codec. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<CombatStackContainer>> COMBAT_STACKS =
            ATTACHMENTS.register(
                    "combat_stacks",
                    () -> AttachmentType.builder((java.util.function.Supplier<CombatStackContainer>) CombatStackContainer::new)
                            .serialize(CombatStackContainer.CODEC)
                            .build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<SpaceChestSessionAttachment>> SPACE_CHEST_SESSION =
            ATTACHMENTS.register("space_chest_session", () -> AttachmentType.builder(SpaceChestSessionAttachment::empty)
                    .serialize(SpaceChestSessionAttachment.CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TrialPlayerState>> TRIAL_PLAYER_STATE =
            ATTACHMENTS.register("trial_player_state", () -> AttachmentType.builder(TrialPlayerState::restored)
                    .serialize(TrialPlayerState.CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<VKitProgressionData>> VKIT_PROGRESSION =
            ATTACHMENTS.register("vkit_progression", () -> AttachmentType.builder(VKitProgressionData::empty)
                    .serialize(VKitProgressionData.CODEC).copyOnDeath().build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PersonalVaultData>> PERSONAL_VAULTS =
            ATTACHMENTS.register("personal_vaults", () -> AttachmentType.builder(PersonalVaultData::empty)
                    .serialize(PersonalVaultData.CODEC).copyOnDeath().build());

    private ModAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
