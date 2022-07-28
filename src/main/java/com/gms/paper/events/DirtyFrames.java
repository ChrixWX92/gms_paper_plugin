package com.gms.paper.events;

import com.gms.paper.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class DirtyFrames implements Listener {

    public DirtyFrames() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(DataPacketSendEvent event) throws Exception {
        if (event.getPacket() instanceof BlockEntityDataPacket bedp) {
//            bedp.decode();
//            ByteArrayInputStream bais = new ByteArrayInputStream(bedp.namedTag);
//            NBTInputStream nbtis = new NBTInputStream(bais);
//            Tag tag = CompoundTag.readNamedTag(nbtis);
//            Log.debug(tag.getName());
//            return;
//            Log.debug("semp = " + semp.eid);
            if (bedp.getEntityRuntimeId() != 0)
                Log.debug(String.valueOf(bedp.getEntityRuntimeId()));
        }
        if (event.getPacket() instanceof LevelChunkPacket lcp) {
            return;
//            Log.debug("semp = " + semp.eid);
        }
        if (event.getPacket() instanceof SetEntityMotionPacket semp) {
            return;
//            Log.debug("semp = " + semp.eid);
        }
        if (event.getPacket() instanceof MoveEntityAbsolutePacket meap) {
            return;
        }
        if (event.getPacket() instanceof BatchPacket bp) {
            return;
        }
        if (event.getPacket().pid() == 0x6B) {
//            de.theamychan.scoreboard.network.packet.SetObjectivePacket
            return;
        }
//        Log.debug("PID: " + String.format("0x%08X", event.getPacket().pid()) + " ||| " + event.getEventName() + " ||| " +event.getPacket().getClass());
//        if (event.getPacket() instanceof BlockEntityDataPacket packet){
//
//            if (packet.pid() == 0x28) {
//                Log.debug("ROTATION");
//                return;
//            }
//            if (packet.x != 0) {
//                if (event.getPlayer().getLevel().getBlockEntity(new Vector3( packet.x, packet.y, packet.z)) instanceof BlockEntityItemFrame beif) {
//                    beif.setItemRotation(4);
//                }
//            }
//            BlockVector3 psbp = packet.getSignedBlockPosition();
//            BlockEntity be = event.getPlayer().getLevel().getBlockEntity(new Vector3(psbp.x, psbp.y, psbp.z));
//            if (be == null) {
//                return;
//            }


//        }

    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(ChunkLoadEvent event){
        new NukkitRunnable() {
            @Override
            public void run() {
                for (BlockEntity entity : event.getChunk().getBlockEntities().values()) {
                    if (entity instanceof BlockEntityItemFrame beif) {
                        beif.setDirty();
                    }
                }
            }
        }.runTaskLater(Main.s_plugin, 5);
    }

}