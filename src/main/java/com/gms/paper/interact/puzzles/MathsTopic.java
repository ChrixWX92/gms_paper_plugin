package com.gms.paper.interact.puzzles;

import com.gms.mc.interact.puzzles.maths.MathsEngine;

import static com.gms.mc.interact.puzzles.maths.MathsEngine.Operator.*;

public enum MathsTopic {

    ADDITION(PLUS),
    SUBTRACTION(MINUS),
    MULTIPLICATION(MULTIPLY),
    DIVISION(DIVIDE);

    protected MathsEngine.Operator associatedOperator;

    MathsTopic(MathsEngine.Operator operator) {
        this.associatedOperator = operator;
    }

    public MathsEngine.Operator getAssociatedOperator() {
        return this.associatedOperator;
    }

}
