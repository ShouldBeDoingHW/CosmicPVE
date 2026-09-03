package com.cosmicpve.upgrade;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum PlayerUpgrade {
    MORE_DAMAGE("more_damage", "More Damage!", 0xFF5555, Items.DIAMOND_SWORD,
            new int[]{4,8,12,16,20}, dollars(1,2,3,4,5), new double[]{.01,.02,.03,.04,.05}),
    LESS_DAMAGE("less_damage", "Less Damage!", 0x55FFFF, Items.SHIELD,
            new int[]{4,8,12,16,20}, dollars(1,2,3,4,5), new double[]{.01,.02,.03,.04,.05}),
    DUNGEON_MASTERY("dungeon_mastery", "Dungeon Mastery!", 0xAA55FF, Items.TRIPWIRE_HOOK,
            new int[]{16,32,48,64}, dollars(5,10,15,20), new double[]{.025,.05,.075,.10}),
    SLOW_MO("slow_mo", "Slow Mo!", 0xFFFF55, Items.CLOCK,
            new int[]{10,20,30}, cents(250_000_000L,500_000_000L,750_000_000L), new double[]{20,40,60}),
    SAFETY_NET("safety_net", "Safety Net!", 0x55FF55, Items.TOTEM_OF_UNDYING,
            new int[]{12,24,36,48,60}, dollars(3,6,9,12,15), new double[]{2,4,6,8,10}),
    PURE_RNG("pure_rng", "Pure RNG!", 0xFF55FF, Items.RABBIT_FOOT,
            new int[]{32}, dollars(8), new double[]{2});

    private final Identifier id; private final String displayName; private final int color; private final Item icon;
    private final int[] crystalCosts; private final long[] moneyCosts; private final double[] effects;
    PlayerUpgrade(String path, String displayName, int color, Item icon, int[] crystals, long[] money, double[] effects) {
        this.id=CosmicPVE.id(path); this.displayName=displayName; this.color=color; this.icon=icon;
        this.crystalCosts=crystals; this.moneyCosts=money; this.effects=effects;
    }
    public Identifier id(){return id;} public String displayName(){return displayName;} public int color(){return color;}
    public Item icon(){return icon;} public int maxTier(){return crystalCosts.length;}
    public int crystalCost(int tier){return crystalCosts[tier-1];} public long moneyCost(int tier){return moneyCosts[tier-1];}
    public double effect(int tier){return tier <= 0 ? 0 : effects[Math.min(tier,maxTier())-1];}
    public static PlayerUpgrade byId(Identifier id){return List.of(values()).stream().filter(v->v.id.equals(id)).findFirst().orElse(null);}
    private static long[] dollars(int... millions){long[] out=new long[millions.length]; for(int i=0;i<out.length;i++) out[i]=millions[i]*100_000_000L; return out;}
    private static long[] cents(long... values){return values;}
}
