package com.gms.paper.commands;

import com.gms.paper.interact.puzzles.PuzzleType;
import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.interact.puzzles.maths.Farm;
import com.gms.paper.util.Log;
import com.gms.paper.util.TextFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.gms.paper.forms.PuzzleForm.*;

public class FarmTalk extends Command {

    public FarmTalk() {
        super("farmtalk");
        this.setDescription("Engages FARM puzzles' dialogue.");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {

        if (!(sender instanceof Player)) {
            Log.error(TextFormat.RED + "Must execute via player.");
            return false;
        }

        Player p = ((Player) sender).getPlayer();
        Farm farmPuzzle;
        if (Arithmetic.currentPuzzle == null || Arithmetic.currentPuzzle.getPuzzleType() != PuzzleType.FARM) {
            sendForm(p, FARM1.getForm());
        } else {
            farmPuzzle = (Farm) Arithmetic.currentPuzzle;
            if (!farmPuzzle.isFinalQuestion()) sendForm(p, FARM1.getForm());
            else sendForm(p, FARM2.getForm());
        }
        return true;
    }

    public static void sendForm(final Player player, final FormWindow window) {
        if (player != null) {
            final ServerScheduler scheduler = Server.getInstance().getScheduler();
            Task task = new Task() {
                public void onRun(int i) {
                    player.showFormWindow(window);
                    scheduler.scheduleDelayedTask(new Task() {
                        public void onRun(int i) {
                            player.sendExperience();
                        }
                    }, 20);
                }
            };
            task.onRun(0);
        }
    }

}