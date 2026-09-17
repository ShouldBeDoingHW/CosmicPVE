package com.cosmicpve.equipment.accessory;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.component.AccessoryLoadout;
import com.cosmicpve.data.component.AccessorySlot;
import com.cosmicpve.data.component.AccessorySocketData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class BeltFoundationTest {
    private final AccessoryApplicationService service = new AccessoryApplicationService();

    @Test void concreteBeltSocketAcceptsOnlyLeggingsAndConsumesExactlyOnce() {
        assertTrue(AccessoryApplicationService.isLeggings(new ItemStack(Items.DIAMOND_LEGGINGS)));
        for (var item : List.of(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_BOOTS,
                Items.DIAMOND_SWORD, Items.ELYTRA))
            assertFalse(AccessoryApplicationService.isLeggings(new ItemStack(item)), item.toString());
        var leggings = new ItemStack(Items.DIAMOND_LEGGINGS); var socket = beltSocket(100);
        assertEquals(AccessoryApplicationService.SocketOutcome.SUCCESS,
                service.socket(socket, leggings, socket, leggings, () -> { fail("100% must not roll"); return 1; }));
        assertTrue(socket.isEmpty());
        assertTrue(leggings.get(ModDataComponents.ACCESSORY_LOADOUT.get()).socketed(AccessorySlot.BELT));
        assertFalse(leggings.get(ModDataComponents.ACCESSORY_LOADOUT.get()).socketed(AccessorySlot.AMULET));

        var failedLeggings = new ItemStack(Items.DIAMOND_LEGGINGS); var failed = beltSocket(1);
        assertEquals(AccessoryApplicationService.SocketOutcome.FAILED,
                service.socket(failed, failedLeggings, failed, failedLeggings, () -> 100));
        assertTrue(failed.isEmpty()); assertFalse(failedLeggings.has(ModDataComponents.ACCESSORY_LOADOUT.get()));
    }

    @Test void repeatBeltSocketDoesNotConsumeOrRoll() {
        var leggings = socketedLeggings(); var socket = beltSocket(50);
        assertEquals(AccessoryApplicationService.SocketOutcome.ALREADY_SOCKETED,
                service.socket(socket, leggings, socket, leggings, () -> { fail("repeat must not roll"); return 1; }));
        assertEquals(1, socket.getCount());
    }

    @Test void omniResolvesToConcreteSlotAndNeverPersistsOmniState() {
        var chest = new ItemStack(Items.DIAMOND_CHESTPLATE); var chestOmni = omni(100);
        assertEquals(AccessoryApplicationService.SocketOutcome.SUCCESS,
                service.socket(chestOmni, chest, chestOmni, chest, () -> 100));
        assertEquals(List.of(AccessorySlot.AMULET), chest.get(ModDataComponents.ACCESSORY_LOADOUT.get()).sockets());
        var legs = new ItemStack(Items.DIAMOND_LEGGINGS); var legOmni = omni(100);
        assertEquals(AccessoryApplicationService.SocketOutcome.SUCCESS,
                service.socket(legOmni, legs, legOmni, legs, () -> 100));
        assertEquals(List.of(AccessorySlot.BELT), legs.get(ModDataComponents.ACCESSORY_LOADOUT.get()).sockets());
        var invalid = omni(100); var helmet = new ItemStack(Items.DIAMOND_HELMET);
        assertEquals(AccessoryApplicationService.SocketOutcome.NOT_SUPPORTED_ARMOR,
                service.socket(invalid, helmet, invalid, helmet, () -> 1));
        assertEquals(1, invalid.getCount());
        assertEquals(List.of(AccessorySlot.AMULET, AccessorySlot.BELT), List.of(AccessorySlot.values()));
    }

    @Test void allBeltsAttachRemoveSymmetricallyAndPreserveSocket() {
        for (var definition : BeltDefinition.values()) {
            var leggings = socketedLeggings(); var belt = BeltItemFactory.create(definition);
            assertEquals(AccessoryApplicationService.AttachOutcome.SUCCESS,
                    service.attach(belt, leggings, belt, leggings)); assertTrue(belt.isEmpty());
            var second = BeltItemFactory.create(BeltDefinition.BANDOLIER);
            assertEquals(AccessoryApplicationService.AttachOutcome.ALREADY_ATTACHED,
                    service.attach(second, leggings, second, leggings)); assertEquals(1, second.getCount());
            var removed = service.remove(leggings, leggings, true);
            assertEquals(AccessoryApplicationService.RemoveOutcome.SUCCESS, removed.outcome());
            assertEquals(definition.id(), removed.returnedAmulet().get(ModDataComponents.ACCESSORY_ITEM.get()).accessoryId());
            assertTrue(leggings.get(ModDataComponents.ACCESSORY_LOADOUT.get()).socketed(AccessorySlot.BELT));
            assertTrue(leggings.get(ModDataComponents.ACCESSORY_LOADOUT.get()).attachments().isEmpty());
        }
    }

    @Test void canonicalNamesColorsAndMathAreExact() {
        assertEquals(0x123B07, BeltDefinition.SHOCK_THERAPY.color());
        assertEquals(0x5C4D04, BeltDefinition.BANDOLIER.color());
        assertEquals(0xC999FF, BeltDefinition.JELLY_ROLL.color());
        for (var definition : BeltDefinition.values()) {
            var name = BeltItemFactory.create(definition).getHoverName();
            assertEquals(definition.displayName(), name.getString()); assertTrue(name.getStyle().isBold());
            assertEquals(definition.color(), name.getStyle().getColor().getValue());
        }
        assertEquals(.12, BeltCombatService.BANDOLIER_BONUS);
        assertEquals(.98, BeltCombatService.JELLY_INCOMING);
        assertEquals(1.0, CosmicLightningService.SHOCK_TRUE_DAMAGE);
        assertEquals(.25F, CosmicLightningService.SHOCK_HEAL);
    }

    @Test void suppliedModelsExporterAndRuntimeResourcesAreMappedWithoutCandy() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"));
        Path exporter = root.resolve("tools/export_belt_models.ps1");
        assertTrue(Files.exists(exporter));
        String exporterText = Files.readString(exporter);
        assertTrue(exporterText.contains("(.5-$p[1]/16.0)"),
                "item OBJ must remain centered instead of baking in the worn waist offset");
        String layerText = Files.readString(root.resolve(
                "src/main/java/com/cosmicpve/client/BeltClientPresentation.java"));
        assertTrue(layerText.contains("BODY_TO_WAIST = 12.0 / 16.0"));
        assertTrue(layerText.contains("poseStack.translate(0.0, BODY_TO_WAIST, 0.0)"));
        for (String id : List.of("bandolier", "shock_therapy")) {
            assertNotNull(getClass().getResource("/assets/cosmicpve/models/item/belt/" + id + ".obj"));
            assertNotNull(getClass().getResource("/assets/cosmicpve/models/item/belt/" + id + ".mtl"));
            assertNotNull(getClass().getResource("/assets/cosmicpve/textures/item/belt/" + id + ".png"));
            try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                    "/assets/cosmicpve/models/item/" + id + "_belt.json")))) {
                var json = JsonParser.parseReader(reader).getAsJsonObject();
                assertEquals("neoforge:obj", json.get("loader").getAsString());
                assertFalse(json.get("flip_v").getAsBoolean(),
                        "exported top-down UVs must not mirror lower palette strips into the sigil");
                assertTrue(json.getAsJsonObject("display").has("gui"));
                assertTrue(json.getAsJsonObject("display").has("firstperson_righthand"));
            }
        }
        assertNull(getClass().getResource("/assets/cosmicpve/models/item/belt/candy_buckle.obj"));
    }

    @Test void exportedUvsSampleEveryAuthoredBeltFaceWithoutPaletteMirroring() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"));
        for (String[] mapping : List.of(
                new String[]{"shock_therapy", "shock_therapy/Shock_Therapy.bbmodel"},
                new String[]{"bandolier", "bandolier/bandolier_belt.bbmodel"})) {
            String id = mapping[0];
            var source = JsonParser.parseString(Files.readString(root.resolve(
                    "blockbench/belts/" + mapping[1]))).getAsJsonObject();
            var wrapper = JsonParser.parseString(Files.readString(root.resolve(
                    "src/main/resources/assets/cosmicpve/models/item/" + id + "_belt.json"))).getAsJsonObject();
            var texture = ImageIO.read(java.util.Objects.requireNonNull(getClass().getResource(
                    "/assets/cosmicpve/textures/item/belt/" + id + ".png")));
            var coordinates = Files.readAllLines(root.resolve(
                    "src/main/resources/assets/cosmicpve/models/item/belt/" + id + ".obj"))
                    .stream().filter(line -> line.startsWith("vt ")).map(line -> {
                        String[] values = line.split("\\s+");
                        return new double[]{Double.parseDouble(values[1]), Double.parseDouble(values[2])};
                    }).toList();
            int cursor = 0;
            for (var elementValue : source.getAsJsonArray("elements")) {
                var element = elementValue.getAsJsonObject();
                for (String faceName : List.of("north", "south", "west", "east", "up", "down")) {
                    var face = element.getAsJsonObject("faces").getAsJsonObject(faceName);
                    if (face == null || !face.has("texture") || face.get("texture").isJsonNull()) continue;
                    var uv = face.getAsJsonArray("uv");
                    double u = 0, v = 0;
                    for (int corner = 0; corner < 4; corner++) {
                        u += coordinates.get(cursor)[0] / 4;
                        v += coordinates.get(cursor++)[1] / 4;
                    }
                    if (wrapper.get("flip_v").getAsBoolean()) v = 1 - v;
                    int expectedX = (int)Math.round((uv.get(0).getAsDouble() + uv.get(2).getAsDouble()) / 2);
                    int expectedY = (int)Math.round((uv.get(1).getAsDouble() + uv.get(3).getAsDouble()) / 2);
                    int actualX = (int)Math.round(u * texture.getWidth());
                    int actualY = (int)Math.round(v * texture.getHeight());
                    assertEquals(texture.getRGB(expectedX, expectedY), texture.getRGB(actualX, actualY),
                            id + " " + element.get("name").getAsString() + " " + faceName);
                }
            }
            assertEquals(coordinates.size(), cursor, "every exported UV must belong to an authored face");
        }
    }

    private static ItemStack beltSocket(int rate) {
        var stack = new ItemStack(ModItems.BELT_SOCKET.get());
        stack.set(ModDataComponents.ACCESSORY_SOCKET.get(), new AccessorySocketData(1, AccessorySlot.BELT, rate));
        return stack;
    }
    private static ItemStack omni(int rate) {
        var stack = new ItemStack(ModItems.OMNI_SOCKET.get()); stack.set(ModDataComponents.OMNI_SOCKET_SUCCESS.get(), rate); return stack;
    }
    private static ItemStack socketedLeggings() {
        var stack = new ItemStack(Items.DIAMOND_LEGGINGS);
        stack.set(ModDataComponents.ACCESSORY_LOADOUT.get(), AccessoryLoadout.empty().withSocket(AccessorySlot.BELT)); return stack;
    }
}
