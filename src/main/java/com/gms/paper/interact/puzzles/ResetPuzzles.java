package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.interact.puzzles.maths.Arithmetic;
import com.gms.mc.util.HologramHelper;
import com.gms.mc.util.Log;
import gt.creeperface.holograms.entity.HologramEntity;

import java.util.Map;

public class ResetPuzzles extends Command{

    public ResetPuzzles(){
        super("reset");
        this.setDescription("Resets the plugin's puzzle tracking ability.");
        this.setAliases(new String[]{"resetpuzzles"}); //alternative commands
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        if (!(sender instanceof Player p)){
            sender.sendMessage(TextFormat.RED+ "Cannot be executed from the console.");
            return false;
        }
        return resetPuzzles(p);
    }

    public static boolean resetPuzzles(Player p) {
        Level level = p.getLevel();
        Arithmetic.removePuzzleInventoryItems(p);
        BackendUtils.resetPuzzleData();
        BackendUtils.setPuzzleType(null);
        Arithmetic.puzzleName = "";
        closeHolograms(HologramHelper.getHologramEntities(level));
        Log.info(TextFormat.DARK_PURPLE + "- PUZZLES RESET -");
        return true;
    }

    public static void closeHolograms(Map<Long, HologramEntity> holograms) {
        for (HologramEntity h : holograms.values()) h.closeHologram();
    }

}