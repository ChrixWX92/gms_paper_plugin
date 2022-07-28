package com.gms.paper.custom.sound;

import cn.nukkit.Player;
import cn.nukkit.level.Level;

import static cn.nukkit.level.Sound.*;

public class SFX extends Thread{

    public enum Type {
        PEN_BEGIN,
        NSEARCH_BEGIN,
        TOWER_BEGIN,
        TOWER_CLIMB,
        TOWER_FALL,
        ISLANDS_BEGIN,
        FREEFALL_TELEPORT,
        PET_SPAWN,
        DRAGON_SPAWN,
        MOBGROUP_DYE,
        MOBGROUP_SHEAR,
        GRID_GEMS,
        FARM_BEGIN
    }

    private final Type type;
    private final Player p;
    private final Level l;
    private final long sp;
    private final int it;

    public SFX(Type type, Player player, long speed, int iterations) {
        this.type = type;
        p = player;
        l = p.getLevel();
        sp = speed;
        it = iterations;
    }

    public void run() {
        try{
            synchronized (this) {
                switch (type) {
                    case PEN_BEGIN -> {
                        for (int i = 0; i < it; i++) {
                            double pitchMaths = 0.4 + ((i * 0.3) + (((float) i + 1) / 10));
                            float pitch = (float) pitchMaths;
                            l.addSound(p.getPosition(), MOB_PANDA_PRESNEEZE, 0.6F, pitch);
                            l.addSound(p.getPosition(), MOB_SNOWGOLEM_SHOOT, 0.5F, 0.5F);
                            this.wait(sp);
                        }
                    }
                    case NSEARCH_BEGIN -> {
                        for (int i = 0; i < it; i++) {
                            double pitchMaths = 1.5 + ((i * 0.3) + (((float) i + 1) / 10));
                            float pitch = (float) pitchMaths;
                            l.addSound(p.getPosition(), LIQUID_LAVAPOP, 1F, pitch);
                            l.addSound(p.getPosition(), MOB_ARMOR_STAND_LAND, 1F, 1.2F);
                            this.wait(sp);
                        }
                    }
                    case TOWER_BEGIN -> {
                        l.addSound(p.getPosition(), ITEM_BOOK_PUT, 1F, 0.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RETURN, 1F, 0.5F);
                        l.addSound(p.getPosition(), FIRE_IGNITE, 1F, 0.7F);
                        l.addSound(p.getPosition(), BLOCK_FURNACE_LIT, 1F, 0.85F);
                        l.addSound(p.getPosition(), ITEM_SHIELD_BLOCK, 0.8F, 0.2F);
                    }
                    case TOWER_CLIMB -> {
                        l.addSound(p.getPosition(), BEACON_ACTIVATE, 1F, 3F);
                        l.addSound(p.getPosition(), BEACON_ACTIVATE, 1F, 2.5F);
                        l.addSound(p.getPosition(), BEACON_ACTIVATE, 1F, 2F);
                        l.addSound(p.getPosition(), FIRE_IGNITE, 1F, 1F);
                        l.addSound(p.getPosition(), BLOCK_FURNACE_LIT, 1F, 0.85F);
                    }
                    case TOWER_FALL -> {
                        l.addSound(p.getPosition(), BEACON_ACTIVATE, 1F, 0.5F);
                        l.addSound(p.getPosition(), FIRE_IGNITE, 1F, 0.5F);
                        l.addSound(p.getPosition(), BLOCK_FURNACE_LIT, 1F, 0.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RETURN, 1F, 0.5F);
                    }
                    case ISLANDS_BEGIN -> {
                        l.addSound(p.getPosition(), MOB_GUARDIAN_AMBIENT, 1F, 0.6F);
                        l.addSound(p.getPosition(), MOB_GUARDIAN_AMBIENT, 1F, 0.8F);
                        l.addSound(p.getPosition(), FIRE_IGNITE, 1F, 0.5F);
                        l.addSound(p.getPosition(), BLOCK_FURNACE_LIT, 1F, 0.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_THROW, 1F, 0.85F);
                    }
                    case FREEFALL_TELEPORT -> {
                        l.addSound(p.getPosition(), BUCKET_FILL_POWDER_SNOW, 1F, 0.5F);
                        l.addSound(p.getPosition(), MOB_BEE_POLLINATE, 1F, 0.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RIPTIDE_1, 1F, 1.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RETURN, 1F, 0.5F);
                    }
                    case PET_SPAWN -> {
                        l.addSound(p.getPosition(), SIGN_INK_SAC_USE, 1F, 0.5F);
                        l.addSound(p.getPosition(), MOB_BEE_POLLINATE, 1F, 0.5F);
                        l.addSound(p.getPosition(), ITEM_TRIDENT_RETURN, 1F, 0.6F);
                    }
                    case DRAGON_SPAWN -> {
                        l.addSound(p.getPosition(), MOB_ENDERDRAGON_DEATH, 1F, 1F);
                        l.addSound(p.getPosition(), RANDOM_ENDERCHESTOPEN, 1F, 0.1F);
                        l.addSound(p.getPosition(), RANDOM_EXPLODE, 0.7F, 0.1F);
                    }
                    case MOBGROUP_DYE -> {
                        l.addSound(p.getPosition(), MOB_BEE_POLLINATE, 1F, 2F);
                        l.addSound(p.getPosition(), SIGN_DYE_USE, 1F, 0.6F);
                        l.addSound(p.getPosition(), SIGN_INK_SAC_USE, 1F, 0.8F);
                    }
                    case MOBGROUP_SHEAR -> {
                        l.addSound(p.getPosition(), MOB_BEE_POLLINATE, 1F, 2F);
                        l.addSound(p.getPosition(), BLOCK_BEEHIVE_SHEAR, 1F, 1.1F);
                        l.addSound(p.getPosition(), SIGN_INK_SAC_USE, 1F, 2F);
                    }
                    case GRID_GEMS -> {
                        l.addSound(p.getPosition(),CHIME_AMETHYST_BLOCK);
                        l.addSound(p.getPosition(),LEASHKNOT_PLACE);
                    }
                    case FARM_BEGIN -> {
                        for (int i = 0; i < it; i++) {
                            double pitchMaths = 0.4 + ((i * 0.3) + (((float) i + 1) / 10));
                            float pitch = (float) pitchMaths;
                            l.addSound(p.getPosition(), LIQUID_LAVAPOP, 1F, 1.4F);
                            l.addSound(p.getPosition(), MOB_ARMOR_STAND_LAND, 1F, pitch);
                            this.wait(sp);
                        }
                    }
                }
            }
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}


