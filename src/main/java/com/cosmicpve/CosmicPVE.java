package com.cosmicpve;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.legacy.LegacyCombatService;
import com.cosmicpve.command.CosmicCommands;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.network.CosmicNetwork;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.equipment.armor.ArmorCrystalEventBridge;
import com.cosmicpve.equipment.EquipmentTooltipService;
import com.cosmicpve.equipment.enchantment.EnchantingEventBridge;
import com.cosmicpve.equipment.skin.WeaponSkinEventBridge;
import com.cosmicpve.equipment.heroic.HeroicCrystalEventBridge;
import com.cosmicpve.equipment.enchantment.OxygenateEventBridge;
import com.cosmicpve.equipment.enchantment.OxygenateService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(CosmicPVE.MOD_ID)
public final class CosmicPVE {
    public static final String MOD_ID = "cosmicpve";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CosmicPVE(IEventBus modBus) {
        ModDataComponents.register(modBus);
        ModAttachments.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        modBus.addListener(CosmicNetwork::registerPayloads);
        CosmicContent.register();
        LegacyCombatService.register(modBus);
        CosmicCombat.register();
        NeoForge.EVENT_BUS.addListener(CosmicCommands::register);
        var crystals = new ArmorCrystalEventBridge();
        NeoForge.EVENT_BUS.addListener(crystals::onStacked);
        var tooltips = new EquipmentTooltipService();
        NeoForge.EVENT_BUS.addListener(tooltips::onTooltip);
        var enchanting = new EnchantingEventBridge();
        NeoForge.EVENT_BUS.addListener(enchanting::onStacked);
        var skins = new WeaponSkinEventBridge();
        NeoForge.EVENT_BUS.addListener(skins::onStacked);
        var heroic = new HeroicCrystalEventBridge();
        NeoForge.EVENT_BUS.addListener(heroic::onStacked);
        var oxygenate = new OxygenateEventBridge(new OxygenateService(new EffectiveEnchantmentsResolver()));
        NeoForge.EVENT_BUS.addListener(oxygenate::onBlockDrops);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
