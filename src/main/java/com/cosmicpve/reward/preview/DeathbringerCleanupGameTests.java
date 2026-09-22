package com.cosmicpve.reward.preview;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.equipment.armor.*;
import com.cosmicpve.equipment.enchantment.*;
import com.cosmicpve.registry.*;
import com.cosmicpve.reward.lootbox.*;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DeathbringerCleanupGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("deathbringer_cleanup", ignored -> DeathbringerCleanupGameTests::verify); }
    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(DeathbringerCleanupGameTests::registerTests);
    }
    private static void registerTests(RegisterGameTestsEvent event) {
        var environment = event.registerEnvironment(CosmicPVE.id("deathbringer_cleanup_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("deathbringer_cleanup"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("deathbringer_cleanup")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 750, 0, true, Rotation.NONE, false, 1, 1, false)));
    }
    private static ServerPlayer mock(GameTestHelper helper) {
        Consumer<PlayerEvent.PlayerLoggedInEvent> configure = event -> {
            if (event.getEntity() instanceof ServerPlayer player)
                net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(player.connection.getConnection());
        };
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.HIGHEST, configure);
        try { return helper.makeMockServerPlayerInLevel(); }
        finally { NeoForge.EVENT_BUS.unregister(configure); }
    }
    private static ItemStack book(net.minecraft.resources.Identifier id, int level) {
        var tier = CosmicEnchantmentSpecs.find(id).orElseThrow().tier();
        ItemStack book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        book.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new com.cosmicpve.data.component.CosmicEnchantmentBookData(
                com.cosmicpve.data.component.CosmicEnchantmentBookData.CURRENT_DATA_VERSION, id, level,
                tier == CosmicEnchantmentTier.MASTERY ? 49 : 100, tier == CosmicEnchantmentTier.MASTERY ? 51 : 100));
        return book;
    }
    private static void verify(GameTestHelper helper) {
        verifyTrialRewards(helper);
        verifyCombat(helper);
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var ordinary = registry.getOrThrow(ModEnchantments.DEATHBRINGER);
        var heroic = registry.getOrThrow(ModEnchantments.PLANETARY_DEATHBRINGER);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        helper.assertTrue(ordinary.value().canEnchant(helmet) && !ordinary.value().canEnchant(new ItemStack(Items.IRON_BOOTS)), "Helmet applicability");
        var application = new CosmicBookApplicationService(registry, new CustomEnchantCapacityService(), new WhiteScrollProtectionService(), () -> 1);
        EnchantmentHelper.updateEnchantments(helmet, mutable -> mutable.set(ordinary, 2));
        var rejected = application.apply(book(heroic.unwrapKey().orElseThrow().identifier(), 1), helmet, helmet);
        helper.assertTrue(rejected.outcome() == CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE, "Conversion requires Deathbringer III");
        EnchantmentHelper.updateEnchantments(helmet, mutable -> mutable.set(ordinary, 3));
        var converted = application.apply(book(heroic.unwrapKey().orElseThrow().identifier(), 1), helmet, helmet);
        helper.assertTrue(converted.outcome() == CosmicBookApplicationResult.Outcome.SUCCESS
                && EnchantmentHelper.getEnchantmentsForCrafting(helmet).getLevel(ordinary) == 0
                && new CustomEnchantCapacityService().used(helmet) == 1, "Conversion replaces one slot without coexistence");

        ServerPlayer player = mock(helper);
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5,2,1.5)));
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        var server = helper.getLevel().getServer();
        var sourceStack = server.createCommandSourceStack().withEntity(player);
        try {
            for (String id : List.of("deathbringer", "planetary_deathbringer")) for (int level = 1; level <= 3; level++)
                helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic enchant book give @s cosmicpve:" + id
                        + " " + level + " 100 100", sourceStack) > 0, "Runtime book command delivers canonical levels");
            helper.assertTrue(server.getCommands().getDispatcher().execute("give @s cosmicpve:call_of_forest_10", sourceStack) > 0, "Call give command registered");
            helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic adventure inspect", sourceStack) > 0, "Adventure inspection command registered");
            helper.assertTrue(server.getCommands().getDispatcher().execute("masklimit", sourceStack) >= 2, "Mask policy report registered");
            helper.assertTrue(server.getCommands().getDispatcher().execute("enchanter", sourceStack) > 0, "Enchanter command registered");
            player.closeContainer();
            for (String set : List.of("yeti", "phantom")) helper.assertTrue(server.getCommands().getDispatcher().execute(
                    "cosmic armor crystal give @s cosmicpve:" + set + " 100", sourceStack) > 0, "Typed armor crystal command registered");
            for (String tier : List.of("ultimate", "legendary", "mastery")) helper.assertTrue(server.getCommands().getDispatcher().execute(
                    "cosmic space-chest give @s " + tier + " 1", sourceStack) > 0, "Tiered Space Chest give command registered");
            helper.assertTrue(server.getCommands().getDispatcher().execute("vkit", sourceStack) > 0, "V-Kit informational menu registered");
            helper.assertTrue(server.getCommands().getDispatcher().execute("cosmic containers memory-chest @s", sourceStack) > 0,
                    "Memory Chest fixture command registered");
            for (String season : List.of("spring", "summer", "fall", "winter")) {
                helper.assertTrue(server.getCommands().getDispatcher().execute(
                        "cosmic containers cosmic-crate give @s " + season, sourceStack) > 0,
                        "Full placeholder crate command registered");
                for (String side : List.of("left", "right")) helper.assertTrue(server.getCommands().getDispatcher().execute(
                        "cosmic containers cosmic-crate give-half @s " + season + " " + side, sourceStack) > 0,
                        "Every seasonal half command registered");
            }
            for (String orb : List.of("armor-9", "armor-10", "weapon-11", "weapon-12"))
                helper.assertTrue(server.getCommands().getDispatcher().execute(
                        "cosmic containers higher-lore-orb @s " + orb, sourceStack) > 0,
                        "Higher-lore Orb command registered");
            player.closeContainer();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
        player.getInventory().clearContent();
        ItemStack cache = new ItemStack(ModItems.SECRET_WEAPON_CACHE.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, cache);
        helper.assertTrue(ModItems.SECRET_WEAPON_CACHE.get().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND)
                == net.minecraft.world.InteractionResult.SUCCESS_SERVER && cache.isEmpty(), "Actual right-click Cache opens and consumes exactly once");
        var obligation = player.getData(ModAttachments.LOOT_ANIMATION);
        helper.assertTrue(obligation.valid() && obligation.allRewards().size() == 1
                && obligation.allRewards().getFirst().has(ModDataComponents.SIGNATURE_WEAPON.get()), "Cache journals one real signature weapon");
        player.closeContainer();
        com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE.closed(player);
        helper.assertTrue(player.getInventory().getNonEquipmentItems().stream().filter(stack -> stack.has(ModDataComponents.SIGNATURE_WEAPON.get()))
                .mapToInt(ItemStack::getCount).sum() == 1, "Cache early-close recovery awards one weapon without duplication");
        player.getInventory().clearContent();
        for (var item : List.of(ModItems.SECRET_WEAPON_CACHE.get(), ModItems.SPACE_DUST_BUNDLE.get(), ModItems.ADMIN_ABUSE.get(),
                ModItems.COSMIC_ENCHANTMENT_TABLE.get(), ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get(),
                ModItems.MYSTERY_CALL_OF_ADVENTURE.get(), ModItems.RANDOM_WEAPON_SKIN_GENERATOR.get(),
                ModItems.MYSTERY_SIMPLE_SPAWNER.get(), ModItems.MYSTERY_ELITE_SPAWNER.get(),
                ModItems.MYSTERY_MASTERY_SPAWNER.get(), ModItems.GODLY_VKIT_BUNDLE.get(), ModItems.MEMORY_CHEST.get())) {
            ItemStack source = new ItemStack(item);
            try {
                helper.assertTrue(server.getCommands().getDispatcher().execute("give @s "
                        + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item), sourceStack) > 0,
                        "Runtime give command materializes every supported preview container");
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException failure) { throw new IllegalStateException(failure); }
            var before = source.copy();
            var outcomes = ((LootPreviewProvider)item).previewOutcomes(player, source);
            helper.assertTrue(!outcomes.isEmpty() && ItemStack.matches(before, source), "Preview enumerates without consumption");
            if (item == ModItems.SPACE_DUST_BUNDLE.get()) helper.assertTrue(outcomes.stream().allMatch(s -> s.getCount() == 10), "Dust preview uses max 10");
            if (item == ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get()) helper.assertTrue(outcomes.size() == 36,
                    "Heroic preview includes twelve enchants and three Success rates, not random Destroy permutations");
            if (item == ModItems.COSMIC_ENCHANTMENT_TABLE.get()) helper.assertTrue(outcomes.size() == 53,
                    "Cosmic preview includes only unique enchantment/level/Success variants");
            if (item == ModItems.MEMORY_CHEST.get()) helper.assertTrue(outcomes.size() == 11,
                    "Memory Chest preview exposes all eleven production outcomes");
            LootPreviewMenu.open(player, source.getHoverName(), outcomes, 0);
            helper.assertTrue(player.containerMenu instanceof LootPreviewMenu, "Preview uses read-only vanilla 54 slot menu");
            var menu = player.containerMenu;
            var displayed = menu.getSlot(0).getItem().copy();
            menu.clicked(0, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
            helper.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(displayed, menu.getSlot(0).getItem()), "Clicks cannot remove previews");
            player.closeContainer();
        }
        player.getInventory().clearContent();
        verifyPremiumContainers(helper, player);
        player.getInventory().clearContent();
        var oldSet = CosmicContent.repository().findArmorSetDefinition(ArmorSetIds.YETI).orElseThrow();
        var newSet = CosmicContent.repository().findArmorSetDefinition(ArmorSetIds.PHANTOM).orElseThrow();
        helmet.set(ModDataComponents.ARMOR_SET_ID.get(), com.cosmicpve.data.component.ArmorSetIdentity.from(oldSet));
        player.getInventory().setItem(0, helmet);
        var crystal = ArmorSetCrystals.create(newSet, 100);
        var confirmation = ArmorCrystalConfirmationService.INSTANCE;
        helper.assertTrue(confirmation.begin(player, crystal, new Slot(player.getInventory(), 0, 0, 0)) && crystal.isEmpty(), "One crystal escrowed");
        helper.assertTrue(helmet.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(oldSet.id()), "Pending armor unchanged");
        helper.assertTrue(confirmation.chat(player, " CONFIRM "), "Confirmation consumed");
        helper.assertTrue(helmet.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(newSet.id()), "Success replaces old identity");
        helper.assertTrue(!confirmation.chat(player, "confirm"), "Duplicate confirm has no transaction");
        var second = ArmorSetCrystals.create(oldSet, 100);
        confirmation.begin(player, second, new Slot(player.getInventory(), 0, 0, 0));
        player.getInventory().setItem(0, helmet.copy());
        helper.assertTrue(confirmation.chat(player, "confirm"), "Invalid-target confirmation remains control text");
        helper.assertTrue(player.getInventory().getItem(0).get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(newSet.id()), "Identical replacement target not guessed");
        var third = ArmorSetCrystals.create(oldSet, 100);
        confirmation.begin(player, third, new Slot(player.getInventory(), 0, 0, 0));
        helper.assertTrue(!confirmation.chat(player, "ordinary chat"), "Unrelated cancel chat not swallowed");
        verifyCancellationHooks(helper, player, oldSet, newSet);
        verifyAirInteraction(helper, player);
        var fourth = ArmorSetCrystals.create(oldSet, 100);
        confirmation.begin(player, fourth, new Slot(player.getInventory(), 0, 0, 0));
        var protectedArmor = helmet.copy();
        var failedCrystal = ArmorSetCrystals.create(oldSet, 1);
        new WhiteScrollProtectionService().apply(protectedArmor);
        var failure = new ArmorCrystalApplicationService(CosmicContent.repository(), () -> 100)
                .applyConfirmedReplacement(failedCrystal, protectedArmor, protectedArmor);
        helper.assertTrue(failure == ArmorCrystalApplicationService.Outcome.FAILED_PROTECTED
                && protectedArmor.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(newSet.id()),
                "Protected failure preserves the old identity and consumes protection normally");
        var paged = java.util.stream.IntStream.rangeClosed(1,55).mapToObj(count -> new ItemStack(Items.APPLE,count)).toList();
        LootPreviewMenu.open(player, net.minecraft.network.chat.Component.literal("Fixture"), paged, 0);
        player.containerMenu.clicked(53, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        helper.assertTrue(player.containerMenu.getSlot(0).getItem().getCount() == 46
                && player.containerMenu.getSlot(9).getItem().getCount() == 55, "Pagination retains all row-major outcomes");
        player.closeContainer();
        verifyAnimation(helper, player);
        helper.runAfterDelay(600, () -> {
            confirmation.tick(player);
            helper.assertTrue(!confirmation.chat(player, "confirm"), "Timeout resolves escrow once");
            player.getInventory().clearContent();
            helper.succeed();
        });
    }

    private static void verifyPremiumContainers(GameTestHelper helper, ServerPlayer player) {
        for (var season : com.cosmicpve.cosmiccrate.CosmicCrateSeason.values()) {
            for (var side : com.cosmicpve.cosmiccrate.CosmicCrateSide.values()) {
                var stack = com.cosmicpve.cosmiccrate.SeasonalCosmicCrates.half(season, side);
                helper.assertTrue(stack.getItem() instanceof com.cosmicpve.cosmiccrate.CosmicCrateHalfItem half
                        && half.season() == season && half.side() == side, "Every half has exact season and side identity");
            }
            var left = com.cosmicpve.cosmiccrate.SeasonalCosmicCrates.half(season, com.cosmicpve.cosmiccrate.CosmicCrateSide.LEFT);
            var right = com.cosmicpve.cosmiccrate.SeasonalCosmicCrates.half(season, com.cosmicpve.cosmiccrate.CosmicCrateSide.RIGHT);
            helper.assertTrue(new com.cosmicpve.cosmiccrate.CosmicCrateCombinationService().combine(left, right, right)
                    == com.cosmicpve.cosmiccrate.CosmicCrateCombinationService.Outcome.SUCCESS
                    && left.isEmpty() && right.isEmpty(), "Each matching half pair consumes exactly once");
        }
        var capacity = new CustomEnchantCapacityService();
        var higher = new HigherLoreOrbApplicationService(capacity);
        ItemStack armor = new ItemStack(Items.DIAMOND_HELMET);
        armor.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new com.cosmicpve.data.component.CustomEnchantMetadata(2,5,3,true,true));
        ItemStack armor9 = new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_9_LORE.get());
        ItemStack armor10 = new ItemStack(ModItems.ARMOR_ENCHANTMENT_ORB_10_LORE.get());
        helper.assertTrue(higher.apply(armor10, armor, armor) == HigherLoreOrbApplicationService.Outcome.REJECTED_CAPACITY
                && armor10.getCount() == 1, "10-lore Armor Orb rejects capacity 8 without consumption");
        helper.assertTrue(higher.apply(armor9, armor, armor) == HigherLoreOrbApplicationService.Outcome.SUCCESS
                && higher.apply(armor10, armor, armor) == HigherLoreOrbApplicationService.Outcome.SUCCESS
                && capacity.capacity(armor) == 10, "Armor follows deterministic 8 to 9 to 10 sequence");
        ItemStack weapon = new ItemStack(Items.NETHERITE_AXE);
        weapon.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), new com.cosmicpve.data.component.CustomEnchantMetadata(2,5,5,false,false));
        ItemStack weapon11 = new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_11_LORE.get());
        ItemStack weapon12 = new ItemStack(ModItems.WEAPON_ENCHANTMENT_ORB_12_LORE.get());
        helper.assertTrue(higher.apply(weapon12, weapon, weapon) == HigherLoreOrbApplicationService.Outcome.REJECTED_CAPACITY
                && higher.apply(weapon11, weapon, weapon) == HigherLoreOrbApplicationService.Outcome.SUCCESS
                && higher.apply(weapon12, weapon, weapon) == HigherLoreOrbApplicationService.Outcome.SUCCESS
                && capacity.capacity(weapon) == 12, "Weapon follows deterministic 10 to 11 to 12 sequence");
        verifyMaximumCapacityBooks(helper, armor, weapon, capacity);

        ItemStack source = new ItemStack(ModItems.MEMORY_CHEST.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, source);
        helper.assertTrue(ModItems.MEMORY_CHEST.get().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND)
                == net.minecraft.world.InteractionResult.SUCCESS_SERVER && source.isEmpty(), "Memory Chest opens shared animation and consumes once");
        helper.assertTrue(player.getData(ModAttachments.LOOT_ANIMATION).valid()
                && player.getData(ModAttachments.LOOT_ANIMATION).allRewards().size() == 1,
                "Memory Chest persists exactly one final reward before animation");
        player.closeContainer();
        com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE.closed(player);
        helper.assertTrue(!player.getData(ModAttachments.LOOT_ANIMATION).valid(), "Memory Chest early close delivers and clears obligation once");
    }

    private static void verifyMaximumCapacityBooks(GameTestHelper helper, ItemStack armor, ItemStack weapon,
            CustomEnchantCapacityService capacity) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var application = new CosmicBookApplicationService(registry, capacity, new WhiteScrollProtectionService(), () -> 1);
        verifyMaximumCapacityBooks(helper, armor, 10, registry, application, capacity);
        verifyMaximumCapacityBooks(helper, weapon, 12, registry, application, capacity);
    }

    private static void verifyMaximumCapacityBooks(GameTestHelper helper, ItemStack target, int maximum,
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> registry,
            CosmicBookApplicationService application, CustomEnchantCapacityService capacity) {
        var candidates = registry.listElements().filter(holder -> holder.unwrapKey().map(key ->
                CosmicEnchantmentSpecs.find(key.identifier()).isPresent()
                        && HeroicEnchantments.ordinaryFor(key.identifier()).isEmpty()).orElse(false))
                .filter(holder -> holder.value().canEnchant(target)).limit(maximum).toList();
        helper.assertTrue(candidates.size() == maximum, "Enough real compatible Cosmic enchantments exist for maximum-capacity proof: "
                + candidates.size() + "/" + maximum + " on " + target.getItem());
        EnchantmentHelper.updateEnchantments(target, mutable -> {
            for (int index = 0; index < maximum - 2; index++) mutable.set(candidates.get(index), 1);
        });
        for (int index = maximum - 2; index < maximum; index++) {
            var result = application.apply(book(candidates.get(index).unwrapKey().orElseThrow().identifier(), 1), target, target);
            helper.assertTrue(result.outcome() == CosmicBookApplicationResult.Outcome.SUCCESS,
                    "Actual Cosmic book occupies unlocked slot " + (index + 1));
        }
        helper.assertTrue(capacity.used(target) == maximum && capacity.capacity(target) == maximum,
                "Actual distinct Cosmic enchantments fill the legitimate absolute capacity");
    }

    private static void verifyTrialRewards(GameTestHelper helper) {
        var generator = new com.cosmicpve.reward.RewardGeneratorService();
        var context = new com.cosmicpve.reward.RewardGenerationContext(helper.getLevel().registryAccess(),
                net.minecraft.util.RandomSource.create(42), null);
        for (var id : List.of(com.cosmicpve.trial.TrialSessionService.APPRENTICE_REWARDS,
                com.cosmicpve.trial.TrialSessionService.HARDCORE_REWARDS, com.cosmicpve.trial.TrialSessionService.IMPOSSIBLE_REWARDS,
                com.cosmicpve.trial.TrialSessionService.DEMONIC_REWARDS)) {
            var table = CosmicContent.repository().requireRewardTable(id);
            for (var entry : table.entries()) {
                var stack = generator.generate(entry.reward(), context).orElseThrow();
                helper.assertTrue(!stack.isEmpty(), "Every active current Trial reward constructs from loaded production data");
                if (entry.reward() instanceof com.cosmicpve.content.definition.reward.RewardDescriptor.AccessorySocket socket) {
                    var data = stack.get(ModDataComponents.ACCESSORY_SOCKET.get());
                    helper.assertTrue(data == null ? stack.get(ModDataComponents.OMNI_SOCKET_SUCCESS.get()) == socket.successRate()
                            : data.successRate() == socket.successRate(), "Trial sockets carry the exact declared rate, not item default 100");
                }
            }
        }
        for (int tier = 1; tier <= 3; tier++) for (int index = 0; index < 5; index++) {
            final int outcome = index;
            var random = new net.minecraft.world.level.levelgen.LegacyRandomSource(0) {
                @Override public int nextInt(int bound) { if (bound != 5) throw new IllegalStateException("Expected five trinket types"); return outcome; }
            };
            var stack = generator.generate(new com.cosmicpve.content.definition.reward.RewardDescriptor.RandomTrialTrinket(tier),
                    new com.cosmicpve.reward.RewardGenerationContext(helper.getLevel().registryAccess(), random, null)).orElseThrow();
            var data = stack.get(ModDataComponents.TRIAL_TRINKET.get());
            var type = com.cosmicpve.data.component.TrialTrinketType.values()[index];
            int value = switch (type) { case TIME -> new int[]{1,3,5}[tier-1]; case FAME -> new int[]{33,66,100}[tier-1];
                case SKIP, INSURANCE, MADNESS -> tier; };
            helper.assertTrue(data != null && data.valid() && data.type() == type && data.value() == value,
                    "Each of five equally selectable trinket types carries its correct tier value");
        }
    }

    private static void verifyAirInteraction(GameTestHelper helper, ServerPlayer player) {
        player.setPos(helper.absoluteVec(new net.minecraft.world.phys.Vec3(1.5,2,1.5)));
        player.setYRot(-90); player.setYHeadRot(-90); player.setXRot(0);
        player.yRotO=-90; player.yHeadRotO=-90; player.xRotO=0;
        helper.assertTrue(com.cosmicpve.network.LootPreviewPayload.leftClickAir(player), "Clear air is previewable");
        helper.setBlock(new net.minecraft.core.BlockPos(3,3,1), net.minecraft.world.level.block.Blocks.STONE);
        helper.assertTrue(!com.cosmicpve.network.LootPreviewPayload.leftClickAir(player), "Mining block rejects preview; eye="
                + player.getEyePosition() + " view=" + player.getViewVector(1) + " block=" + helper.absolutePos(new net.minecraft.core.BlockPos(3,3,1)));
        helper.setBlock(new net.minecraft.core.BlockPos(3,3,1), net.minecraft.world.level.block.Blocks.AIR);
        var entity = helper.spawn(net.minecraft.world.entity.EntityType.ZOMBIE, new net.minecraft.core.BlockPos(3,2,1));
        entity.setNoAi(true);
        helper.assertTrue(!com.cosmicpve.network.LootPreviewPayload.leftClickAir(player), "Entity attack rejects preview");
        entity.discard();
        var original = player.getMainHandItem();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.APPLE));
        helper.assertTrue(!LootPreviewMenu.openHeld(player), "Unsupported actual held stack rejected");
        var source = new ItemStack(ModItems.SPACE_DUST_BUNDLE.get());
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, source);
        helper.assertTrue(LootPreviewMenu.openHeld(player) && source.getCount() == 1, "Supported actual held source previews without consumption");
        player.closeContainer();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, original);
    }

    private static void verifyCancellationHooks(GameTestHelper helper, ServerPlayer player,
            com.cosmicpve.content.definition.armor.ArmorSetDefinition oldSet,
            com.cosmicpve.content.definition.armor.ArmorSetDefinition newSet) {
        var service = ArmorCrystalConfirmationService.INSTANCE;
        var armor = player.getInventory().getItem(0);
        player.getInventory().clearContent(); player.getInventory().setItem(0, armor);
        var slot = new Slot(player.getInventory(),0,0,0);
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        service.cancel(player,false); service.cancel(player,false);
        helper.assertTrue(crystalCount(player) == 2, "Conflicting application and repeated cancellation return each escrow once");
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        new ArmorCrystalEventBridge().onLogout(new PlayerEvent.PlayerLoggedOutEvent(player));
        helper.assertTrue(crystalCount(player) == 3, "Logout hook returns escrow");
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        new ArmorCrystalEventBridge().onDeath(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(player,player.damageSources().genericKill()));
        helper.assertTrue(crystalCount(player) == 4, "Death hook returns escrow before normal drops");
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        armor.setDamageValue(armor.getDamageValue()+1);
        service.tick(player);
        helper.assertTrue(crystalCount(player) == 5 && !service.chat(player,"confirm"), "Material target change cancels without guessing");
        service.begin(player, ArmorSetCrystals.create(oldSet,100),slot);
        player.getInventory().setItem(8,armor); player.getInventory().setItem(0,ItemStack.EMPTY);
        service.tick(player);
        helper.assertTrue(crystalCount(player) == 6, "Moving exact armor to another slot cancels");
        player.getInventory().setItem(8,ItemStack.EMPTY); player.getInventory().setItem(0,armor);
        new WhiteScrollProtectionService().apply(armor);
        service.begin(player,ArmorSetCrystals.create(oldSet,1),slot);
        long seed = 0;
        while(net.minecraft.util.RandomSource.create(seed).nextInt(100) != 99) seed++;
        player.getRandom().setSeed(seed);
        service.chat(player,"confirm");
        helper.assertTrue(!armor.isEmpty() && armor.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(newSet.id())
                && !new WhiteScrollProtectionService().isProtected(armor), "Confirmed destructive failure consumes White Scroll and preserves identity");
        var doomed = armor.copy(); player.getInventory().setItem(0,doomed);
        service.begin(player,ArmorSetCrystals.create(oldSet,1),slot);
        player.getRandom().setSeed(seed); service.chat(player,"confirm");
        helper.assertTrue(doomed.isEmpty(), "Unprotected confirmed destructive failure destroys selected armor");
        player.getInventory().setItem(0,armor);
        player.getInventory().clearContent(); player.getInventory().setItem(0,armor);
        helper.assertTrue(service.begin(player,ArmorSetCrystals.create(oldSet,100),slot), "Overflow transaction starts");
        for (int i = 1; i < player.getInventory().getContainerSize(); i++) player.getInventory().setItem(i,new ItemStack(Items.COBBLESTONE,64));
        service.chat(player,"cancel"); service.cancel(player,false);
        var drops = player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,player.getBoundingBox().inflate(4),
                entity -> entity.getItem().is(ModItems.ARMOR_SET_CRYSTAL.get()));
        helper.assertTrue(drops.stream().mapToInt(entity -> entity.getItem().getCount()).sum() == 1,
                "Inventory overflow safely drops exactly one escrow; entities=" + drops.size() + " inventory=" + crystalCount(player)
                        + " player=" + player.position());
        drops.forEach(net.minecraft.world.entity.Entity::discard);
        player.getInventory().clearContent(); player.getInventory().setItem(0,armor);
    }
    private static int crystalCount(ServerPlayer player) {
        int count = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.ARMOR_SET_CRYSTAL.get())) count += stack.getCount();
        }
        return count;
    }

    private static void verifyCombat(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var wearer = helper.spawn(ModEntities.FOREST_FANATIC.get(), new net.minecraft.core.BlockPos(2,2,2));
        var target = helper.spawn(ModEntities.DREADMANE.get(), new net.minecraft.core.BlockPos(5,2,2));
        var bonus = new com.cosmicpve.combat.enchantment.DeathbringerBehavior(new EffectiveEnchantmentsResolver());
        var ordinaryContext = new com.cosmicpve.combat.api.CombatContext(wearer, wearer, wearer, target,
                java.util.Optional.empty(), target.damageSources().mobAttack(wearer), com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY, java.util.Set.of(), com.cosmicpve.combat.api.WeaponSnapshot.empty(),
                EffectiveEnchantments.EMPTY, 1, java.util.OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
        var trueContext = new com.cosmicpve.combat.api.CombatContext(wearer, wearer, wearer, target,
                java.util.Optional.empty(), target.damageSources().mobAttack(wearer), com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.TRUE, java.util.Set.of(), com.cosmicpve.combat.api.WeaponSnapshot.empty(),
                EffectiveEnchantments.EMPTY, 2, java.util.OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
        for (var key : List.of(ModEnchantments.DEATHBRINGER, ModEnchantments.PLANETARY_DEATHBRINGER)) {
            for (int level = 1; level <= 3; level++) {
                int chosenLevel = level;
                var head = new ItemStack(Items.IRON_HELMET);
                EnchantmentHelper.updateEnchantments(head, mutable -> mutable.set(registry.getOrThrow(key), chosenLevel));
                wearer.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, head);
                double expected = ((key == ModEnchantments.DEATHBRINGER ? 1 : 5) + level) / 100.0;
                helper.assertTrue(bonus.resolve(ordinaryContext).size() == 1
                        && Math.abs(bonus.resolve(ordinaryContext).getFirst().bonus() - expected) < .000001,
                        "Actual equipped helmet contributes exact passive ordinary bonus");
                helper.assertTrue(bonus.resolve(trueContext).isEmpty(), "True channel unaffected");
            }
        }
        EnchantmentSuppressionService.GLOBAL.suppressEnchantment(wearer, CosmicPVE.id("cleanup_fixture"),
                ModEnchantments.PLANETARY_DEATHBRINGER.identifier(), 20, helper.getLevel().getServer().getTickCount());
        helper.assertTrue(bonus.resolve(ordinaryContext).isEmpty(), "Heroic suppression removes passive bonus");
        var pirate = helper.spawn(ModEntities.SPACE_PIRATE_VARIANT_1.get(), new net.minecraft.core.BlockPos(8,2,2));
        var armor = java.util.stream.IntStream.range(0,4).mapToObj(i -> new com.cosmicpve.entity.spacepirate.SpacePirateEquipmentPlan.ArmorRoll(
                com.cosmicpve.entity.spacepirate.SpacePirateEquipmentPlan.ArmorMaterial.DIAMOND,4)).toList();
        new com.cosmicpve.entity.spacepirate.SpacePirateEquipmentService().apply(pirate, helper.getLevel().registryAccess(),
                new com.cosmicpve.entity.spacepirate.SpacePirateEquipmentPlan(com.cosmicpve.entity.spacepirate.SpacePirateVariant.VARIANT_1,armor,0,0,true,4));
        var weapon = EnchantmentHelper.getEnchantmentsForCrafting(pirate.getMainHandItem());
        helper.assertTrue(weapon.getLevel(registry.getOrThrow(ModEnchantments.SILENCE)) == 4
                && weapon.getLevel(registry.getOrThrow(ModEnchantments.INSANITY)) == 8
                && weapon.getLevel(registry.getOrThrow(ModEnchantments.PUMMEL)) == 3, "Pirate Silence is actual weapon enchantment and preserves existing enchants");
        wearer.discard(); target.discard(); pirate.discard();
    }

    private static void verifyAnimation(GameTestHelper helper, ServerPlayer player) {
        var animation = com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE;
        var finals = List.of(CosmicDustService.dust(CosmicEnchantmentTier.SIMPLE, 1),
                CosmicDustService.dust(CosmicEnchantmentTier.UNIQUE, 4), CosmicDustService.dust(CosmicEnchantmentTier.ELITE, 10));
        var previews = new java.util.concurrent.atomic.AtomicInteger();
        helper.assertTrue(animation.open(player, finals, random -> {
            previews.incrementAndGet();
            return SpaceDustBundleRewards.cosmeticPreview(random);
        }, () -> {}), "Shared three-result animation opens");
        var menu = (com.cosmicpve.reward.animation.SingleRewardAnimationMenu)player.containerMenu;
        helper.runAfterDelay(99, () -> {
            animation.tick(player, menu);
            for (int i = 3; i <= 5; i++) helper.assertTrue(menu.getSlot(i).getItem().getCount() >= 1
                    && menu.getSlot(i).getItem().getCount() <= 10, "Each cosmetic slot displays count 1-10");
            var obligation = player.getData(ModAttachments.LOOT_ANIMATION).allRewards();
            for (int i = 0; i < 3; i++) helper.assertTrue(ItemStack.matches(finals.get(i), obligation.get(i)), "Cycling does not reroll finals");
        });
        helper.runAfterDelay(100, () -> {
            animation.tick(player, menu);
            for (int i = 0; i < 3; i++) helper.assertTrue(ItemStack.matches(finals.get(i), menu.getSlot(3 + i).getItem()), "Final displayed quantity equals obligation");
        });
        helper.runAfterDelay(159, () -> {
            helper.assertTrue(player.containerMenu == menu, "All final slots stay open through hold tick 59");
            int calls = previews.get();
            animation.tick(player, menu);
            helper.assertTrue(previews.get() == calls, "No cosmetic/reward calls during final hold");
        });
        helper.runAfterDelay(160, () -> {
            animation.tick(player, menu);
            helper.assertTrue(player.containerMenu == player.inventoryMenu, "Automatic close at final hold tick 60");
            animation.closed(player);
            for (var expected : finals) {
                int count = 0;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    var stack = player.getInventory().getItem(i);
                    if (ItemStack.isSameItemSameComponents(stack, expected)) count += stack.getCount();
                }
                helper.assertTrue(count == expected.getCount(), "Final awards exactly once, including duplicate close");
            }
        });
    }
}
