package com.playforgemanager.core;

import java.util.List;

public interface Scheduler {
    List<Fixture> generateFixtures(List<? extends Team> teams);
}