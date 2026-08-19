package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.attachment.PlayerProfileData;
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

    private ModAttachments() {}

    public static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
    }
}
