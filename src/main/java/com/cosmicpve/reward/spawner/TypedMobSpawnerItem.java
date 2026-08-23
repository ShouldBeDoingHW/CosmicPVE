package com.cosmicpve.reward.spawner;

import com.cosmicpve.data.component.MobSpawnerData;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

public final class TypedMobSpawnerItem extends Item {
    public TypedMobSpawnerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MobSpawnerData data = stack.get(ModDataComponents.MOB_SPAWNER.get());
        if (data == null || !data.isCurrent()) return super.getName(stack);
        var entityType = BuiltInRegistries.ENTITY_TYPE.get(data.entityTypeId());
        return entityType.isPresent()
                ? Component.translatable("item.cosmicpve.mob_spawner.named", entityType.orElseThrow().value().getDescription())
                : super.getName(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        MobSpawnerData data = stack.get(ModDataComponents.MOB_SPAWNER.get());
        if (data == null || !data.isCurrent()) return InteractionResult.FAIL;
        var entityType = BuiltInRegistries.ENTITY_TYPE.get(data.entityTypeId());
        if (entityType.isEmpty()) return InteractionResult.FAIL;
        var probe = entityType.orElseThrow().value().create(level, EntitySpawnReason.COMMAND);
        if (!(probe instanceof Mob)) {
            if (probe != null) probe.discard();
            return InteractionResult.FAIL;
        }
        probe.discard();
        var position = new BlockPlaceContext(context).getClickedPos();
        if (!level.getBlockState(position).canBeReplaced() || !level.mayInteract(context.getPlayer(), position)) {
            return InteractionResult.FAIL;
        }
        if (!level.setBlock(position, Blocks.SPAWNER.defaultBlockState(), 11)) return InteractionResult.FAIL;
        if (!(level.getBlockEntity(position) instanceof SpawnerBlockEntity spawner)) {
            level.removeBlock(position, false);
            return InteractionResult.FAIL;
        }
        MobSpawnerConfiguration.configure(spawner, entityType.orElseThrow().value(), level, position);
        level.playSound(null, position, SoundEvents.SPAWNER_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
