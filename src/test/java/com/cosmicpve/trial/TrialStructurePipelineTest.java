package com.cosmicpve.trial;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TrialStructurePipelineTest {
    private static final Path PROJECT = Path.of(System.getProperty("cosmicpve.projectDir", "."));
    private static final Path SOURCE = PROJECT.resolve("trial rooms");
    private static final Path OBSOLETE_DUPLICATE_SOURCE =
            PROJECT.resolve("src/main/resources/data/cosmicpve/structure/trial");

    /** Canonical source filename to the production room definition that consumes it. */
    private static final Map<String, String> STRUCTURES = new LinkedHashMap<>();

    static {
        STRUCTURES.put("bomb_squad.nbt", "cosmicpve:trial/bomb_squad");
        STRUCTURES.put("circuit_circus.nbt", "cosmicpve:trial/circuit_circus");
        STRUCTURES.put("cold_snap.nbt", "cosmicpve:trial/cold_snap");
        STRUCTURES.put("deadeye_east.nbt", "cosmicpve:trial/deadeye");
        STRUCTURES.put("deadeye_west.nbt", "cosmicpve:trial/deadeye");
        STRUCTURES.put("decision_box.nbt", "cosmicpve:trial/decision_box");
        STRUCTURES.put("development_room.nbt", "cosmicpve:trial/development_room");
        STRUCTURES.put("fire_colony.nbt", "cosmicpve:trial/fire_colony");
        STRUCTURES.put("haze_seek.nbt", "cosmicpve:trial/haze_seek");
        STRUCTURES.put("hidden_graveyard.nbt", "cosmicpve:trial/hidden_graveyard");
        STRUCTURES.put("raiding_rainbow.nbt", "cosmicpve:trial/raiding_rainbow");
        STRUCTURES.put("warzone_giants_east.nbt", "cosmicpve:trial/warzone_giants");
        STRUCTURES.put("warzone_giants_west.nbt", "cosmicpve:trial/warzone_giants");
        STRUCTURES.put("zero_g.nbt", "cosmicpve:trial/zero_g");
    }

    @Test
    void everyCanonicalSourceIsPublishedByteForByte() throws IOException {
        var sourceNames = Files.list(SOURCE)
                .filter(path -> path.getFileName().toString().endsWith(".nbt"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .toList();
        assertEquals(STRUCTURES.keySet().stream().sorted().toList(), sourceNames);

        for (var filename : STRUCTURES.keySet()) {
            var resourcePath = "data/cosmicpve/structure/trial/" + filename;
            try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                assertNotNull(stream, () -> "Missing processed structure " + resourcePath);
                assertArrayEquals(Files.readAllBytes(SOURCE.resolve(filename)), stream.readAllBytes(),
                        () -> "Processed structure drifted from trial rooms/ source: " + filename);
            }
        }
    }

    @Test
    void ordinaryResourceSourceContainsNoDuplicateTrialNbts() throws IOException {
        if (!Files.exists(OBSOLETE_DUPLICATE_SOURCE)) return;
        try (var files = Files.list(OBSOLETE_DUPLICATE_SOURCE)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().endsWith(".nbt")));
        }
    }
}
