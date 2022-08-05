package com.gms.paper.interact.puzzles.handlers;

import com.gms.paper.PlayerInstance;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Vector3D;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.IOException;

public class PUZZLE_InteractionHandler extends InteractionHandler{

    boolean handled = false;

    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {

        action = event.getAction();
        player = event.getPlayer();
        buttonBlock = event.getClickedBlock();
        blockLoc = new GamePosition(null, new Vector3D(buttonBlock.getX(), buttonBlock.getY(), buttonBlock.getZ()), true);
        signLoc = new GamePosition(blockLoc, new Vector3D(0, -2, 0), false); /// buttonBlock.getLocation().add(new Location(0, -2, 0)); //info sign
        world = buttonBlock.getWorld();
        signBlock = world.getBlock(signLoc);
        signText = getSignInfo(level, signLoc);
        buttonType = signText[0].split(",")[0]; //Where the button/activity type is stored
        playerInstance = PlayerInstance.getPlayer(player.getName());
        assert playerInstance != null;
        profile = playerInstance.getProfile();
        s_curr = this;

    }

}
