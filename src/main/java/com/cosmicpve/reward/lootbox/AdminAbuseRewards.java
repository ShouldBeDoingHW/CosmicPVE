package com.cosmicpve.reward.lootbox;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/** Canonical equal-weight Admin Abuse pool. */
public final class AdminAbuseRewards {
    public static final int CANONICAL_WEIGHT = 1;
    public static final List<Outcome> ALL = List.of(Outcome.GHOSTLY_VEIL, Outcome.COVERT_CLOAK,
            Outcome.NANKADA, Outcome.ASHOKA, Outcome.IDATEN, Outcome.FREEMAN_WALKERS,
            Outcome.IZANAGI, Outcome.ETERNAL_STRIDERS);
    private AdminAbuseRewards() {}

    public static Outcome select(RandomSource random) {
        int roll = random.nextInt(ALL.stream().mapToInt(Outcome::weight).sum());
        for (Outcome outcome : ALL) if ((roll -= outcome.weight()) < 0) return outcome;
        throw new IllegalStateException("Unreachable Admin Abuse reward selection");
    }
    public static Optional<Outcome> find(String serializedName) {
        return ALL.stream().filter(value -> value.serializedName().equals(serializedName)).findFirst();
    }
    public enum Outcome {
        GHOSTLY_VEIL("ghostly_veil", "Fashioned directly out of liquid darkness. Are you scared yet?", 0x061630),
        COVERT_CLOAK("covert_cloak", "I solemnly swear that I am up to no good.", 0x061630),
        NANKADA("nankada", "Years of love can be forgotten in a moment of hatred.", 0xFFE578),
        ASHOKA("ashoka", "Shhh. Do you hear that? It’s the final seconds of your life.", 0x103963),
        IDATEN("idaten", "Lightweight and clout flavored. Any time, any place.", 0xAA0000),
        FREEMAN_WALKERS("freeman_walkers", "Sandles capable of breaking mach 3 in order to hunt you down.", 0xFF55FF),
        IZANAGI("izanagi", "Live and die by the blade or forever be alone.", 0xFFAA00),
        ETERNAL_STRIDERS("eternal_striders", "A timeless piece of a timeless legend.", 0x55FFFF);
        private final String serializedName, flavor; private final int flavorColor;
        Outcome(String serializedName, String flavor, int flavorColor) {
            this.serializedName = serializedName; this.flavor = flavor; this.flavorColor = flavorColor;
        }
        public String serializedName() { return serializedName; }
        public Identifier id() { return CosmicPVE.id(serializedName); }
        public int weight() { return CANONICAL_WEIGHT; }
        public Component flavor() { return Component.literal(flavor).withStyle(style -> style.withColor(
                this == GHOSTLY_VEIL || this == COVERT_CLOAK ? 0x0C2B66 : flavorColor)
                .withBold(this == GHOSTLY_VEIL || this == COVERT_CLOAK).withItalic(true)); }
    }
}
