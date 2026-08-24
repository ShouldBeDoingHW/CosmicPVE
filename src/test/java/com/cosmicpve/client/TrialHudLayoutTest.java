package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TrialHudLayoutTest {
    @Test void narrowCardIsFlushRightAndVerticallyCenteredWithoutDynamicExpansion() {
        assertEquals(132, TrialClientPresentation.HUD_HEIGHT);
        assertEquals(120, TrialClientPresentation.HUD_WIDTH);
        var normal = TrialClientPresentation.layout(320, 240);
        assertEquals(200, normal.left()); assertEquals(54, normal.top());
        assertEquals(320, normal.right()); assertEquals(120, normal.width()); assertEquals(132, normal.height());
        var tiny = TrialClientPresentation.layout(80, 100);
        assertEquals(0, tiny.left()); assertEquals(0, tiny.top());
        assertEquals(80, tiny.width()); assertEquals(100, tiny.height());
    }

    @Test void hierarchyFormatsTierRoomAndHumanReadableTimeWithoutIdentifiers() {
        assertEquals("Tier (1/3)", TrialClientPresentation.tierHeading("Apprentice"));
        assertEquals("Tier (2/3)", TrialClientPresentation.tierHeading("Hardcore"));
        assertEquals("Tier (3/3)", TrialClientPresentation.tierHeading("Demonic"));
        assertEquals("Room (#1)", TrialClientPresentation.roomHeading(1));
        assertEquals("Room (#19)", TrialClientPresentation.roomHeading(19));
        assertEquals("9m 11s", TrialClientPresentation.formatSeconds(551));
        assertEquals("10m 00s", TrialClientPresentation.formatSeconds(600));
        assertEquals("0m 47s", TrialClientPresentation.formatSeconds(47));
        assertFalse(TrialClientPresentation.tierHeading("Apprentice").contains("ID"));
    }
}
