package com.cosmicpve.equipment.accessory;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.AccessoryItemData;
import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class AmuletFoundationTest {
    private final AccessoryApplicationService service = new AccessoryApplicationService();

    @Test void dataModelIsVersionedSlotAwareAndRoundTrips() {
        var loadout = AccessoryLoadout.empty().withSocket(AccessorySlot.AMULET)
                .withAttachment(AccessorySlot.AMULET, AmuletDefinition.BLOOD_DIAMOND.id());
        assertTrue(loadout.valid()); assertTrue(loadout.socketed(AccessorySlot.AMULET));
        assertFalse(loadout.socketed(AccessorySlot.BELT));
        var decoded = AccessoryLoadout.CODEC.parse(JsonOps.INSTANCE,
                AccessoryLoadout.CODEC.encodeStart(JsonOps.INSTANCE, loadout).getOrThrow()).getOrThrow();
        assertEquals(loadout, decoded);
        assertTrue(AccessoryLoadout.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(
                "{\"sockets\":[],\"attachments\":{\"amulet\":\"cosmicpve:icicle\"}}"))
                .error().isPresent());
    }

    @Test void socketRatesAreOneToOneHundredAndHaveNoDestroyField() {
        var one = new AccessorySocketData(1, AccessorySlot.AMULET, 1);
        var hundred = new AccessorySocketData(1, AccessorySlot.AMULET, 100);
        assertTrue(one.valid()); assertTrue(hundred.valid());
        String json = AccessorySocketData.CODEC.encodeStart(JsonOps.INSTANCE, hundred).getOrThrow().toString();
        assertTrue(json.contains("success_rate")); assertFalse(json.toLowerCase().contains("destroy"));
        assertTrue(AccessorySocketData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"slot\":\"amulet\",\"success_rate\":0}")).error().isPresent());
    }

    @Test void registrationProvidesOnlyTheRequestedAccessoryItems() {
        assertEquals("cosmicpve:amulet_socket", ModItems.AMULET_SOCKET.getId().toString());
        assertEquals("cosmicpve:blood_diamond_amulet", ModItems.BLOOD_DIAMOND_AMULET.getId().toString());
        assertEquals("cosmicpve:icicle_amulet", ModItems.ICICLE_AMULET.getId().toString());
        assertEquals("cosmicpve:black_heart_amulet", ModItems.BLACK_HEART_AMULET.getId().toString());
        var socketName = new ItemStack(ModItems.AMULET_SOCKET.get()).getHoverName();
        assertEquals("item.cosmicpve.amulet_socket", socketName.getString());
        assertTrue(socketName.getStyle().isBold());
        assertEquals(AccessorySocketItem.COLOR, socketName.getStyle().getColor().getValue());
        assertEquals(0x055251, AccessorySocketItem.COLOR);
    }

    @Test void onlyRealChestplatesAreEligible() {
        assertTrue(AccessoryApplicationService.isChestplate(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        for (var item : List.of(Items.DIAMOND_HELMET, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS,
                Items.DIAMOND_SWORD, Items.ELYTRA))
            assertFalse(AccessoryApplicationService.isChestplate(new ItemStack(item)), item.toString());
    }

    @Test void successfulAndFailedSocketRollsConsumeExactlyOnceAndFailureIsAtomic() {
        var successSocket = socket(100); var armor = metadataChestplate();
        var name = armor.get(DataComponents.CUSTOM_NAME); int damage = armor.getDamageValue();
        assertEquals(AccessoryApplicationService.SocketOutcome.SUCCESS,
                service.socket(successSocket, armor, successSocket, armor, () -> { fail("100% must not roll"); return 1; }));
        assertTrue(successSocket.isEmpty()); assertEquals(name, armor.get(DataComponents.CUSTOM_NAME));
        assertEquals(damage, armor.getDamageValue());
        assertTrue(armor.get(ModDataComponents.ACCESSORY_LOADOUT.get()).socketed(AccessorySlot.AMULET));

        var failedSocket = socket(1); var failedArmor = metadataChestplate(); var before = failedArmor.copy();
        assertEquals(AccessoryApplicationService.SocketOutcome.FAILED,
                service.socket(failedSocket, failedArmor, failedSocket, failedArmor, () -> 100));
        assertTrue(failedSocket.isEmpty()); assertFalse(failedArmor.has(ModDataComponents.ACCESSORY_LOADOUT.get()));
        assertEquals(before.getDamageValue(), failedArmor.getDamageValue());
        assertEquals(before.get(DataComponents.CUSTOM_NAME), failedArmor.get(DataComponents.CUSTOM_NAME));
    }

    @Test void repeatSocketIsRejectedWithoutConsumptionOrReroll() {
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        armor.set(ModDataComponents.ACCESSORY_LOADOUT.get(), AccessoryLoadout.empty().withSocket(AccessorySlot.AMULET));
        var socket = socket(50);
        assertEquals(AccessoryApplicationService.SocketOutcome.ALREADY_SOCKETED,
                service.socket(socket, armor, socket, armor, () -> { fail("repeat must not roll"); return 1; }));
        assertEquals(1, socket.getCount());
    }

    @Test void attachRemoveAndReattachAreAtomicAndSocketIsPermanent() {
        var armor = new ItemStack(Items.DIAMOND_CHESTPLATE);
        var blood = AmuletItemFactory.create(AmuletDefinition.BLOOD_DIAMOND);
        assertEquals(AccessoryApplicationService.AttachOutcome.UNSOCKETED,
                service.attach(blood, armor, blood, armor)); assertEquals(1, blood.getCount());
        armor.set(ModDataComponents.ACCESSORY_LOADOUT.get(), AccessoryLoadout.empty().withSocket(AccessorySlot.AMULET));
        assertEquals(AccessoryApplicationService.AttachOutcome.SUCCESS,
                service.attach(blood, armor, blood, armor)); assertTrue(blood.isEmpty());
        var icicle = AmuletItemFactory.create(AmuletDefinition.ICICLE);
        assertEquals(AccessoryApplicationService.AttachOutcome.ALREADY_ATTACHED,
                service.attach(icicle, armor, icicle, armor)); assertEquals(1, icicle.getCount());
        var removed = service.remove(armor, armor, true);
        assertEquals(AccessoryApplicationService.RemoveOutcome.SUCCESS, removed.outcome());
        assertEquals(AmuletDefinition.BLOOD_DIAMOND.id(),
                removed.returnedAmulet().get(ModDataComponents.ACCESSORY_ITEM.get()).accessoryId());
        assertTrue(armor.get(ModDataComponents.ACCESSORY_LOADOUT.get()).socketed(AccessorySlot.AMULET));
        assertTrue(armor.get(ModDataComponents.ACCESSORY_LOADOUT.get()).attachments().isEmpty());
        assertEquals(AccessoryApplicationService.AttachOutcome.SUCCESS,
                service.attach(icicle, armor, icicle, armor));
    }

    @Test void namesColorsAndCombatConstantsAreCanonical() {
        assertEquals(0x5C0404, AmuletDefinition.BLOOD_DIAMOND.color());
        assertEquals(0xBDF0FF, AmuletDefinition.ICICLE.color());
        assertEquals(0x310082, AmuletDefinition.BLACK_HEART.color());
        for (var value : AmuletDefinition.values()) {
            var name = AmuletItemFactory.create(value).getHoverName();
            assertEquals(value.displayName(), name.getString()); assertTrue(name.getStyle().isBold());
            assertEquals(value.color(), name.getStyle().getColor().getValue());
        }
        assertEquals(0.0, AmuletCombatService.bloodDiamondBonus(0, 0));
        assertEquals(0.08, AmuletCombatService.bloodDiamondBonus(3, 5));
        assertEquals(0.20, AmuletCombatService.bloodDiamondBonus(10, 10));
        assertEquals(0.10, AmuletCombatService.ICICLE_CHANCE);
        assertEquals(80, BlackHeartStateService.DURATION_TICKS);
        assertEquals(0.05, AmuletCombatService.BLACK_HEART_BONUS);
    }

    @Test void blockbenchMappingsAndGeneratedRuntimeResourcesAreComplete() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"));
        Map<String,String> mappings = Map.of(
                "blood_diamond", "blockbench/amulets/blood_diamond/blood_diamond.bbmodel",
                "icicle", "blockbench/amulets/icicle/icicle.bbmodel",
                "black_heart", "blockbench/amulets/blackened_heart/blackened_heart.bbmodel");
        for (var entry : mappings.entrySet()) {
            assertTrue(Files.exists(root.resolve(entry.getValue())));
            assertNotNull(getClass().getResource("/assets/cosmicpve/models/item/amulet/" + entry.getKey() + ".obj"));
            assertNotNull(getClass().getResource("/assets/cosmicpve/models/item/amulet/" + entry.getKey() + ".mtl"));
            BufferedImage image = ImageIO.read(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                    "/assets/cosmicpve/textures/item/amulet/" + entry.getKey() + ".png")));
            assertEquals(32, image.getWidth()); assertEquals(32, image.getHeight());
            try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                    "/assets/cosmicpve/models/item/" + entry.getKey() + "_amulet.json")))) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("neoforge:obj", json.get("loader").getAsString());
                assertTrue(json.get("model").getAsString().endsWith(entry.getKey() + ".obj"));
                assertEquals("cosmicpve:item/amulet/" + entry.getKey(),
                        json.getAsJsonObject("textures").get("particle").getAsString());
                var display = json.getAsJsonObject("display");
                assertTrue(display.has("gui"));
                assertTrue(display.has("firstperson_righthand"));
                assertTrue(display.has("thirdperson_righthand"));
            }
        }
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/amulet_socket.json")))) {
            assertEquals("minecraft:item/tripwire_hook", JsonParser.parseReader(reader).getAsJsonObject()
                    .get("parent").getAsString());
        }
    }

    private static ItemStack socket(int rate) {
        var stack = new ItemStack(ModItems.AMULET_SOCKET.get());
        stack.set(ModDataComponents.ACCESSORY_SOCKET.get(), new AccessorySocketData(1, AccessorySlot.AMULET, rate));
        return stack;
    }
    private static ItemStack metadataChestplate() {
        var stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
        stack.setDamageValue(17); stack.set(DataComponents.CUSTOM_NAME, Component.literal("Keep Me")); return stack;
    }
}
