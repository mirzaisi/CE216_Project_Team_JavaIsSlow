package com.playforgemanager.application;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Team;

import java.util.List;

public interface WeekProgressionStrategy {
    WeekProgressionContext createContext(GameSession session);

    default void applyTraining(GameSession session, WeekProgressionContext context) {
    }

    default void updateAvailability(GameSession session, WeekProgressionContext context) {
    }

    void prepareMatches(GameSession session, WeekProgressionContext context);

    void simulateMatches(GameSession session, WeekProgressionContext context);

    default void refreshStandings(GameSession session, WeekProgressionContext context) {
    }

    default void processPostMatch(GameSession session, WeekProgressionContext context) {
    }

    void advanceWeek(GameSession session, WeekProgressionContext context);

    default List<Team> rankTeams(GameSession session) {
        return session.getActiveSport()
                .getStandingsPolicy()
                .rankTeams(session.getCurrentSeason().getLeague());
    }
}
