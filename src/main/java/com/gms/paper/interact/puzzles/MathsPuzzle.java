package com.gms.paper.interact.puzzles;

import cn.nukkit.Player;

public abstract class MathsPuzzle extends Puzzle {

    public MathsTopic topic;

    public MathsPuzzle(Player player, PuzzleType puzzleType, String id, MathsTopic topic){
        super(player, puzzleType, id);
        this.topic = topic;
    }

}

