package com.playforgemanager.application;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class FixtureListQueryService {

    public FixtureListView build(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Team controlledTeam = validatedSession.getControlledTeam();

        return new FixtureListView(
                controlledTeam.getName(),
                validatedSession.getCurrentSeason().getCurrentWeek(),
                QueryViewSupport.buildFixtureSummaries(
                        validatedSession.getCurrentSeason().getLeague().getFixtures(),
                        controlledTeam
                )
        );
    }
}
