package com.gms.paper.error;

import com.gms.paper.util.Log;

public class InvalidFrameWriteException extends Exception {
    public InvalidFrameWriteException (int type) {
        super("Invalid method call for problem type " + type);
        Log.error(super.getMessage());
    }
}