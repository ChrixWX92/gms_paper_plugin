package com.gms.paper.interact.puzzles.handlers.anchors.unique;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.Helper;
import com.gms.mc.util.Log;

import java.util.Map;

public class RemoveDeny extends Command{

    public RemoveDeny(){
        super("removedeny");
        this.setDescription("If the command sender is within the apothem(s) of any DENY anchors, the respective anchor(s) are removed from the level.");
        this.setPermission("anchors.edit");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!(sender instanceof Player player)){
            sender.sendMessage(TextFormat.RED+ "Cannot be executed from the console.");
            return false;
        }

        if (!Helper.isDev()) {
            Log.logAndSend(sender, "Only supported in dev mode!");
            return false;
        }

        if (DenyAnchor.anchorLocs.isEmpty()){
            Log.logGeneric(sender, "No DENY Anchors found in the command sender's level.");
            return false;
        } else {
            boolean found = false;
            for (Map.Entry<Vector3, Integer> e : DenyAnchor.anchorLocs.entrySet()) {
                if (Arithmetic.inApothem(e.getValue(), e.getKey(), player.getLocation(), true)) {
                    player.getLevel().getBlockEntity(e.getKey()).close();
                    player.getLevel().setBlock(e.getKey(), new BlockAir(), true, true);
                    DenyAnchor.anchorLocs.remove(e.getKey());
                    Log.logGeneric(sender, "DENY Anchor located at " + e.getKey().asVector3f() + TextFormat.GREEN + " REMOVED" + TextFormat.WHITE + ".");
                    found = true;
                }
            }
            if (!found) Log.logGeneric(sender, "The command sender is not within the apothem of any deny anchor(s) in this level.");
            return true;
        }
    }
}
