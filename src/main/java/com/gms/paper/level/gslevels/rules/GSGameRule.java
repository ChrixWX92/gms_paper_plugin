package com.gms.paper.level.gslevels.rules;

import java.util.Optional;

public enum GSGameRule {

    ELYTRA_ENABLED("elytraEnabled"),
    LOSE_AIR("loseAir"),
    OPEN_CHEST("openChest"),
    USE_CRAFTING_TABLE("useCraftingTable"),
    USE_ANVIL("useAnvil"),
    CAN_SUFFOCATE("canSuffocate"),
    BECOME_HUNGRY("becomeHungry"),
    MOVE_RH("moveRH"),
    BUILD_NETHER_PORTAL("buildNetherPortal"),
    DESTROY_BEDROCK("destroyBedrock"),
    CAN_FLY("canFly"),
    PLAYER_INVINCIBLE("playerInvincible"),
    HANDLE_DEATH("handleDeath");

    private final String name;

    GSGameRule(String name) {
        this.name = name;
    }

    public static Optional<GSGameRule> parseString(String gsGameRuleString) {
        GSGameRule[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            GSGameRule gsGameRule = var1[var3];
            if (gsGameRule.getName().equalsIgnoreCase(gsGameRuleString)) {
                return Optional.of(gsGameRule);
            }
        }

        return Optional.empty();
    }

    public static String[] getNames() {
        String[] stringValues = new String[values().length];

        for(int i = 0; i < values().length; ++i) {
            stringValues[i] = values()[i].getName();
        }

        return stringValues;
    }

    public String getName() {
        return this.name;
    }

}

