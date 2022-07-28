package com.gms.paper.interact;

import org.bukkit.entity.Player;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.paper.Main;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.util.Helper;

import java.io.IOException;
import java.util.ArrayList;

public class StaticWordList_InteractionHandler extends InteractionHandler {
    public ArrayList<String> wordList = new ArrayList<>();
    public String chosenWord;
    public boolean isRandom = false;

    public String chooseAndRemoveAWordFromList(ArrayList<String> list, Player player, boolean isRandom) {
        int index;
        if (isRandom) {
            index = Helper.generateRandomIntIntRange(0, wordList.size() - 1);
        }
        else {
            index = wordList.size();
        }

        chosenWord = wordList.get(index);
        wordList.remove(chosenWord);

        String msg = "Find " + "\n" + chosenWord;
        Helper.setPlayerTitle(player, msg);
        //Log.logAndSend(player, msg);

        new NukkitRunnable(){

            String word = chosenWord;
            Level level = player.level;

            @Override
            public void run () {

                if (word == chosenWord && wordList.size() >= 1 && player.level == level){
                    Helper.setPlayerTitle(player, "", "The word is... " + chosenWord, 10, 100, 20);
                }

                else {
                    cancel();
                }
            }
        }.runTaskTimer(Main.s_plugin, 400, 400);

        return chosenWord;
    }

    @Override
    public void setupQuestion(GamePosition questionLocation) throws InvalidBackendQueryException, IOException {
        chooseAndRemoveAWordFromList(wordList, player, isRandom);
        super.setupQuestion(questionLocation);
    }

    protected void copyFrom(StaticWordList_InteractionHandler rhs) {
        wordList = rhs.wordList;
        chosenWord = rhs.chosenWord;
        isRandom = rhs.isRandom;
    }
}
