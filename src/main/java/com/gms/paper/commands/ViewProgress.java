package com.gms.paper.commands;

import com.gms.paper.util.TextFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ViewProgress extends Command {
    public ViewProgress() {
        super("viewProgress");
        this.setDescription("DEV: View Progress");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "This command is for players");
            return false;
        }

        Player player = (Player) sender;
//        PlayerInstance playerInstance = PlayerInstance.getGlobalPlayerInstance(player.getName());
//        sender.sendMessage(playerInstance.progress.toString());

        return false;
    }
}