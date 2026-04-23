package com.playforgemanager.application;

import java.util.List;

public record FixtureListView(
        String teamName,
        int currentWeek,
        List<FixtureSummaryView> fixtures
) {
}
