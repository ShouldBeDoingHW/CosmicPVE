package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.spacechest.SpaceChestMenu;
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
    private ModMenus() {}
    public static void register(IEventBus bus) { MENUS.register(bus); }
}
