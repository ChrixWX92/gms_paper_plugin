package com.gms.paper.commands;

import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class World extends Command {
    public World(){
        super("world");
        this.setDescription("Change worlds");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Log.logAndSend(sender, "This command is just for players");
            return false;
        }

        Player player = (Player) sender;
        return Helper.teleportToWorld(player, args[0]);
    }
}