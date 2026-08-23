package com.cosmicpve.trial.room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.RandomSource;

public final class RaidingRainbowLogic {
    private RaidingRainbowLogic() {}
    public static List<RainbowColor> shuffled(RandomSource random) {
        var result = new ArrayList<>(Arrays.asList(RainbowColor.values()));
        for (int i = result.size() - 1; i > 0; i--) java.util.Collections.swap(result, i, random.nextInt(i + 1));
        return List.copyOf(result);
    }
    public static Step evaluate(List<RainbowColor> sequence, int progress, RainbowColor killed) {
        if (sequence.size() != RainbowColor.values().length || progress < 0 || progress >= sequence.size())
            throw new IllegalArgumentException("Invalid Raiding Rainbow sequence state");
        boolean correct = sequence.get(progress) == killed;
        int next = correct ? progress + 1 : 0;
        return new Step(correct, correct && next == sequence.size(), next);
    }
    public record Step(boolean correct, boolean complete, int progress) {}
}
