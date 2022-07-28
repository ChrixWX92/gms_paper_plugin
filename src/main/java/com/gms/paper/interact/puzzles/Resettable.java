package com.gms.paper.interact.puzzles;

import com.gms.mc.error.InvalidFrameWriteException;

import java.lang.reflect.InvocationTargetException;

public interface Resettable {
    void reset() throws InvalidFrameWriteException, InterruptedException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;
    boolean solve() throws InvalidFrameWriteException, InterruptedException, CloneNotSupportedException;
}
