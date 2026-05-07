package com.playforgemanager.handball;

import com.playforgemanager.core.InjuryPolicy;
import com.playforgemanager.core.MatchEngine;
import com.playforgemanager.core.Ruleset;
import com.playforgemanager.core.Scheduler;
import com.playforgemanager.core.Sport;
import com.playforgemanager.core.StandingsPolicy;

public class HandballSport implements Sport {
    private final HandballRuleset ruleset;
    private final Scheduler scheduler;
    private final StandingsPolicy standingsPolicy;
    private final MatchEngine matchEngine;
    private final InjuryPolicy injuryPolicy;

    public HandballSport() {
        this.ruleset = new HandballRuleset();
        this.scheduler = new RoundRobinHandballScheduler();
        this.standingsPolicy = new HandballStandingsPolicy(ruleset);
        this.matchEngine = new HandballMatchEngine();
        this.injuryPolicy = new HandballInjuryPolicy();
    }

    @Override
    public String getName() {
        return "Handball";
    }

    @Override
    public Ruleset getRuleset() {
        return ruleset;
    }

    public HandballRuleset getHandballRuleset() {
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
