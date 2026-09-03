package com.cosmicpve.upgrade;
import java.util.function.DoubleSupplier;import net.minecraft.server.level.ServerPlayer;
public final class DungeonKeyPreservationService {private final PlayerUpgradeService upgrades=new PlayerUpgradeService();public double chance(ServerPlayer player){return upgrades.tier(player,PlayerUpgrade.DUNGEON_MASTERY)*.025;}public boolean preserves(ServerPlayer player,DoubleSupplier random){double chance=chance(player);return chance>0&&random.getAsDouble()<chance;}}
