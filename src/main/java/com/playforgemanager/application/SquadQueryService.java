package com.playforgemanager.application;

import com.playforgemanager.core.GameSession;
import com.playforgemanager.core.Team;

import java.util.Objects;

public class SquadQueryService {

    public SquadView build(GameSession session) {
        GameSession validatedSession = Objects.requireNonNull(session, "Game session cannot be null.");
        Team controlledTeam = validatedSession.getControlledTeam();

        return new SquadView(
                controlledTeam.getName(),
                controlledTeam.getRoster().size(),
                QueryViewSupport.countAvailablePlayers(controlledTeam),
                QueryViewSupport.buildPlayerSummaries(controlledTeam)
        );
    }
}
