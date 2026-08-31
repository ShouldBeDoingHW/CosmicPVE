package com.cosmicpve.vkit;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.reward.animation.LootAnimationFeedback;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class GodlyVKitBundleTest {
    @Test void presentationIsCanonical() throws Exception {
        var item = ModItems.GODLY_VKIT_BUNDLE.get();
        var stack = new ItemStack(item);
        assertEquals(1, stack.getMaxStackSize());
        assertTrue(item.isFoil(stack));
        assertEquals("Godly Vkit Bundle!", item.getName(stack).getString());
        var siblings = item.getName(stack).getSiblings();
        assertEquals(2, siblings.size());
        assertEquals(0xF7658D, item.getName(stack).getStyle().getColor().getValue());
        assertEquals(0xF22960, siblings.get(0).getStyle().getColor().getValue());
        assertEquals(0xFA0246, siblings.get(1).getStyle().getColor().getValue());
        assertTrue(item.getName(stack).getStyle().isBold());
        var tooltip = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        item.appendHoverText(stack, null, null, tooltip::add, null);
        assertEquals(1, tooltip.size());
        assertEquals(GodlyVKitBundleItem.LORE, tooltip.getFirst().getString());
        assertEquals(0xFFFF55, tooltip.getFirst().getStyle().getColor().getValue());
        var json = JsonParser.parseString(Files.readString(Path.of(System.getProperty("cosmicpve.projectDir"),
                "src/main/resources/assets/cosmicpve/items/godly_vkit_bundle.json")));
        assertEquals("minecraft:item/red_bundle", json.getAsJsonObject().getAsJsonObject("model").get("model").getAsString());
    }

    @Test void redemptionConsumesOneAndProducesFourDistinctRewardsOnce() {
        ItemStack bundle = new ItemStack(Items.BUNDLE);
        List<ItemStack> rewards = List.of(new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()),
                new ItemStack(ModItems.OGRE_VKIT_CRYSTAL.get()), new ItemStack(ModItems.JUDGEMENT_VKIT_CRYSTAL.get()),
                new ItemStack(ModItems.SLAYER_VKIT_CRYSTAL.get()));
        var delivered = new java.util.ArrayList<ItemStack>();
        var primary = new java.util.concurrent.atomic.AtomicReference<ItemStack>();
        var feedback = new AtomicInteger();
        var result = GodlyVKitBundleItem.commit(bundle, rewards, primary::set, delivered::addAll,
                feedback::incrementAndGet);
        assertTrue(bundle.isEmpty());
        assertEquals(ModItems.PHOENIX_VKIT_CRYSTAL.get(), primary.get().getItem());
        assertEquals(ModItems.PHOENIX_VKIT_CRYSTAL.get(), result.heldItemTransformedTo().getItem());
        assertEquals(List.of(ModItems.OGRE_VKIT_CRYSTAL.get(), ModItems.JUDGEMENT_VKIT_CRYSTAL.get(),
                        ModItems.SLAYER_VKIT_CRYSTAL.get()),
                delivered.stream().map(ItemStack::getItem).toList());
        assertEquals(1, feedback.get());
        assertEquals(0.1F, LootAnimationFeedback.REVEAL_PITCH);
    }
}
