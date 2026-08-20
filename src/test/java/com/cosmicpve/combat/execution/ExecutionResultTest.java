package com.cosmicpve.combat.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.cosmicpve.CosmicPVE;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExecutionResultTest {
    @Test
    void executionReceiptIsTerminalAndHasNoDamageChannelOrAmount() {
        var result = new ExecutionResult(
                new ExecutionCause(CosmicPVE.id("test_failure")),
                UUID.randomUUID(), Optional.empty(), 7L, OptionalLong.empty(), true);

        assertEquals("cosmicpve:test_failure", result.cause().id().toString());
        assertFalse(java.util.Arrays.stream(ExecutionResult.class.getRecordComponents())
                .anyMatch(component -> component.getName().toLowerCase(java.util.Locale.ROOT).contains("damage")));
    }
}
