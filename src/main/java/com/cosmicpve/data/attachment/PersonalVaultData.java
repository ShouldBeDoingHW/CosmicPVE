package com.cosmicpve.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/** Versioned sparse per-player Personal Vault state plus persisted combat-tag expiry. */
public record PersonalVaultData(int dataVersion, long totalUnlockedRows,
        long combatTagExpiresAt, List<PersonalVaultContents> vaults) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final MapCodec<PersonalVaultData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(PersonalVaultData::dataVersion),
            Codec.LONG.optionalFieldOf("total_unlocked_rows", 0L).forGetter(PersonalVaultData::totalUnlockedRows),
            Codec.LONG.optionalFieldOf("combat_tag_expires_at", 0L).forGetter(PersonalVaultData::combatTagExpiresAt),
            PersonalVaultContents.CODEC.listOf().optionalFieldOf("vaults", List.of())
                    .forGetter(PersonalVaultData::vaults)
    ).apply(instance, PersonalVaultData::new));

    public PersonalVaultData {
        if (dataVersion < 1 || totalUnlockedRows < 0 || combatTagExpiresAt < 0)
            throw new IllegalArgumentException("Personal Vault values must be non-negative and versioned");
        var ids = new HashSet<Long>();
        for (var vault : vaults) if (!ids.add(vault.vaultNumber()))
            throw new IllegalArgumentException("Duplicate Personal Vault " + vault.vaultNumber());
        vaults = List.copyOf(vaults);
    }

    public static PersonalVaultData empty() { return new PersonalVaultData(CURRENT_DATA_VERSION, 0L, 0L, List.of()); }

    public int rowsUnlocked(long vaultNumber) {
        if (vaultNumber < 1) return 0;
        long fullVaults = totalUnlockedRows / 3L;
        if (vaultNumber <= fullVaults) return 3;
        return vaultNumber == fullVaults + 1L ? (int) (totalUnlockedRows % 3L) : 0;
    }

    public long nextVaultNumber() { return totalUnlockedRows / 3L + 1L; }
    public int nextRowWithinVault() { return (int) (totalUnlockedRows % 3L) + 1; }
    public Optional<PersonalVaultContents> vault(long number) {
        return vaults.stream().filter(entry -> entry.vaultNumber() == number).findFirst();
    }
}
