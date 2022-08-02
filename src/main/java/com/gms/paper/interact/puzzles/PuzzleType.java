package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import com.gms.mc.data.Course;
import com.gms.mc.data.QuestionIdInfo;
import com.gms.mc.data.QuestionSet;
import com.gms.mc.interact.puzzles.handlers.ASSEMBLE_InteractionHandler;
import com.gms.mc.interact.puzzles.handlers.CHECKS_InteractionHandler;
import com.gms.mc.interact.puzzles.handlers.MOBGROUP_InteractionHandler;
import com.gms.mc.interact.puzzles.handlers.PAIRS_InteractionHandler;
import com.gms.mc.interact.puzzles.maths.*;
import com.gms.mc.util.Log;
import com.gms.paper.data.Course;
import com.gms.paper.data.QuestionIdInfo;
import com.gms.paper.data.QuestionSet;
import com.gms.paper.util.Log;
import com.gms.paper.util.TextFormat;
import org.bukkit.entity.Player;

import java.util.Objects;

public enum PuzzleType {

    GRID("GRD", Grid.class),
    PILLARS("PLS", null),
    ANVILS("ANV", Anvils.class),
    PEN("PEN", null),
    NUMBERSEARCH("NSR", NumberSearch.class),
    TOWER("TWR", Tower.class),
    FARM("FRM", Farm.class),
    MCTP("MCT", null),
    TH("TRH",null),
    TPQS("TQS", null),
    MCQ("MCQ", null),
    CHECKS("CKS", null),
    MOBGROUP("MGP", null),
    PAIRS("PRS", null),
    ASSEMBLE("ASB", null),
    GAUGES("GGS", Gauges.class);

    public final String abbreviation;
    public final Class<? extends Puzzle> clazz;

    PuzzleType(String abbreviation, Class<? extends Puzzle> clazz) {
        this.abbreviation = abbreviation;
        this.clazz = clazz;
    }

    public static String[] abbreviations() {
        String[] abbreviations = new String[PuzzleType.values().length];
        for (PuzzleType puzzleType : PuzzleType.values()) {
            abbreviations[puzzleType.ordinal()] = puzzleType.abbreviation;
        }
        return abbreviations;
    }

    public static PuzzleType getPuzzleType(Player player, String questionSetID){
        try {
            QuestionIdInfo idInfo = new QuestionIdInfo(questionSetID);
            Course c = Course.get(idInfo.courseId);
            return getPuzzleType(player, c.getQuestionSet(player, idInfo), idInfo);
        }
        catch(Exception e){
            Log.error(TextFormat.RED + "Unable to retrieve data from backend.");
            e.printStackTrace();
            return null;
        }
    }

    public static PuzzleType getPuzzleType(Player p, QuestionSet qs, QuestionIdInfo qidinf) throws Exception {
        switch (qs.getQuestion(p, qidinf, 1).type.toUpperCase()) {
            case "GRID" -> {return PuzzleType.GRID;}
            case "MCTP" -> {return PuzzleType.MCTP;}
            case "TH" -> {return PuzzleType.TH;}
            case "TPQS" -> {return PuzzleType.TPQS;}
            case "MCQ" -> {return PuzzleType.MCQ;}
            case "CHECKS" -> {return PuzzleType.CHECKS;}
            case "MOBGROUP" -> {return PuzzleType.MOBGROUP;}
            case "PAIRS" -> {return PuzzleType.PAIRS;}
            case "ASSEMBLE" -> {return PuzzleType.ASSEMBLE;}
            default ->
                    throw new Exception("Unknown PuzzleType - " + qs.getQuestion(p, qidinf, 1).type.toUpperCase()){};
        }
    }

    public static void initializePuzzleType(Player p, String[] anchorText, Vector3 anchorLoc) throws Exception {

        String questionSetID = anchorText[1];

        switch(Objects.requireNonNull(PuzzleType.getPuzzleType(p, questionSetID))){
            case MCTP, TH, TPQS, MCQ -> throw new Exception("PuzzleType " + Objects.requireNonNull(PuzzleType.getPuzzleType(p, questionSetID)) +  " not compatible with ANCHOR functionality."){};
            case CHECKS -> CHECKS_InteractionHandler.fetchChecks(p, questionSetID, anchorText[3], anchorLoc.add(0, 3));
            case MOBGROUP -> MOBGROUP_InteractionHandler.fetchMobGroup(p, questionSetID);
            case PAIRS -> PAIRS_InteractionHandler.fetchPairs(p, questionSetID);
            case ASSEMBLE -> ASSEMBLE_InteractionHandler.fetchAssemble(p, questionSetID);
        }

    }

}
