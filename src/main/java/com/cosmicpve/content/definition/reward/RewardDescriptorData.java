package com.cosmicpve.content.definition.reward;

import com.cosmicpve.content.validation.ContentDiagnostic;
import com.cosmicpve.content.validation.ValidationResult;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.reward.spawner.MobSpawnerEligibility;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public record RewardDescriptorData(RewardType type, Optional<Identifier> itemId, Optional<Long> cents,
        Optional<CosmicEnchantmentTier> rarity, Optional<Integer> successRate,
        Optional<Identifier> entityTypeId, Optional<GeneratedEquipmentDefinitionData> generatedEquipment,
        Optional<Integer> maskCount, Optional<Identifier> armorSetId, Optional<Long> experience,
        Optional<Identifier> portalPreset, Optional<Integer> trinketTier) {
    public static final Codec<RewardDescriptorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RewardType.CODEC.fieldOf("type").forGetter(RewardDescriptorData::type),
            Identifier.CODEC.optionalFieldOf("item").forGetter(RewardDescriptorData::itemId),
            Codec.LONG.optionalFieldOf("cents").forGetter(RewardDescriptorData::cents),
            CosmicEnchantmentTier.CODEC.optionalFieldOf("rarity").forGetter(RewardDescriptorData::rarity),
            Codec.INT.optionalFieldOf("success_rate").forGetter(RewardDescriptorData::successRate),
            Identifier.CODEC.optionalFieldOf("entity_type").forGetter(RewardDescriptorData::entityTypeId),
            GeneratedEquipmentDefinitionData.CODEC.optionalFieldOf("generated_equipment")
                    .forGetter(RewardDescriptorData::generatedEquipment),
            Codec.INT.optionalFieldOf("mask_count").forGetter(RewardDescriptorData::maskCount),
            Identifier.CODEC.optionalFieldOf("armor_set").forGetter(RewardDescriptorData::armorSetId),
            Codec.LONG.optionalFieldOf("experience").forGetter(RewardDescriptorData::experience),
            Identifier.CODEC.optionalFieldOf("portal_preset").forGetter(RewardDescriptorData::portalPreset),
            Codec.INT.optionalFieldOf("trinket_tier").forGetter(RewardDescriptorData::trinketTier)
    ).apply(instance, RewardDescriptorData::new));

    public RewardDescriptorData(RewardType type, Optional<Identifier> item, Optional<Long> cents,
            Optional<CosmicEnchantmentTier> rarity, Optional<Integer> rate, Optional<Identifier> entity,
            Optional<GeneratedEquipmentDefinitionData> equipment, Optional<Integer> masks,
            Optional<Identifier> armor, Optional<Long> xp, Optional<Identifier> preset) {
        this(type,item,cents,rarity,rate,entity,equipment,masks,armor,xp,preset,Optional.empty());
    }

    public RewardDescriptorData(RewardType type, Optional<Identifier> item, Optional<Long> cents,
            Optional<CosmicEnchantmentTier> rarity, Optional<Integer> rate, Optional<Identifier> entity,
            Optional<GeneratedEquipmentDefinitionData> equipment, Optional<Integer> masks,
            Optional<Identifier> armor, Optional<Long> xp) {
        this(type,item,cents,rarity,rate,entity,equipment,masks,armor,xp,Optional.empty(),Optional.empty());
    }

    public ValidationResult<RewardDescriptor> resolve(String source, RegistryAccess registries) {
        try {
            return switch (type) {
                case PINPOINT_BOOK -> ValidationResult.success(new RewardDescriptor.PinpointBook());
                case RANDOM_RANGER_ARMOR -> ValidationResult.success(new RewardDescriptor.RandomRangerArmor());
                case ADVANCED_BANKNOTE -> ValidationResult.success(new RewardDescriptor.AdvancedBanknote());
                case SKIP_TWO_PORTAL -> ValidationResult.success(new RewardDescriptor.SkipTwoPortal());
                case MADNESS_THREE_PORTAL -> ValidationResult.success(new RewardDescriptor.MadnessThreePortal());
                case MEMORY_CHEST -> ValidationResult.success(new RewardDescriptor.MemoryChest());
                case RANDOM_TRIAL_TRINKET -> ValidationResult.success(new RewardDescriptor.RandomTrialTrinket(
                        required(trinketTier, "trinket_tier")));
                case ACCESSORY_SOCKET -> {
                    var id = required(itemId, "item");
                    if (!List.of(Identifier.parse("cosmicpve:amulet_socket"), Identifier.parse("cosmicpve:belt_socket"),
                            Identifier.parse("cosmicpve:omni_socket")).contains(id))
                        yield failure(source, "accessory_socket item must be Amulet, Belt or Omni Socket");
                    yield ValidationResult.success(new RewardDescriptor.AccessorySocket(id, rate()));
                }
                case TRIAL_PORTAL_PRESET -> ValidationResult.success(new RewardDescriptor.TrialPortalPreset(
                        required(portalPreset,"portal_preset")));
                case STATIC_ITEM -> {
                    var id = required(itemId, "item");
                    if (!BuiltInRegistries.ITEM.containsKey(id)) yield failure(source, "Unknown item: " + id);
                    yield ValidationResult.success(new RewardDescriptor.StaticItem(id));
                }
                case BANKNOTE -> {
                    long value = required(cents, "cents");
                    if (value <= 0) yield failure(source, "Banknote cents must be positive");
                    yield ValidationResult.success(new RewardDescriptor.Banknote(value));
                }
                case COSMIC_BOOK -> {
                    var tier = required(rarity, "rarity");
                    var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
                    boolean available = CosmicEnchantmentSpecs.ALL.stream().anyMatch(spec -> spec.tier() == tier
                            && registry.get(spec.id()).isPresent());
                    if (!available) yield failure(source, "No implemented enchantment exists for rarity " + tier);
                    yield ValidationResult.success(new RewardDescriptor.CosmicBook(tier));
                }
                case UNEXAMINED_BOOK -> {
                    var tier = required(rarity, "rarity");
                    var registry = registries.lookupOrThrow(Registries.ENCHANTMENT);
                    boolean available = CosmicEnchantmentSpecs.ALL.stream().anyMatch(spec -> spec.tier() == tier
                            && registry.get(spec.id()).isPresent());
                    if (!available) yield failure(source, "No implemented enchantment exists for rarity " + tier);
                    yield ValidationResult.success(new RewardDescriptor.UnexaminedBook(tier));
                }
                case BLACK_SCROLL -> ValidationResult.success(new RewardDescriptor.BlackScroll(rate()));
                case ARMOR_ORB -> ValidationResult.success(new RewardDescriptor.ArmorOrb(optionalRate()));
                case WEAPON_ORB -> ValidationResult.success(new RewardDescriptor.WeaponOrb(optionalRate()));
                case MOB_SPAWNER -> {
                    var id = required(entityTypeId, "entity_type");
                    if (!MobSpawnerEligibility.isEligible(id))
                        yield failure(source, "Spawner entity must be a registered Mob type: " + id);
                    yield ValidationResult.success(new RewardDescriptor.MobSpawner(id));
                }
                case GENERATED_EQUIPMENT -> {
                    var resolved = required(generatedEquipment, "generated_equipment")
                            .resolve(source, registries.lookupOrThrow(Registries.ENCHANTMENT));
                    if (!resolved.isSuccess()) yield ValidationResult.failure(resolved.diagnostics());
                    yield ValidationResult.success(new RewardDescriptor.GeneratedEquipment(resolved.valueOrThrow()));
                }
                case SPACE_CHEST -> {
                    var tier = required(rarity, "rarity");
                    yield ValidationResult.success(new RewardDescriptor.SpaceChest(switch (tier) {
                        case ULTIMATE -> com.cosmicpve.spacechest.SpaceChestTier.ULTIMATE;
                        case LEGENDARY -> com.cosmicpve.spacechest.SpaceChestTier.LEGENDARY;
                        case MASTERY -> com.cosmicpve.spacechest.SpaceChestTier.MASTERY;
                        default -> throw new IllegalArgumentException("Space Chest rarity must be Ultimate, Legendary, or Mastery");
                    }));
                }
                case MASK -> {
                    int count = maskCount.orElse(1);
                    if (count < 1 || count > 5) yield failure(source, "mask_count must be in [1,5]");
                    yield ValidationResult.success(new RewardDescriptor.Mask(count));
                }
                case ARMOR_SET_CRYSTAL -> ValidationResult.success(new RewardDescriptor.ArmorSetCrystal(
                        required(armorSetId, "armor_set"), rate()));
                case XP_BOTTLE -> {
                    long value = required(experience, "experience");
                    if (value < 1 || value > Integer.MAX_VALUE) yield failure(source,
                            "experience must be in [1," + Integer.MAX_VALUE + "]");
                    yield ValidationResult.success(new RewardDescriptor.XpBottle(value));
                }
                case RANDOM_VKIT_CRYSTAL -> ValidationResult.success(new RewardDescriptor.RandomVKitCrystal());
                case ENCHANTED_BLACK_SCROLL -> ValidationResult.success(
                        new RewardDescriptor.EnchantedBlackScroll(rate()));
            };
        } catch (IllegalArgumentException exception) {
            return failure(source, exception.getMessage());
        }
    }

    private int rate() {
        int value = required(successRate, "success_rate");
        if (value < 1 || value > 100) throw new IllegalArgumentException("success_rate must be in [1,100]");
        return value;
    }
    private int optionalRate() {
        if (successRate.isEmpty()) return 0;
        return rate();
    }
    private static <T> T required(Optional<T> value, String name) {
        return value.orElseThrow(() -> new IllegalArgumentException("Missing required field: " + name));
    }
    private static ValidationResult<RewardDescriptor> failure(String source, String message) {
        return ValidationResult.failure(List.of(ContentDiagnostic.error(source, message)));
    }
}
