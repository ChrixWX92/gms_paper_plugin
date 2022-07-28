package com.gms.paper.commands;

import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Home extends Command {
    public Home() {
        super("home");
        this.setDescription("Go home!");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Log.errorMsg("This command is just for players"));
            return false;
        }
        Arithmetic.puzzleName = null;
        Player player = (Player) sender;
        return Helper.teleportToLobby(player);
    }
}
