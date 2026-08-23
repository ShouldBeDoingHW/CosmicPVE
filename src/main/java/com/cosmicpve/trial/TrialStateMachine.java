package com.cosmicpve.trial;

public final class TrialStateMachine {
    private TrialStateMachine() {}
    public static TrialSession tickGameplayTimer(TrialSession session) {
        if (session.state() != TrialLifecycleState.ROOM_ACTIVE) return session;
        return session.withTimer(Math.max(0, session.timerTicks() - 1));
    }
    public static TrialSession tickStateCountdown(TrialSession session) {
        if (session.state() != TrialLifecycleState.JOINING
                && session.state() != TrialLifecycleState.DECISION
                && session.state() != TrialLifecycleState.ROOM_INTRO) return session;
        return session.withStateTicks(Math.max(0, session.stateTicksRemaining() - 1));
    }
    public static boolean canCompleteRoom(TrialSession session) {
        return session.state() == TrialLifecycleState.ROOM_ACTIVE;
    }
}
