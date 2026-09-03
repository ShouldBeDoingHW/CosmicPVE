package com.cosmicpve.upgrade;

import com.cosmicpve.economy.MoneyAmount;
import com.cosmicpve.registry.ModMenus;
import com.cosmicpve.vkit.VKitEquipmentGenerator;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

public final class PlayerUpgradesMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT=27; public static final int[] UPGRADE_SLOTS={10,12,14,19,21,23};
    public static final Component TITLE=Component.literal("PLAYER UPGRADES").withStyle(s->s.withColor(0x7EF2E0).withBold(true));
    private final SimpleContainer display=new SimpleContainer(SLOT_COUNT); private final Player owner;
    private PlayerUpgradesMenu(int id,Inventory inventory){super(ModMenus.PLAYER_UPGRADES.get(),id);owner=inventory.player;for(int i=0;i<SLOT_COUNT;i++)addSlot(new DisplaySlot(display,i,8+(i%9)*18,18+(i/9)*18));refresh();}
    public PlayerUpgradesMenu(int id,Inventory inventory,ServerPlayer ignored){this(id,inventory);} public static PlayerUpgradesMenu client(int id,Inventory inventory,RegistryFriendlyByteBuf ignored){return new PlayerUpgradesMenu(id,inventory);}
    private void refresh(){for(int i=0;i<SLOT_COUNT;i++)display.setItem(i,filler());if(owner instanceof ServerPlayer player){var service=new PlayerUpgradeService();var money=new com.cosmicpve.economy.MoneyService();for(int i=0;i<PlayerUpgrade.values().length;i++){var u=PlayerUpgrade.values()[i];display.setItem(UPGRADE_SLOTS[i],icon(u,service.tier(player,u),service.banked(player),money.balance(player)));}}}
    public static ItemStack icon(PlayerUpgrade u,int tier,long crystals,long money){var stack=new ItemStack(u.icon());stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE,true);stack.set(DataComponents.CUSTOM_NAME,Component.literal(u.displayName()).withStyle(s->s.withColor(u.color()).withBold(true).withItalic(false)));var lore=new java.util.ArrayList<Component>();lore.add(Component.literal("Current Tier: ").withStyle(s->s.withColor(0xAAAAAA)).append(Component.literal(tier==0?"NONE":VKitEquipmentGenerator.roman(tier)).withStyle(s->s.withColor(0xFFFFFF).withBold(true))));if(tier>=u.maxTier()){lore.add(Component.literal("MAXED").withStyle(s->s.withColor(0x55FF55).withBold(true)));lore.add(effect(u,tier));}else{int next=tier+1;lore.add(Component.literal("Next Tier: ").withStyle(s->s.withColor(0xAAAAAA)).append(Component.literal(VKitEquipmentGenerator.roman(next)).withStyle(s->s.withColor(0xFFFFFF).withBold(true))));lore.add(effect(u,next));lore.add(Component.empty());lore.add(Component.literal("Upgrade Crystals").withStyle(s->s.withColor(0x7EF2E0).withBold(true)));lore.add(Component.literal("Cost: "+u.crystalCost(next)).withStyle(s->s.withColor(0xAAAAAA)));lore.add(Component.literal("You Have: "+crystals).withStyle(s->s.withColor(crystals>=u.crystalCost(next)?0x7EF2E0:0xFF5555)));lore.add(Component.literal("Money").withStyle(s->s.withColor(0x55FF55).withBold(true)));lore.add(Component.literal("Cost: "+MoneyAmount.format(u.moneyCost(next))).withStyle(s->s.withColor(0xAAAAAA)));lore.add(Component.literal("You Have: "+MoneyAmount.format(money)).withStyle(s->s.withColor(money>=u.moneyCost(next)?0x55FF55:0xFF5555)));}stack.set(DataComponents.LORE,new ItemLore(List.copyOf(lore)));return stack;}
    private static Component effect(PlayerUpgrade u,int tier){String text=switch(u){case MORE_DAMAGE->"+"+tier+"% ordinary damage";case LESS_DAMAGE->"-"+tier+"% ordinary damage taken";case DUNGEON_MASTERY->(u.effect(tier)*100)+"% key preservation chance";case SLOW_MO->"+"+(int)u.effect(tier)+" seconds initial Trial time";case SAFETY_NET->"-"+(int)u.effect(tier)+" book Destroy Rate points";case PURE_RNG->"+2 total Luck levels";};return Component.literal(text).withStyle(s->s.withColor(0xFFFF55));}
    @Override public void clicked(int slot,int button,ClickType type,Player player){if(!(player instanceof ServerPlayer server))return;for(int i=0;i<UPGRADE_SLOTS.length;i++)if(slot==UPGRADE_SLOTS[i]){var u=PlayerUpgrade.values()[i];var result=new PlayerUpgradeService().purchaseNext(server,u);if(result==PlayerUpgradeService.PurchaseResult.SUCCESS){server.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.PLAYER_LEVELUP),SoundSource.PLAYERS,server.getX(),server.getY(),server.getZ(),1F,1.1F,server.getRandom().nextLong()));int tier=new PlayerUpgradeService().tier(server,u);server.sendSystemMessage(Component.literal("UPGRADE PURCHASED! ").withStyle(s->s.withColor(0x7EF2E0).withBold(true)).append(Component.literal(u.displayName()+" "+VKitEquipmentGenerator.roman(tier)).withStyle(s->s.withColor(u.color()).withBold(true))));refresh();broadcastChanges();}else server.displayClientMessage(Component.literal(switch(result){case MAXED->"That upgrade is already maxed.";case INSUFFICIENT_CRYSTALS->"You do not have enough banked Upgrade Crystals.";case INSUFFICIENT_MONEY->"You do not have enough money.";default->"The upgrade purchase failed safely.";}),true);return;}}
    @Override public ItemStack quickMoveStack(Player p,int i){return ItemStack.EMPTY;}@Override public boolean stillValid(Player p){return owner==p;}private static ItemStack filler(){var s=new ItemStack(Items.BLACK_STAINED_GLASS_PANE);s.set(DataComponents.CUSTOM_NAME,Component.literal(" "));return s;}private static final class DisplaySlot extends Slot{DisplaySlot(Container c,int i,int x,int y){super(c,i,x,y);}@Override public boolean mayPlace(ItemStack s){return false;}@Override public boolean mayPickup(Player p){return false;}}
}
