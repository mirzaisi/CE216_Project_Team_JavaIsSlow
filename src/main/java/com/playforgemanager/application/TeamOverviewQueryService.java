package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Team;

import java.util.List;
import java.util.Objects;

public class TeamOverviewQueryService {
    private final StandingsQueryService standingsQueryService;

    public TeamOverviewQueryService() {
        this(new StandingsQueryService());
    }

    public TeamOverviewQueryService(StandingsQueryService standingsQueryService) {
        this.standingsQueryService = Objects.requireNonNull(
                standingsQueryService,
                "Standings query service cannot be null."
        );
    }

    public TeamOverviewView build(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Team controlledTeam = validatedSession.getControlledTeam();
        List<StandingsRowView> standingsRows = standingsQueryService.build(validatedSession).rows();

        Fixture nextFixture = null;

        // Finds the next unplayed fixture involving the controlled team.
        for (Fixture fixture : validatedSession.getCurrentSeason().getLeague().getFixtures()) {
            if (fixture.isPlayed()) {
                continue;
            }

            if (fixture.getHomeTeam() != controlledTeam && fixture.getAwayTeam() != controlledTeam) {
                continue;
            }

            if (nextFixture == null || fixture.getWeek() < nextFixture.getWeek()) {
                nextFixture = fixture;
            }
        }

        Integer selectedLineupSize = controlledTeam.getSelectedLineup() == null
                ? null
                : controlledTeam.getSelectedLineup().size();

        String selectedTacticName = controlledTeam.getSelectedTactic() == null
                ? null
                : controlledTeam.getSelectedTactic().getName();

        String trainingFocus = controlledTeam.getTrainingPlan() == null
                ? null
                : controlledTeam.getTrainingPlan().getFocus();

        int currentRank = standingsRows.size();

        // Finds the controlled team's current rank from the standings rows.
        for (StandingsRowView row : standingsRows) {
            if (row.teamName().equals(controlledTeam.getName())) {
                currentRank = row.rank();
                break;
            }
        }

        // Builds the display-ready overview for the controlled team.
        return new TeamOverviewView(
                validatedSession.getSelectedSportId(),
                validatedSession.getActiveSport().getName(),
                validatedSession.getCurrentSeason().getLeague().getName(),
                controlledTeam.getName(),
                validatedSession.getCurrentSeason().getCurrentWeek(),
                validatedSession.getCurrentSeason().isCompleted(),
                validatedSession.getProgressionState(),
                currentRank,
                validatedSession.getCurrentSeason().getLeague().getTeamCount(),
                controlledTeam.getRoster().size(),
                QueryViewSupport.countAvailablePlayers(controlledTeam),
                selectedLineupSize,
                selectedTacticName,
                trainingFocus,
                nextFixture == null ? null : QueryViewSupport.toFixtureSummary(nextFixture, controlledTeam)
        );
    }
}
