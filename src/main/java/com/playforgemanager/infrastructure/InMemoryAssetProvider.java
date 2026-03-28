package com.playforgemanager.infrastructure;

import com.playforgemanager.core.AssetProvider;

import java.util.List;

public class InMemoryAssetProvider implements AssetProvider {

    @Override
    public List<String> getMaleNames() {
        return List.of(
                "Ali", "Mert", "Can", "Emir", "Kerem", "Deniz", "Arda", "Bora",
                "Eren", "Kaan", "Yigit", "Baris", "Serkan", "Ozan", "Burak", "Hakan",
                "Tuna", "Umut", "Cem", "Volkan"
        );
    }

    @Override
    public List<String> getFemaleNames() {
        return List.of(
                "Ece", "Zeynep", "Defne", "Elif", "Aylin", "Mina", "Sena", "Yasemin",
                "Derya", "Selin", "Naz", "Irem"
        );
    }

    @Override
    public List<String> getTeamNames() {
        return List.of(
                "Red Hawks",
                "Blue Wolves",
                "Golden Stars",
                "Iron Lions",
                "Silver Falcons",
                "Black Storm"
        );
    }

    @Override
    public List<String> getLogoReferences() {
        return List.of(
                "logo-red-hawks",
                "logo-blue-wolves",
                "logo-golden-stars",
                "logo-iron-lions",
                "logo-silver-falcons",
                "logo-black-storm"
        );
    }
}