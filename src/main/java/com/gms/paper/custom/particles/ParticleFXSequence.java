package com.gms.paper.custom.particles;

import com.gms.paper.util.Log;
import com.gms.paper.util.Vector3D;
import org.bukkit.Particle;
import org.bukkit.World;

public class ParticleFXSequence implements Runnable {

    private final ParticleFX type;
    private final World w;
    private final Vector3D l;

    public ParticleFXSequence(ParticleFX type, World world, Vector3D location) {
        this.type = type;
        this.w = world;
        this.l = location;
    }

    void allAround(ParticleEffect effect, double yOffset) {
        Vector3D newL = this.l.add(0,yOffset);
        this.w.addParticleEffect(newL, effect);
        this.w.addParticleEffect(newL.add(-1), effect);
        this.w.addParticleEffect(newL.add(1), effect);
        this.w.addParticleEffect(newL.add(0,0,1), effect);
        this.w.addParticleEffect(newL.add(0,0,-1), effect);
    }

    void disperse(ParticleEffect effect, int instances) {
        Vector3D newL = this.l.floor().add(0.5,0.5,0.5);
        for (int i = 0 ; i < instances; i++) {
            double xOffset = ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            double yOffset = ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            double zOffset = ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            this.w.addParticleEffect(newL.add(xOffset, yOffset, zOffset), effect);
        }
    }

    void disperse(Particle effect, int instances) {
        Vector3D newL = this.l.floor().add(0.5,0.5,0.5);
        for (int i = 0 ; i < instances; i++) {
            effect.x = newL.x + ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            effect.y = newL.y + ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            effect.z = newL.z + ThreadLocalRandom.current().nextDouble(-0.5, 1.5);
            Log.debug(effect.x + " " + effect.y + " " +effect.z);
            this.w.addParticle(effect);
        }
    }

    @Override
    public void run() {

        while (!this.w.isChunkLoaded((int) this.l.x, (int) this.l.z)) {
            w.loadChunk((int) this.l.x, (int) this.l.z,true);
        }

        switch (this.type){
            case RETURN_HOME_PLUME -> {

                allAround(EXPLOSION_EGG_DESTROY, 0);
                allAround(EXPLOSION_EGG_DESTROY, 1);

            }
            case FREEFALL_TELEPORT -> {
                for (int i = 0 ; i < 9 ; i++) {
                    try {
                        this.wait(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    allAround(FALLING_DUST_TOP_SNOW, 0);
                    allAround(FALLING_DUST_TOP_SNOW, 1);
                    allAround(FALLING_DUST_TOP_SNOW, 2);
                    allAround(EVOCATION_FANG, 0);
                    allAround(EVOCATION_FANG, 1);
                }
            }
            case TOWER_TELEPORT -> {
                for (int i = 0 ; i < 9 ; i++) {
                    try {
                        this.wait(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    allAround(FALLING_DUST_TOP_SNOW, 0);
                    allAround(FALLING_DUST_TOP_SNOW, 1);
                    allAround(FALLING_DUST_TOP_SNOW, 2);
                    allAround(SPARKLER, 0);
                    allAround(SPARKLER, 1);
                    allAround(SPARKLER, 2);
                }
            }
            case PET_SPAWN -> {
                for (int i = 0 ; i < 9 ; i++) {
                    try {
                        this.wait(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    allAround(GLOW, 0);
                    allAround(GLOW, 1);
                    allAround(GLOW, 2);
                    allAround(MOBSPELL, 0);
                    allAround(MOB_PORTAL, 1);
                    allAround(MOB_BLOCK_SPAWN, 2);
                }
            }
            case DRAGON_SPAWN -> {
                for (int i = 0 ; i < 300 ; i++) {
                    /*try {
                        this.wait(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/ //TODO: Add scheduling
                    allAround(DRAGON_BREATH_FIRE, i % 5);
                    if ((i % 4) == 0){allAround(DRAGON_BREATH_TRAIL, i % 9);}
                    if ((i % 12) == 0){allAround(SPLASHPOTIONSPELL, i % 17);}
                    if ((i % 16) == 0){allAround(HUGE_EXPLOSION_LEVEL, i % 21);}
                    if ((i % 30) == 0){allAround(HUGE_EXPLOSION_LAB_MISC, i % 4);}
                }
                this.w.addParticleEffect(this.l, DRAGON_DYING_EXPLOSION);
                this.w.addParticleEffect(this.l, DRAGON_DEATH_EXPLOSION);//TODO: May need to change y axis from anchor
            }
            case MOBGROUP_DYE -> {
                for (int i = 0 ; i < 9 ; i++) {
                    allAround(GLOW, 0);
                    allAround(GUARDIAN_WATER_MOVE, 1);
                    allAround(MOBSPELL, 0);
                    allAround(MOB_PORTAL, 1);
                    allAround(GUARDIAN_WATER_MOVE, 0);
                }
            }
            case COMPLETE -> {
                for (int i = 0 ; i < 3 ; i++) {
                    allAround(TOTEM_MANUAL, 0);
                }
            }
            case FARM_START -> {
                disperse(FALLING_DUST_SCAFFOLDING, 10);
            }
            case FARM_UPROOT -> {
                disperse(new DustParticle(this.l, DIRT_BLOCK_COLOR), 15);
            }
        }
    }
}