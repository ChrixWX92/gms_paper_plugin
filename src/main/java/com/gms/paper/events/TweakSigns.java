package com.gms.paper.events;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.level.ChunkLoadEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.FullChunk;
import org.bukkit.event.Listener;

import java.util.Map;


public class TweakSigns implements Listener {

    private void checkChunk(Level level) {

        Map<Long, ? extends FullChunk> chunksMap = level.getChunks();
        for (var chunk : chunksMap.entrySet()) {
            Map<Long, BlockEntity> entMap = chunk.getValue().getBlockEntities();
            for (var entitySign : entMap.entrySet()) {
                if (entitySign.getValue() instanceof BlockEntitySign blockEntitySign) {
                    textChanges(blockEntitySign);
                }
            }
        }
    }

    private void textChanges(BlockEntitySign sign){
        //Log.debug(Arrays.toString(sign.getText()));
        String[] newText = sign.getText();
        for (int i = 0 ; i < newText.length ; i++) {
            String line = newText[i];
            if (line != null && line.contains("§f")){
                //Log.debug(line);
                newText[i] = line.replaceAll("§f", "§0");
            }
        }
        sign.setText(newText);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
         checkChunk(event.getWorld());

    }
}
