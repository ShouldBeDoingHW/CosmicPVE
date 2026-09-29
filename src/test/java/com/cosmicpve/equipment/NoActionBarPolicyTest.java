package com.cosmicpve.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** Only the central timed-Madness countdown composer may emit Cosmic action-bar text. */
class NoActionBarPolicyTest {
    private static final Pattern ACTION_BAR = Pattern.compile(
            "displayClientMessage\\s*\\([^;]*,\\s*true\\s*\\)|ClientboundSetActionBarTextPacket|setOverlayMessage|setActionBarText",
            Pattern.DOTALL);

    @Test void productionSourcesDoNotSendCosmicActionBarText() throws Exception {
        Path source = Path.of(System.getProperty("cosmicpve.projectDir"), "src", "main", "java");
        int authorized = 0;
        try (var files = Files.walk(source)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                String content = Files.readString(file);
                var matcher = ACTION_BAR.matcher(content);
                if (file.getFileName().toString().equals("MadnessCountdownComposer.java")) {
                    while (matcher.find()) authorized++;
                } else assertFalse(matcher.find(), file.toString());
            }
        }
        assertEquals(1, authorized, "Exactly one centralized Madness action-bar send call");
    }
}
