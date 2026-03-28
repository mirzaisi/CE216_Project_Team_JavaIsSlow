package com.playforgemanager.core;

import java.util.List;

public interface Lineup {
    List<? extends Player> getSelectedPlayers();
    int size();
}