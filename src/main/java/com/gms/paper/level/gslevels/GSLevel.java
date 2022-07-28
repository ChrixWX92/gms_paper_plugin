package com.gms.paper.level.gslevels;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.format.LevelProvider;
import com.gms.paper.level.gslevels.rules.GSGameRules;
import com.gms.paper.util.Helper;

public class GSLevel extends Level {

    private GSLevel parent;
    private GSLevel[] children;
    private GSGameRules gsGameRules;

    public GSLevel(Level level) {
        this(level, null);
    }

    public GSLevel(Level level, GSLevel parent) {
        this(level, parent, new GSLevel[0]);
    }

    public GSLevel(Level level, GSLevel parent, GSLevel[] children) {
        this(level, parent, children, GSGameRules.getDefault());
    }

    public GSLevel(Level level, GSLevel parent, GSLevel[] children, GSGameRules gsGameRules) {
        this(level, parent, children, gsGameRules, level.getProvider().getClass());
    }
    //LevelProviderManager.getProviderByName("world")

    public GSLevel(Level level, GSLevel parent, GSLevel[] children, GSGameRules gsGameRules, Class<? extends LevelProvider> provider) {
        this(level.getServer(), level, parent, children, gsGameRules, provider);
    }

    public GSLevel(Server server, Level level, GSLevel parent, GSLevel[] children, GSGameRules gsGameRules, Class<? extends LevelProvider> provider) {
        this(server, Helper.getCleanLevelName(level.getName()), Helper.getWorldPath(level.getName()), parent, children, gsGameRules, provider);
    }

    public GSLevel(Server server, String levelName, String levelPath, GSLevel parent, GSLevel[] children, GSGameRules gsGameRules, Class<? extends LevelProvider> provider) {
        super(server, levelName, levelPath, provider);

        this.parent = parent;
        this.children = children;
        this.gsGameRules = gsGameRules;
    }

    public GSLevel getParent() {
        return parent;
    }

    public void setParent(GSLevel level) {
        this.parent = level;
    }

    public GSLevel[] getChildren() {
        return children;
    }

    public void setChildren(GSLevel[] levels) {
        this.children = levels;
    }

    public GSGameRules getGSGameRules() {
        /*for (Map.Entry e : gsGameRules.getGSGameRules().entrySet()) {
            Log.debug(e.getKey().toString());
            Log.debug(e.getValue().toString());
        }*/
        return gsGameRules;
    }

    public void setGSGameRules(GSGameRules gsGameRules) {
        this.gsGameRules = gsGameRules;
    }
}
