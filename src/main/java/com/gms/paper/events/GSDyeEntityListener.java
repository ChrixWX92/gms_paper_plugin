package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.Event;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.player.PlayerInteractEntityEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemShears;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.paper.Main;
import com.gms.paper.custom.items.ItemGSDye;
import com.gms.paper.custom.particles.ParticleFX;
import com.gms.paper.custom.particles.ParticleFXSequence;
import com.gms.paper.custom.sound.MusicMaker;
import com.gms.paper.custom.sound.SFX;
import com.gms.paper.interact.puzzles.BackendUtils;
import com.gms.paper.interact.puzzles.MobGroup;
import com.gms.paper.util.Log;
import io.netty.util.internal.ThreadLocalRandom;
import nukkitcoders.mobplugin.entities.animal.walking.Sheep;
import org.bukkit.event.Listener;

import java.util.Map;

import static cn.nukkit.level.Sound.MOB_SHEEP_SAY;

public class GSDyeEntityListener implements Listener {

    public GSDyeEntityListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onRightClick(PlayerInteractEntityEvent event) {
        sheepHandle(event, null, event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeftClick(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        sheepHandle(null, event, player);
    }

    private void sheepHandle(PlayerInteractEntityEvent piee, EntityDamageByEntityEvent edbee, Player p) {
        Entity entity;
        Item clickedItem;
        Event event;

        if (piee == null & edbee != null) {
            entity = edbee.getEntity();
            clickedItem = p.getInventory().getItemInHand();
            event = edbee;
            if (clickedItem instanceof ItemShears) {
                ItemShears newShears = new ItemShears();
                newShears.setDamage(0);
                p.getInventory().removeItem(clickedItem);
                new NukkitRunnable() {
                    @Override
                    public void run() {
                        p.getInventory().setItemInHand(newShears);
                    }
                }.runTaskLater(Main.s_plugin, 1);
                p.getInventory().setItemInHand(newShears);

            }
        }
        else if (piee != null & edbee == null) {
            entity = piee.getEntity();
            clickedItem = piee.getItem();
            event = piee;
        }
        else {
            Log.error("Incorrect event type passed to GSEntityDyeListener.sheepHandle().");
            return;
        }

        try {
            if (entity instanceof Sheep sheep) {
                if (clickedItem instanceof ItemGSDye) {
                    event.setCancelled();
                    if (sheep.getColor() != clickedItem.getDamage()) {

                        if (sheep.getColor() != 0) {
                            returnItem(p, sheep.getColor());
                        }

                        sheep.setColor(clickedItem.getDamage());

                        int nlIndex = sheep.getNameTag().indexOf("\n") + 1;
                        if (nlIndex >= 0) {
                            String header = sheep.getNameTag().substring(0, nlIndex);
                            sheep.setNameTag(sheep.getNameTag().replace(header, ""));
                        }

                        sheep.setNameTag(clickedItem.getName() + "\n" + sheep.getNameTag());

                        clickedItem.setCount(clickedItem.getCount() - 1);
                        p.getInventory().setItemInHand(clickedItem);

                        p.getWorld().addSound(p.getPosition(), MOB_SHEEP_SAY, 1F, (float) ThreadLocalRandom.current().nextDouble(0.7, 1.3));
                        MusicMaker.playSFX(SFX.Type.MOBGROUP_DYE, p);
                        ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.MOBGROUP_DYE, sheep.getWorld(), sheep.getLocation());
                        synchronized (pFX) {
                            pFX.run();
                        }
                    }
                }
                else if (clickedItem instanceof ItemShears) {
                    event.setCancelled();
                    if (sheep.getColor() != 0) {
                        //Log.debug(String.valueOf(sheep.getColor()));
                        returnItem(p, sheep.getColor());
                        sheep.setColor(0);

                        int nlIndex = sheep.getNameTag().indexOf("\n") + 1;
                        if (nlIndex >= 0) {
                            String header = sheep.getNameTag().substring(0, nlIndex);
                            sheep.setNameTag(sheep.getNameTag().replace(header, ""));
                        }

                        //TODO: Change the below to trimming effects

                        p.getWorld().addSound(p.getPosition(), MOB_SHEEP_SAY, 1F, (float) ThreadLocalRandom.current().nextDouble(0.7, 1.3));
                        MusicMaker.playSFX(SFX.Type.MOBGROUP_SHEAR, p);
                        ParticleFXSequence pFX = new ParticleFXSequence(ParticleFX.MOBGROUP_DYE, sheep.getWorld(), sheep.getLocation());
                        synchronized (pFX) {
                            pFX.run();
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            Log.exception(e, "Exception occurred in GSDyeEntityListener!");
        }
    }

    private void returnItem(Player p, int damage) {
        for (Map.Entry<Integer, Item> entry : p.getInventory().getContents().entrySet()) {
            Item v = entry.getValue();
            if (v instanceof ItemGSDye) {
                if (v.getDamage() == damage) {
                    v.setCount(v.count + 1);//MobGroup.getItems().get(damage);
                    p.getInventory().setItem(entry.getKey(), v);
                    return;
                }
            }
        }
        for (Map.Entry<Integer, Item> entry : MobGroup.getItems().entrySet()) {
            Item v = entry.getValue();
            if (v instanceof ItemGSDye) {
                if (v.getDamage() == damage) {
                    Item dye = new ItemGSDye(1, v.getName(), BackendUtils.getQuestionSetID(), damage);
                    p.getInventory().addItem(dye);
                    return;
                }
            }
        }
    }

}


