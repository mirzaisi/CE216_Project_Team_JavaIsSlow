package com.playforgemanager.football;

import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;

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
        this.matchEngine = new FootballMatchEngine();
        this.injuryPolicy = new FootballInjuryPolicy();
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
}
