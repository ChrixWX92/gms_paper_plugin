package com.gms.paper.error;

import cn.nukkit.blockentity.BlockEntity;
import com.gms.mc.util.Log;

public class NoBlockEntityException extends Exception {

    public NoBlockEntityException(String message, BlockEntity blockEntity){
        super(message);
        Log.error(blockEntity.getClass().getName() + " Block Entity with ID " + blockEntity.getId() + " and name " + blockEntity.getName() + " could not be found. Block Entity may not have initialized successfully.");
        this.printStackTrace();
    }

}
