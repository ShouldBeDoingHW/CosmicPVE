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

    public boolean open(ServerPlayer player, ItemStack source, AnimatedLootboxItem.Kind kind) {
        if (!matches(source, kind)) return false;
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> openSecretCache(player, source);
            case COSMIC_ENCHANTMENT_TABLE -> openCosmicTable(player, source);
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> openHeroicTable(player, source);
            case ADMIN_ABUSE -> openAdminAbuse(player, source);
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
        ItemStack reward = candidates.get(player.getRandom().nextInt(candidates.size())).copy();
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
    private List<ItemStack> adminCandidates(ServerPlayer player) {
        return AdminAbuseRewards.ALL.stream().map(outcome -> admin.create(outcome, player.registryAccess())).toList();
    }
    private static boolean matches(ItemStack stack, AnimatedLootboxItem.Kind kind) {
        return switch (kind) {
            case SECRET_WEAPON_CACHE -> stack.is(ModItems.SECRET_WEAPON_CACHE.get());
            case COSMIC_ENCHANTMENT_TABLE -> stack.is(ModItems.COSMIC_ENCHANTMENT_TABLE.get());
            case HEROIC_COSMIC_ENCHANTMENT_TABLE -> stack.is(ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get());
            case ADMIN_ABUSE -> stack.is(ModItems.ADMIN_ABUSE.get());
        };
    }
}
