package com.gms.paper.data.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class GSInputStream extends ObjectInputStream {

    protected GSInputStream() throws IOException, SecurityException {
        super();
        super.enableResolveObject(true);
    }

    public GSInputStream(InputStream in) throws IOException {
        super(in);
        super.enableResolveObject(true);
    }

    private static IOException newIOException(String string, Throwable cause) {
        return new IOException(string, cause);
    }
}
