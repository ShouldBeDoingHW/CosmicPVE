package com.cosmicpve.instance.protection;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class InstanceProtectionServiceTest {
    private static final UUID SESSION=UUID.randomUUID();
    private static final Identifier ROOM=Identifier.parse("cosmicpve:trial/test");
    @Test void defaultPolicyDeniesRepresentativeMutationSources() {
        for(var cause:List.of(InstanceMutationCause.BREAK,InstanceMutationCause.PLACE,InstanceMutationCause.USE,InstanceMutationCause.EXPLOSION,
                InstanceMutationCause.MOB_GRIEF,InstanceMutationCause.PISTON,InstanceMutationCause.FIRE,InstanceMutationCause.FLUID)) {
            assertTrue(InstanceProtectionService.shouldDeny(true,false,SESSION,ROOM,cause,BlockPos.ZERO,List.of()),cause.toString());
        }
    }
    @Test void creativeDebugBypassAndOutsidePositionsAreAllowed() {
        assertFalse(InstanceProtectionService.shouldDeny(true,true,SESSION,ROOM,InstanceMutationCause.BREAK,BlockPos.ZERO,List.of()));
        assertFalse(InstanceProtectionService.shouldDeny(false,false,SESSION,ROOM,InstanceMutationCause.BREAK,BlockPos.ZERO,List.of()));
    }
    @Test void narrowRoomPolicyCanAllowOneInteractionWithoutOpeningOthers() {
        InstanceProtectionPolicy bombWall=(session,room,cause,pos) -> session.equals(SESSION)
                && cause==InstanceMutationCause.EXPLOSION && pos.equals(new BlockPos(4,5,6));
        assertFalse(InstanceProtectionService.shouldDeny(true,false,SESSION,ROOM,InstanceMutationCause.EXPLOSION,
                new BlockPos(4,5,6),List.of(bombWall)));
        assertTrue(InstanceProtectionService.shouldDeny(true,false,SESSION,ROOM,InstanceMutationCause.BREAK,
                new BlockPos(4,5,6),List.of(bombWall)));
    }
}
