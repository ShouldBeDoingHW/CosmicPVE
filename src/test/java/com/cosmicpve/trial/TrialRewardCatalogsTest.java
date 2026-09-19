package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrialRewardCatalogsTest {
    @Test void newRewardDescriptorsValidateAndRoundTripWithoutInventingItems() {
        var codec = com.cosmicpve.content.definition.reward.RewardDescriptorData.CODEC;
        for (String json : List.of("{\"type\":\"random_trial_trinket\",\"trinket_tier\":3}",
                "{\"type\":\"accessory_socket\",\"item\":\"cosmicpve:amulet_socket\",\"success_rate\":25}",
                "{\"type\":\"accessory_socket\",\"item\":\"cosmicpve:belt_socket\",\"success_rate\":65}",
                "{\"type\":\"accessory_socket\",\"item\":\"cosmicpve:omni_socket\",\"success_rate\":50}")) {
            var data = codec.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
            assertTrue(data.resolve("test", net.minecraft.core.RegistryAccess.EMPTY).isSuccess());
            assertEquals(data, codec.parse(com.mojang.serialization.JsonOps.INSTANCE,
                    codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, data).getOrThrow()).getOrThrow());
        }
        for (String json : List.of("{\"type\":\"random_trial_trinket\",\"trinket_tier\":0}",
                "{\"type\":\"random_trial_trinket\",\"trinket_tier\":4}", "{\"type\":\"random_trial_trinket\"}",
                "{\"type\":\"accessory_socket\",\"item\":\"minecraft:diamond\",\"success_rate\":25}",
                "{\"type\":\"accessory_socket\",\"item\":\"cosmicpve:belt_socket\",\"success_rate\":101}")) {
            var data = codec.parse(com.mojang.serialization.JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
            assertFalse(data.resolve("test", net.minecraft.core.RegistryAccess.EMPTY).isSuccess());
        }
    }
    @Test void catalogsExactlyMatchTheCurrentDesignCanonicalRewardSection() throws Exception {
        String design = java.nio.file.Files.readString(java.nio.file.Path.of(System.getProperty("cosmicpve.projectDir"), "docs", "Cosmic_Design.md"));
        String canonical = design.substring(design.indexOf("# Canonical Reward Catalogs"));
        var catalogs = java.util.Map.of("Apprentice", TrialRewardCatalogs.APPRENTICE, "Hardcore", TrialRewardCatalogs.HARDCORE,
                "Impossible", TrialRewardCatalogs.IMPOSSIBLE, "Demonic", TrialRewardCatalogs.DEMONIC);
        for (var entry : catalogs.entrySet()) {
            String section = canonical.split("## " + entry.getKey() + "\\R", 2)[1].split("\\R## ", 2)[0];
            var rows = new java.util.ArrayList<TrialRewardCatalogs.Row>();
            for (String line : section.lines().toList()) {
                if (!line.startsWith("|")) continue;
                String[] cells = line.split("\\|", -1);
                if (cells.length < 5 || !cells[2].trim().matches("\\d+")) continue;
                String name = cells[1].trim().replace("†", "");
                rows.add(new TrialRewardCatalogs.Row(name, Integer.parseInt(cells[2].trim()),
                        Integer.parseInt(cells[3].trim()), !name.contains("Abandoned Spaceship Portal")));
            }
            assertEquals(rows, entry.getValue().declared(), entry.getKey());
        }
    }
    @Test void canonicalDeclaredAndActiveTotalsArePinned() {
        assertCatalog(TrialRewardCatalogs.APPRENTICE,14,101,101);
        assertCatalog(TrialRewardCatalogs.HARDCORE,14,87,82);
        assertCatalog(TrialRewardCatalogs.IMPOSSIBLE,21,183,175);
        assertCatalog(TrialRewardCatalogs.DEMONIC,19,137,133);
    }

    @Test void runtimeTablesExactlyEqualTheirActiveCanonicalRows() throws Exception {
        assertRuntime("apprentice",TrialRewardCatalogs.APPRENTICE);
        assertRuntime("hardcore_development",TrialRewardCatalogs.HARDCORE);
        assertRuntime("impossible",TrialRewardCatalogs.IMPOSSIBLE);
        assertRuntime("demonic_development",TrialRewardCatalogs.DEMONIC);
    }

    @Test void onlyUnavailablePortalRowsAreInactive() {
        for(var catalog:List.of(TrialRewardCatalogs.APPRENTICE,TrialRewardCatalogs.HARDCORE,
                TrialRewardCatalogs.IMPOSSIBLE,TrialRewardCatalogs.DEMONIC))
            assertTrue(catalog.declared().stream().filter(row->!row.active()).allMatch(row->
                    row.name().contains("Abandoned Spaceship Portal")));
    }

    @Test void activeRunPhaseAloneSelectsTheRewardTable() {
        assertEquals(TrialSessionService.APPRENTICE_REWARDS,TrialSessionService.rewardTableFor(TrialPhase.APPRENTICE));
        assertEquals(TrialSessionService.HARDCORE_REWARDS,TrialSessionService.rewardTableFor(TrialPhase.HARDCORE));
        assertEquals(TrialSessionService.IMPOSSIBLE_REWARDS,TrialSessionService.rewardTableFor(TrialPhase.IMPOSSIBLE));
        assertEquals(TrialSessionService.DEMONIC_REWARDS,TrialSessionService.rewardTableFor(TrialPhase.DEMONIC));
    }

    private static void assertCatalog(TrialRewardCatalogs.Catalog catalog,int rows,int declared,int active){
        assertEquals(rows,catalog.declared().size()); assertEquals(declared,catalog.declaredWeight());
        assertEquals(active,catalog.activeWeight());
    }

    private void assertRuntime(String file,TrialRewardCatalogs.Catalog catalog) throws Exception {
        try(var stream=getClass().getClassLoader().getResourceAsStream(
                "data/cosmicpve/cosmicpve/reward_tables/trial/"+file+".json")){
            assertNotNull(stream);
            var entries=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("entries");
            var actual=entries.asList().stream().map(value->row(value.getAsJsonObject())).toList();
            var expected=catalog.declared().stream().filter(TrialRewardCatalogs.Row::active).toList();
            assertEquals(expected,actual,file);
        }
    }

    private static TrialRewardCatalogs.Row row(JsonObject entry){
        int quantity=entry.has("minimum_quantity")?entry.get("minimum_quantity").getAsInt():1;
        assertEquals(quantity,entry.has("maximum_quantity")?entry.get("maximum_quantity").getAsInt():1);
        JsonObject reward=entry.getAsJsonObject("reward"); String type=reward.get("type").getAsString();
        String name=switch(type){
            case "unexamined_book"->"Unexamined "+title(reward.get("rarity").getAsString())+" Enchantment Book";
            case "space_chest"->title(reward.get("rarity").getAsString())+" Space Chest";
            case "mask"->reward.get("mask_count").getAsInt()==2?"Random Double Mask":"Random Mask";
            case "black_scroll"->reward.get("success_rate").getAsInt()+"% Black Scroll";
            case "enchanted_black_scroll"->reward.get("success_rate").getAsInt()+"% Enchanted Black Scroll";
            case "weapon_orb"->reward.get("success_rate").getAsInt()+"% Weapon Enchantment Orb";
            case "armor_orb"->reward.get("success_rate").getAsInt()+"% Armor Enchantment Orb";
            case "random_vkit_crystal"->"Random V-Kit Crystal";
            case "random_trial_trinket"->"Random Tier "+reward.get("trinket_tier").getAsInt()+" Trial Trinket";
            case "accessory_socket"->reward.get("success_rate").getAsInt()+"% "+title(reward.get("item").getAsString().split(":")[1]);
            case "armor_set_crystal"->reward.get("success_rate").getAsInt()+"% "+
                    title(reward.get("armor_set").getAsString().split(":")[1])+" Crystal";
            case "static_item"->staticName(reward.get("item").getAsString().split(":")[1]);
            default->throw new AssertionError("Unhandled runtime Trial reward descriptor "+type);
        };
        return new TrialRewardCatalogs.Row(name,entry.get("weight").getAsInt(),quantity,true);
    }

    private static String staticName(String path){return switch(path){
        case "mystery_call_of_adventure"->"Mystery Call of Adventure";
        case "random_weapon_skin_generator"->"Random Weapon Skin Generator";
        case "repair_scroll"->"Repair Scroll"; case "white_scroll"->"White Scroll";
        case "transmog_scroll"->"Transmog Scroll"; case "mystery_simple_spawner"->"Mystery Simple Spawner";
        case "mystery_elite_spawner"->"Mystery Elite Spawner"; case "space_dust_bundle"->"Space Dust Bundle";
        case "trial_trinket_skip_1"->"Skip 1 Room Trial Trinket"; case "trial_trinket_skip_2"->"Skip 2 Room Trial Trinket";
        case "trial_trinket_skip_3"->"Skip 3 Room Trial Trinket"; case "trial_trinket_time_1"->"+1 Minute Trial Trinket";
        case "trial_trinket_time_3"->"+3 Minute Trial Trinket"; case "trial_trinket_time_5"->"+5 Minute Trial Trinket";
        case "trial_trinket_insurance_1"->"+1 Insurance Trial Trinket";
        case "trial_trinket_insurance_2"->"+2 Insurance Trial Trinket";
        case "trial_trinket_insurance_3"->"+3 Insurance Trial Trinket";
        case "trial_trinket_fame_33"->"+33% Fame Trial Trinket";
        case "trial_trinket_fame_66"->"+66% Fame Trial Trinket";
        case "trial_trinket_fame_100"->"+100% Fame Trial Trinket";
        case "trial_trinket_madness_1"->"+1 Madness Ballot Trial Trinket";
        case "trial_trinket_madness_2"->"+2 Madness Ballot Trial Trinket";
        case "trial_trinket_madness_3"->"+3 Madness Ballot Trial Trinket";
        case "conquest_chest_flare"->"Conquest Chest Flare"; case "mask_splicer"->"Mask Splicer";
        case "heroic_crystal"->"Heroic Crystal"; case "cosmic_enchantment_table"->"Cosmic Enchantment Table";
        case "heroic_cosmic_enchantment_table"->"Heroic Cosmic Enchantment Table";
        case "secret_weapon_cache"->"Secret Weapon Cache"; case "admin_abuse"->"Admin Abuse";
        case "godly_vkit_bundle"->"Godly V-Kit Bundle"; default->throw new AssertionError("Unknown item "+path);
    };}
    private static String title(String value){return java.util.Arrays.stream(value.split("_"))
            .map(part->Character.toUpperCase(part.charAt(0))+part.substring(1)).collect(java.util.stream.Collectors.joining(" "));}
}
