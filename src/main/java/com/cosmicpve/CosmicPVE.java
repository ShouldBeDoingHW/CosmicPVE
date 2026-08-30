package com.cosmicpve;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.legacy.LegacyCombatService;
import com.cosmicpve.command.CosmicCommands;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.network.CosmicNetwork;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.registry.ModBlocks;
import com.cosmicpve.registry.ModEntities;
import com.cosmicpve.registry.ModMobEffects;
import com.cosmicpve.registry.ModMenus;
import com.cosmicpve.registry.ModBlockEntities;
import com.cosmicpve.spacechest.SpaceChestEventBridge;
import com.cosmicpve.loot.ModLootModifiers;
import com.cosmicpve.progression.ArmorRecipeProgression;
import com.cosmicpve.equipment.armor.ArmorCrystalEventBridge;
import com.cosmicpve.equipment.EquipmentTooltipService;
import com.cosmicpve.equipment.enchantment.EnchantingEventBridge;
import com.cosmicpve.equipment.skin.WeaponSkinEventBridge;
import com.cosmicpve.equipment.heroic.HeroicCrystalEventBridge;
import com.cosmicpve.equipment.enchantment.OxygenateEventBridge;
import com.cosmicpve.equipment.enchantment.OxygenateService;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.MiningEnchantmentEventBridge;
import com.cosmicpve.equipment.repair.RepairScrollEventBridge;
import com.cosmicpve.trial.TrialBootstrap;
import com.cosmicpve.trial.trinket.TrialTrinketEventBridge;
import com.cosmicpve.equipment.mask.MaskEventBridge;
import com.cosmicpve.equipment.mask.MaskAnvilEventBridge;
import com.cosmicpve.conquest.ConquestBootstrap;
import com.cosmicpve.conquest.ConquestGameTests;
import com.cosmicpve.vkit.VKitGameTests;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

@Mod(CosmicPVE.MOD_ID)
public final class CosmicPVE {
    public static final String MOD_ID = "cosmicpve";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CosmicPVE(IEventBus modBus) {
        ModDataComponents.register(modBus);
        ModMobEffects.register(modBus);
        ModAttachments.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ConquestGameTests.register(modBus);
        VKitGameTests.register(modBus);
        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModMenus.register(modBus);
        ModLootModifiers.register(modBus);
        modBus.addListener(CosmicNetwork::registerPayloads);
        modBus.addListener(CosmicPVE::registerProgressionDataPack);
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
        var miningEnchantments = new MiningEnchantmentEventBridge();
        NeoForge.EVENT_BUS.addListener(miningEnchantments::onBlockDrops);
        NeoForge.EVENT_BUS.addListener(miningEnchantments::onBreakSpeed);
        var repairs = new RepairScrollEventBridge();
        NeoForge.EVENT_BUS.addListener(repairs::onStacked);
        var trialTrinkets = new TrialTrinketEventBridge();
        NeoForge.EVENT_BUS.addListener(trialTrinkets::onStacked);
        var masks = new MaskEventBridge();
        NeoForge.EVENT_BUS.addListener(masks::onStacked);
        var maskAnvils = new MaskAnvilEventBridge();
        NeoForge.EVENT_BUS.addListener(maskAnvils::onAnvilUpdate);
        NeoForge.EVENT_BUS.addListener(maskAnvils::onAnvilCraft);
        ArmorRecipeProgression.register();
        TrialBootstrap.register();
        ConquestBootstrap.register();
        var spaceChests = new SpaceChestEventBridge();
        NeoForge.EVENT_BUS.addListener(spaceChests::onLogin);
        NeoForge.EVENT_BUS.addListener(spaceChests::onRespawn);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    private static void registerProgressionDataPack(AddPackFindersEvent event) {
        event.addPackFinders(id("resourcepacks/progression_cleanup"), PackType.SERVER_DATA,
                Component.literal("CosmicPVE Progression Cleanup"), PackSource.BUILT_IN,
                true, Pack.Position.TOP);
    }
}
