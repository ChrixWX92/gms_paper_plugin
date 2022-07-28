package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import com.gms.mc.interact.puzzles.maths.Arithmetic;

public abstract class Puzzle {

    protected Player player;
    protected Level level;
    PuzzleType puzzleType;
    protected String id;
    protected String name;
    protected final String questionStub; // Used for tags that hold elements of question data
    protected final String answerStub; // Used for tags that hold elements of answer data
    protected final String identifierStub; // Used for tags that are primarily used to identify entities

    Puzzle(Player player, PuzzleType puzzleType, String id){
        this.player = player;
        this.level = player.getLevel();
        this.puzzleType = puzzleType;
        this.id = id;
        Arithmetic.currentPuzzle = this;
        Arithmetic.puzzleName = id;
        this.name = this.puzzleType.abbreviation + "-" + this.id;
        this.questionStub = this.name + "_Q";
        this.answerStub = this.name + "_A";
        this.identifierStub = this.name + "_I";
    }

    public Player getPlayer() {
        return player;
    }

    public Level getLevel() {
        return level;
    }

    public PuzzleType getPuzzleType() {
        return puzzleType;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuestionStub() {
        return questionStub;
    }

    public String getAnswerStub() {
        return answerStub;
    }

    public String getIdentifierStub() {
        return identifierStub;
    }

    public String addTagModifier(String stub, Object modifier) {
        String string = String.valueOf(modifier);
        if (stub.endsWith("_")) {
            return stub + string;
        } else {
            return stub + "_" + string;
        }
    }

}
