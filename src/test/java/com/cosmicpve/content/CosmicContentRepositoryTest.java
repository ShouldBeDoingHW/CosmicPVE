package com.cosmicpve.content;

import com.cosmicpve.content.definition.scaling.ScalingProfileData;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CosmicContentRepositoryTest {
    private static final ScalingProfileData VALID_SCALING =
            new ScalingProfileData(1.0D, 1.5D, 2.0D, 2.5D);

    @Test
    void rejectsDuplicateIds() {
        ContentCandidateBuilder builder = new ContentCandidateBuilder();
        Identifier id = Identifier.parse("cosmicpve:duplicate");
        builder.addScalingProfile(id, VALID_SCALING);
        builder.addScalingProfile(id, VALID_SCALING);

        assertFalse(builder.build().isSuccess());
    }

    @Test
    void reportsUnknownDefinitions() {
        CosmicContentRepository repository = new CosmicContentRepository();
        Identifier missing = Identifier.parse("cosmicpve:missing");

        assertTrue(repository.findScalingProfile(missing).isEmpty());
        assertThrows(UnknownContentDefinitionException.class, () -> repository.requireScalingProfile(missing));
    }

    @Test
    void publishesOnlyCompleteValidSnapshots() {
        CosmicContentRepository repository = new CosmicContentRepository();
        Identifier firstId = Identifier.parse("cosmicpve:first");
        ContentCandidateBuilder firstBuilder = new ContentCandidateBuilder();
        firstBuilder.addScalingProfile(firstId, VALID_SCALING);

        assertTrue(repository.publish(firstBuilder.build()));
        ContentSnapshot firstPublished = repository.snapshot();
        assertEquals(1L, firstPublished.revision());

        ContentCandidateBuilder invalidBuilder = new ContentCandidateBuilder();
        invalidBuilder.addScalingProfile(firstId, VALID_SCALING);
        invalidBuilder.addScalingProfile(firstId, VALID_SCALING);
        assertFalse(repository.publish(invalidBuilder.build()));
        assertSame(firstPublished, repository.snapshot());

        Identifier secondId = Identifier.parse("cosmicpve:second");
        ContentCandidateBuilder replacementBuilder = new ContentCandidateBuilder();
        replacementBuilder.addScalingProfile(secondId, VALID_SCALING);
        assertTrue(repository.publish(replacementBuilder.build()));
        assertEquals(2L, repository.snapshot().revision());
        assertTrue(repository.findScalingProfile(firstId).isEmpty());
        assertTrue(repository.findScalingProfile(secondId).isPresent());
    }
}
