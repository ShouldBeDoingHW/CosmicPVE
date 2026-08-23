package com.cosmicpve.trial;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.instance.protection.InstanceProtectionService;
import com.cosmicpve.instance.structure.InstanceStructureService;
import com.cosmicpve.trial.persistence.TrialInventoryTransactionService;
import com.cosmicpve.trial.persistence.TrialSessionRepository;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class TrialRuntime {
    public static final ResourceKey<Level> INSTANCE_DIMENSION =
            ResourceKey.create(Registries.DIMENSION, CosmicPVE.id("cosmic_instance"));
    private static final TrialSessionRepository REPOSITORY = new TrialSessionRepository();
    private static final TrialInventoryTransactionService INVENTORIES = new TrialInventoryTransactionService();
    private static final InstanceStructureService STRUCTURES = new InstanceStructureService();
    private static final InstanceProtectionService PROTECTION = new InstanceProtectionService();
    private static final TrialSessionService SESSIONS = new TrialSessionService(
            REPOSITORY, INVENTORIES, STRUCTURES, PROTECTION, new TrialTitleService());
    private TrialRuntime() {}
    public static TrialSessionService sessions() { return SESSIONS; }
    public static TrialInventoryTransactionService inventories() { return INVENTORIES; }
    public static InstanceProtectionService protection() { return PROTECTION; }
}
