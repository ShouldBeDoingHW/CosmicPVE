package com.cosmicpve.trial;

import java.util.List;

/** Canonical Trial reward declarations, including dependency-bound rows excluded from runtime tables. */
public final class TrialRewardCatalogs {
    public static final Catalog APPRENTICE = catalog(List.of(
            row("Unexamined Simple Enchantment Book",8,2,true), row("Unexamined Simple Enchantment Book",6,1,true),
            row("Unexamined Unique Enchantment Book",12,1,true), row("Unexamined Elite Enchantment Book",10,1,true),
            row("Unexamined Ultimate Enchantment Book",8,1,true), row("Unexamined Legendary Enchantment Book",8,1,true),
            row("Repair Scroll",8,1,true), row("White Scroll",5,1,true), row("Transmog Scroll",8,1,true),
            row("Ultimate Space Chest",4,1,true), row("Mystery Simple Spawner",8,1,true), row("Space Dust Bundle",4,1,true)));
    public static final Catalog HARDCORE = catalog(List.of(
            row("Ultimate Space Chest",5,1,true), row("White Scroll",5,1,true), row("Space Dust Bundle",5,1,true),
            row("Skip 1 Room Trial Trinket",10,1,true), row("+33% Fame Trial Trinket",10,1,true),
            row("+1 Minute Trial Trinket",10,1,true), row("+1 Insurance Trial Trinket",10,1,true),
            row("Skip 2 Room Trial Trinket",3,1,true), row("+66% Fame Trial Trinket",3,1,true),
            row("+3 Minute Trial Trinket",3,1,true), row("+2 Insurance Trial Trinket",3,1,true),
            row("Unexamined Heroic Enchantment Book",5,1,true), row("Unexamined Mastery Enchantment Book",5,1,true),
            row("Conquest Chest Flare",5,1,true), row("Mystery Elite Spawner",5,1,true),
            row("Mask Splicer",5,1,true), row("Heroic Crystal",5,1,true), row("Abandoned Spaceship Portal",5,1,false),
            row("Mystery Call of Adventure",7,1,true)));
    public static final Catalog IMPOSSIBLE = catalog(ImpossibleRewardCatalog.DECLARED.stream()
            .map(row -> new Row(row.name(),row.weight(),row.quantity(),row.active())).toList());
    public static final Catalog DEMONIC = catalog(List.of(
            row("Secret Weapon Cache",4,1,true), row("Cosmic Enchantment Table",10,1,true),
            row("Mastery Space Chest",8,1,true), row("Random Double Mask",10,1,true), row("Admin Abuse",4,1,true),
            row("Skip 3 Room Trial Trinket",8,1,true), row("+100% Fame Trial Trinket",8,1,true),
            row("+5 Minute Trial Trinket",8,1,true), row("+3 Insurance Trial Trinket",8,1,true),
            row("75% Dragonslayer Crystal",8,1,true), row("100% Dragonslayer Crystal",4,1,true),
            row("100% Enchanted Black Scroll",10,1,true), row("Legendary Space Chest",10,1,true),
            row("Random V-Kit Unlock",12,1,true), row("Godly V-Kit Bundle",4,1,true),
            row("White Scroll",10,3,true), row("Heroic Abandoned Spaceship Portal",4,1,false),
            row("Random Weapon Skin Generator",5,1,true)));

    private TrialRewardCatalogs() {}
    private static Row row(String name,int weight,int quantity,boolean active){return new Row(name,weight,quantity,active);}
    private static Catalog catalog(List<Row> rows){return new Catalog(rows);}
    public record Catalog(List<Row> declared,int declaredWeight,int activeWeight){
        public Catalog(List<Row> rows){this(List.copyOf(rows),rows.stream().mapToInt(Row::weight).sum(),
                rows.stream().filter(Row::active).mapToInt(Row::weight).sum());}
    }
    public record Row(String name,int weight,int quantity,boolean active){}
}
