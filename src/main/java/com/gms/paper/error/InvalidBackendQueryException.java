package com.gms.paper.error;

public class InvalidBackendQueryException extends Exception {

    public InvalidBackendQueryException(String message){
        super(message);
        this.printStackTrace();
    }

}
