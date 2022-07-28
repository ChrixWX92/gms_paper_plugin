package com.gms.paper.events;

import org.bukkit.entity.Player;
import cn.nukkit.event.Listener;
import cn.nukkit.item.*;
import cn.nukkit.item.enchantment.Enchantment;
import org.bukkit.event.Listener;

public class ItemsToSell implements Listener {
    public static void getItem(int id, Player player) {
        switch (id) {
            case (0):
                Item bow = new ItemBow();
                player.getInventory().addItem(bow);
                break;
            case (1):
                Item arrow = new ItemArrow();
                player.getInventory().addItem(arrow);
                break;
            case (2):
                Item apple = new ItemApple();
                player.getInventory().addItem(apple);
                break;
            case (3):
                Item bp = new ItemPotatoBaked();
                player.getInventory().addItem(bp);
                break;
            case (4):
                Item cookedChicken = new ItemChickenCooked();
                player.getInventory().addItem(cookedChicken);
                break;
            case (5):
                Item cookedPorkchop = new ItemPorkchopCooked();
                player.getInventory().addItem(cookedPorkchop);
                break;
            case (6):
                Item steak = new ItemSteak();
                player.getInventory().addItem(steak);
                break;
            case (7):
                Item cookie = new ItemCookie();
                player.getInventory().addItem(cookie);
                break;
            case (8):
                player.getInventory().addItem(new ItemMushroomStew());
                break;
            case (9):
                player.getInventory().addItem(new ItemPumpkinPie());
                break;
            case (10):
                player.getInventory().addItem(new ItemHelmetLeather());
                break;
            case (11):
                player.getInventory().addItem(new ItemBootsLeather());
                break;
            case (12):
                player.getInventory().addItem(new ItemLeggingsLeather());
                break;
            case (13):
                player.getInventory().addItem(new ItemChestplateLeather());
                break;
            case (14):
                player.getInventory().addItem(new ItemHelmetIron());
                break;
            case (15):
                player.getInventory().addItem(new ItemBootsIron());
                break;
            case (16):
                player.getInventory().addItem(new ItemLeggingsIron());
                break;
            case (17):
                player.getInventory().addItem(new ItemChestplateIron());
                break;
            case (18):
                player.getInventory().addItem(new ItemHelmetDiamond());
                break;
            case (19):
                player.getInventory().addItem(new ItemBootsDiamond());
                break;
            case (20):
                player.getInventory().addItem(new ItemLeggingsDiamond());
                break;
            case (21):
                player.getInventory().addItem(new ItemChestplateDiamond());
                break;
            case (22):
                player.getInventory().addItem(new ItemSwordIron());
                break;
            case (23):
                player.getInventory().addItem(new ItemSwordDiamond());
                break;
            case (24):
                player.getInventory().addItem(new ItemShield());
                break;
            case (25):
                player.getInventory().addItem(new ItemElytra());
                break;
            case (35):
                Item flamebow = new ItemBow();
                Enchantment flame = Enchantment.getEnchantment(Enchantment.ID_BOW_FLAME);
                flamebow.addEnchantment(flame);
                player.getInventory().addItem(flamebow);
                break;
            case (36):
                Item ironCPProtFire = new ItemChestplateIron();
                Enchantment flameprot = Enchantment.getEnchantment(Enchantment.ID_PROTECTION_FIRE);
                ironCPProtFire.addEnchantment(flameprot);
                player.getInventory().addItem(ironCPProtFire);
                break;
            case (37):
                Item ironCPProt = new ItemChestplateIron();
                Enchantment prot = Enchantment.getEnchantment(Enchantment.ID_PROTECTION_ALL);
                ironCPProt.addEnchantment(prot);
                player.getInventory().addItem(ironCPProt);
                break;
            case (38):
                Item ironSwordSharp = new ItemSwordIron();
                Enchantment sharpness = Enchantment.getEnchantment(Enchantment.ID_DAMAGE_ALL);
                ironSwordSharp.addEnchantment(sharpness);
                player.getInventory().addItem(ironSwordSharp);
                break;
            case (39):
                player.getInventory().addItem(new ItemBread());
                break;
            case (40):
                player.getInventory().addItem(new ItemSalmonCooked());
                break;
            case (41):
                player.getInventory().addItem(new ItemRabbitStew());
                break;
            case (42):
                player.getInventory().addItem(new ItemBeetrootSoup());
                break;
            case (44):
                Item ironBootDS = new ItemBootsIron();
                Enchantment depthStrider = Enchantment.getEnchantment(Enchantment.ID_WATER_WALKER);
                ironBootDS.addEnchantment(depthStrider);
                player.getInventory().addItem(ironBootDS);
                break;
            case (45):
                Item ironBootSF = new ItemBootsIron();
                Enchantment slowFall = Enchantment.getEnchantment(Enchantment.ID_PROTECTION_FALL);
                ironBootSF.addEnchantment(slowFall);
                player.getInventory().addItem(ironBootSF);
                break;
            case (46):
                Item infbow = new ItemBow();
                Enchantment inf = Enchantment.getEnchantment(Enchantment.ID_BOW_INFINITY);
                infbow.addEnchantment(inf);
                player.getInventory().addItem(infbow);
                break;
            case (47):
                Item ironSwordKB = new ItemSwordIron();
                Enchantment kb = Enchantment.getEnchantment(Enchantment.ID_KNOCKBACK);
                ironSwordKB.addEnchantment(kb);
                player.getInventory().addItem(ironSwordKB);
                break;
            case (100):
                Item superAxe = new ItemAxeDiamond();
                superAxe.setLore("The best axe ever");
                superAxe.setCustomName("SUPER AXE");
                Enchantment knockback = Enchantment.getEnchantment(Enchantment.ID_KNOCKBACK);
                superAxe.addEnchantment(knockback);
                player.getInventory().addItem(superAxe);
                break;
            default:
                break;
        }

    }
}
