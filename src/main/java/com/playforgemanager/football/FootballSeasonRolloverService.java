package com.playforgemanager.football;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Team;

import java.util.Objects;

public final class FootballSeasonRolloverService {
    public GameSession rollOver(GameSession completedSession) {
        Objects.requireNonNull(completedSession, "Completed session cannot be null.");

        if (!(completedSession.getCurrentSeason() instanceof FootballSeason completedFootballSeason)) {
            throw new IllegalArgumentException("Football rollover requires a FootballSeason.");
        }

        FootballSeason nextSeason = completedFootballSeason.createNextSeason(completedSession.getActiveSport());
        Team controlledTeam = resolveControlledTeam(nextSeason, completedSession.getControlledTeam());

        return new GameSession(
                completedSession.getActiveSport(),
                nextSeason,
                controlledTeam,
                ProgressionState.READY_TO_START,
                completedSession.getSelectedSportId()
        );
    }

    private Team resolveControlledTeam(FootballSeason nextSeason, Team previousControlledTeam) {
        Objects.requireNonNull(nextSeason, "Next season cannot be null.");
        Objects.requireNonNull(previousControlledTeam, "Previous controlled team cannot be null.");

        return nextSeason.getLeague().getTeams().stream()
                .filter(team -> team.getId().equals(previousControlledTeam.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Controlled team was not carried into the next season."));
    }
}
