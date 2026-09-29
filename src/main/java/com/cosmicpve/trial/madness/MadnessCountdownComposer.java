package com.cosmicpve.trial.madness;

import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** The sole Cosmic action-bar exception: active-room timed Madness warnings. */
public final class MadnessCountdownComposer {
    private MadnessCountdownComposer() {}

    public static Component compose(Map<String, Integer> seconds) {
        Component message = Component.empty();
        for (String id : new String[]{"inventory_shuffle", "owl_gene", "statues", "rocket_man"}) {
            Integer count = seconds.get(id);
            if (count == null || count < 1 || count > 3) continue;
            if (!message.getString().isEmpty()) message = message.copy().append(Component.literal(" | "));
            message = message.copy().append(Component.literal(label(id) + ": ").withColor(0x8C1708))
                    .append(Component.literal(Integer.toString(count)).withColor(0xFFFFFF));
        }
        return message;
    }

    private static String label(String id) {
        return switch (id) {
            case "inventory_shuffle" -> "Inventory Shuffle";
            case "owl_gene" -> "Owl Gene";
            case "statues" -> "Statues";
            case "rocket_man" -> "Rocket Man";
            default -> throw new IllegalArgumentException(id);
        };
    }

    public static void send(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
