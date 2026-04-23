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

        Fixture nextFixture = validatedSession.getCurrentSeason().getLeague().getFixtures().stream()
                .filter(fixture -> !fixture.isPlayed())
                .filter(fixture -> fixture.getHomeTeam() == controlledTeam || fixture.getAwayTeam() == controlledTeam)
                .min(java.util.Comparator.comparingInt(Fixture::getWeek))
                .orElse(null);

        Integer selectedLineupSize = controlledTeam.getSelectedLineup() == null
                ? null
                : controlledTeam.getSelectedLineup().size();

        String selectedTacticName = controlledTeam.getSelectedTactic() == null
                ? null
                : controlledTeam.getSelectedTactic().getName();

        String trainingFocus = controlledTeam.getTrainingPlan() == null
                ? null
                : controlledTeam.getTrainingPlan().getFocus();

        int currentRank = standingsRows.stream()
                .filter(row -> row.teamName().equals(controlledTeam.getName()))
                .mapToInt(StandingsRowView::rank)
                .findFirst()
                .orElse(standingsRows.size());

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
