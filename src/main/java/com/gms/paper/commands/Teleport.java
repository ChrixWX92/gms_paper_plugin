package com.gms.paper.commands;

import com.gms.paper.data.GamePosition;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Teleport extends Command {
    public Teleport() {
        super("teleport");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Log.logAndSend(sender, "This command is just for players!");
            return false;
        }

        Player p = (Player)sender;
        if (!Helper.isDev()) {
            Log.logAndSend(sender, "Only supported in dev mode!");
            return false;
        }

        String coordsStr = args[0];
        GamePosition pos = Helper.parseLocation(coordsStr);

        Log.debug(String.format("Teleporting to: %s", pos));

        p.teleport(pos.toLocation(p.getWorld()));

        return true;
    }
}
