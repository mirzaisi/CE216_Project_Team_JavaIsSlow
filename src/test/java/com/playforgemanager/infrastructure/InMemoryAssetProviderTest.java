package com.playforgemanager.infrastructure;

import com.playforgemanager.core.AssetProvider;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryAssetProviderTest {

    @Test
    void provideesNonEmptyMaleNames() {
        AssetProvider provider = new InMemoryAssetProvider();

        List<String> names = provider.getMaleNames();

        assertFalse(names.isEmpty());
        assertTrue(names.size() >= 10);
        for (String name : names) {
            assertFalse(name == null || name.isBlank());
        }
    }

    @Test
    void providesNonEmptyFemaleNames() {
        AssetProvider provider = new InMemoryAssetProvider();

        List<String> names = provider.getFemaleNames();

        assertFalse(names.isEmpty());
        assertTrue(names.size() >= 5);
        for (String name : names) {
            assertFalse(name == null || name.isBlank());
        }
    }

    @Test
    void providesEnoughTeamNamesForMinimumLeague() {
        AssetProvider provider = new InMemoryAssetProvider();

        List<String> teams = provider.getTeamNames();

        assertTrue(teams.size() >= 4);
        for (String team : teams) {
            assertFalse(team == null || team.isBlank());
        }
    }

    @Test
    void teamNamesAreUnique() {
        AssetProvider provider = new InMemoryAssetProvider();

        List<String> teams = provider.getTeamNames();
        Set<String> unique = new HashSet<>(teams);

        assertEquals(teams.size(), unique.size());
    }

    @Test
    void providesLogoReferencesForEachTeamName() {
        AssetProvider provider = new InMemoryAssetProvider();

        List<String> teams = provider.getTeamNames();
        List<String> logos = provider.getLogoReferences();

        assertFalse(logos.isEmpty());
        assertTrue(logos.size() >= teams.size());
        for (String logo : logos) {
            assertFalse(logo == null || logo.isBlank());
        }
    }

    @Test
    void maleAndFemaleNameListsDoNotOverlap() {
        AssetProvider provider = new InMemoryAssetProvider();

        Set<String> males = new HashSet<>(provider.getMaleNames());
        Set<String> females = new HashSet<>(provider.getFemaleNames());

        males.retainAll(females);
        assertTrue(males.isEmpty());
    }
}
