package com.cosmicpve.trial;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class TrialInsuranceServiceTest {
    private final TrialInsuranceService service=new TrialInsuranceService();
    @Test void emptyAndUndersizedPotsReturnEveryAvailableEntry() {
        assertTrue(service.select(List.of(),3,RandomSource.create(1)).isEmpty());
        var pot=List.of(entry(Items.APPLE,1),entry(Items.DIAMOND,2));
        var selected=service.select(pot,3,RandomSource.create(2));
        assertEquals(2,selected.size()); assertEquals(2,selected.stream().map(TrialPotEntry::acquisitionId).distinct().count());
    }
    @Test void levelsSelectWithoutReplacementAndRetainWholeBundles() {
        var bundle=new TrialPotEntry(UUID.randomUUID(),List.of(new ItemStack(Items.APPLE,2),new ItemStack(Items.CARROT,3)));
        var pot=List.of(bundle,entry(Items.DIAMOND,1),entry(Items.EMERALD,1));
        var selected=service.select(pot,3,RandomSource.create(5));
        assertEquals(3,selected.size()); assertEquals(3,selected.stream().map(TrialPotEntry::acquisitionId).distinct().count());
        var flattened=service.flattenedCopies(List.of(bundle));
        assertEquals(2,flattened.size()); assertEquals(2,flattened.get(0).getCount()); assertEquals(3,flattened.get(1).getCount());
    }
    private static TrialPotEntry entry(net.minecraft.world.item.Item item,int count) {
        return new TrialPotEntry(UUID.randomUUID(),List.of(new ItemStack(item,count)));
    }
}
