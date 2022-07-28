package com.gms.paper.data;

import cn.nukkit.command.CommandSender;
import cn.nukkit.math.Vector3;
import com.gms.paper.Main;
import com.gms.paper.interact.puzzles.handlers.anchors.AnchorHandler;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Lesson extends GenericContent {
    public String title;
    public float durationMinutes;
    public JsonObject metadata;

    @SerializedName("questionSets")
    public JsonArray _questionSetInfo;

    public String type;
    public String worldSettingsCategory;
    public boolean worldChangesSaved;

    private transient ArrayList<QuestionSet> _questionSets = new ArrayList<QuestionSet>();
    private transient HashMap<String, QuestionSet> _questionSetLUT = new HashMap<>();
    private transient Activity _activity;

//    public QuestionSet[] questionSets;
    public Vector3D spawnPos;
    public String description;

    public Lesson cache() {
        /// If already cached
        if (isCached())
            return this;

        for (int i = 0; i < _questionSetInfo.size(); i++) {
            _questionSets.add(null);
            getQuestionSet(i);
        }

        return setCached();
    }

    public int getNumQuestionSets() {
        return _questionSetInfo.size();
    }

    public boolean isUserBuildZone() {
        return worldSettingsCategory.equalsIgnoreCase("Buildzone") ||
                worldSettingsCategory.equalsIgnoreCase("Zoo");
    }
    public boolean isFunZone() { return worldSettingsCategory.equalsIgnoreCase("Funzone"); }
    public boolean isLearningZone() { return worldSettingsCategory.equalsIgnoreCase("Learning"); }

    public File initUserBuildZone(Path profileDir) {
        return initUserWorld(profileDir, null);
    }

    public File initLessonZone(Path profileDir, String sessionKey) {
        return initUserWorld(profileDir, sessionKey);
    }

    public File initUserWorld(Path profileDir, String sessionKey) {
        String cleanContentId = contentId.replace(".", "");
        var srcPath = new File(Helper.getNukkitWorldDir(cleanContentId).toString()); // new File(Paths.get(Main.s_plugin.getServer().getDataPath(), "worlds", cleanContentId).toString());
        File dstPath = null;
        boolean hasSessionKey = sessionKey != null && !sessionKey.isEmpty();

        if (!hasSessionKey)
            dstPath = new File(Paths.get(profileDir.toString(), cleanContentId).toString());
        else
            dstPath = new File(Paths.get(Helper.getSessionDir().toString(), sessionKey, cleanContentId).toString());

        if (!srcPath.exists()) {
            Log.error(String.format("Unable to find source world: %s", srcPath.toString()));
            return null;
        }

        /// The world already exists in the user profile ... don't do anything (only if not enforced)
        if (dstPath.exists()) {
            if (!hasSessionKey)
                return dstPath;
            else {
                Log.debug(String.format("Deleting directory: %s", dstPath));
                /// This directory needs to be cleared out
                Helper.deleteDirectory(dstPath);
            }
        }

        if (!dstPath.exists())
            dstPath.mkdirs();

        Log.debug(String.format("Fresh copy world %s: %s => %s", contentId, srcPath.toString(), dstPath.toString()));

        Helper.copyFolder(new File(srcPath.toString()), new File(dstPath.toString()));

        return dstPath;
    }

    public boolean restoreFromUserProfile(Path profileDir) {
        String cleanContentId = cleanContentId();
        var srcPath = new File(Paths.get(profileDir.toString(), cleanContentId).toString());
        var dstPath = new File(Helper.getNukkitWorldDir(cleanContentId).toString());

        if (!srcPath.exists())
            return false;

        Log.debug(String.format("Restoring %s: %s => %s", contentId, srcPath.toString(), dstPath.toString()));

        Helper.copyFolder(srcPath, dstPath);

        return true;
    }

    public QuestionSet getQuestionSet(CommandSender sender, QuestionIdInfo idInfo) {
        QuestionSet qset = _questionSetLUT.get(idInfo.questionSetContentId);
        return qset;
//        if (idInfo.questionSetId < 0 || idInfo.questionSetId > _questionSets.size()) {
//            Log.logAndSend(sender, String.format("QuestionSet index out of range: %d [Total QuestionSets: %d, QID: %s]", idInfo.questionSetId, _questionSets.size(), idInfo.fullId));
//            return null;
//        }
//
//        return _questionSets.get(idInfo.questionSetId);
    }

    public static Lesson getId(String id) {
        return CmsApi.s_public.getContentFromId("lessons", id, Lesson.class);
    }

    public static Lesson get(String contentId) {
        return CmsApi.s_public.getContent("lessons", contentId, Lesson.class);
    }

    public QuestionSet getQuestionSet(int index) {
        if (index < 0 || index >= _questionSetInfo.size())
            throw new IndexOutOfBoundsException(String.format("Index out of range for lesson questionSet: %d [Max = %d]", index, _questionSetInfo.size()));

        if (_questionSets.get(index) != null)
            return _questionSets.get(index);

        /// Make sure the array list is the correct size
        for (int i = _questionSets.size(); i <= index; i++)
            _questionSets.add(null);

        String questionSetId = _questionSetInfo.get(index).toString().replace("\"", "");
        if (!Helper.isMongoId(questionSetId))
            questionSetId = _questionSetInfo.get(index).getAsJsonObject().get("_id").getAsString();

        QuestionSet qset = QuestionSet.getId(questionSetId);
        assert qset != null;

        _questionSetLUT.put(qset._id, qset);
        _questionSetLUT.put(qset.contentId, qset);

        /// Cache the question set
        qset.cache();

        _questionSets.set(index, qset);

        return qset;
    }

    synchronized public void tick() {
        if (_activity != null)
            _activity.tick();
    }

    private void applyGameRules() {
        /// Chris to add
    }

    synchronized public void enter(String worldPath) {
        /// Cache this lesson first ...
        cache();

        var gsLevel = Main.getGsLevelManager().initGSLevel(cleanContentId(), worldPath);
        Main.getGsLevelManager().setCurrent(gsLevel);

        AnchorHandler.resetAnchorScan();
        applyGameRules();

        if (Helper.isLocalDev())
            return;

        _activity = Activity.begin(Activity.Type.s_lesson, new String[][] {
                { "contentId", contentId },
                { "lessonId", _id }
        });
    }

    synchronized public void exit() {
//        if (isUserBuildZone())
//            backupToUserProfile(User.getCurrent().getProfile().getProfileDir());

        if (Helper.isLocalDev() || _activity == null)
            return;

        _activity.end();
        _activity = null;
    }
}
