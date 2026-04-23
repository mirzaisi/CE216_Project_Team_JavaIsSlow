package com.playforgemanager.application;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.football.FootballSportFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SportRegistryTest {

    @Test
    void getRegistrationAcceptsSportIdAndDisplayName() {
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(new FakeAssetProvider(), 4)));

        assertEquals("football", registry.getRegistration("football").getSportId());
        assertEquals("football", registry.getRegistration(" Football ").getSportId());
    }

    @Test
    void registerRejectsDuplicateIds() {
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(new FakeAssetProvider(), 4)));

        assertThrows(IllegalArgumentException.class, () ->
                registry.register(new SportRegistration("football", "Association Football",
                        new FootballSportFactory(new FakeAssetProvider(), 4)))
        );
    }

    @Test
    void registerRejectsDuplicateDisplayNames() {
        SportRegistry registry = new SportRegistry()
                .register(new SportRegistration("football", "Football", new FootballSportFactory(new FakeAssetProvider(), 4)));

        assertThrows(IllegalArgumentException.class, () ->
                registry.register(new SportRegistration("soccer", "Football",
                        new FootballSportFactory(new FakeAssetProvider(), 4)))
        );
    }

    private static class FakeAssetProvider implements AssetProvider {
        @Override
        public List<String> getMaleNames() {
            return List.of("Ali", "Mert");
        }

        @Override
        public List<String> getFemaleNames() {
            return List.of("Ece", "Zeynep");
        }

        @Override
        public List<String> getTeamNames() {
            return List.of("Red Hawks", "Blue Wolves", "Golden Stars", "Iron Lions");
        }

        @Override
        public List<String> getLogoReferences() {
            return List.of("logo1", "logo2", "logo3", "logo4");
        }
    }
}
