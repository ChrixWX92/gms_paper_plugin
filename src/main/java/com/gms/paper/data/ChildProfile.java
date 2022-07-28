package com.gms.paper.data;

import org.bukkit.entity.Player;
import com.gms.paper.MCServer;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonObject;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChildProfile {
    public String _id;
    public JsonObject parent;
    public Lesson[] lessons;
    public Lesson[] lessonsFinished;
    public int tickets;
    public String minecraftId = "";

    private transient LessonProgress _lessonProgress;

    public String getId() { return _id; }

    public boolean isMinecraftPlayer(String minecraftId) {
        if (Helper.isLocalDev())
            return true;

        return this.minecraftId.equals(minecraftId);
    }

    public boolean isDev() {
        return _id.equals(Helper.s_mongoId);
    }

    public Path getProfileDir() {
        Path rootDir;
        if (isDev())
            rootDir = Helper.getNukkitDevDir();
        else
            rootDir = Helper.getDrvRoot();

        String[] parts = Helper.splitMongoId(_id);
        Path path = Paths.get(rootDir.toString(), Helper.s_profileDirName, parts[0], parts[1], parts[2]);
        File file = new File(path.toString());

        if (!file.exists())
            file.mkdirs();

        return path;
    }

    public LessonProgress getProgress(QuestionIdInfo idInfo, GamePosition pos) {
        if (_lessonProgress == null || !_lessonProgress.contentId.equals(idInfo.lessonContentId)) {
            _lessonProgress = LessonProgress.loadOrCreate(idInfo, pos);
        }

        if (idInfo.questionSetId != _lessonProgress.currentQuestionSet() || idInfo.questionId != _lessonProgress.currentQuestion())
            _lessonProgress.setCurrent(idInfo.questionSetId, idInfo.questionId);

        return _lessonProgress;
    }

    public void showTicketsStatus(Player player, String msg) {
        if (msg != null && !msg.isEmpty())
            player.sendMessage(msg);
        else
            player.sendMessage(String.format("Tickets: %d", tickets));
    }

    public void showTicketsStatus(Player player) {
        showTicketsStatus(player, null);
    }

    public boolean setTickets(Player p, int tickets) {
        if (!Helper.isDev())
            return false;

        int delta = this.tickets - tickets;

        if (delta != 0) {
            Log.logAndSend(p, String.format("Set tickets called with: %d [Current: %d, Delta: %d]", tickets, this.tickets, -delta));
            modifyTickets(delta);
        }

        return true;
    }

    /// ticketsSpent value can be +ve, meaning tickets are getting redeemed
    private void modifyTickets(int amendment) {
        if (Helper.isLocalDev()) {
            tickets -= amendment;
            return;
        }

        /// Send api request
        tickets = MCServer.getAPI().updateTickets("child-profiles/modify-tickets", new String[][] {
                { "ticketsOwned", Integer.toString(tickets) },
                { "ticketsSpent", Integer.toString(amendment) }
        });
    }

    public void earnTickets(int tickets) {
        modifyTickets(-tickets);
    }

    public void spendTickets(int tickets) {
        modifyTickets(tickets);
    }

    public void setCurrentProgress(LessonProgress progress) {
        _lessonProgress = progress;
    }

    public LessonProgress getCurrentProgress() {
        return _lessonProgress;
    }
}
