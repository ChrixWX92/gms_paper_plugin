package com.gms.paper.level.gslevels.rules;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import java.util.EnumMap;
import java.util.Map;

public class GSGameRules {
    private final EnumMap<GSGameRule, Object> gsGameRules = new EnumMap<>(GSGameRule.class);
    private boolean stale;

    private GSGameRules() {
    }

    public static GSGameRules getDefault() {
        GSGameRules gsGameRules = new GSGameRules();

        gsGameRules.gsGameRules.put(GSGameRule.ELYTRA_ENABLED, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.LOSE_AIR, false); //TODO: Cannot figure this out just yet - can't die from it though
        gsGameRules.gsGameRules.put(GSGameRule.OPEN_CHEST, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.USE_CRAFTING_TABLE, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.USE_ANVIL, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.BECOME_HUNGRY, false); // TODO: Works, but updates randomly
        gsGameRules.gsGameRules.put(GSGameRule.CAN_SUFFOCATE, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.MOVE_RH, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.BUILD_NETHER_PORTAL, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.DESTROY_BEDROCK, false); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.CAN_FLY, false); //TODO: Unsure - how to activate?
        gsGameRules.gsGameRules.put(GSGameRule.PLAYER_INVINCIBLE, true); // DONE
        gsGameRules.gsGameRules.put(GSGameRule.HANDLE_DEATH, true); // DONE
        return gsGameRules;
    }

    public Map<GSGameRule, Object> getGSGameRules() {
        return ImmutableMap.copyOf(this.gsGameRules);
    }

    public boolean isStale() {
        return this.stale;
    }

    public void refresh() {
        this.stale = false;
    }

    public void setGSGameRule(GSGameRule gsGameRule, boolean value) {
        if (!this.gsGameRules.containsKey(gsGameRule)) {
            throw new IllegalArgumentException("GSGamerule does not exist");
        } else {
            if (this.gsGameRules.get(gsGameRule) instanceof Boolean) {
                this.gsGameRules.put(gsGameRule, value);
                this.stale = true;
            } else {
                throw new IllegalArgumentException("GSGamerule not of value type.");
            }
        }
    }

    public void setGSGameRule(GSGameRule gsGameRule, int value) {
        if (!this.gsGameRules.containsKey(gsGameRule)) {
            throw new IllegalArgumentException("GSGamerule does not exist");
        } else {
            if (this.gsGameRules.get(gsGameRule) instanceof Integer) {
                this.gsGameRules.put(gsGameRule, value);
                this.stale = true;
            } else {
                throw new IllegalArgumentException("GSGamerule not of value type.");
            }
        }
    }

    public void setGSGameRule(GSGameRule gsGameRule, float value) {
        if (!this.gsGameRules.containsKey(gsGameRule)) {
            throw new IllegalArgumentException("GSGamerule does not exist");
        } else {
            if (this.gsGameRules.get(gsGameRule) instanceof Float) {
                this.gsGameRules.put(gsGameRule, value);
                this.stale = true;
            } else {
                throw new IllegalArgumentException("GSGamerule not of value type.");
            }
        }
    }

    public void setGSGameRules(GSGameRule gsGameRule, String value) throws IllegalArgumentException {
        Preconditions.checkNotNull(gsGameRule, "gsGameRule");
        Preconditions.checkNotNull(value, "value");

        if (this.gsGameRules.get(gsGameRule) instanceof Boolean) {
            if (value.equalsIgnoreCase("true")) {
                this.setGSGameRule(gsGameRule, true);
            } else {
                if (!value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Was not a boolean");
                }
                this.setGSGameRule(gsGameRule, false);
            }
        }
        else if (this.gsGameRules.get(gsGameRule) instanceof Integer) this.setGSGameRule(gsGameRule, Integer.parseInt(value));
        else if (this.gsGameRules.get(gsGameRule) instanceof Float) this.setGSGameRule(gsGameRule, Float.parseFloat(value));
    }

    public boolean getBoolean(GSGameRule gsGameRule) {
        if (this.gsGameRules.get(gsGameRule) instanceof Boolean) return (boolean) this.gsGameRules.get(gsGameRule);
        else throw new IllegalArgumentException("GSGamerule not of value type.");
    }

    public int getInteger(GSGameRule gsGameRule) {
        if (this.gsGameRules.get(gsGameRule) instanceof Integer) return (int) this.gsGameRules.get(gsGameRule);
        else throw new IllegalArgumentException("GSGamerule not of value type.");
    }

    public float getFloat(GSGameRule gsGameRule) {
        if (this.gsGameRules.get(gsGameRule) instanceof Float) return (float) this.gsGameRules.get(gsGameRule);
        else throw new IllegalArgumentException("GSGamerule not of value type.");
    }

    public String getString(GSGameRule gsGameRule) {
        Preconditions.checkNotNull(gsGameRule, "gsGameRule");
        return this.gsGameRules.get(gsGameRule).toString();
    }

    public Class<?> getGSGameRuleType(GSGameRule gsGameRule) {
        Preconditions.checkNotNull(gsGameRule, "gsGameRule");
        return this.gsGameRules.get(gsGameRule).getClass();
    }

    public boolean hasRule(GSGameRule gsGameRule) {
        return this.gsGameRules.containsKey(gsGameRule);
    }

    public GSGameRule[] getRules() {
        return this.gsGameRules.keySet().toArray(new GSGameRule[0]);
    }

}


