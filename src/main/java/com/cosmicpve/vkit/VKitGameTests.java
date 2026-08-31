package com.cosmicpve.vkit;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelFutureListener;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import com.cosmicpve.network.TrialCelebrationPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry integration coverage for every V-Kit reward and level. */
public final class VKitGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GENERATION =
            FUNCTIONS.register("vkit_generation", ignored -> VKitGameTests::generation);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> REDEMPTION =
            FUNCTIONS.register("vkit_crystal_redemption", ignored -> VKitGameTests::redemption);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> GODLY_BUNDLE =
            FUNCTIONS.register("godly_vkit_bundle_redemption", ignored -> VKitGameTests::godlyBundleRedemption);

    private VKitGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(VKitGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("vkit_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("vkit_generation"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("vkit_generation")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("vkit_crystal_redemption"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("vkit_crystal_redemption")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 200, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
        event.registerTest(CosmicPVE.id("godly_vkit_bundle_redemption"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("godly_vkit_bundle_redemption")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void generation(GameTestHelper helper) {
        verifyWeaponNormalization(helper);
        var enchantments = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var generator = new VKitEquipmentGenerator();
        long seed = 81723L;
        for (VKitDefinition definition : VKitDefinition.ALL) {
            for (VKitEquipmentType type : VKitEquipmentType.values()) {
                for (int level = 1; level <= 10; level++) {
                    var stack = generator.generate(definition, type, level, enchantments, RandomSource.create(seed++));
                    var identity = stack.get(ModDataComponents.VKIT_EQUIPMENT.get());
                    helper.assertTrue(identity != null && identity.kitId().equals(definition.id())
                                    && identity.kitLevel() == level && identity.equipmentType() == type,
                            "V-Kit equipment identity must survive generation");
                    helper.assertTrue(stack.is(type == VKitEquipmentType.ARMOR
                                    ? definition.armor().item() : definition.weapon().item()),
                            "V-Kit generated the wrong base item");
                    var applied = EnchantmentHelper.getEnchantmentsForCrafting(stack);
                    int cosmicPoints = applied.entrySet().stream()
                            .filter(entry -> entry.getKey().unwrapKey().map(key ->
                                    key.identifier().getNamespace().equals(CosmicPVE.MOD_ID)).orElse(false))
                            .mapToInt(entry -> entry.getIntValue()).sum();
                    long cosmicCount = applied.keySet().stream().filter(holder -> holder.unwrapKey().map(key ->
                            key.identifier().getNamespace().equals(CosmicPVE.MOD_ID)).orElse(false)).count();
                    helper.assertTrue(cosmicPoints == VKitEquipmentGenerator.pointBudget(level),
                            "V-Kit must spend its exact Cosmic point budget");
                    helper.assertTrue(cosmicCount <= 5, "V-Kit must respect base Cosmic capacity");
                    helper.assertTrue(stack.getHoverName().getString().endsWith("(" + VKitEquipmentGenerator.roman(level) + ")"),
                            "V-Kit item name must use the permanent Roman-numeral suffix");
                    helper.assertTrue(stack.getHoverName().getStyle().isBold()
                                    && stack.getHoverName().getStyle().isItalic()
                                    && stack.getHoverName().getStyle().getColor().getValue() == definition.color(),
                            "V-Kit item name style must use its configured kit presentation");
                    assertVanilla(helper, stack, level, enchantments);
                }
            }
        }
        helper.succeed();
    }

    private static void redemption(GameTestHelper helper) {
        var context = connectedTestPlayer(helper);
        var player = context.player();
        var inventory = player.getInventory();
        var progression = new VKitProgressionService();
        progression.set(player, VKitDefinition.PHOENIX, 0);
        inventory.clearContent();
        inventory.setSelectedSlot(0);
        for (int slot = 1; slot < net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE; slot++) {
            inventory.setItem(slot, new net.minecraft.world.item.ItemStack(Items.COBBLESTONE, 64));
        }

        for (int redemption = 1; redemption <= 32; redemption++) {
            inventory.setItem(0, new net.minecraft.world.item.ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()));
            var originalCrystal = player.getMainHandItem();
            int soundsBefore = context.connection().soundPackets().size();
            player.gameMode.useItem(player, helper.getLevel(), originalCrystal, InteractionHand.MAIN_HAND);

            var reward = player.getMainHandItem();
            var identity = reward.get(ModDataComponents.VKIT_EQUIPMENT.get());
            int expectedLevel = Math.min(redemption, VKitEquipmentGenerator.MAX_KIT_LEVEL);
            helper.assertTrue(identity != null && identity.kitId().equals(VKitDefinition.PHOENIX.id())
                            && identity.kitLevel() == expectedLevel,
                    "actual server item use must replace the consumed hand crystal with its generated reward");
            helper.assertTrue(progression.level(player, VKitDefinition.PHOENIX) == expectedLevel,
                    "actual redemption must advance progression exactly once");
            long generatedStacks = inventory.getNonEquipmentItems().stream()
                    .filter(stack -> stack.has(ModDataComponents.VKIT_EQUIPMENT.get())).count();
            helper.assertTrue(generatedStacks == 1,
                    "each full-inventory redemption must retain exactly one generated V-Kit stack");
            assertOneSuccessSound(helper, context.connection(), soundsBefore);
        }

        // A partially empty inventory must use the same deterministic hand-replacement path.
        inventory.clearContent();
        inventory.setSelectedSlot(0);
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()));
        int partialSoundsBefore = context.connection().soundPackets().size();
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().has(ModDataComponents.VKIT_EQUIPMENT.get()),
                "partial inventories must also retain the generated reward in the used hand");
        assertOneSuccessSound(helper, context.connection(), partialSoundsBefore);
        inventory.setItem(1, player.getMainHandItem());

        var rejected = new net.minecraft.world.item.ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get());
        rejected.remove(ModDataComponents.VKIT_CRYSTAL.get());
        inventory.setItem(0, rejected);
        int rejectedSoundsBefore = context.connection().soundPackets().size();
        player.gameMode.useItem(player, helper.getLevel(), rejected, InteractionHand.MAIN_HAND);
        helper.assertTrue(context.connection().soundPackets().size() == rejectedSoundsBefore,
                "a rejected Crystal use must emit no player-level-up sound");

        helper.runAfterDelay(2, () -> {
            helper.assertTrue(inventory.getItem(1).has(ModDataComponents.VKIT_EQUIPMENT.get()),
                    "the generated hand replacement must survive later server ticks");
            helper.assertTrue(context.connection().soundPackets().size() == rejectedSoundsBefore,
                    "rejected use must not emit delayed feedback");
            helper.assertTrue(progression.level(player, VKitDefinition.PHOENIX) == 10,
                    "rejected use must not alter capped progression");
            // The prior successful partial-inventory reward was already proven as the hand result;
            // the repeated full-inventory path above additionally establishes later-tick survival.
            helper.assertTrue(context.connection().soundPackets().size() == 33,
                    "33 successful redemptions must emit exactly 33 player-local sounds");
            helper.succeed();
        });
    }

    private static void godlyBundleRedemption(GameTestHelper helper) {
        var context = connectedTestPlayer(helper);
        var player = context.player();
        var testPosition = helper.absolutePos(new net.minecraft.core.BlockPos(1, 1, 1));
        player.setPos(testPosition.getX() + 0.5D, testPosition.getY() + 1.0D, testPosition.getZ() + 0.5D);
        var inventory = player.getInventory();
        inventory.clearContent();
        inventory.setSelectedSlot(0);
        inventory.setItem(0, new net.minecraft.world.item.ItemStack(ModItems.GODLY_VKIT_BUNDLE.get()));
        for (int slot = 1; slot < net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE; slot++)
            inventory.setItem(slot, new net.minecraft.world.item.ItemStack(Items.COBBLESTONE, 64));

        int soundsBefore = context.connection().soundPackets().size();
        int fireworksBefore = context.connection().celebrationPackets();
        player.gameMode.useItem(player, helper.getLevel(), player.getMainHandItem(), InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().is(ModItems.PHOENIX_VKIT_CRYSTAL.get()),
                "Godly Bundle must use Phoenix as the authoritative transformed-hand result");
        helper.runAfterDelay(2, () -> {
            var dropped = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            player.getBoundingBox().inflate(8.0D)).stream()
                    .map(net.minecraft.world.entity.item.ItemEntity::getItem).toList();
            helper.assertTrue(dropped.size() == 3, "full inventory must overflow exactly three Godly Bundle rewards; saw "
                    + dropped.size() + " near " + player.position() + " with " + dropped);
            helper.assertTrue(dropped.stream().filter(stack -> stack.is(ModItems.OGRE_VKIT_CRYSTAL.get())).count() == 1
                            && dropped.stream().filter(stack -> stack.is(ModItems.JUDGEMENT_VKIT_CRYSTAL.get())).count() == 1
                            && dropped.stream().filter(stack -> stack.is(ModItems.SLAYER_VKIT_CRYSTAL.get())).count() == 1,
                    "Godly Bundle overflow must contain exactly Ogre, Judgement, and Slayer Crystals");
            helper.assertTrue(context.connection().soundPackets().size() == soundsBefore + 1
                            && context.connection().soundPackets().getLast().getSound().value() == SoundEvents.PLAYER_LEVELUP,
                    "Godly Bundle must send exactly one targeted level-up sound");
            helper.assertTrue(context.connection().celebrationPackets() == fireworksBefore + 1,
                    "Godly Bundle must request exactly one cosmetic firework");
            helper.succeed();
        });
    }

    private static void assertOneSuccessSound(GameTestHelper helper, RecordingConnection connection, int before) {
        helper.assertTrue(connection.soundPackets().size() == before + 1,
                "each successful redemption must emit exactly one sound packet");
        ClientboundSoundPacket sound = connection.soundPackets().getLast();
        helper.assertTrue(sound.getSound().value() == SoundEvents.PLAYER_LEVELUP
                        && sound.getSource() == SoundSource.PLAYERS,
                "successful redemption must target the player with minecraft:entity.player.levelup");
    }

    private static TestPlayerContext connectedTestPlayer(GameTestHelper helper) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "vkit-redemption-test");
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        ServerPlayer player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(), profile,
                cookie.clientInformation());
        RecordingConnection connection = new RecordingConnection();
        new ServerGamePacketListenerImpl(helper.getLevel().getServer(), connection, player, cookie);
        return new TestPlayerContext(player, connection);
    }

    private record TestPlayerContext(ServerPlayer player, RecordingConnection connection) {}

    private static final class RecordingConnection extends Connection {
        private final List<ClientboundSoundPacket> soundPackets = new ArrayList<>();
        private int celebrationPackets;

        private RecordingConnection() {
            super(PacketFlow.SERVERBOUND);
            // NeoForge's attachment synchronization consults channel attributes when a full
            // inventory overflows to an ItemEntity. Give this recording connection an in-memory
            // channel and the standard mock payload negotiation while continuing to intercept
            // outbound packets below.
            new io.netty.channel.embedded.EmbeddedChannel(this);
            net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(this);
        }

        @Override
        public void send(Packet<?> packet, ChannelFutureListener listener, boolean flush) {
            if (packet instanceof ClientboundSoundPacket sound) soundPackets.add(sound);
            if (packet instanceof ClientboundCustomPayloadPacket custom
                    && custom.payload() instanceof TrialCelebrationPayload) celebrationPackets++;
        }

        private List<ClientboundSoundPacket> soundPackets() {
            return soundPackets;
        }

        private int celebrationPackets() { return celebrationPackets; }
    }

    private static void verifyWeaponNormalization(GameTestHelper helper) {
        var pairs = java.util.List.of(
                java.util.Map.entry(Items.WOODEN_SWORD, Items.WOODEN_AXE),
                java.util.Map.entry(Items.COPPER_SWORD, Items.COPPER_AXE),
                java.util.Map.entry(Items.STONE_SWORD, Items.STONE_AXE),
                java.util.Map.entry(Items.GOLDEN_SWORD, Items.GOLDEN_AXE),
                java.util.Map.entry(Items.IRON_SWORD, Items.IRON_AXE),
                java.util.Map.entry(Items.DIAMOND_SWORD, Items.DIAMOND_AXE),
                java.util.Map.entry(Items.NETHERITE_SWORD, Items.NETHERITE_AXE));
        for (var pair : pairs) {
            var sword = new net.minecraft.world.item.ItemStack(pair.getKey()).getOrDefault(
                    net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            var axe = new net.minecraft.world.item.ItemStack(pair.getValue()).getOrDefault(
                    net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            double swordDamage = sword.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    1.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            double axeDamage = axe.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    1.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            helper.assertTrue(swordDamage == axeDamage, "Normalized sword and axe base damage must match");
            if (pair.getKey() == Items.NETHERITE_SWORD) {
                helper.assertTrue(swordDamage == 10.0 && axeDamage == 10.0,
                        "Netherite Sword and Axe must both use 10 base damage");
            }
        }
    }

    private static void assertVanilla(GameTestHelper helper, net.minecraft.world.item.ItemStack stack, int level,
            net.minecraft.core.Registry<Enchantment> enchantments) {
        var applied = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (stack.is(Items.BOW)) {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.POWER)) == 5,
                    "V-Kit bow must have Power V");
        } else if (stack.getItem() instanceof CrossbowItem) {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.PIERCING)) == 4,
                    "V-Kit crossbow must have Piercing IV");
        } else if (stack.get(ModDataComponents.VKIT_EQUIPMENT.get()).equipmentType() == VKitEquipmentType.ARMOR) {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.PROTECTION)) == 4,
                    "V-Kit armor must have Protection IV");
        } else {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.SHARPNESS)) == 5,
                    "V-Kit melee weapon must have Sharpness V");
        }
        helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.UNBREAKING)) == (level >= 3 ? 3 : 0),
                "Unbreaking milestone is incorrect");
        if (stack.getItem() instanceof BowItem) {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.INFINITY)) == (level >= 8 ? 1 : 0),
                    "Bow Infinity milestone is incorrect");
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.MENDING)) == 0,
                    "V-Kit bow must never receive Mending");
        } else {
            helper.assertTrue(applied.getLevel(enchantments.getOrThrow(Enchantments.MENDING)) == (level >= 8 ? 1 : 0),
                    "Mending milestone is incorrect");
        }
    }

}
