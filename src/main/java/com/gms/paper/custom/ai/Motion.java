package com.gms.paper.custom.ai;

import com.gms.paper.Main;
import org.bukkit.entity.Entity;

public class Motion {
    
    public static void jump (Entity entity) {
        //if (entity != null) entity.move(0, -6, 0);
        //entity.addMotion(0, 3, 0);
        //entity.fastMove(0,3,0);
//        entity.teleport(new Vector3(115, 19 , -328));
        float entityY = (float) entity.y;
        float fallY = (float) (entityY + (Math.sin(3.14/20)*2));
        for (int i = 0 ; i < 10 ; i++) {
            int finalI = i*2;

//            new NukkitRunnable() {
//                @Override
//                public void run() {
//                    MoveEntityAbsolutePacket pk = new MoveEntityAbsolutePacket();
//                    pk.eid = entity.getId();
//                    pk.x = (float) entity.x;
//                    pk.y = entityY + (Math.cos(3.14/finalI)*2);
//                    pk.z = (float) entity.z;
//                    pk.yaw = (float) entity.yaw;
//                    pk.headYaw = (float) entity.headYaw;
//                    pk.pitch = (float) entity.pitch;
//                    pk.onGround = entity.onGround;
//                    Server.broadcastPacket(entity.getViewers().values(), pk);
//                    Main.s_plugin.getServer().broadcastMessage(pk.y + "");
//                }
//            }.runTaskLater(Main.s_plugin, i);

            float traj = (float) (fallY + (Math.sin(3.14/finalI)*2));

            if (traj > entityY) {

                new NukkitRunnable() {
                    @Override
                    public void run() {
                        MoveEntityAbsolutePacket pk = new MoveEntityAbsolutePacket();
                        pk.eid = entity.getId();
                        pk.x = (float) entity.x;
                        pk.y = traj+1;
                        pk.z = (float) entity.z;
                        pk.yaw = (float) entity.yaw;
                        pk.headYaw = (float) entity.headYaw;
                        pk.pitch = (float) entity.pitch;
                        pk.onGround = entity.onGround;
                        Server.broadcastPacket(entity.getViewers().values(), pk);
                    }
                }.runTaskLater(Main.s_plugin, i + 10);
            }
        }
    }
}
