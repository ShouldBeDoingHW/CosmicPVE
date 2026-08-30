package com.cosmicpve.personalvault;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.activity.ActivityType;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import org.junit.jupiter.api.Test;

class PersonalVaultPolicyTest {
    @Test void combatTagUsesExactPersistentExpiryBoundaryAndRefresh() {
        assertEquals(300, PersonalVaultCombatTagService.expiresAt(100));
        assertTrue(PersonalVaultCombatTagService.taggedAt(300, 299));
        assertFalse(PersonalVaultCombatTagService.taggedAt(300, 300));
        assertEquals(450, PersonalVaultCombatTagService.expiresAt(250));
        assertEquals(Long.MAX_VALUE, PersonalVaultCombatTagService.expiresAt(Long.MAX_VALUE - 10));
    }

    @Test void onlyAuthoritativeActivitiesAreRestrictedByCentralPolicy() {
        assertFalse(PersonalVaultAccessService.restricted(ActivityType.NONE));
        assertTrue(PersonalVaultAccessService.restricted(ActivityType.TRIAL));
        assertTrue(PersonalVaultAccessService.restricted(ActivityType.INVASION));
        assertTrue(PersonalVaultAccessService.restricted(ActivityType.DUNGEON));
    }

    @Test void unlockPresentationIsStackableGlintingWhiteAndBold() {
        ItemStack unlock = new ItemStack(ModItems.PERSONAL_VAULT_UNLOCK.get(), 5);
        assertEquals(64, unlock.getMaxStackSize());
        assertTrue(unlock.getItem().isFoil(unlock));
        assertTrue(unlock.getHoverName().getStyle().isBold());
        assertEquals(0xFFFFFF, unlock.getHoverName().getStyle().getColor().getValue());
        assertEquals(SoundEvents.ARROW_HIT_PLAYER, PersonalVaultUnlockFeedback.sound());
    }

    @Test void unlockTransactionConsumesAndSignalsExactlyOnceOrNotAtAll() {
        ItemStack single = new ItemStack(ModItems.PERSONAL_VAULT_UNLOCK.get());
        int[] progression = {0}, feedback = {0};
        assertTrue(PersonalVaultUnlockItem.commitUnlock(single, () -> { progression[0]++; return true; },
                () -> feedback[0]++) instanceof net.minecraft.world.InteractionResult.Success);
        assertTrue(single.isEmpty());
        assertArrayEquals(new int[] {1, 1}, new int[] {progression[0], feedback[0]});

        ItemStack stacked = new ItemStack(ModItems.PERSONAL_VAULT_UNLOCK.get(), 5);
        assertTrue(PersonalVaultUnlockItem.commitUnlock(stacked, () -> true,
                () -> feedback[0]++) instanceof net.minecraft.world.InteractionResult.Success);
        assertEquals(4, stacked.getCount());
        assertEquals(2, feedback[0]);

        ItemStack rejected = new ItemStack(ModItems.PERSONAL_VAULT_UNLOCK.get(), 3);
        assertSame(net.minecraft.world.InteractionResult.FAIL,
                PersonalVaultUnlockItem.commitUnlock(rejected, () -> false, () -> feedback[0]++));
        assertEquals(3, rejected.getCount());
        assertEquals(2, feedback[0]);
    }
}
