package com.playforgemanager.core;

import java.util.List;

public interface AssetProvider {
    List<String> getMaleNames();
    List<String> getFemaleNames();
    List<String> getTeamNames();
    List<String> getLogoReferences();
}