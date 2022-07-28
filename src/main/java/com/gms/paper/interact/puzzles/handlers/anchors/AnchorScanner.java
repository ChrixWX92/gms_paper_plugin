package com.gms.paper.interact.puzzles.handlers.anchors;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.math.Vector3;
import com.gms.mc.util.Helper;
import com.gms.mc.util.Log;
import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class AnchorScanner implements Listener {

    private static boolean forceLoad = false;

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo().getLevel() == null) return;
        if (event.getFrom().getLevel() == null) return;
        if (event.getTo().getLevel().getName().equals(event.getFrom().getLevel().getName()) && !forceLoad) return;
        forceLoad = false;
        AnchorHandler.setLevelAnchors(new HashMap<>());
        if (event.getTo().getLevel() != null) {
            Log.info("ANCHORS: Scanning region files for Anchors...");
            File levelFolder = new File(Helper.getLevelPath(event.getTo().getLevel().getName()), "region");
            File[] listOfFiles = levelFolder.listFiles();
            if (listOfFiles != null) {
                if (retrieveAnchors(listOfFiles)) AnchorHandler.setAnchorScan(true);
            } else {
                Log.error(Log.errorMsg("ANCHORS: Region data retrieval failed. Ensure filepaths to the 'region' directory are accurate, and that the directory is not empty."));
            }
        }
    }


    public boolean retrieveAnchors(File[] listOfFiles) {
        //Instant start = Instant.now();

        boolean found = false;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                try {
                    MCAFile mcaFile = MCAUtil.read(listOfFiles[i]);
                    for (Chunk chunk : mcaFile.getChunks()) {
                        if (chunk != null) {
                            CompoundTag lcomp = chunk.getData().getCompoundTag("Level");
                            ListTag<?> te = lcomp.getListTag("TileEntities");
                            for (Tag<?> be : te) {
                                if (((CompoundTag) be).containsKey("id")){
                                    CompoundTag beComp = ((CompoundTag) be);
                                    if (beComp.getStringTag("id").getValue().equals("Sign")){
                                        if (beComp.containsKey("Text")) {
                                            String text = beComp.getStringTag("Text").getValue().substring(0, beComp.getStringTag("Text").getValue().indexOf("\n"));
                                            if (text.equalsIgnoreCase("ANCHOR")) {
                                                int x = beComp.getInt("x");
                                                int y = beComp.getInt("y");
                                                int z = beComp.getInt("z");
                                                //Log.debug(Arrays.toString(parseTextTag(beComp.getStringTag("Text").getValue())));
                                                AnchorHandler.putLevelAnchor(new Vector3(x, y, z),  parseTextTag(beComp.getStringTag("Text").getValue()));
                                                found = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                } catch (IOException ioException) {
                    Log.debug("CATCHING");
                    ioException.printStackTrace();
                }
            }
        }
        //Instant end = Instant.now();
        //Log.info("Anchor scan executed in: " + Duration.between(start, end));
        return found;
    }

    public String[] parseTextTag(String value) {

        String[] anchorText = new String[4];
        int[] indices = new int[4];
        int index = value.indexOf("\n");
        int counter = 0;
        while (index >= 0) {
            indices[counter] = index;
            counter++;
            index = value.indexOf("\n", index + 1);
        }

        anchorText[0] = value.substring(0, indices[0]).replaceAll("\n", "");
        anchorText[1] = value.substring(indices[0], indices[1]).replaceAll("\n", "");
        anchorText[2] = value.substring(indices[1], indices[2]).replaceAll("\n", "");
        anchorText[3] = value.substring(indices[2]).replaceAll("\n", "");

        return anchorText;
    }

    public static boolean isForceLoad() {
        return AnchorScanner.forceLoad;
    }

    public static void setForceLoad(boolean forceLoad) {
        AnchorScanner.forceLoad = forceLoad;
    }

}