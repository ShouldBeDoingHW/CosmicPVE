package com.cosmicpve.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;
import com.cosmicpve.reward.*;

/** Reusable adapter from lazy vanilla containers to the shared typed reward table service. */
public final class RewardTableLootModifier extends LootModifier {
    public static final MapCodec<RewardTableLootModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(v -> v.conditions),
            Identifier.CODEC.fieldOf("loot_table").forGetter(v -> v.lootTable),
            Identifier.CODEC.fieldOf("reward_table").forGetter(v -> v.rewardTable)
    ).apply(i,RewardTableLootModifier::new));
    private final Identifier lootTable,rewardTable;
    public RewardTableLootModifier(LootItemCondition[] conditions,Identifier lootTable,Identifier rewardTable) {
        super(conditions);this.lootTable=lootTable;this.rewardTable=rewardTable;
    }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generated,LootContext context) {
        if(!lootTable.equals(context.getQueriedLootTableId()))return generated;
        generated.addAll(new RewardTableService(com.cosmicpve.content.CosmicContent.repository(),new RewardGeneratorService())
                .roll(rewardTable,context.getRandom().nextIntBetweenInclusive(1,4),
                        new RewardGenerationContext(context.getLevel().registryAccess(),context.getRandom(),null)));
        return generated;
    }
    @Override public MapCodec<? extends IGlobalLootModifier> codec() { return CODEC; }
}
