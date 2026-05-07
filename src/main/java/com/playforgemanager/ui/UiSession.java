package com.playforgemanager.ui;

import com.playforgemanager.application.MatchProcessingResult;
import com.playforgemanager.application.WeekProgressionResult;
import com.playforgemanager.core.GameSession;

public final class UiSession {
    private GameSession activeSession;
    private MatchProcessingResult lastMatchResult;
    private WeekProgressionResult lastWeekResult;

    public boolean hasSession() {
        return activeSession != null;
    }

    public GameSession getActiveSession() {
        if (activeSession == null) {
            throw new IllegalStateException("No active session.");
        }
        return activeSession;
    }

    public void setActiveSession(GameSession session) {
        this.activeSession = session;
        this.lastMatchResult = null;
        this.lastWeekResult = null;
    }

    public void clear() {
        this.activeSession = null;
        this.lastMatchResult = null;
        this.lastWeekResult = null;
    }

    public MatchProcessingResult getLastMatchResult() {
        return lastMatchResult;
    }

    public void setLastMatchResult(MatchProcessingResult lastMatchResult) {
        this.lastMatchResult = lastMatchResult;
    }

    public WeekProgressionResult getLastWeekResult() {
        return lastWeekResult;
    }

    public void setLastWeekResult(WeekProgressionResult lastWeekResult) {
        this.lastWeekResult = lastWeekResult;
    }
}
