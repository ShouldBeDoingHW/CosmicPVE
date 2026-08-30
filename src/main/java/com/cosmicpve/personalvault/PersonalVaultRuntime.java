package com.cosmicpve.personalvault;

public final class PersonalVaultRuntime {
    private static final PersonalVaultService VAULTS = new PersonalVaultService();
    private static final PersonalVaultCombatTagService COMBAT_TAGS = new PersonalVaultCombatTagService(VAULTS);
    private static final PersonalVaultAccessService ACCESS = new PersonalVaultAccessService(COMBAT_TAGS);
    private PersonalVaultRuntime() {}
    public static PersonalVaultService vaults() { return VAULTS; }
    public static PersonalVaultCombatTagService combatTags() { return COMBAT_TAGS; }
    public static PersonalVaultAccessService access() { return ACCESS; }
}
