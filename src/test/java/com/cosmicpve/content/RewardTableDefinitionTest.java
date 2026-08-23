package com.cosmicpve.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.content.definition.reward.RewardDescriptorData;
import com.cosmicpve.content.definition.reward.RewardTableData;
import com.cosmicpve.content.validation.ValidationResult;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class RewardTableDefinitionTest {
    @Test void decodesAndResolvesValidStaticReward() {
        var data = RewardTableData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {"entries":[{"weight":3,"minimum_quantity":1,"maximum_quantity":2,
                "reward":{"type":"static_item","item":"minecraft:golden_apple"}}]}""")).getOrThrow();
        var result = data.resolve(Identifier.parse("cosmicpve:test"), RegistryAccess.EMPTY);
        assertTrue(result.isSuccess());
        assertEquals(3, result.valueOrThrow().totalWeight());
        assertEquals(2, result.valueOrThrow().entries().getFirst().maximumQuantity());
    }

    @Test void rejectsEmptyBadWeightBadQuantityAndUnknownItem() {
        assertFalse(decode("{\"entries\":[]}").resolve(Identifier.parse("cosmicpve:empty"), RegistryAccess.EMPTY).isSuccess());
        assertFalse(decode("{\"entries\":[{\"weight\":0,\"reward\":{\"type\":\"static_item\",\"item\":\"minecraft:apple\"}}]}")
                .resolve(Identifier.parse("cosmicpve:weight"), RegistryAccess.EMPTY).isSuccess());
        assertFalse(decode("{\"entries\":[{\"weight\":1,\"minimum_quantity\":3,\"maximum_quantity\":2,"
                + "\"reward\":{\"type\":\"static_item\",\"item\":\"minecraft:apple\"}}]}")
                .resolve(Identifier.parse("cosmicpve:quantity"), RegistryAccess.EMPTY).isSuccess());
        assertFalse(decode("{\"entries\":[{\"weight\":1,\"reward\":{\"type\":\"static_item\","
                + "\"item\":\"cosmicpve:not_real\"}}]}")
                .resolve(Identifier.parse("cosmicpve:item"), RegistryAccess.EMPTY).isSuccess());
    }

    @Test void failedPublicationKeepsActiveSnapshotAtomic() {
        var repository = new CosmicContentRepository();
        var table = decode("{\"entries\":[{\"weight\":1,\"reward\":{\"type\":\"banknote\",\"cents\":5000}}]}")
                .resolve(Identifier.parse("cosmicpve:money"), RegistryAccess.EMPTY).valueOrThrow();
        assertTrue(repository.publish(ValidationResult.success(new ContentSnapshot(
                0, Map.of(), Map.of(), Map.of(), Map.of(table.id(), table)))));
        var active = repository.snapshot();
        assertFalse(repository.publish(ValidationResult.failure(java.util.List.of(
                com.cosmicpve.content.validation.ContentDiagnostic.error("test", "bad")))));
        assertSame(active, repository.snapshot());
    }

    @Test void descriptorCodecRejectsUnknownType() {
        assertTrue(RewardDescriptorData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"type\":\"not_a_reward\"}")).error().isPresent());
    }

    private static RewardTableData decode(String json) {
        return RewardTableData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    }
}
