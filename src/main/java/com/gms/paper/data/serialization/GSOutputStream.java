package com.gms.paper.data.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class GSOutputStream extends ObjectOutputStream {

    protected GSOutputStream() throws IOException, SecurityException {
        super();
        super.enableReplaceObject(true);
    }

    public GSOutputStream(OutputStream out) throws IOException {
        super(out);
        super.enableReplaceObject(true);
    }

}