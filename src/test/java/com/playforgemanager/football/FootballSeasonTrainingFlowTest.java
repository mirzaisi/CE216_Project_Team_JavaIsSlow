package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonTrainingFlowTest {
    @Test
    void seasonAppliesTrainingBeforeMatchSimulation() {
        FootballRuleset ruleset = new FootballRuleset();
        FootballTeam home = createTeam("HOME", "Home FC");
        FootballTeam away = createTeam("AWAY", "Away FC");

        home.assignTrainingPlan(new FootballTrainingPlan("Attacking Play", 85, 70, 70, false));
        away.assignTrainingPlan(new FootballTrainingPlan("Balanced", 55, 55, 55, true));
        home.assignLineup(ruleset.buildLineup(home.getAvailablePlayers()), ruleset);
        away.assignLineup(ruleset.buildLineup(away.getAvailablePlayers()), ruleset);

        FootballLeague league = new FootballLeague("Test League");
        league.addTeam(home);
        league.addTeam(away);
        league.getFixtures().add(new Fixture(1, home, away));

        AtomicBoolean trainingObserved = new AtomicBoolean(false);
        FootballSeason season = new FootballSeason(league, new FootballTrainingEffectService());
        season.playCurrentWeek(new AssertingSport(ruleset, trainingObserved), FootballMatch::new);

        assertTrue(trainingObserved.get());
        assertEquals(2, league.getFixtures().get(0).getPlayedMatch().getHomeScore());
        assertEquals(0, league.getFixtures().get(0).getPlayedMatch().getAwayScore());
        assertTrue(season.isCompleted());
    }

    private FootballTeam createTeam(String idPrefix, String name) {
        FootballTeam team = new FootballTeam(idPrefix, name);
        team.addPlayer(player(idPrefix + "-GK", FootballPosition.GOALKEEPER));
        team.addPlayer(player(idPrefix + "-D1", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D2", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D3", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D4", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D5", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-M1", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M2", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M3", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M4", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M5", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-F1", FootballPosition.FORWARD));
        team.addPlayer(player(idPrefix + "-F2", FootballPosition.FORWARD));
        return team;
    }

    private FootballPlayer player(String id, FootballPosition position) {
        int attack = position == FootballPosition.FORWARD ? 75 : 68;
        int defense = position == FootballPosition.DEFENDER ? 76 : 65;
        int stamina = 70;
        int passing = position == FootballPosition.MIDFIELDER ? 75 : 66;
        int speed = position == FootballPosition.FORWARD ? 74 : 67;
        return new FootballPlayer(id, id, position, new FootballAttributeProfile(attack, defense, stamina, passing, speed));
    }

    private static final class AssertingSport implements Sport {
        private final FootballRuleset ruleset;
        private final FootballStandingsPolicy standingsPolicy;
        private final MatchEngine matchEngine;
        private final InjuryPolicy injuryPolicy;

        private AssertingSport(FootballRuleset ruleset, AtomicBoolean trainingObserved) {
            this.ruleset = ruleset;
            this.standingsPolicy = new FootballStandingsPolicy(ruleset);
            this.matchEngine = (match, activeRuleset) -> {
                FootballLineup homeLineup = (FootballLineup) match.getHomeLineup();
                boolean boostedForwardFound = homeLineup.getStartingPlayers().stream()
                        .filter(player -> player.getPosition() == FootballPosition.FORWARD)
                        .anyMatch(player -> player.getEffectiveAttributeProfile().getAttack()
                                > player.getAttributeProfile().getAttack());
                trainingObserved.set(boostedForwardFound);
                match.setResult(2, 0);
            };
            this.injuryPolicy = new FootballInjuryPolicy(2);
        }

        @Override
        public String getName() { return "Football"; }
        @Override
        public Ruleset getRuleset() { return ruleset; }
        @Override
        public Scheduler getScheduler() { return null; }
        @Override
        public StandingsPolicy getStandingsPolicy() { return standingsPolicy; }
        @Override
        public MatchEngine getMatchEngine() { return matchEngine; }
        @Override
        public InjuryPolicy getInjuryPolicy() { return injuryPolicy; }
    }
}
