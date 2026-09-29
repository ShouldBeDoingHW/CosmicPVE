package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.trial.madness.MadnessCountdownComposer;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MadnessCountdownComposerTest {
    @Test void combinesOnlyFourPeriodicWarningsInStableOrder() {
        var message = MadnessCountdownComposer.compose(Map.of(
                "rocket_man", 1, "owl_gene", 2, "inventory_shuffle", 3,
                "statues", 1, "time_glitch", 2, "cursed_life", 3));
        assertEquals("Inventory Shuffle: 3 | Owl Gene: 2 | Statues: 1 | Rocket Man: 1", message.getString());
        assertEquals("", MadnessCountdownComposer.compose(Map.of("time_glitch", 2)).getString());
        assertEquals("", MadnessCountdownComposer.compose(Map.of("rocket_man", 0)).getString());
    }
}
