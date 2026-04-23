package com.playforgemanager.application;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.ProgressionState;
import com.playforgemanager.core.Tactic;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MatchProcessingService {
    private final MatchProcessingRegistry processingRegistry;

    public MatchProcessingService(MatchProcessingRegistry processingRegistry) {
        this.processingRegistry = Objects.requireNonNull(
                processingRegistry,
                "Match processing registry cannot be null."
        );
    }

    public MatchProcessingResult playControlledMatch(GameSession session, Lineup lineup, Tactic tactic) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Team controlledTeam = validatedSession.getControlledTeam();
        controlledTeam.setSelectedLineup(Objects.requireNonNull(lineup, "Lineup cannot be null."));
        controlledTeam.setSelectedTactic(Objects.requireNonNull(tactic, "Tactic cannot be null."));

        Fixture targetFixture = findControlledTeamFixture(validatedSession);
        if (targetFixture.isPlayed()) {
            throw new IllegalStateException("The controlled team's current fixture has already been played.");
        }

        List<Team> involvedTeams = List.of(targetFixture.getHomeTeam(), targetFixture.getAwayTeam());
        List<Integer> availabilityBefore = snapshotAvailability(involvedTeams);

        MatchProcessingStrategy strategy = processingRegistry.getStrategy(validatedSession.getSelectedSportId());
        Match match = strategy.processMatch(validatedSession, targetFixture);
        if (!match.isPlayed()) {
            throw new IllegalStateException("Processed match must be played.");
        }

        validatedSession.markInProgress();

        boolean controlledTeamHome = match.getHomeTeam() == controlledTeam;
        String opponentName = controlledTeamHome
                ? match.getAwayTeam().getName()
                : match.getHomeTeam().getName();
        int controlledScore = controlledTeamHome ? match.getHomeScore() : match.getAwayScore();
        int opponentScore = controlledTeamHome ? match.getAwayScore() : match.getHomeScore();
        List<Team> rankedTeams = strategy.rankTeams(validatedSession);

        return new MatchProcessingResult(
                validatedSession.getSelectedSportId(),
                targetFixture.getWeek(),
                targetFixture,
                controlledTeam.getName(),
                opponentName,
                controlledTeamHome,
                controlledScore,
                opponentScore,
                rankedTeams,
                findRank(rankedTeams, controlledTeam),
                buildAvailabilityChanges(involvedTeams, availabilityBefore),
                validatedSession.getProgressionState()
        );
    }

    private Fixture findControlledTeamFixture(GameSession session) {
        Team controlledTeam = session.getControlledTeam();
        int currentWeek = session.getCurrentSeason().getCurrentWeek();
        List<Fixture> fixtures = session.getCurrentSeason().getLeague().getFixtures().stream()
                .filter(fixture -> fixture.getWeek() == currentWeek)
                .filter(fixture -> fixture.getHomeTeam() == controlledTeam || fixture.getAwayTeam() == controlledTeam)
                .toList();

        if (fixtures.isEmpty()) {
            throw new IllegalStateException("No current-week fixture found for the controlled team.");
        }
        if (fixtures.size() > 1) {
            throw new IllegalStateException("Controlled team has more than one fixture in the current week.");
        }

        return fixtures.get(0);
    }

    private List<Integer> snapshotAvailability(List<Team> teams) {
        List<Integer> counts = new ArrayList<>(teams.size());
        for (Team team : teams) {
            counts.add(countAvailablePlayers(team));
        }
        return List.copyOf(counts);
    }

    private List<TeamAvailabilityChange> buildAvailabilityChanges(List<Team> teams, List<Integer> beforeCounts) {
        List<TeamAvailabilityChange> changes = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            changes.add(new TeamAvailabilityChange(
                    teams.get(i),
                    beforeCounts.get(i),
                    countAvailablePlayers(teams.get(i))
            ));
        }
        return List.copyOf(changes);
    }

    private int countAvailablePlayers(Team team) {
        int availableCount = 0;
        for (Player player : team.getRoster()) {
            if (player.isAvailable()) {
                availableCount++;
            }
        }
        return availableCount;
    }

    private int findRank(List<Team> rankedTeams, Team controlledTeam) {
        for (int i = 0; i < rankedTeams.size(); i++) {
            if (rankedTeams.get(i) == controlledTeam) {
                return i + 1;
            }
        }
        throw new IllegalStateException("Controlled team must appear in ranked teams.");
    }
}
