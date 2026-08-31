package com.cosmicpve.reward.lootbox;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public record SignatureWeaponDefinition(Identifier id, String displayName, Item baseItem,
        Identifier matchingSetId, SignatureWeaponIdentity.Kind kind) {
    public static final SignatureWeaponDefinition PHANTOM_SCYTHE = melee("phantom_scythe", "Phantom Scythe", Items.NETHERITE_SWORD, ArmorSetIds.PHANTOM);
    public static final SignatureWeaponDefinition YETI_MAUL = melee("yeti_maul", "Yeti Maul", Items.NETHERITE_AXE, ArmorSetIds.YETI);
    public static final SignatureWeaponDefinition YJIKI_CLAW = melee("yjiki_claw", "Yjiki Claw", Items.NETHERITE_SWORD, ArmorSetIds.YJIKI);
    public static final SignatureWeaponDefinition RANGERS_BOW = ranged("rangers_bow", "Ranger's Bow", Items.BOW, ArmorSetIds.RANGER);
    public static final SignatureWeaponDefinition ENGINEERS_HAMMER = melee("engineers_hammer", "Engineer's Hammer", Items.NETHERITE_AXE, ArmorSetIds.ENGINEER);
    public static final SignatureWeaponDefinition TRAVELERS_SPACE_BLASTER = ranged("travelers_space_blaster", "Traveler's Space Blaster", Items.CROSSBOW, ArmorSetIds.DIMENSIONAL_TRAVELER);
    public static final List<SignatureWeaponDefinition> ALL = List.of(PHANTOM_SCYTHE, YETI_MAUL, YJIKI_CLAW,
            RANGERS_BOW, ENGINEERS_HAMMER, TRAVELERS_SPACE_BLASTER);

    private static SignatureWeaponDefinition melee(String id, String name, Item item, Identifier set) {
        return new SignatureWeaponDefinition(CosmicPVE.id(id), name, item, set, SignatureWeaponIdentity.Kind.MELEE);
    }
    private static SignatureWeaponDefinition ranged(String id, String name, Item item, Identifier set) {
        return new SignatureWeaponDefinition(CosmicPVE.id(id), name, item, set, SignatureWeaponIdentity.Kind.RANGED);
    }
    public static Optional<SignatureWeaponDefinition> find(Identifier id) {
        return ALL.stream().filter(value -> value.id().equals(id)).findFirst();
    }
}
