package com.cosmicpve.personalvault;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.attachment.PersonalVaultContents;
import com.cosmicpve.data.attachment.PersonalVaultData;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class PersonalVaultDataTest {
    @Test void strictUnlockMappingHasNoArbitraryVaultCap() {
        assertRows(0, 0, 0, 0);
        assertRows(1, 1, 0, 0);
        assertRows(2, 2, 0, 0);
        assertRows(3, 3, 0, 0);
        assertRows(4, 3, 1, 0);
        assertRows(6, 3, 3, 0);
        assertRows(7, 3, 3, 1);
        var thirty = data(30);
        assertEquals(3, thirty.rowsUnlocked(10));
        assertEquals(0, thirty.rowsUnlocked(11));
        var thirtyOne = data(31);
        assertEquals(1, thirtyOne.rowsUnlocked(11));
        assertEquals(1_000_001L, data(3_000_001L).nextVaultNumber());
    }

    @Test void sparseDistinctVaultContentsRoundTripWithStackState() {
        ItemStack namedDamagedSword = new ItemStack(Items.DIAMOND_SWORD, 1);
        namedDamagedSword.setDamageValue(37);
        namedDamagedSword.set(DataComponents.CUSTOM_NAME, Component.literal("Outside Blade"));
        ItemStack apples = new ItemStack(Items.GOLDEN_APPLE, 7);
        var original = new PersonalVaultData(1, 7, 1234,
                List.of(new PersonalVaultContents(1, List.of(namedDamagedSword)),
                        new PersonalVaultContents(3, List.of(apples))));
        var encoded = PersonalVaultData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        var decoded = PersonalVaultData.CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(7, decoded.totalUnlockedRows());
        assertEquals(1234, decoded.combatTagExpiresAt());
        assertEquals(37, decoded.vault(1).orElseThrow().item(0).getDamageValue());
        assertEquals("Outside Blade", decoded.vault(1).orElseThrow().item(0).getHoverName().getString());
        assertEquals(7, decoded.vault(3).orElseThrow().item(0).getCount());
        assertTrue(decoded.vault(2).isEmpty());
    }

    @Test void expansionPreservesEarlierRowsAndVaultsRemainIsolated() {
        ItemStack first = new ItemStack(Items.EMERALD, 4);
        var vaultOne = new PersonalVaultContents(1, List.of()).withItem(8, first);
        var playerA = new PersonalVaultData(1, 1, 0, List.of(vaultOne));
        var expanded = new PersonalVaultData(1, 3, 0, playerA.vaults());
        assertEquals(4, expanded.vault(1).orElseThrow().item(8).getCount());
        assertTrue(expanded.vault(1).orElseThrow().item(9).isEmpty());
        assertTrue(expanded.vault(2).isEmpty());
        assertTrue(PersonalVaultData.empty().vault(1).isEmpty());
    }

    private static PersonalVaultData data(long rows) { return new PersonalVaultData(1, rows, 0, List.of()); }
    private static void assertRows(long total, int first, int second, int third) {
        var data = data(total);
        assertEquals(first, data.rowsUnlocked(1));
        assertEquals(second, data.rowsUnlocked(2));
        assertEquals(third, data.rowsUnlocked(3));
    }
}
