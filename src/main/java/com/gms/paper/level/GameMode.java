package com.gms.paper.level;

import static org.bukkit.GameMode.*;

public enum GameMode {

    S_ADVENTURE(2, ADVENTURE),
    S_DEV(1, CREATIVE);

    public final int value;
    public final org.bukkit.GameMode bukkitGameMode;

    GameMode(int value, org.bukkit.GameMode bukkitGameMode){
        this.value = value;
        this.bukkitGameMode = bukkitGameMode;
    }

    public static GameMode getGameModel(String mode) {
        if (mode.toLowerCase().equals("dev"))
            return S_DEV;
        return S_ADVENTURE;
    }

}
