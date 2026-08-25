package com.cosmicpve.equipment.mask;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import net.minecraft.world.item.component.ResolvableProfile;

public final class MaskProfiles {
    public static final String MULTI_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM4YzUzZTY2ZjI4Y2YyYzdmYjE1MjNjOWU1ZGUxYWUwY2Y0ZDdhMWZhZjU1M2U3NTI0OTRhOGQ2ZDJlMzIifX19";
    private MaskProfiles() {}
    private static final java.util.concurrent.ConcurrentMap<String, ResolvableProfile> CACHE =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static ResolvableProfile profile(String texture) {
        return CACHE.computeIfAbsent(texture, MaskProfiles::create);
    }
    private static ResolvableProfile create(String texture) {
        var properties = new PropertyMap(com.google.common.collect.ImmutableMultimap.of(
                "textures", new Property("textures", texture)));
        var profile = new GameProfile(UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8)), "CosmicMask", properties);
        return ResolvableProfile.createResolved(profile);
    }
}
