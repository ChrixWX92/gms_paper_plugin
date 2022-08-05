package com.gms.paper.interact.puzzles;

import com.gms.paper.interact.puzzles.maths.Arithmetic;
import com.gms.paper.util.HologramHelper;
import com.gms.paper.util.Log;
import com.gms.paper.util.TextFormat;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ResetPuzzles extends Command {

    public ResetPuzzles(){
        super("reset");
        this.setDescription("Resets the plugin's puzzle tracking ability.");
        this.setAliases(Collections.singletonList("resetpuzzles")); //alternative commands
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, String[] args) {
        if (!(sender instanceof Player p)){
            sender.sendMessage(TextFormat.RED+ "Cannot be executed from the console.");
            return false;
        }
        return resetPuzzles(p);
    }

    public static boolean resetPuzzles(Player p) {
        World world = p.getWorld();
        Arithmetic.removePuzzleInventoryItems(p);
        BackendUtils.resetPuzzleData();
        BackendUtils.setPuzzleType(null);
        Arithmetic.puzzleName = "";
        closeHolograms(HologramHelper.getHologramEntities(world));
        Log.info(TextFormat.DARK_PURPLE + "- PUZZLES RESET -");
        return true;
    }

    public static void closeHolograms(Map<Long, HologramEntity> holograms) {
        for (HologramEntity h : holograms.values()) h.closeHologram();
    }

}