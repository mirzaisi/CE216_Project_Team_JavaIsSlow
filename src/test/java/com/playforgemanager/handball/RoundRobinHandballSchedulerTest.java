package com.playforgemanager.handball;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.Team;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoundRobinHandballSchedulerTest {

    @Test
    void generateFixturesCreatesExpectedCountForFourTeams() {
        RoundRobinHandballScheduler scheduler = new RoundRobinHandballScheduler();

        List<Fixture> fixtures = scheduler.generateFixtures(sampleTeams());

        assertEquals(12, fixtures.size());
    }

    @Test
    void generateFixturesPreventsSelfMatches() {
        RoundRobinHandballScheduler scheduler = new RoundRobinHandballScheduler();

        List<Fixture> fixtures = scheduler.generateFixtures(sampleTeams());

        for (Fixture fixture : fixtures) {
            assertNotEquals(fixture.getHomeTeam(), fixture.getAwayTeam());
        }
    }

    @Test
    void generateFixturesCreatesDoubleRoundRobinPairings() {
        RoundRobinHandballScheduler scheduler = new RoundRobinHandballScheduler();

        List<Fixture> fixtures = scheduler.generateFixtures(sampleTeams());

        Map<String, Long> pairCounts = fixtures.stream()
                .collect(Collectors.groupingBy(
                        fixture -> normalizedPair(fixture.getHomeTeam(), fixture.getAwayTeam()),
                        Collectors.counting()
                ));

        assertEquals(6, pairCounts.size());
        assertTrue(pairCounts.values().stream().allMatch(count -> count == 2));
    }

    @Test
    void generateFixturesBalancesHomeAndAwayCountsForFourTeams() {
        RoundRobinHandballScheduler scheduler = new RoundRobinHandballScheduler();

        List<Fixture> fixtures = scheduler.generateFixtures(sampleTeams());

        for (Team team : sampleTeams()) {
            long homeCount = fixtures.stream().filter(fixture -> fixture.getHomeTeam().equals(team)).count();
            long awayCount = fixtures.stream().filter(fixture -> fixture.getAwayTeam().equals(team)).count();
            assertEquals(homeCount, awayCount);
        }
    }

    private List<Team> sampleTeams() {
        return List.of(
                new HandballTeam("team-1", "Red Hawks"),
                new HandballTeam("team-2", "Blue Wolves"),
                new HandballTeam("team-3", "Golden Stars"),
                new HandballTeam("team-4", "Iron Lions")
        );
    }

    private String normalizedPair(Team first, Team second) {
        return first.getName().compareTo(second.getName()) <= 0
                ? first.getName() + " vs " + second.getName()
                : second.getName() + " vs " + first.getName();
    }
}
