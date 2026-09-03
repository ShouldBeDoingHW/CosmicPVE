package com.cosmicpve.upgrade;

import com.cosmicpve.data.attachment.PlayerUpgradeData;
import com.cosmicpve.economy.MoneyService;
import com.cosmicpve.registry.ModAttachments;
import java.util.HashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Sole mutation authority for banked crystals and purchased upgrade tiers. */
public final class PlayerUpgradeService {
    private final MoneyService money = new MoneyService();
    public long banked(ServerPlayer player){return player.getData(ModAttachments.PLAYER_UPGRADES).bankedCrystals();}
    public int tier(Player player, PlayerUpgrade upgrade){return player.getData(ModAttachments.PLAYER_UPGRADES).tier(upgrade.id());}
    public boolean setBanked(ServerPlayer player,long value){if(value<0)return false; var old=data(player); set(player,new PlayerUpgradeData(old.dataVersion(),value,old.tiers())); return true;}
    public boolean addBanked(ServerPlayer player,long amount){if(amount<=0)return false; try{return setBanked(player,Math.addExact(banked(player),amount));}catch(ArithmeticException e){return false;}}
    public boolean setTier(ServerPlayer player,PlayerUpgrade upgrade,int tier){if(tier<0||tier>upgrade.maxTier())return false; var old=data(player); var next=new HashMap<>(old.tiers()); if(tier==0)next.remove(upgrade.id());else next.put(upgrade.id(),tier); set(player,new PlayerUpgradeData(old.dataVersion(),old.bankedCrystals(),next)); return true;}
    public PurchaseResult purchaseNext(ServerPlayer player,PlayerUpgrade upgrade){
        int current=tier(player,upgrade); if(current>=upgrade.maxTier())return PurchaseResult.MAXED; int next=current+1;
        int crystals=upgrade.crystalCost(next); long cents=upgrade.moneyCost(next);
        if(banked(player)<crystals)return PurchaseResult.INSUFFICIENT_CRYSTALS;
        if(money.balance(player)<cents)return PurchaseResult.INSUFFICIENT_MONEY;
        var old=data(player); var tiers=new HashMap<>(old.tiers()); tiers.put(upgrade.id(),next);
        if(!money.subtract(player,cents))return PurchaseResult.INSUFFICIENT_MONEY;
        try { set(player,new PlayerUpgradeData(old.dataVersion(),old.bankedCrystals()-crystals,tiers)); }
        catch(RuntimeException failure){ money.add(player,cents); return PurchaseResult.FAILED; }
        return PurchaseResult.SUCCESS;
    }
    private PlayerUpgradeData data(ServerPlayer player){return player.getData(ModAttachments.PLAYER_UPGRADES);}
    private void set(ServerPlayer player,PlayerUpgradeData data){player.setData(ModAttachments.PLAYER_UPGRADES,data);}
    public enum PurchaseResult{SUCCESS,MAXED,INSUFFICIENT_CRYSTALS,INSUFFICIENT_MONEY,FAILED}
}
