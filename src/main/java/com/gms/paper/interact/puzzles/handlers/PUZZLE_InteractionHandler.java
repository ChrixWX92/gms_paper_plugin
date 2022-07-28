package com.gms.paper.interact.puzzles.handlers;

import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import com.gms.mc.PlayerInstance;
import com.gms.mc.data.GamePosition;
import com.gms.mc.error.InvalidBackendQueryException;
import com.gms.mc.interact.InteractionHandler;

import java.io.IOException;

public class PUZZLE_InteractionHandler extends InteractionHandler{

    boolean handled = false;

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {

        action = event.getAction();
        player = event.getPlayer();
        buttonBlock = event.getBlock();
        blockLoc = new GamePosition(null, buttonBlock.getLocation(), true);
        signLoc = new GamePosition(blockLoc, new Location(0, -2, 0), false); /// buttonBlock.getLocation().add(new Location(0, -2, 0)); //info sign
        level = buttonBlock.getLocation().level;
        signBlock = level.getBlock(signLoc);
        signText = getSignInfo(level, signLoc);
        buttonType = signText[0].split(",")[0]; //Where the button/activity type is stored
        playerInstance = PlayerInstance.getPlayer(player.getName());
        assert playerInstance != null;
        profile = playerInstance.getProfile();
        s_curr = this;

    }

}