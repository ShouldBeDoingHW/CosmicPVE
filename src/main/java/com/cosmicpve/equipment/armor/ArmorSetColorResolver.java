package com.cosmicpve.equipment.armor;

import com.cosmicpve.data.component.ArmorSetIdentity;
import java.util.OptionalInt;
import org.jspecify.annotations.Nullable;

public final class ArmorSetColorResolver {
    private ArmorSetColorResolver() {}
    public static OptionalInt color(@Nullable ArmorSetIdentity identity) {
        return identity == null ? OptionalInt.empty() : OptionalInt.of(identity.color());
    }
    public static int argbOrWhite(@Nullable ArmorSetIdentity identity) {
        return color(identity).isEmpty() ? 0xFFFFFFFF : 0xFF000000 | color(identity).getAsInt();
    }
}
