package com.cosmicpve.upgrade;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class UpgradeCrystalItem extends Item {
    public static final int COLOR=0x7EF2E0, YELLOW=0xFFFF55;
    private final PlayerUpgradeService upgrades=new PlayerUpgradeService();
    public UpgradeCrystalItem(Properties properties){super(properties);}
    @Override public boolean isFoil(ItemStack stack){return true;}
    public static Component displayName(){return Component.literal("Upgrade Crystal").withStyle(s->s.withColor(COLOR).withBold(true).withItalic(true));}
    @Override public Component getName(ItemStack stack){return displayName();}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,TooltipDisplay display,Consumer<Component> consumer,TooltipFlag flag){
        consumer.accept(Component.literal("Potential, crystallized and waiting to be claimed.").withStyle(s->s.withColor(YELLOW).withItalic(true)));
        consumer.accept(Component.literal("Right-click to permanently bank this stack.").withStyle(s->s.withColor(0xAAAAAA)));
    }
    @Override public InteractionResult use(Level level,net.minecraft.world.entity.player.Player player,InteractionHand hand){
        if(level.isClientSide())return InteractionResult.SUCCESS; if(!(player instanceof ServerPlayer server))return InteractionResult.FAIL;
        ItemStack held=player.getItemInHand(hand); int count=held.getCount(); if(count<=0||!upgrades.addBanked(server,count))return InteractionResult.FAIL;
        held.setCount(0); server.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP), SoundSource.PLAYERS,
                server.getX(), server.getY(), server.getZ(), 1F, 1F, server.getRandom().nextLong()));
        server.sendSystemMessage(Component.literal("Banked +"+count+" Upgrade Crystals!").withStyle(s->s.withColor(COLOR).withBold(true)));
        server.sendSystemMessage(Component.literal("Total Banked: ").withStyle(s->s.withColor(0xAAAAAA)).append(Component.literal(Long.toString(upgrades.banked(server))).withStyle(s->s.withColor(COLOR))));
        return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(ItemStack.EMPTY);
    }
}
