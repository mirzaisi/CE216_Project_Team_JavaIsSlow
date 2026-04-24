package com.playforgemanager.application.save;

import com.playforgemanager.application.SportRegistration;
import com.playforgemanager.core.GameSession;

public interface SaveGameRestorer {
    GameSession restore(SaveGameDocument document, SportRegistration registration);
}
