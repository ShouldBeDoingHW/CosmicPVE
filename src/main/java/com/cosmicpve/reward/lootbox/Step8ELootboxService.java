package com.cosmicpve.reward.lootbox;

import com.cosmicpve.registry.ModItems;
import com.cosmicpve.reward.animation.LootAnimationPreviewProvider;
import com.cosmicpve.reward.animation.SingleRewardAnimationService;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class Step8ELootboxService {
    public static final Step8ELootboxService INSTANCE = new Step8ELootboxService();
    private final SignatureWeaponFactory signatures = new SignatureWeaponFactory();
    private final AdminAbuseRewardFactory admin = new AdminAbuseRewardFactory();
    private final CosmicEnchantmentTableRewards table = new CosmicEnchantmentTableRewards();
    private final HeroicCosmicEnchantmentTableRewards heroicTable = new HeroicCosmicEnchantmentTableRewards();
    private Step8ELootboxService() {}

    public List<ItemStack> previewOutcomes(ServerPlayer player, AnimatedLootboxItem.Kind kind) {
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> signatureCandidates(player);
            case ADMIN_ABUSE -> adminCandidates(player);
            case MYSTERY_CALL_OF_ADVENTURE -> ModItems.productionCalls();
            case RANDOM_WEAPON_SKIN_GENERATOR -> randomWeaponSkinCandidates();
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> HeroicCosmicEnchantmentTableRewards.POOL.stream()
                    .flatMap(id -> HeroicCosmicEnchantmentTableRewards.SUCCESS.stream()
                            .map(success -> previewBook(id, success, 1, false))).toList();
            case COSMIC_ENCHANTMENT_TABLE -> CosmicEnchantmentTableRewards.POOL.stream().flatMap(key -> {
                boolean mastery = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(key.identifier()).orElseThrow().tier()
                        == com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier.MASTERY;
                return (mastery ? CosmicEnchantmentTableRewards.MASTERY_SUCCESS : CosmicEnchantmentTableRewards.ORDINARY_SUCCESS)
                        .stream().map(success -> previewBook(key.identifier(), success, mastery ? 51 : 1, mastery));
            }).toList();
        };
    }
    private static ItemStack previewBook(net.minecraft.resources.Identifier id, int success, int destroy, boolean mastery) {
        var spec = com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs.find(id).orElseThrow();
        var book = new ItemStack(ModItems.COSMIC_ENCHANTMENT_BOOK.get());
        // Preview identity is enchantment + level + Success, not a random Destroy roll.
        book.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(previewDestroyRange(mastery ? 51 : 1)));
        book.set(com.cosmicpve.registry.ModDataComponents.COSMIC_ENCHANT_BOOK.get(),
                new com.cosmicpve.data.component.CosmicEnchantmentBookData(
                        com.cosmicpve.data.component.CosmicEnchantmentBookData.CURRENT_DATA_VERSION, id, spec.maxLevel(), success, destroy));
        if (mastery) book.set(com.cosmicpve.registry.ModDataComponents.COSMIC_BOOK_RATE_OVERRIDE.get(),
                new com.cosmicpve.data.component.CosmicBookRateOverride(
                        com.cosmicpve.data.component.CosmicBookRateOverride.CURRENT_DATA_VERSION, CosmicEnchantmentTableRewards.SOURCE_ID));
        return book;
    }
    private static net.minecraft.nbt.CompoundTag previewDestroyRange(int minimum) {
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putInt("cosmic_preview_destroy_min", minimum);
        return tag;
    }

    public boolean open(ServerPlayer player, ItemStack source, AnimatedLootboxItem.Kind kind) {
        if (!matches(source, kind)) return false;
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> openSecretCache(player, source);
            case COSMIC_ENCHANTMENT_TABLE -> openCosmicTable(player, source);
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> openHeroicTable(player, source);
            case ADMIN_ABUSE -> openAdminAbuse(player, source);
            case MYSTERY_CALL_OF_ADVENTURE -> openMysteryCall(player, source);
            case RANDOM_WEAPON_SKIN_GENERATOR -> openRandomWeaponSkin(player, source);
        };
    }
    public boolean forceHeroicTable(ServerPlayer player, net.minecraft.resources.Identifier id, int success) {
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return SingleRewardAnimationService.INSTANCE.open(player, heroicTable.create(registry, id, success, player.getRandom()),
                random -> heroicTable.create(registry, random), () -> {});
    }
    public boolean forceSignature(ServerPlayer player, SignatureWeaponDefinition definition) {
        List<ItemStack> previews = signatureCandidates(player);
        return SingleRewardAnimationService.INSTANCE.open(player, signatures.create(definition, player.registryAccess()),
                LootAnimationPreviewProvider.uniform(previews), () -> {});
    }
    public boolean forceTable(ServerPlayer player, net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key,
            int success) {
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return SingleRewardAnimationService.INSTANCE.open(player, table.create(registry, key, success, player.getRandom()),
                random -> table.create(registry, random), () -> {});
    }
    public boolean forceAdmin(ServerPlayer player, AdminAbuseRewards.Outcome outcome) {
        return SingleRewardAnimationService.INSTANCE.open(player, admin.create(outcome, player.registryAccess()),
                LootAnimationPreviewProvider.uniform(adminCandidates(player)), () -> {});
    }
    private boolean openSecretCache(ServerPlayer player, ItemStack source) {
        List<ItemStack> candidates = signatureCandidates(player);
        ItemStack reward = selectSignatureWeapon(candidates, player.getRandom());
        return SingleRewardAnimationService.INSTANCE.open(player, reward,
                LootAnimationPreviewProvider.uniform(candidates), () -> source.shrink(1));
    }
    private boolean openCosmicTable(ServerPlayer player, ItemStack source) {
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack reward = table.create(registry, player.getRandom());
        return SingleRewardAnimationService.INSTANCE.open(player, reward,
                random -> table.create(registry, random), () -> source.shrink(1));
    }
    private boolean openAdminAbuse(ServerPlayer player, ItemStack source) {
        var selected = AdminAbuseRewards.select(player.getRandom());
        ItemStack reward = admin.create(selected, player.registryAccess());
        return SingleRewardAnimationService.INSTANCE.open(player, reward,
                LootAnimationPreviewProvider.weighted(AdminAbuseRewards.ALL.stream().map(outcome ->
                        new LootAnimationPreviewProvider.WeightedPreview(
                                admin.create(outcome, player.registryAccess()), outcome.weight())).toList()),
                () -> source.shrink(1));
    }
    private boolean openHeroicTable(ServerPlayer player, ItemStack source) {
        var registry = player.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemStack reward = heroicTable.create(registry, player.getRandom());
        return SingleRewardAnimationService.INSTANCE.open(player, reward,
                random -> heroicTable.create(registry, random), () -> source.shrink(1));
    }
    private List<ItemStack> signatureCandidates(ServerPlayer player) {
        return SignatureWeaponDefinition.ALL.stream().map(definition -> signatures.create(definition, player.registryAccess())).toList();
    }
    private boolean openMysteryCall(ServerPlayer player, ItemStack source) {
        var candidates=ModItems.productionCalls();
        if(candidates.isEmpty())return false;
        return SingleRewardAnimationService.INSTANCE.open(player,selectCall(candidates,player.getRandom()),
                random -> selectCall(candidates, random),()->source.shrink(1));
    }
    private boolean openRandomWeaponSkin(ServerPlayer player, ItemStack source) {
        var candidates = randomWeaponSkinCandidates();
        ItemStack reward = candidates.get(player.getRandom().nextInt(candidates.size())).copy();
        return SingleRewardAnimationService.INSTANCE.open(player, reward,
                LootAnimationPreviewProvider.uniform(candidates), () -> source.shrink(1));
    }
    public static List<ItemStack> randomWeaponSkinCandidates() {
        return List.of(
                com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.STORMBRINGER),
                com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.BOOSTED_CHAINSAW),
                com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.SPINAL_TAP),
                com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.GRIM_AXE),
                com.cosmicpve.equipment.skin.WeaponSkinItemFactory.create(
                        com.cosmicpve.equipment.skin.WeaponSkinDefinitions.MAUIS_HOOK));
    }
    public static ItemStack selectRandomWeaponSkin(List<ItemStack> candidates, net.minecraft.util.RandomSource random) {
        if (candidates.size() != 5) throw new IllegalArgumentException("Weapon Skin Generator requires five outcomes");
        return candidates.get(random.nextInt(candidates.size())).copy();
    }
    public static ItemStack selectSignatureWeapon(List<ItemStack> candidates, net.minecraft.util.RandomSource random) {
        if (candidates.size() != SignatureWeaponDefinition.ALL.size())
            throw new IllegalArgumentException("Secret Weapon Cache requires all six signature weapons");
        return candidates.get(random.nextInt(candidates.size())).copy();
    }
    public static int durationWeight(int minutes) {
        return switch (minutes) { case 10 -> 6; case 20 -> 4; case 30 -> 2;
            default -> throw new IllegalArgumentException("Unsupported Call duration " + minutes); };
    }
    private static int callWeight(ItemStack stack) {
        var data = stack.get(com.cosmicpve.registry.ModDataComponents.CALL_OF_FOREST.get());
        if (data == null) throw new IllegalArgumentException("Call missing duration");
        return durationWeight(data.minutes());
    }
    public static ItemStack selectCall(List<ItemStack> candidates,net.minecraft.util.RandomSource random) {
        if(candidates.isEmpty())throw new IllegalArgumentException("No production Calls");
        int total = candidates.stream().mapToInt(Step8ELootboxService::callWeight).sum();
        int roll = random.nextInt(total);
        for (var candidate : candidates) {
            roll -= callWeight(candidate);
            if (roll < 0) return candidate.copy();
        }
        throw new IllegalStateException("Call selection exhausted");
    }
    private List<ItemStack> adminCandidates(ServerPlayer player) {
        return AdminAbuseRewards.ALL.stream().map(outcome -> admin.create(outcome, player.registryAccess())).toList();
    }
    private static boolean matches(ItemStack stack, AnimatedLootboxItem.Kind kind) {
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> stack.is(ModItems.SECRET_WEAPON_CACHE.get());
            case COSMIC_ENCHANTMENT_TABLE -> stack.is(ModItems.COSMIC_ENCHANTMENT_TABLE.get());
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> stack.is(ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get());
            case ADMIN_ABUSE -> stack.is(ModItems.ADMIN_ABUSE.get());
            case MYSTERY_CALL_OF_ADVENTURE -> stack.is(ModItems.MYSTERY_CALL_OF_ADVENTURE.get());
            case RANDOM_WEAPON_SKIN_GENERATOR -> stack.is(ModItems.RANDOM_WEAPON_SKIN_GENERATOR.get());
        };
    }
}
