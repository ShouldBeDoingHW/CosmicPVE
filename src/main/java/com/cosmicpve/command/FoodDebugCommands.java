package com.cosmicpve.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** Narrow development aid for observing Nutrition's exact hunger/saturation units. */
public final class FoodDebugCommands {
    private FoodDebugCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> create() {
        return Commands.literal("food")
                .then(Commands.literal("show").executes(context -> show(context.getSource())))
                .then(Commands.literal("set")
                        .then(Commands.argument("hunger", IntegerArgumentType.integer(0, 20))
                                .then(Commands.argument("saturation", FloatArgumentType.floatArg(0.0F, 20.0F))
                                        .executes(context -> set(
                                                context.getSource(),
                                                IntegerArgumentType.getInteger(context, "hunger"),
                                                FloatArgumentType.getFloat(context, "saturation"))))));
    }

    private static int show(net.minecraft.commands.CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var food = source.getPlayerOrException().getFoodData();
        source.sendSuccess(() -> Component.literal(
                "Food: hunger=" + food.getFoodLevel() + ", saturation=" + food.getSaturationLevel()), false);
        return 1;
    }

    private static int set(net.minecraft.commands.CommandSourceStack source, int hunger, float saturation)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var food = source.getPlayerOrException().getFoodData();
        food.setFoodLevel(hunger);
        food.setSaturation(Math.min(saturation, hunger));
        source.sendSuccess(() -> Component.literal(
                "Food: hunger=" + food.getFoodLevel() + ", saturation=" + food.getSaturationLevel()), false);
        return 1;
    }
}
