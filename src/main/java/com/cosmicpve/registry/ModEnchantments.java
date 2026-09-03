package com.cosmicpve.registry;

import com.cosmicpve.CosmicPVE;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> EXECUTE = createKey("execute");
    public static final ResourceKey<Enchantment> ANGELIC = createKey("angelic");
    public static final ResourceKey<Enchantment> LIGHTNING = createKey("lightning");
    public static final ResourceKey<Enchantment> ENDER_SHIFT = createKey("ender_shift");
    public static final ResourceKey<Enchantment> DOUBLESTRIKE = createKey("doublestrike");
    public static final ResourceKey<Enchantment> BLEED = createKey("bleed");
    public static final ResourceKey<Enchantment> LUCK = createKey("luck");
    public static final ResourceKey<Enchantment> POISON = createKey("poison");
    public static final ResourceKey<Enchantment> PUMMEL = createKey("pummel");
    public static final ResourceKey<Enchantment> GREATSWORD = createKey("greatsword");
    public static final ResourceKey<Enchantment> INSANITY = createKey("insanity");
    public static final ResourceKey<Enchantment> VENOM = createKey("venom");
    public static final ResourceKey<Enchantment> AEGIS = createKey("aegis");
    public static final ResourceKey<Enchantment> EAGLE_EYE = createKey("eagle_eye");
    public static final ResourceKey<Enchantment> RAGE = createKey("rage");
    public static final ResourceKey<Enchantment> MOLTEN = createKey("molten");
    public static final ResourceKey<Enchantment> NUTRITION = createKey("nutrition");
    public static final ResourceKey<Enchantment> GLOWING = createKey("glowing");
    public static final ResourceKey<Enchantment> OBSIDIANSHIELD = createKey("obsidianshield");
    public static final ResourceKey<Enchantment> OXYGENATE = createKey("oxygenate");
    public static final ResourceKey<Enchantment> ARMORED = createKey("armored");
    public static final ResourceKey<Enchantment> DEATH_PACT = createKey("death_pact");
    public static final ResourceKey<Enchantment> AUTO_SMELT = createKey("auto_smelt");
    public static final ResourceKey<Enchantment> EXPERIENCE = createKey("experience");
    public static final ResourceKey<Enchantment> BLESSED = createKey("blessed");
    public static final ResourceKey<Enchantment> IMPLANTS = createKey("implants");
    public static final ResourceKey<Enchantment> TRAP = createKey("trap");
    public static final ResourceKey<Enchantment> CACTUS = createKey("cactus");
    public static final ResourceKey<Enchantment> GEARS = createKey("gears");
    public static final ResourceKey<Enchantment> PERMAFROST = createKey("permafrost");
    public static final ResourceKey<Enchantment> MORTAL_COIL = createKey("mortal_coil");
    public static final ResourceKey<Enchantment> SELF_DESTRUCT = createKey("self_destruct");
    public static final ResourceKey<Enchantment> PHOENIX = createKey("phoenix");
    public static final ResourceKey<Enchantment> DIVINE_IMMOLATION = createKey("divine_immolation");
    public static final ResourceKey<Enchantment> VIRUS = createKey("virus");
    public static final ResourceKey<Enchantment> DEVOUR = createKey("devour");
    public static final ResourceKey<Enchantment> UNDEAD_RUSE = createKey("undead_ruse");
    public static final ResourceKey<Enchantment> OBLITERATE = createKey("obliterate");
    public static final ResourceKey<Enchantment> SOUL_TETHER = createKey("soul_tether");
    public static final ResourceKey<Enchantment> DODGE = createKey("dodge");
    public static final ResourceKey<Enchantment> LEADERSHIP = createKey("leadership");
    public static final ResourceKey<Enchantment> HERO_KILLER = createKey("hero_killer");
    public static final ResourceKey<Enchantment> SOUL_SIPHON = createKey("soul_siphon");
    public static final ResourceKey<Enchantment> BLACKOUT = createKey("blackout");
    public static final ResourceKey<Enchantment> ENDER_WALKER = createKey("ender_walker");
    public static final ResourceKey<Enchantment> VOODOO = createKey("voodoo");
    public static final ResourceKey<Enchantment> SNIPER = createKey("sniper");
    public static final ResourceKey<Enchantment> SNARE = createKey("snare");
    public static final ResourceKey<Enchantment> PLAGUE_CARRIER = createKey("plague_carrier");
    public static final ResourceKey<Enchantment> OBSIDIAN_DESTROYER = createKey("obsidian_destroyer");
    public static final ResourceKey<Enchantment> DOMINATE = createKey("dominate");
    public static final ResourceKey<Enchantment> HEX = createKey("hex");
    public static final ResourceKey<Enchantment> STORMCALLER = createKey("stormcaller");
    public static final ResourceKey<Enchantment> INVERSION = createKey("inversion");
    public static final ResourceKey<Enchantment> SPIRIT_LINK = createKey("spirit_link");
    public static final ResourceKey<Enchantment> DEEP_BLEED = createKey("deep_bleed");
    public static final ResourceKey<Enchantment> MIGHTY_CACTUS = createKey("mighty_cactus");
    public static final ResourceKey<Enchantment> PALADIN_ARMORED = createKey("paladin_armored");
    public static final ResourceKey<Enchantment> BLIGHTED_VIRUS = createKey("blighted_virus");
    public static final ResourceKey<Enchantment> ALIEN_IMPLANTS = createKey("alien_implants");
    public static final ResourceKey<Enchantment> LETHAL_SNIPER = createKey("lethal_sniper");
    public static final ResourceKey<Enchantment> ETERNAL_SNARE = createKey("eternal_snare");
    public static final ResourceKey<Enchantment> PERMANENT_EXECUTE = createKey("permanent_execute");

    private ModEnchantments() {}

    public static ResourceKey<Enchantment> createKey(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, CosmicPVE.id(path));
    }
}
