package com.cosmicpve.trial.madness;

import com.cosmicpve.trial.TrialRuntime;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
import java.util.List;
import net.minecraft.resources.Identifier;

/** Vanilla one-row client; every slot, including player inventory, is display-only during voting. */
public final class MadnessMenu extends ChestMenu {
    private final SimpleContainer display;
    private final ServerPlayer owner;
    private final UUID sessionId;
    private final long ballotSerial;
    private final List<Identifier> ballot;
    public MadnessMenu(int id, Inventory inventory, ServerPlayer owner) {
        this(id, inventory, owner, new SimpleContainer(9));
    }
    private MadnessMenu(int id, Inventory inventory, ServerPlayer owner, SimpleContainer display) {
        super(MenuType.GENERIC_9x1,id,inventory,display,1);
        this.owner = owner; this.display = display;
        var session = TrialRuntime.sessions().active(owner.level().getServer()).orElseThrow();
        sessionId = session.sessionId(); ballotSerial = session.progress().madness().ballotSerial();
        ballot = session.progress().madness().options().stream().map(MadnessDefinition::id).toList();
        refresh();
    }
    public void refresh() {
        var session = TrialRuntime.sessions().active(owner.level().getServer()).orElse(null);
        if (session == null) return;
        var state = session.progress().madness();
        for (int index = 0; index < 9; index++) {
            ItemStack stack = ItemStack.EMPTY;
            if (index < state.options().size()) {
                var option = state.options().get(index);
                stack = new ItemStack(BuiltInRegistries.ITEM.getValue(option.icon()));
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(option.name()).withColor(0x8C1708));
                stack.set(DataComponents.LORE, new net.minecraft.world.item.component.ItemLore(List.of(
                        Component.literal(option.description()).withStyle(style -> style.withColor(0xAAAAAA).withItalic(false)))));
                stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, state.votes().stream()
                        .anyMatch(v -> v.player().equals(owner.getUUID()) && v.option().equals(option.id())));
            }
            display.setItem(index,stack);
        }
        broadcastChanges();
    }
    @Override public void clicked(int slot, int button, ClickType type, Player player) {
        if (button == 0 && type == ClickType.PICKUP && slot >= 0 && slot < ballot.size() && stillValid(player)) {
            TrialRuntime.sessions().voteMadness(owner, sessionId, ballotSerial, ballot, ballot.get(slot));
        }
    }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player player) {
        return TrialRuntime.sessions().active(owner.level().getServer()).filter(s -> s.sessionId().equals(sessionId)
                && s.state() == com.cosmicpve.trial.TrialLifecycleState.DECISION
                && s.activeParticipant(owner.getUUID())
                && s.progress().madness().ballotSerial() == ballotSerial
                && s.progress().madness().owesVote(owner.getUUID())
                && s.progress().madness().options().stream().map(MadnessDefinition::id).toList().equals(ballot)).isPresent();
    }
}
