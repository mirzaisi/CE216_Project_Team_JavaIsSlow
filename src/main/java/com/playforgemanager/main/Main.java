package com.playforgemanager.main;

import com.playforgemanager.core.AssetProvider;
import com.playforgemanager.core.SportFactory;
import com.playforgemanager.football.BootstrapFootballConsoleDemo;
import com.playforgemanager.football.FootballSportFactory;
import com.playforgemanager.infrastructure.InMemoryAssetProvider;

public class Main {
    public static void main(String[] args) {
        AssetProvider assetProvider = new InMemoryAssetProvider();
        SportFactory sportFactory = new FootballSportFactory(assetProvider, 4);

        BootstrapFootballConsoleDemo demo = new BootstrapFootballConsoleDemo();
        demo.run(sportFactory, "PlayForge Demo League");
    }
}