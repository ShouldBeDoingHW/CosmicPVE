package com.cosmicpve.combat.enchantment;

import com.cosmicpve.CosmicPVE;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Runtime proof for Self Destruct's entity-local fuse and protected explosion policy. */
public final class SelfDestructGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, CosmicPVE.MOD_ID);
    static { FUNCTIONS.register("self_destruct_fuse", ignored -> SelfDestructGameTests::verify); }

    private SelfDestructGameTests() {}

    public static void register(IEventBus bus) {
        FUNCTIONS.register(bus);
        bus.addListener(SelfDestructGameTests::registerTests);
    }

    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition> environment = event.registerEnvironment(
                CosmicPVE.id("self_destruct_fuse_environment"), new TestEnvironmentDefinition.AllOf());
        event.registerTest(CosmicPVE.id("self_destruct_fuse"), new FunctionGameTestInstance(
                ResourceKey.create(Registries.TEST_FUNCTION, CosmicPVE.id("self_destruct_fuse")),
                new TestData<>(environment, CosmicPVE.id("trial/development_room"), 100, 0, true,
                        Rotation.NONE, false, 1, 1, false)));
    }

    private static void verify(GameTestHelper helper) {
        var owner = helper.makeMockPlayer(GameType.SURVIVAL);
        var center = helper.absolutePos(new BlockPos(8, 3, 8));
        owner.snapTo(center.getX() + .5, center.getY(), center.getZ() + .5, 0, 0);
        owner.setHealth(owner.getMaxHealth());
        float ownerHealth = owner.getHealth();

        helper.setBlock(new BlockPos(8, 2, 8), Blocks.DIRT);
        helper.assertTrue(SelfDestructBehavior.activate(owner) == 4,
                "Self Destruct must insert exactly four cardinal TNT entities");
        List<PrimedTnt> tnt = helper.getEntities(EntityType.TNT);
        helper.assertTrue(tnt.size() == 4, "Self Destruct must produce exactly four TNT entities");
        for (PrimedTnt entity : tnt) {
            helper.assertTrue(entity.getFuse() == 20, "Every Self Destruct TNT must store a 20-tick fuse");
            helper.assertTrue(SelfDestructBehavior.isEventTnt(entity) && entity.getOwner() == owner,
                    "Every Self Destruct TNT must retain event ownership and tagging");
        }

        PrimedTnt ordinary = new PrimedTnt(helper.getLevel(), center.getX() + 7, center.getY(), center.getZ(), owner);
        helper.assertTrue(ordinary.getFuse() == 80 && !SelfDestructBehavior.isEventTnt(ordinary),
                "Unrelated vanilla TNT must retain its ordinary 80-tick fuse and remain untagged");
        ordinary.discard();

        helper.runAfterDelay(18, () -> helper.assertTrue(tnt.stream().noneMatch(PrimedTnt::isRemoved),
                "Self Destruct TNT must not explode before its 20-tick fuse elapses"));
        helper.runAfterDelay(23, () -> {
            helper.assertTrue(tnt.stream().allMatch(PrimedTnt::isRemoved),
                    "All four Self Destruct TNT must explode after approximately one second");
            helper.assertTrue(owner.getHealth() == ownerHealth,
                    "The Self Destruct caster must remain immune to its four explosions");
            helper.assertBlockPresent(Blocks.DIRT, new BlockPos(8, 2, 8));
            helper.succeed();
        });
    }
}
