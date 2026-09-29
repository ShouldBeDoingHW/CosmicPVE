package com.cosmicpve.economy.flashsale;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.data.component.MysterySpawnerTier;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.spawner.MysterySpawners;
import com.cosmicpve.tinkerer.GearSalvageService;
import com.cosmicpve.vkit.VKitDefinition;
import com.cosmicpve.vkit.VKitProgressionService;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry coverage for every supported Step 8F standard reward row and active sale factory. */
public final class Step8FGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONSTRUCTION =
            FUNCTIONS.register("step8f_reward_construction", ignored -> Step8FGameTests::construction);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> USE_WRITEBACK =
            FUNCTIONS.register("step8f1_item_use_writeback", ignored -> Step8FGameTests::itemUseWriteback);
    private Step8FGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(Step8FGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("step8f_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("step8f_reward_construction"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("step8f_reward_construction")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("step8f1_item_use_writeback"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("step8f1_item_use_writeback")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 250, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void construction(GameTestHelper helper) {
        var generator = new RewardGeneratorService();
        var random = helper.getLevel().getRandom();
        var context = new RewardGenerationContext(helper.getLevel().registryAccess(), random, null);
        var expectedCounts = Map.of("ultimate", 20, "legendary", 21, "mastery", 24);
        expectedCounts.forEach((tier, expected) -> {
            var table = CosmicContent.repository().requireRewardTable(CosmicPVE.id("space_chest/" + tier));
            helper.assertTrue(table.entries().size() == expected, tier + " Space Chest row count must be exact");
            table.entries().forEach(entry -> helper.assertTrue(generator.generate(entry.reward(), context).isPresent(),
                    tier + " reward must construct: " + entry.reward()));
            var equipment = table.entries().stream().filter(entry -> entry.reward()
                    instanceof com.cosmicpve.content.definition.reward.RewardDescriptor.GeneratedEquipment).toList();
            helper.assertTrue(equipment.size() == 2, tier + " must have exactly one armor and one weapon row");
            var categories = new java.util.HashSet<com.cosmicpve.content.definition.reward.GeneratedEquipmentCategory>();
            for (var row : equipment) {
                var definition = ((com.cosmicpve.content.definition.reward.RewardDescriptor.GeneratedEquipment) row.reward()).definition();
                categories.add(definition.category());
                int expectedWeight = switch (tier) { case "ultimate" -> 7; case "legendary" -> 8; default -> 10; };
                helper.assertTrue(row.weight() == expectedWeight, tier + " equipment row weight");
                var service = new com.cosmicpve.reward.GeneratedEquipmentService();
                var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
                var observedTypes = new java.util.HashSet<net.minecraft.world.item.Item>();
                var sampleRandom = net.minecraft.util.RandomSource.create(12345 + tier.hashCode());
                for (int seed = 0; seed < 100; seed++) {
                    var item = service.generate(definition, enchantments, sampleRandom);
                    observedTypes.add(item.getItem());
                    helper.assertTrue(item.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME) != null
                            && !item.getHoverName().getString().isBlank()
                            && !item.getHoverName().getStyle().isItalic(), "Generated name must be stable and non-italic");
                    helper.assertTrue(com.cosmicpve.reward.GeneratedEquipmentNames.nouns(item.getItem()).stream()
                            .anyMatch(noun -> item.getHoverName().getString().contains(" " + noun)),
                            "Generated name must use an equipment-compatible noun");
                    var cosmic = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(item)
                            .entrySet().stream().filter(entry -> entry.getKey().unwrapKey().map(key ->
                                    key.identifier().getNamespace().equals(CosmicPVE.MOD_ID)).orElse(false)).toList();
                    helper.assertTrue(cosmic.size() >= Math.min(definition.minimumEnchantments(),
                                    service.candidates(item, definition, enchantments).size())
                            && cosmic.size() <= definition.maximumEnchantments(), "Cosmic count must be within available range");
                    cosmic.forEach(entry -> {
                        var spec = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(
                                entry.getKey().unwrapKey().orElseThrow().identifier()).orElseThrow();
                        helper.assertTrue(spec.randomPoolEligible() && spec.tier().ordinal() <= definition.maximumRarity().ordinal()
                                && entry.getIntValue() >= 1 && entry.getIntValue() <= spec.maxLevel()
                                && entry.getKey().value().canEnchant(item), "Generated Cosmic enchant must be valid");
                    });
                    assertStandardEnchants(helper, item, tier, enchantments);
                }
                helper.assertTrue(observedTypes.size() == 4, "Every generated equipment category must reach all four item types");
            }
            helper.assertTrue(categories.size() == 2, tier + " needs distinct armor and weapon categories");
        });
        var impossible = CosmicContent.repository().requireRewardTable(CosmicPVE.id("trial/impossible"));
        helper.assertTrue(impossible.entries().size() == 22 && impossible.totalWeight() == 184,
                "Impossible table must include its exact rows plus the production Memory Chest and Swag Bag");
        impossible.entries().forEach(entry -> helper.assertTrue(generator.generate(entry.reward(), context).isPresent(),
                "Impossible reward must construct: " + entry.reward()));
        helper.assertTrue(FlashSaleCatalog.CANONICAL_ROWS.size() == 29, "Canonical Flash Sale row count must be 29");
        helper.assertTrue(FlashSaleCatalog.productionRows().size() == 28,
                "Only the unresolved Abandoned Spaceship Portal row may be nonselectable");
        FlashSaleCatalog.productionRows().forEach(entry -> helper.assertTrue(entry.create(random).isPresent(),
                "Active Flash Sale reward must construct: " + entry.id()));
        helper.assertTrue(FlashSaleCatalog.find("memory_chest").orElseThrow().productionSelectable(),
                "Memory Chest must be a production Flash Sale row");
        helper.succeed();
    }

    private static void itemUseWriteback(GameTestHelper helper) {
        var context = connectedTestPlayer(helper);
        var player = context.player();
        for (MysterySpawnerTier tier : MysterySpawnerTier.values()) {
            verifyMysteryUse(helper, player, tier, false);
            verifyMysteryUse(helper, player, tier, true);
        }

        var inventory = player.getInventory();
        inventory.clearContent();
        inventory.setSelectedSlot(0);
        inventory.setItem(0, MysterySpawners.create(MysterySpawnerTier.SIMPLE, 2));
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().is(ModItems.MYSTERY_SIMPLE_SPAWNER.get())
                        && player.getMainHandItem().getCount() == 1,
                "multi-source use must preserve exactly one remaining Mystery Spawner in hand");
        helper.assertTrue(countTypedSpawners(inventory) == 1,
                "multi-source use must safely deliver exactly one generated Mob Spawner");

        inventory.clearContent();
        inventory.setSelectedSlot(0);
        player.totalExperience = 0;
        player.experienceLevel = 0;
        player.experienceProgress = 0;
        inventory.setItem(0, new GearSalvageService().bottle(2_275));
        int soundsBefore = context.connection().sounds().size();
        int messagesBefore = context.connection().messages().size();
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(player.totalExperience == 2_275 && player.getMainHandItem().isEmpty(),
                "typed XP Bottle must grant exact raw XP and consume once");
        helper.assertTrue(context.connection().sounds().size() == soundsBefore + 1,
                "typed XP Bottle must retain its one success sound");
        ClientboundSoundPacket xpSound = context.connection().sounds().getLast();
        helper.assertTrue(xpSound.getSound().value() == net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP
                        && xpSound.getPitch() == 1.5F,
                "typed XP Bottle must use the accepted level-up sound at pitch 1.5");
        helper.assertTrue(context.connection().messages().size() == messagesBefore + 1
                        && context.connection().messages().getLast().content().getString().equals("+2,275 XP")
                        && context.connection().messages().getLast().content().getStyle().isBold()
                        && context.connection().messages().getLast().content().getStyle().getColor().getValue() == 0x55FF55,
                "typed XP Bottle must send one bold standard-green grouped-XP message");

        inventory.setItem(0, new ItemStack(ModItems.SALVAGED_XP_BOTTLE.get()));
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(context.connection().sounds().size() == soundsBefore + 1
                        && context.connection().messages().size() == messagesBefore + 1
                        && player.getMainHandItem().getCount() == 1,
                "malformed XP Bottle must emit no feedback and consume nothing");

        var progression = new VKitProgressionService();
        progression.set(player, VKitDefinition.PHOENIX, 9);
        inventory.setItem(0, new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()));
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(progression.level(player, VKitDefinition.PHOENIX) == 10
                        && context.connection().messages().getLast().content().getString().equals(
                                "Phoenix V-Kit is now level X."),
                "level IX redemption must retain the normal reaching-X message");
        inventory.setItem(0, new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()));
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(progression.level(player, VKitDefinition.PHOENIX) == 10
                        && player.getMainHandItem().has(ModDataComponents.VKIT_EQUIPMENT.get())
                        && context.connection().messages().getLast().content().getString().equals(
                                "Your Phoenix Vkit is already level 10. Nice!"),
                "level-X repeat redemption must still award level-X equipment with the corrected message");
        helper.succeed();
    }

    private static void verifyMysteryUse(GameTestHelper helper, ServerPlayer player, MysterySpawnerTier tier,
            boolean fullInventory) {
        Inventory inventory = player.getInventory();
        inventory.clearContent();
        inventory.setSelectedSlot(0);
        if (fullInventory) {
            for (int slot = 1; slot < Inventory.INVENTORY_SIZE; slot++)
                inventory.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
        }
        inventory.setItem(0, MysterySpawners.create(tier, 1));
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        ItemStack reward = player.getMainHandItem();
        var identity = reward.get(ModDataComponents.MOB_SPAWNER.get());
        helper.assertTrue(reward.is(ModItems.MOB_SPAWNER.get()) && identity != null
                        && MysterySpawners.pool(tier).contains(identity.entityTypeId()),
                tier + " final source must transform into one canonical typed Mob Spawner");
        helper.assertTrue(countTypedSpawners(inventory) == 1,
                tier + " use must retain exactly one reward with no duplicate");
    }

    private static long countTypedSpawners(Inventory inventory) {
        return inventory.getNonEquipmentItems().stream()
                .filter(stack -> stack.is(ModItems.MOB_SPAWNER.get())
                        && stack.has(ModDataComponents.MOB_SPAWNER.get())).count();
    }

    private static void assertStandardEnchants(GameTestHelper helper, net.minecraft.world.item.ItemStack stack,
            String tier, net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry) {
        var item = stack.getItem();
        helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.UNBREAKING) == 3,
                "Generated equipment needs Unbreaking III");
        boolean mastery = tier.equals("mastery");
        boolean bow = item == net.minecraft.world.item.Items.BOW;
        helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.MENDING)
                == (mastery && !bow ? 1 : 0), "Only eligible Mastery gear receives Mending");
        if (item == net.minecraft.world.item.Items.IRON_HELMET || item == net.minecraft.world.item.Items.IRON_CHESTPLATE
                || item == net.minecraft.world.item.Items.IRON_LEGGINGS || item == net.minecraft.world.item.Items.IRON_BOOTS) {
            helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.PROTECTION) == 4,
                    "Armor needs Protection IV");
        } else if (item == net.minecraft.world.item.Items.DIAMOND_SWORD || item == net.minecraft.world.item.Items.DIAMOND_AXE) {
            helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.SHARPNESS) == 5,
                    "Diamond melee needs Sharpness V");
            helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.FIRE_ASPECT)
                    == (item == net.minecraft.world.item.Items.DIAMOND_SWORD ? 2 : 0), "Only Sword gets Fire Aspect II");
        } else if (bow) {
            helper.assertTrue(vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.POWER) == 5
                    && vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.INFINITY) == 1
                    && vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.FLAME) == 1,
                    "Bow needs Power V, Infinity I, Flame I");
        } else {
            helper.assertTrue(item == net.minecraft.world.item.Items.CROSSBOW
                    && vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.QUICK_CHARGE) == 3
                    && vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.PIERCING) == 0
                    && vanillaLevel(registry, stack, net.minecraft.world.item.enchantment.Enchantments.MULTISHOT) == 0,
                    "Crossbow needs Quick Charge III without Piercing or Multishot");
        }
    }

    private static int vanillaLevel(net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry,
            net.minecraft.world.item.ItemStack stack,
            net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(registry.getOrThrow(key), stack);
    }

    private static TestPlayerContext connectedTestPlayer(GameTestHelper helper) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "step8f1-use-test");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile,
                cookie.clientInformation());
        RecordingConnection connection = new RecordingConnection();
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie);
        return new TestPlayerContext(player, connection);
    }

    private record TestPlayerContext(ServerPlayer player, RecordingConnection connection) {}

    private static final class RecordingConnection extends Connection {
        private final List<ClientboundSoundPacket> sounds = new ArrayList<>();
        private final List<ClientboundSystemChatPacket> messages = new ArrayList<>();
        private RecordingConnection() {
            super(PacketFlow.SERVERBOUND);
            new io.netty.channel.embedded.EmbeddedChannel(this);
            net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(this);
        }
        @Override public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
            if (packet instanceof ClientboundSoundPacket sound) sounds.add(sound);
            if (packet instanceof ClientboundSystemChatPacket message) messages.add(message);
        }
        private List<ClientboundSoundPacket> sounds() { return sounds; }
        private List<ClientboundSystemChatPacket> messages() { return messages; }
    }
}
