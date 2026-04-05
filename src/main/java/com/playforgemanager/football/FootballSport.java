package com.playforgemanager.football;

import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.Lineup;
import com.playforgemanager.core.Match;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Player;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;
import com.playforgemanager.core.Team;

import java.util.Objects;
import java.util.Random;

public class FootballSport implements Sport {
    private final FootballRuleset ruleset;
    private final Scheduler scheduler;
    private final StandingsPolicy standingsPolicy;
    private final MatchEngine matchEngine;
    private final InjuryPolicy injuryPolicy;

    public FootballSport() {
        this.ruleset = new FootballRuleset();
        this.scheduler = new RoundRobinFootballScheduler();
        this.standingsPolicy = new FootballStandingsPolicy(ruleset);
        this.matchEngine = new BasicFootballMatchEngine();
        this.injuryPolicy = new NoOpInjuryPolicy();
    }

    @Override
    public String getName() {
        return "Football";
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }

    public FootballRuleset getFootballRuleset() {
        return ruleset;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public StandingsPolicy getStandingsPolicy() {
        return standingsPolicy;
    }

    @Override
    public MatchEngine getMatchEngine() {
        return matchEngine;
    }

    @Override
    public InjuryPolicy getInjuryPolicy() {
        return injuryPolicy;
    }

    private static class BasicFootballMatchEngine implements MatchEngine {
        @Override
        public void simulate(Match match, Ruleset ruleset) {
            Objects.requireNonNull(match, "Match cannot be null.");
            Objects.requireNonNull(ruleset, "Ruleset cannot be null.");

            if (match.isPlayed()) {
                return;
            }

            validateLineup(match.getHomeLineup(), ruleset, "home");
            validateLineup(match.getAwayLineup(), ruleset, "away");

            long seed = 31L * match.getHomeTeam().getName().hashCode()
                    + match.getAwayTeam().getName().hashCode();
            Random random = new Random(seed);

            int homeScore = random.nextInt(4);
            int awayScore = random.nextInt(4);

            if (random.nextBoolean()) {
                homeScore = Math.min(5, homeScore + 1);
            }

            match.setResult(homeScore, awayScore);
        }

        private void validateLineup(Lineup lineup, Ruleset ruleset, String side) {
            if (lineup == null) {
                throw new IllegalArgumentException("The " + side + " lineup cannot be null.");
            }

            if (ruleset instanceof FootballRuleset footballRuleset) {
                footballRuleset.validateLineupOrThrow(lineup);
                return;
            }

            if (!ruleset.isValidLineup(lineup)) {
                throw new IllegalArgumentException("The " + side + " lineup is invalid.");
            }
        }
    }

    private static class NoOpInjuryPolicy implements InjuryPolicy {
        @Override
        public void applyPostMatch(Match match) {
            Objects.requireNonNull(match, "Match cannot be null.");
        }

        @Override
        public void recoverPlayers(Team team) {
            Objects.requireNonNull(team, "Team cannot be null.");

            for (Player player : team.getRoster()) {
                if (player.getInjuryMatchesRemaining() > 0) {
                    player.recoverOneMatch();
                }
            }
        }
    }
}
