package com.playforgemanager.football;

import com.playforgemanager.core.Fixture;
import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FootballSeasonCoachTrainingImpactTest {
    @Test
    void seasonProgressionAppliesCoachInfluencedTrainingBeforeSimulation() {
        FootballRuleset ruleset = new FootballRuleset();
        FootballTeam home = createTeam("HOME", "Home FC");
        FootballTeam away = createTeam("AWAY", "Away FC");

        home.assignTrainingPlan(new FootballTrainingPlan("Attacking Play", 75, 60, 60, false));
        home.addCoach(new FootballCoach("coach-attack", "Attack Coach", "Head Coach", "Attacking Play", 90));
        away.assignTrainingPlan(new FootballTrainingPlan("Balanced", 55, 55, 55, false));

        home.assignLineup(ruleset.buildLineup(home.getAvailablePlayers()), ruleset);
        away.assignLineup(ruleset.buildLineup(away.getAvailablePlayers()), ruleset);

        FootballLeague league = new FootballLeague("Coach Impact League");
        league.addTeam(home);
        league.addTeam(away);
        league.getFixtures().add(new Fixture(1, home, away));

        AtomicBoolean coachBoostObserved = new AtomicBoolean(false);
        FootballSeason season = new FootballSeason(league, new FootballTrainingEffectService());
        season.playCurrentWeek(new CoachAwareSport(ruleset, coachBoostObserved), FootballMatch::new);

        assertTrue(coachBoostObserved.get());
    }

    private FootballTeam createTeam(String idPrefix, String name) {
        FootballTeam team = new FootballTeam(idPrefix, name);
        team.addPlayer(player(idPrefix + "-GK", FootballPosition.GOALKEEPER));
        team.addPlayer(player(idPrefix + "-D1", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D2", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D3", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-D4", FootballPosition.DEFENDER));
        team.addPlayer(player(idPrefix + "-M1", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M2", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M3", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-M4", FootballPosition.MIDFIELDER));
        team.addPlayer(player(idPrefix + "-F1", FootballPosition.FORWARD));
        team.addPlayer(player(idPrefix + "-F2", FootballPosition.FORWARD));
        return team;
    }

    private FootballPlayer player(String id, FootballPosition position) {
        return new FootballPlayer(id, id, position, new FootballAttributeProfile(70, 70, 70, 70, 70));
    }

    private static final class CoachAwareSport implements Sport {
        private final FootballRuleset ruleset;
        private final FootballStandingsPolicy standingsPolicy;
        private final MatchEngine matchEngine;
        private final InjuryPolicy injuryPolicy;

        private CoachAwareSport(FootballRuleset ruleset, AtomicBoolean coachBoostObserved) {
            this.ruleset = ruleset;
            this.standingsPolicy = new FootballStandingsPolicy(ruleset);
            this.matchEngine = (match, activeRuleset) -> {
                FootballLineup homeLineup = (FootballLineup) match.getHomeLineup();
                boolean strongCoachBoostFound = homeLineup.getStartingPlayers().stream()
                        .filter(player -> player.getPosition() == FootballPosition.FORWARD)
                        .anyMatch(player -> player.getEffectiveAttributeProfile().getAttack()
                                >= player.getAttributeProfile().getAttack() + 7);
                coachBoostObserved.set(strongCoachBoostFound);
                match.setResult(1, 0);
            };
            this.injuryPolicy = new FootballInjuryPolicy(1);
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
