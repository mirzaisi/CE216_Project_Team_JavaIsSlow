package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Team;

import java.util.List;

public interface MatchProcessingStrategy {
    Match processMatch(GameSession session, Fixture fixture);

    default List<Team> rankTeams(GameSession session) {
        return session.getActiveSport()
                .getStandingsPolicy()
                .rankTeams(session.getCurrentSeason().getLeague());
    }
}
