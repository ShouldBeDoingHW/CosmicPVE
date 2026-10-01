package com.cosmicpve.reward.lootbox;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Loaded-registry construction coverage for all Step 8E generated rewards. */
public final class Step8EGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    private static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONSTRUCTION =
            FUNCTIONS.register("step8e_reward_construction", ignored -> Step8EGameTests::construction);
    private Step8EGameTests() {}
    public static void register(IEventBus bus) { FUNCTIONS.register(bus); bus.addListener(Step8EGameTests::registerTests); }
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("step8e_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("step8e_reward_construction"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("step8e_reward_construction")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }
    private static void construction(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var signatureFactory = new SignatureWeaponFactory();
        for (var definition : SignatureWeaponDefinition.ALL) {
            var stack = signatureFactory.create(definition, access);
            var identity = stack.get(ModDataComponents.SIGNATURE_WEAPON.get());
            helper.assertTrue(identity != null && identity.signatureId().equals(definition.id())
                    && identity.matchingArmorSetId().equals(definition.matchingSetId()) && identity.kind() == definition.kind(),
                    "Signature weapon identity must match its definition");
            helper.assertTrue(stack.is(definition.baseItem()), "Signature weapon base item must be exact");
            var applied = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING)) == 3,
                    "Every signature weapon needs Unbreaking III");
            if (definition.kind() == com.cosmicpve.data.component.SignatureWeaponIdentity.Kind.MELEE)
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS)) == 5,
                        "Melee signature needs Sharpness V");
            if (definition == SignatureWeaponDefinition.RANGERS_BOW) {
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.POWER)) == 5,
                        "Ranger's Bow needs Power V");
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.INFINITY)) == 1
                        && applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FLAME)) == 1
                        && applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING)) == 0,
                        "Ranger's Bow needs Infinity and Flame but not Mending");
            } else helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING)) == 1,
                    "Every non-Ranger signature weapon needs Mending I");
            if (definition == SignatureWeaponDefinition.TRAVELERS_SPACE_BLASTER) {
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.QUICK_CHARGE)) == 3,
                        "Space Blaster needs Quick Charge III");
                helper.assertTrue(applied.getLevel(access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PIERCING)) == 4,
                        "Space Blaster needs Piercing IV");
            }
        }
        var adminFactory = new AdminAbuseRewardFactory();
        int[] expectedCosmic = {7, 8, 10, 10, 8, 8, 10, 8};
        int[] expectedCapacity = {8, 8, 10, 10, 8, 8, 10, 8};
        int index = 0;
        for (var outcome : AdminAbuseRewards.ALL) {
            var stack = adminFactory.create(outcome, access);
            var identity = stack.get(ModDataComponents.ADMIN_ABUSE_REWARD.get());
            var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
            long cosmic = EnchantmentHelper.getEnchantmentsForCrafting(stack).keySet().stream().filter(holder ->
                    holder.unwrapKey().map(key -> key.identifier().getNamespace().equals(CosmicPVE.MOD_ID)).orElse(false)).count();
            helper.assertTrue(identity != null && identity.rewardId().equals(outcome.id()), "Admin reward identity must be exact");
            helper.assertTrue(metadata != null && metadata.whiteScrollProtected() && metadata.transmogSorted()
                            && new com.cosmicpve.equipment.enchantment.WhiteScrollProtectionService().isProtected(stack)
                            && com.cosmicpve.equipment.enchantment.HolyWhiteScrollService.isHoly(stack),
                    "Every Admin reward must retain real White Scroll and Holy protection plus Transmog");
            var base = switch (outcome) {
                case GHOSTLY_VEIL -> Items.IRON_HELMET;
                case COVERT_CLOAK -> Items.IRON_CHESTPLATE;
                case NANKADA -> Items.NETHERITE_SWORD;
                case ASHOKA, IZANAGI -> Items.NETHERITE_AXE;
                case IDATEN -> Items.IRON_LEGGINGS;
                case FREEMAN_WALKERS, ETERNAL_STRIDERS -> Items.IRON_BOOTS;
            };
            helper.assertTrue(stack.is(base), "Admin reward base item must be exact for " + outcome);
            boolean heroicArmor = outcome != AdminAbuseRewards.Outcome.NANKADA
                    && outcome != AdminAbuseRewards.Outcome.ASHOKA
                    && outcome != AdminAbuseRewards.Outcome.IZANAGI;
            helper.assertTrue(stack.has(ModDataComponents.HEROIC.get()) == heroicArmor,
                    "Admin reward Heroic armor state must be exact for " + outcome);
            if (!heroicArmor) {
                var weapon = stack.get(ModDataComponents.SIGNATURE_WEAPON.get());
                var expectedSet = switch (outcome) {
                    case NANKADA -> com.cosmicpve.equipment.armor.ArmorSetIds.YJIKI;
                    case ASHOKA -> com.cosmicpve.equipment.armor.ArmorSetIds.PHANTOM;
                    case IZANAGI -> com.cosmicpve.equipment.armor.ArmorSetIds.YETI;
                    default -> throw new AssertionError(outcome);
                };
                helper.assertTrue(weapon != null && weapon.matchingArmorSetId().equals(expectedSet)
                                && SignatureWeaponCombatService.matches(weapon,
                                        com.cosmicpve.combat.api.AttackCategory.MELEE, java.util.Optional.of(expectedSet))
                                && !SignatureWeaponCombatService.matches(weapon,
                                        com.cosmicpve.combat.api.AttackCategory.MELEE, java.util.Optional.empty()),
                        "Admin signature weapon must resolve only with its matching active set for " + outcome);
            }
            helper.assertTrue(cosmic == expectedCosmic[index++], "Admin reward Cosmic enchantment count must be exact");
            helper.assertTrue(new com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService().capacity(stack)
                    == expectedCapacity[index - 1], "Admin reward capacity must reflect actual Orb upgrades");
            verifyExactAdminEnchantments(helper, access, stack, outcome);
            if (outcome == AdminAbuseRewards.Outcome.GHOSTLY_VEIL || outcome == AdminAbuseRewards.Outcome.COVERT_CLOAK)
                helper.assertTrue(Boolean.TRUE.equals(stack.get(ModDataComponents.OMNI_ARMOR.get()))
                        && stack.has(ModDataComponents.HEROIC.get()), "Admin armor must be typed Omni and Heroic");
            switch (outcome) {
                case GHOSTLY_VEIL -> helper.assertTrue(level(access, stack,
                        com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED) == 4
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.ALIEN_IMPLANTS) == 3
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.ARMORED) == 0
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.IMPLANTS) == 0,
                        "Ghostly Veil must use Paladin Armored IV and Alien Implants III");
                case COVERT_CLOAK -> helper.assertTrue(level(access, stack,
                        com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED) == 4
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.GODLY_OVERLOAD) == 3
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.UNDEAD_RUSE) == 0,
                        "Covert Cloak must use Paladin Armored IV and Godly Overload III without Undead Ruse");
                case NANKADA -> helper.assertTrue(level(access, stack,
                        com.cosmicpve.registry.ModEnchantments.PERMANENT_EXECUTE) == 5
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.SILENCE) == 4
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.EXECUTE) == 0,
                        "Nankada must use Permanent Execute V and Silence IV");
                case ASHOKA -> helper.assertTrue(level(access, stack,
                        com.cosmicpve.registry.ModEnchantments.DEEP_BLEED) == 6
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.SILENCE) == 4
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.BLEED) == 0
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.PUMMEL) == 3
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.INSANITY) == 8,
                        "Ashoka must use its current ten-enchantment payload without ordinary Bleed");
                case IDATEN -> helper.assertTrue(stack.is(Items.IRON_LEGGINGS)
                        && stack.has(ModDataComponents.HEROIC.get())
                        && stack.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(
                                com.cosmicpve.equipment.armor.ArmorSetIds.DIMENSIONAL_TRAVELER)
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.EPIDEMIC_CARRIER) == 7
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.PLAGUE_CARRIER) == 0
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.LUCK) == 10,
                        "Idaten must be Heroic Dimensional Traveler with Epidemic VII and Luck X");
                case FREEMAN_WALKERS -> helper.assertTrue(stack.is(Items.IRON_BOOTS)
                        && stack.has(ModDataComponents.HEROIC.get())
                        && stack.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(
                                com.cosmicpve.equipment.armor.ArmorSetIds.ENGINEER),
                        "Freeman Walkers must be Heroic Engineer boots");
                case IZANAGI -> helper.assertTrue(stack.is(Items.NETHERITE_AXE)
                        && stack.get(ModDataComponents.SIGNATURE_WEAPON.get()).matchingArmorSetId().equals(
                                com.cosmicpve.equipment.armor.ArmorSetIds.YETI)
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.MIGHTY_CLEAVE) == 8
                        && level(access, stack, com.cosmicpve.registry.ModEnchantments.CLEAVE) == 0,
                        "Izanagi must carry the Yeti weapon bonus and Mighty Cleave VIII");
                case ETERNAL_STRIDERS -> helper.assertTrue(stack.is(Items.IRON_BOOTS)
                        && stack.has(ModDataComponents.HEROIC.get())
                        && stack.get(ModDataComponents.ARMOR_SET_ID.get()).setId().equals(
                                com.cosmicpve.equipment.armor.ArmorSetIds.DRAGONSLAYER),
                        "Eternal Striders must be Heroic Dragonslayer boots");
            }
        }
        var previewPlayer = com.cosmicpve.adventure.AdventureGameTests.player(helper, "adminpreview");
        var previewSource = new ItemStack(com.cosmicpve.registry.ModItems.ADMIN_ABUSE.get(), 2);
        previewPlayer.getInventory().setItem(0, previewSource);
        var preview = Step8ELootboxService.INSTANCE.previewOutcomes(previewPlayer, AnimatedLootboxItem.Kind.ADMIN_ABUSE);
        helper.assertTrue(preview.size() == 8 && previewSource.getCount() == 2
                        && !previewPlayer.getData(com.cosmicpve.registry.ModAttachments.LOOT_ANIMATION).valid(),
                "Admin preview must expose eight results without consuming or persisting a roll");
        for (int outcomeIndex = 0; outcomeIndex < 8; outcomeIndex++)
            helper.assertTrue(ItemStack.matches(preview.get(outcomeIndex),
                            adminFactory.create(AdminAbuseRewards.ALL.get(outcomeIndex), access)),
                    "Admin preview must contain the unchanged production-equivalent stack at index " + outcomeIndex);
        previewPlayer.discard();
        var table = new CosmicEnchantmentTableRewards();
        var enchantments = access.lookupOrThrow(Registries.ENCHANTMENT);
        for (int sample = 0; sample < 100; sample++) {
            var book = table.create(enchantments, helper.getLevel().getRandom());
            var data = book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            helper.assertTrue(data != null && CosmicEnchantmentTableRewards.POOL.stream()
                    .anyMatch(key -> key.identifier().equals(data.enchantmentId())), "Table reward must use exact pool");
            helper.assertTrue(data.level() == enchantments.get(data.enchantmentId()).orElseThrow().value().getMaxLevel(),
                    "Table book must use registered maximum level");
            var spec = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(data.enchantmentId()).orElseThrow();
            helper.assertTrue(com.cosmicpve.equipment.enchantment.CosmicBookRateRules.allows(book, spec, data),
                    "Every generated Table book must be applicable under its scoped rate policy");
        }
        var heroicTable = new HeroicCosmicEnchantmentTableRewards();
        var seenHeroics = new java.util.HashSet<net.minecraft.resources.Identifier>();
        for (int sample = 0; sample < 300; sample++) {
            var book = heroicTable.create(enchantments, helper.getLevel().getRandom());
            var data = book.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
            helper.assertTrue(data != null && HeroicCosmicEnchantmentTableRewards.POOL.contains(data.enchantmentId()),
                    "Heroic Table reward must use one of the canonical replacements");
            helper.assertTrue(HeroicCosmicEnchantmentTableRewards.SUCCESS.contains(data.successRate())
                    && data.destroyRate() >= 1 && data.destroyRate() <= 100,
                    "Heroic Table rates must be 25/50/75 Success and 1-100 Destroy");
            helper.assertTrue(data.level() == enchantments.get(data.enchantmentId()).orElseThrow().value().getMaxLevel(),
                    "Heroic Table books must be maximum level");
            seenHeroics.add(data.enchantmentId());
        }
        helper.assertTrue(seenHeroics.size() == 14, "Loaded-registry sampling must reach all fourteen Heroics");
        verifyHeroicConversion(helper, enchantments);
        verifyDamageCategories(helper);
        verifyDragonReductionAndOmniActivation(helper, enchantments);
        verifyWeaponSkinGenerator(helper);
        helper.succeed();
    }

    private static void verifyDamageCategories(GameTestHelper helper) {
        var sources = helper.getLevel().damageSources();
        helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(sources.lava())
                == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.LAVA,
                "Lava must classify once as Lava rather than also applying Fire");
        helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(sources.onFire())
                == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.FIRE, "On-fire damage must classify as Fire");
        helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(sources.wither())
                == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.WITHER, "Wither must remain separate");
        helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(sources.drown())
                == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.OTHER, "Drowning must not be protected");
        var poison = new net.minecraft.world.damagesource.DamageSource(helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(net.neoforged.neoforge.common.NeoForgeMod.POISON_DAMAGE));
        helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(poison)
                == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.POISON,
                "Only the poison damage source must classify as Poison");
        var damageTypes = helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);
        for (var excluded : java.util.List.of(net.minecraft.world.damagesource.DamageTypes.FREEZE,
                net.minecraft.world.damagesource.DamageTypes.MAGIC,
                net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT,
                net.minecraft.world.damagesource.DamageTypes.GENERIC)) {
            helper.assertTrue(com.cosmicpve.equipment.armor.DamageCategoryClassifier.classify(
                    new net.minecraft.world.damagesource.DamageSource(damageTypes.getOrThrow(excluded)))
                    == com.cosmicpve.equipment.armor.DamageCategoryClassifier.Category.OTHER,
                    "Freeze, generic magic, lightning, and ordinary damage must be excluded");
        }
    }

    private static void verifyWeaponSkinGenerator(GameTestHelper helper) {
        var player = com.cosmicpve.adventure.AdventureGameTests.player(helper, "weaponskingenerator");
        player.setPos(helper.absolutePos(new net.minecraft.core.BlockPos(5, 3, 5)).getCenter());
        for (int slot = 0; slot < 36; slot++) player.getInventory().setItem(slot, new ItemStack(Items.STONE, 64));
        var source = new ItemStack(com.cosmicpve.registry.ModItems.RANDOM_WEAPON_SKIN_GENERATOR.get(), 2);
        player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, source);
        var previews = Step8ELootboxService.INSTANCE.previewOutcomes(
                player, AnimatedLootboxItem.Kind.RANDOM_WEAPON_SKIN_GENERATOR);
        helper.assertTrue(previews.size() == 7 && source.getCount() == 2
                        && !player.getData(com.cosmicpve.registry.ModAttachments.LOOT_ANIMATION).valid(),
                "Weapon Skin Generator preview must show seven loose skins without consumption or a persisted roll");
        helper.assertTrue(previews.stream().allMatch(stack -> stack.has(ModDataComponents.WEAPON_SKIN_ITEM.get())
                        && !stack.has(ModDataComponents.WEAPON_SKIN.get())),
                "All seven preview entries must be actual loose skins");
        helper.assertTrue(source.use(player.level(), player, net.minecraft.world.InteractionHand.OFF_HAND)
                instanceof net.minecraft.world.InteractionResult.Success, "Generator must use the standard animation");
        var pending = player.getData(com.cosmicpve.registry.ModAttachments.LOOT_ANIMATION);
        var reward = pending.allRewards().getFirst();
        helper.assertTrue(source.getCount() == 1 && pending.valid() && pending.allRewards().size() == 1,
                "Generator consumes exactly one and persists exactly one selected reward");
        helper.assertTrue(reward.has(ModDataComponents.WEAPON_SKIN_ITEM.get())
                && !reward.has(ModDataComponents.WEAPON_SKIN.get())
                && previews.stream().anyMatch(stack -> ItemStack.matches(stack, reward)),
                "Generator reward must be exactly one of the seven actual loose previewed skins");
        player.closeContainer();
        com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE.recover(player);
        com.cosmicpve.reward.animation.SingleRewardAnimationService.INSTANCE.recover(player);
        var drops = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                player.getBoundingBox().inflate(5), entity -> ItemStack.matches(entity.getItem(), reward));
        helper.assertTrue(drops.size() == 1 && !player.getData(com.cosmicpve.registry.ModAttachments.LOOT_ANIMATION).valid(),
                "Full-inventory recovery must overflow exactly one selected loose skin");
        drops.forEach(net.minecraft.world.entity.item.ItemEntity::discard);
        player.discard();
    }

    private static void verifyDragonReductionAndOmniActivation(GameTestHelper helper,
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> enchantments) {
        var player = com.cosmicpve.adventure.AdventureGameTests.player(helper, "dragoncategories");
        var definition = com.cosmicpve.content.CosmicContent.repository()
                .requireArmorSetDefinition(com.cosmicpve.equipment.armor.ArmorSetIds.DRAGONSLAYER);
        var identity = com.cosmicpve.data.component.ArmorSetIdentity.from(definition);
        var helmet = new ItemStack(Items.IRON_HELMET); helmet.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        var chest = new ItemStack(Items.IRON_CHESTPLATE); chest.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        var legs = new ItemStack(Items.IRON_LEGGINGS); legs.set(ModDataComponents.OMNI_ARMOR.get(), true);
        var boots = new ItemStack(Items.IRON_BOOTS); boots.set(ModDataComponents.OMNI_ARMOR.get(), true);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, helmet);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, chest);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, legs);
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, boots);
        var behavior = new com.cosmicpve.equipment.armor.CategoryDamageReductionBehavior(
                new com.cosmicpve.equipment.armor.ArmorSetResolver(com.cosmicpve.content.CosmicContent.repository()),
                new com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver(),
                new com.cosmicpve.equipment.mask.MaskResolver(com.cosmicpve.content.CosmicContent.repository()));
        helper.assertTrue(multiplier(behavior, categoryContext(player, helper.getLevel().damageSources().lava())) == .25,
                "Two Dragonslayer anchors plus two Omni pieces must activate 75% Lava reduction");
        EnchantmentHelper.updateEnchantments(legs, mutable -> mutable.set(
                enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.OBSIDIANSHIELD), 1));
        helper.assertTrue(multiplier(behavior, categoryContext(player, helper.getLevel().damageSources().lava())) == 0.0,
                "Dragonslayer 75% plus Obsidianshield I 25% must flat-add to 100%");

        helmet.remove(ModDataComponents.ARMOR_SET_ID.get()); chest.remove(ModDataComponents.ARMOR_SET_ID.get());
        legs.remove(ModDataComponents.OMNI_ARMOR.get()); boots.remove(ModDataComponents.OMNI_ARMOR.get());
        helmet.set(ModDataComponents.MASK_LOADOUT.get(), com.cosmicpve.equipment.mask.MaskItemFactory
                .create(CosmicPVE.id("dragon")).get(ModDataComponents.MASK_ITEM.get()));
        EnchantmentHelper.updateEnchantments(legs, mutable -> mutable.set(
                enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.OBSIDIANSHIELD), 2));
        helper.assertTrue(multiplier(behavior, categoryContext(player, helper.getLevel().damageSources().lava())) == 0.0,
                "Dragon 50% plus Obsidianshield II 50% must flat-add to 100%");
        EnchantmentHelper.updateEnchantments(legs, mutable -> mutable.removeIf(ignored -> true));
        helper.assertTrue(multiplier(behavior, categoryContext(player, helper.getLevel().damageSources().lava())) == .5,
                "Dragon alone must reduce Lava exactly once by 50%");
        helper.assertTrue(behavior.resolveIncoming(categoryContext(player, helper.getLevel().damageSources().wither())).isEmpty(),
                "Dragon category behavior must not protect Wither");
        player.discard();
    }

    private static double multiplier(com.cosmicpve.equipment.armor.CategoryDamageReductionBehavior behavior,
            com.cosmicpve.combat.api.CombatContext context) {
        var contributions = behavior.resolveIncoming(context);
        return contributions.isEmpty() ? 1.0 : contributions.getFirst().multiplier();
    }

    private static com.cosmicpve.combat.api.CombatContext categoryContext(
            net.minecraft.world.entity.LivingEntity target, net.minecraft.world.damagesource.DamageSource source) {
        return new com.cosmicpve.combat.api.CombatContext(null, null, null, target, java.util.Optional.empty(), source,
                com.cosmicpve.combat.api.AttackCategory.ENVIRONMENTAL, com.cosmicpve.combat.api.DamageChannel.ORDINARY,
                java.util.Set.of(), com.cosmicpve.combat.api.WeaponSnapshot.empty(),
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY, 1, java.util.OptionalLong.empty(),
                com.cosmicpve.combat.api.RecursionPolicy.NORMAL);
    }

    private static void verifyHeroicConversion(GameTestHelper helper,
            net.minecraft.core.Registry<net.minecraft.world.item.enchantment.Enchantment> enchantments) {
        ItemStack axe = new ItemStack(Items.NETHERITE_AXE);
        EnchantmentHelper.updateEnchantments(axe, mutable -> {
            mutable.set(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.BLEED), 6);
            mutable.set(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.PUMMEL), 3);
            mutable.set(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.INSANITY), 8);
            mutable.set(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.DEVOUR), 4);
            mutable.set(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.SOUL_TETHER), 3);
        });
        var service = new com.cosmicpve.equipment.enchantment.CosmicBookApplicationService(enchantments,
                new com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService(),
                new com.cosmicpve.equipment.enchantment.WhiteScrollProtectionService(), () -> 1);
        ItemStack heroicBook = book(com.cosmicpve.registry.ModEnchantments.DEEP_BLEED.identifier(), 6, 100, 100);
        var converted = service.apply(heroicBook, axe, axe);
        helper.assertTrue(converted.outcome()
                == com.cosmicpve.equipment.enchantment.CosmicBookApplicationResult.Outcome.SUCCESS,
                "Maximum ordinary enchantment must convert even at capacity");
        var applied = EnchantmentHelper.getEnchantmentsForCrafting(axe);
        helper.assertTrue(applied.getLevel(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.BLEED)) == 0
                && applied.getLevel(enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.DEEP_BLEED)) == 6,
                "Conversion must remove the ordinary counterpart and write the real Heroic enchantment");

        ItemStack missingPrerequisite = new ItemStack(Items.NETHERITE_AXE);
        ItemStack rejectedHeroic = book(com.cosmicpve.registry.ModEnchantments.DEEP_BLEED.identifier(), 1, 100, 100);
        helper.assertTrue(service.apply(rejectedHeroic, missingPrerequisite, missingPrerequisite).outcome()
                == com.cosmicpve.equipment.enchantment.CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE
                && rejectedHeroic.getCount() == 1, "Missing max ordinary prerequisite must consume nothing");
        ItemStack ordinaryBook = book(com.cosmicpve.registry.ModEnchantments.BLEED.identifier(), 6, 100, 100);
        helper.assertTrue(service.apply(ordinaryBook, axe, axe).outcome()
                == com.cosmicpve.equipment.enchantment.CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_COUNTERPART
                && ordinaryBook.getCount() == 1, "Ordinary counterpart cannot coexist with its Heroic replacement");
        var blackScroll = new com.cosmicpve.equipment.enchantment.BlackScrollExtractionService(
                enchantments, ignored -> 0, () -> 50);
        helper.assertTrue(blackScroll.eligibleActualEnchantments(axe).stream()
                .noneMatch(value -> value.id().equals(com.cosmicpve.registry.ModEnchantments.DEEP_BLEED.identifier())),
                "Black Scrolls must exclude Heroic enchantments");

        ItemStack leggings = new ItemStack(Items.IRON_LEGGINGS);
        EnchantmentHelper.updateEnchantments(leggings, mutable -> mutable.set(
                enchantments.getOrThrow(com.cosmicpve.registry.ModEnchantments.PLAGUE_CARRIER), 7));
        int priorCapacity = new com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService().capacity(leggings);
        var epidemicBook = book(com.cosmicpve.registry.ModEnchantments.EPIDEMIC_CARRIER.identifier(), 1, 100, 100);
        helper.assertTrue(service.apply(epidemicBook, leggings, leggings).outcome()
                        == com.cosmicpve.equipment.enchantment.CosmicBookApplicationResult.Outcome.SUCCESS,
                "Epidemic I must convert from Plague Carrier VII through the generic Heroic path");
        helper.assertTrue(level(helper.getLevel().registryAccess(), leggings,
                        com.cosmicpve.registry.ModEnchantments.PLAGUE_CARRIER) == 0
                        && level(helper.getLevel().registryAccess(), leggings,
                                com.cosmicpve.registry.ModEnchantments.EPIDEMIC_CARRIER) == 1
                        && new com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService().capacity(leggings)
                                == priorCapacity,
                "Epidemic conversion must replace the counterpart in one capacity slot");
        ItemStack noPlague = new ItemStack(Items.IRON_LEGGINGS);
        var refused = book(com.cosmicpve.registry.ModEnchantments.EPIDEMIC_CARRIER.identifier(), 1, 100, 100);
        helper.assertTrue(service.apply(refused, noPlague, noPlague).outcome()
                        == com.cosmicpve.equipment.enchantment.CosmicBookApplicationResult.Outcome.REJECTED_HEROIC_PREREQUISITE
                        && refused.getCount() == 1,
                "Epidemic conversion must require maximum Plague Carrier VII and consume nothing on rejection");
    }

    private static ItemStack book(net.minecraft.resources.Identifier id, int level, int success, int destroy) {
        ItemStack stack = new ItemStack(com.cosmicpve.registry.ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        stack.set(ModDataComponents.COSMIC_ENCHANT_BOOK.get(), new com.cosmicpve.data.component.CosmicEnchantmentBookData(
                com.cosmicpve.data.component.CosmicEnchantmentBookData.CURRENT_DATA_VERSION,
                id, level, success, destroy));
        return stack;
    }

    private static int level(net.minecraft.core.RegistryAccess access, ItemStack stack,
            ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key) {
        return EnchantmentHelper.getItemEnchantmentLevel(
                access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key), stack);
    }

    private static void verifyExactAdminEnchantments(GameTestHelper helper, net.minecraft.core.RegistryAccess access,
            ItemStack stack, AdminAbuseRewards.Outcome outcome) {
        var actual = new java.util.HashMap<ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, Integer>();
        EnchantmentHelper.getEnchantmentsForCrafting(stack).entrySet().forEach(entry ->
                actual.put(entry.getKey().unwrapKey().orElseThrow(), entry.getIntValue()));
        var expected = new java.util.HashMap<ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, Integer>();
        switch (outcome) {
            case GHOSTLY_VEIL -> {
                armorVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.MORTAL_COIL, 2,
                        com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED, 4,
                        com.cosmicpve.registry.ModEnchantments.MOLTEN, 4,
                        com.cosmicpve.registry.ModEnchantments.ALIEN_IMPLANTS, 3,
                        com.cosmicpve.registry.ModEnchantments.VOODOO, 6,
                        com.cosmicpve.registry.ModEnchantments.ENDER_SHIFT, 3,
                        com.cosmicpve.registry.ModEnchantments.GLOWING, 1);
            }
            case COVERT_CLOAK -> {
                armorVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.DEATH_PACT, 5,
                        com.cosmicpve.registry.ModEnchantments.PERMAFROST, 6,
                        com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED, 4,
                        com.cosmicpve.registry.ModEnchantments.GODLY_OVERLOAD, 3,
                        com.cosmicpve.registry.ModEnchantments.AEGIS, 6,
                        com.cosmicpve.registry.ModEnchantments.ANGELIC, 5,
                        com.cosmicpve.registry.ModEnchantments.FORBIDDEN_CURSE, 5,
                        com.cosmicpve.registry.ModEnchantments.STORMCALLER, 5);
            }
            case NANKADA -> {
                weaponVanilla(expected); add(expected, Enchantments.FIRE_ASPECT, 2);
                add(expected, com.cosmicpve.registry.ModEnchantments.SOUL_SIPHON, 4,
                        com.cosmicpve.registry.ModEnchantments.BLACKOUT, 4,
                        com.cosmicpve.registry.ModEnchantments.RAGE, 6,
                        com.cosmicpve.registry.ModEnchantments.DOUBLESTRIKE, 3,
                        com.cosmicpve.registry.ModEnchantments.PERMANENT_EXECUTE, 5,
                        com.cosmicpve.registry.ModEnchantments.GREATSWORD, 4,
                        com.cosmicpve.registry.ModEnchantments.POISON, 3,
                        com.cosmicpve.registry.ModEnchantments.SILENCE, 4,
                        com.cosmicpve.registry.ModEnchantments.TITAN_TRAP, 3,
                        com.cosmicpve.registry.ModEnchantments.THUNDERING_BLOW, 3);
            }
            case ASHOKA -> {
                weaponVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.HERO_KILLER, 3,
                        com.cosmicpve.registry.ModEnchantments.SOUL_TETHER, 3,
                        com.cosmicpve.registry.ModEnchantments.SOUL_SIPHON, 4,
                        com.cosmicpve.registry.ModEnchantments.RAGE, 6,
                        com.cosmicpve.registry.ModEnchantments.DEEP_BLEED, 6,
                        com.cosmicpve.registry.ModEnchantments.DEVOUR, 4,
                        com.cosmicpve.registry.ModEnchantments.SILENCE, 4,
                        com.cosmicpve.registry.ModEnchantments.PYRE, 3,
                        com.cosmicpve.registry.ModEnchantments.PUMMEL, 3,
                        com.cosmicpve.registry.ModEnchantments.INSANITY, 8);
            }
            case IDATEN -> {
                armorVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED, 4,
                        com.cosmicpve.registry.ModEnchantments.MIGHTY_CACTUS, 2,
                        com.cosmicpve.registry.ModEnchantments.EPIDEMIC_CARRIER, 7,
                        com.cosmicpve.registry.ModEnchantments.SELF_DESTRUCT, 3,
                        com.cosmicpve.registry.ModEnchantments.ANGELIC, 5,
                        com.cosmicpve.registry.ModEnchantments.OBSIDIANSHIELD, 2,
                        com.cosmicpve.registry.ModEnchantments.TANK, 4,
                        com.cosmicpve.registry.ModEnchantments.LUCK, 10);
            }
            case FREEMAN_WALKERS -> {
                armorVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED, 4,
                        com.cosmicpve.registry.ModEnchantments.TANK, 4,
                        com.cosmicpve.registry.ModEnchantments.GEARS, 3,
                        com.cosmicpve.registry.ModEnchantments.PHOENIX, 3,
                        com.cosmicpve.registry.ModEnchantments.STORMCALLER, 5,
                        com.cosmicpve.registry.ModEnchantments.NIMBLE, 4,
                        com.cosmicpve.registry.ModEnchantments.LUCK, 10,
                        com.cosmicpve.registry.ModEnchantments.DODGE, 5);
            }
            case IZANAGI -> {
                weaponVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.DEEP_BLEED, 6,
                        com.cosmicpve.registry.ModEnchantments.MIGHTY_CLEAVE, 8,
                        com.cosmicpve.registry.ModEnchantments.INSANITY, 8,
                        com.cosmicpve.registry.ModEnchantments.HEX, 5,
                        com.cosmicpve.registry.ModEnchantments.DEVOUR, 4,
                        com.cosmicpve.registry.ModEnchantments.BOSS_SLAYER, 3,
                        com.cosmicpve.registry.ModEnchantments.BLESSED, 4,
                        com.cosmicpve.registry.ModEnchantments.BERSERK, 5,
                        com.cosmicpve.registry.ModEnchantments.ANTI_GANK, 4,
                        com.cosmicpve.registry.ModEnchantments.OBLITERATE, 3);
            }
            case ETERNAL_STRIDERS -> {
                armorVanilla(expected);
                add(expected, com.cosmicpve.registry.ModEnchantments.LUCK, 10,
                        com.cosmicpve.registry.ModEnchantments.DODGE, 5,
                        com.cosmicpve.registry.ModEnchantments.PALADIN_ARMORED, 4,
                        com.cosmicpve.registry.ModEnchantments.ENDER_WALKER, 5,
                        com.cosmicpve.registry.ModEnchantments.GEARS, 3,
                        com.cosmicpve.registry.ModEnchantments.PHOENIX, 3,
                        com.cosmicpve.registry.ModEnchantments.NIMBLE, 4,
                        com.cosmicpve.registry.ModEnchantments.ANGELIC, 5);
            }
        }
        helper.assertTrue(actual.equals(expected), "Exact Admin Abuse enchantment payload differs for " + outcome
                + ": expected " + expected + ", got " + actual);
    }

    private static void armorVanilla(java.util.Map<ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, Integer> expected) {
        add(expected, Enchantments.PROTECTION, 4, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1);
    }
    private static void weaponVanilla(java.util.Map<ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, Integer> expected) {
        add(expected, Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3, Enchantments.MENDING, 1);
    }
    private static void add(java.util.Map<ResourceKey<net.minecraft.world.item.enchantment.Enchantment>, Integer> expected,
            Object... entries) {
        for (int i = 0; i < entries.length; i += 2) {
            @SuppressWarnings("unchecked")
            var key = (ResourceKey<net.minecraft.world.item.enchantment.Enchantment>) entries[i];
            expected.put(key, (Integer) entries[i + 1]);
        }
    }
}
