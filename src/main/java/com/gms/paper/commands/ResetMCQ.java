package com.gms.paper.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.interact.mcq.MCQ_InteractionHandler;
import com.gms.paper.util.Log;
import org.bukkit.command.Command;
import org.jetbrains.annotations.NotNull;

public class ResetMCQ extends Command {
    // Structure: if we have more commands, each command should live in its own class, then this class should just call them
    public ResetMCQ(){
        super("resetMCQ");
        this.setDescription("Reset current MCQ question set");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            Log.logAndSend(sender, "This command is just for players");
            return false;
        }

        Player player = (Player) sender;
        MCQ_InteractionHandler mcqHandler = (MCQ_InteractionHandler) InteractionHandler.getCurrent();

        mcqHandler.resetHandlerState(player.getWorld()); //reset question set
        return true;
    }
}
