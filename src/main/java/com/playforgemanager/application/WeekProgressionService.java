package com.playforgemanager.application;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WeekProgressionService {
    private final WeekProgressionRegistry progressionRegistry;

    public WeekProgressionService(WeekProgressionRegistry progressionRegistry) {
        this.progressionRegistry = Objects.requireNonNull(
                progressionRegistry,
                "Week progression registry cannot be null."
        );
    }

    public WeekProgressionResult advanceOneStep(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        if (validatedSession.getCurrentSeason().isCompleted()) {
            throw new IllegalStateException("Current season is already completed.");
        }

        WeekProgressionStrategy strategy = progressionRegistry.getStrategy(validatedSession.getSelectedSportId());
        List<Team> leagueTeams = validatedSession.getCurrentSeason().getLeague().getTeams();
        List<Integer> availabilityBefore = snapshotAvailability(leagueTeams);

        WeekProgressionContext context = strategy.createContext(validatedSession);
        strategy.applyTraining(validatedSession, context);
        strategy.updateAvailability(validatedSession, context);
        strategy.prepareMatches(validatedSession, context);
        strategy.simulateMatches(validatedSession, context);
        strategy.refreshStandings(validatedSession, context);
        strategy.processPostMatch(validatedSession, context);
        strategy.advanceWeek(validatedSession, context);

        if (validatedSession.getCurrentSeason().isCompleted()) {
            validatedSession.markCompleted();
        } else {
            validatedSession.markInProgress();
        }

        return new WeekProgressionResult(
                validatedSession.getSelectedSportId(),
                context.getWeekNumber(),
                validatedSession.getCurrentSeason().getCurrentWeek(),
                validatedSession.getCurrentSeason().isCompleted(),
                validatedSession.getProgressionState(),
                context.getScheduledFixtures(),
                strategy.rankTeams(validatedSession),
                buildAvailabilityChanges(leagueTeams, availabilityBefore)
        );
    }

    private List<Integer> snapshotAvailability(List<Team> teams) {
        List<Integer> snapshot = new ArrayList<>(teams.size());
        for (Team team : teams) {
            snapshot.add(countAvailablePlayers(team));
        }
        return List.copyOf(snapshot);
    }

    private List<TeamAvailabilityChange> buildAvailabilityChanges(List<Team> teams, List<Integer> availabilityBefore) {
        List<TeamAvailabilityChange> changes = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size(); i++) {
            changes.add(new TeamAvailabilityChange(
                    teams.get(i),
                    availabilityBefore.get(i),
                    countAvailablePlayers(teams.get(i))
            ));
        }
        return List.copyOf(changes);
    }

    private int countAvailablePlayers(Team team) {
        int availablePlayers = 0;
        for (Player player : team.getRoster()) {
            if (player.isAvailable()) {
                availablePlayers++;
            }
        }
        return availablePlayers;
    }
}
