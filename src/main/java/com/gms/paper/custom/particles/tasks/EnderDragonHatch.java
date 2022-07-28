package com.gms.paper.custom.particles.tasks;

import org.bukkit.entity.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.paper.custom.particles.ParticleFX;
import com.gms.paper.custom.particles.ParticleFXSequence;

public class EnderDragonHatch extends NukkitRunnable {

    Player p;
    Vector3D l;
    boolean done;

    public EnderDragonHatch(Player player, Vector3D location) {
        this.p = player;
        this.l = location;
        this.done = false;
    }

    @Override
    public void run() {
        //MusicMaker.playSFX(SFX.Type.DRAGON_SPAWN, player);
        ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.DRAGON_SPAWN, p.getWorld(), l);
        synchronized (this) {
            done = true;
            notifyAll();
        }

    }
}
