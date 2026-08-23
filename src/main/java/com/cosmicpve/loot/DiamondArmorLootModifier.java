package com.cosmicpve.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/** Data-scoped post-generation replacement that preserves the vanilla table's original roll structure. */
public final class DiamondArmorLootModifier extends LootModifier {
    public static final MapCodec<DiamondArmorLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(value -> value.conditions),
            Identifier.CODEC.listOf().fieldOf("loot_tables").forGetter(DiamondArmorLootModifier::lootTables)
    ).apply(instance, DiamondArmorLootModifier::new));

    private final List<Identifier> lootTables;
    private final DiamondArmorLootReplacementService replacements = new DiamondArmorLootReplacementService();

    public DiamondArmorLootModifier(LootItemCondition[] conditions, List<Identifier> lootTables) {
        super(conditions);
        this.lootTables = List.copyOf(lootTables);
        if (this.lootTables.isEmpty() || this.lootTables.stream().distinct().count() != this.lootTables.size()) {
            throw new IllegalArgumentException("Diamond armor replacement loot-table IDs must be non-empty and unique");
        }
    }

    public List<Identifier> lootTables() {
        return lootTables;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!lootTables.contains(context.getQueriedLootTableId())) return generatedLoot;
        for (int index = 0; index < generatedLoot.size(); index++) {
            ItemStack generated = generatedLoot.get(index);
            if (replacements.isDiamondArmor(generated)) {
                generatedLoot.set(index, replacements.replace(generated, context.getRandom()));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.DIAMOND_ARMOR_REPLACEMENT.get();
    }
}
