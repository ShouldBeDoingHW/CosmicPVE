package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.spacechest.SpaceChestMenu;
import com.cosmicpve.trial.TrialDecisionMenu;
import com.cosmicpve.tinkerer.TinkererMenu;
import com.cosmicpve.personalvault.PersonalVaultMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, CosmicPVE.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<SpaceChestMenu>> SPACE_CHEST = MENUS.register(
            "space_chest", () -> IMenuTypeExtension.create(SpaceChestMenu::client));
    public static final DeferredHolder<MenuType<?>, MenuType<TrialDecisionMenu>> TRIAL_DECISION = MENUS.register(
            "trial_decision", () -> IMenuTypeExtension.create(TrialDecisionMenu::client));
    public static final DeferredHolder<MenuType<?>, MenuType<TinkererMenu>> TINKERER = MENUS.register(
            "tinkerer", () -> IMenuTypeExtension.create(TinkererMenu::client));
    public static final DeferredHolder<MenuType<?>, MenuType<PersonalVaultMenu>> PERSONAL_VAULT = MENUS.register(
            "personal_vault", () -> IMenuTypeExtension.create(PersonalVaultMenu::client));
    private ModMenus() {}
    public static void register(IEventBus bus) { MENUS.register(bus); }
}
