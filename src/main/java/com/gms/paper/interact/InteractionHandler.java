package com.gms.paper.interact;

import com.gms.paper.util.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.gms.paper.PlayerInstance;
import com.gms.paper.data.*;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.legacy.MCTP_InteractionHandler;
import com.gms.paper.interact.mcq.MCQ_InteractionHandler;
import com.gms.paper.interact.puzzles.handlers.*;
import com.gms.paper.interact.puzzles.maths.MATHS_InteractionHandler;
import com.gms.paper.interact.puzzles.utils.TextCleaner;
import com.gms.paper.interact.tpqs.TPQS_InteractionHandler;
import com.gms.paper.interact.treasureHunt.THA_InteractionHandler;
import com.gms.paper.interact.treasureHunt.THTP_InteractionHandler;
import com.gms.paper.interact.puzzles.PuzzleType;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

public class InteractionHandler {
    static HashMap<String, InteractionHandler> s_handlers = new HashMap<>();

    protected static InteractionHandler s_questionSetup;
    protected static InteractionHandler s_curr;
    protected static InteractionHandler s_pendingTeleport;

    protected static long lastPressed = System.currentTimeMillis();

    public static void reset() {
        s_questionSetup = null;
        s_curr = null;
        s_pendingTeleport = null;
    }

    public static boolean isPendingTeleport() {
        return s_pendingTeleport != null;
    }

    public static InteractionHandler getPendingTeleport() {
        return s_pendingTeleport;
    }

    public static Vector3D s_questionOffset = new Vector3D(0, 2, -6);
    public static Vector3D[] s_answerOffsets = new Vector3D[]{
            new Vector3D(-4, 0, -3),
            new Vector3D(-2, 0, -3),
            new Vector3D(0, 0, -3),
            new Vector3D(2, 0, -3),
            new Vector3D(4, 0, -3)
    };
    public static Vector3D s_extraPromptOffset = new Vector3D(0, 1, -6);

    public static Vector3D s_questionAdditionalInfoOffset = new Vector3D(0, 2, 0);

    public static Vector3D s_lessonFinishedOffset = new Vector3D(0, 10, 0);

    public static Vector3D s_answerBlockOffset = new Vector3D(0, -2, -1);
    public static Vector3D s_blockSignOffset = new Vector3D(0, -2, 0);
    public static String s_answerMarker = "§3";
    public static String s_answerBlockName = Material.GOLD_BLOCK.name();
    public static String s_stoneButtonName = "Stone Button";
    public static String s_woodenButtonName = "Wooden Button";

    public static void handleInteraction(World world, PlayerInteractEvent event, String buttonType) throws IOException, InvalidBackendQueryException {
        //reset previous handler state
        if (s_questionSetup != null && !buttonType.equals("QA")) {
            try {
                s_questionSetup.resetHandlerState(world);
            }
            catch (Exception e) {
                Log.exception(e, "Exception while resetting question setup. Clearing s_questionSetup!");
                s_questionSetup = null;
            }
        }

        InteractionHandler handler = InteractionHandler.getHandler(buttonType);

        /// Must have a handler for every button type
        if (handler == null)
            throw new RuntimeException(String.format("Unable to find handler for button type: %s", buttonType));

        Constructor[] ctors = handler.getClass().getConstructors();
        InteractionHandler handlerInst = null;

        try {
            handlerInst = (InteractionHandler) ctors[0].newInstance();
        }
        catch (Exception e) {
            Log.exception(e, String.format("Unable to create instance of handler class: %s [Button Type: %s]", handler.getClass().getName(), buttonType));
        }

        handlerInst.handle(event);

    }

    public static void initHandlers() {
        s_handlers.put("QA", new QA_InteractionHandler());
        s_handlers.put("TPQS", new TPQS_InteractionHandler());
        s_handlers.put("MCQ", new MCQ_InteractionHandler());
        s_handlers.put("WORLD", new WORLD_InteractionHandler());
        s_handlers.put("TITLE", new TITLE_InteractionHandler());
        s_handlers.put("MCTP", new MCTP_InteractionHandler());
        s_handlers.put("OPEN", new OPEN_InteractionHandler());
        s_handlers.put("RANDOM", new RANDOM_InteractionHandler());
        s_handlers.put("THA", new THA_InteractionHandler());
        s_handlers.put("THTP", new THTP_InteractionHandler());
        s_handlers.put("TP", new TP_InteractionHandler());
        s_handlers.put("TPT", new TPT_InteractionHandler());
        s_handlers.put("SHOP", new SHOP_InteractionHandler());
        s_handlers.put("BUY", new BUY_InteractionHandler());

        /// Maths related
        s_handlers.put("SUM", new MATHS_InteractionHandler());
        s_handlers.put("GRID", new MATHS_InteractionHandler());
        s_handlers.put("PILLARS", new MATHS_InteractionHandler());
        s_handlers.put("ANVILS", new MATHS_InteractionHandler());
        s_handlers.put("PEN", new MATHS_InteractionHandler());
        s_handlers.put("NSEARCH", new MATHS_InteractionHandler());
        s_handlers.put("TOWER", new MATHS_InteractionHandler());
        s_handlers.put("ISLANDS", new MATHS_InteractionHandler());
        s_handlers.put("FARM", new MATHS_InteractionHandler());
        s_handlers.put("FREEFALL", new MATHS_InteractionHandler());
        s_handlers.put("SUBMIT", new MATHS_InteractionHandler());
        s_handlers.put("COUNT", new MATHS_InteractionHandler());
        for (String puzzleType : PuzzleType.abbreviations()) s_handlers.put(puzzleType, new MATHS_InteractionHandler());

        /// New puzzle formats
        s_handlers.put("PUZZLE", new PUZZLE_InteractionHandler());
        s_handlers.put("CHECKS", new CHECKS_InteractionHandler());
        s_handlers.put("MOBGROUP", new MOBGROUP_InteractionHandler());
        s_handlers.put("PAIRS", new PAIRS_InteractionHandler());
        s_handlers.put("ASSEMBLE", new ASSEMBLE_InteractionHandler());
    }

    static InteractionHandler getHandler(String type) {
        return s_handlers.get(type);
    }

    public static String[] getSignInfo(World world, GamePosition signLocation) {
        try {


           BlockEntitySign blockEntitySign = (BlockEntitySign) world.getBlockEntity(signLocation.round());

            if (blockEntitySign == null) {
                GamePosition signLocMCQ = new GamePosition(signLocation, new Location(0, -1, 0), false); // sign can be 3 blocks down for MCQ
                blockEntitySign = (BlockEntitySign) world.getBlockEntity(signLocMCQ.round());
            }

            String[] signText = blockEntitySign.getText();

            //Java adds in black formatting which messes up everything so get rid of it here
            for (int i = 0; i < signText.length; i++) {
                signText[i] = signText[i].replaceAll("§0", "");
            }

            return signText;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String[] getSignInfo(BlockEntitySign blockEntitySign) {
        try {
            String[] signText = blockEntitySign.getText();

            //Java adds in black formatting which messes up everything so get rid of it here
            for (int i = 0; i < signText.length; i++) {
                signText[i] = signText[i].replaceAll("§0", "");
            }

            return signText;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String[] getPhraseBank() {
        String[] pb = {
                "§2Correct, \nwell done!",
                "§3Not quite, \nbut try again!",
                "§3Not quite, \ntry asking the pigs\n what they like!",
                "§3Not quite, \ndo you know what kind\n of creature might howl?",
                "§3Not quite, \nkeep exploring \nthe house!",
                "§3Not quite, \nmaybe try asking \nthe pigs for help!",
                "§4Not quite, \ntry looking at the \nitems in the frame!",
                "§3Not quite, \ntry asking Steve\n for help!",
                "§3Not quite, \nhave a look at the\n story on the wall!",
                "§3Not quite, \nyou should have a \nlook in the kitchen!",
                "§3Not quite, \nmake sure you talk\n to all the chickens!",
                "§3Not quite, \nmake sure to talk \nto all the animals \nagain!",
                "§3Not quite, \nhave a look in\n the shop!",
                "§3Mr. Cow has \ngone to jail",
                "§3Mr. Cow has \ngiven back the \ndiamonds"
        };
        return pb;
    }

    public static boolean populateSign(Level level, String text, Vector3D signLocation) {
        return populateSign(level, text, signLocation, false);
    }

    public static boolean populateSign(Level level, String text, Vector3D signLocation, boolean clearText) {
        //Make this so it lights up more than one line
        //if length of string is more than 13 or 14 characters
        if (!(level.getBlockEntity(signLocation) instanceof BlockEntitySign)) {
            return false;
        }
        String cleanText = "";

        TextCleaner tc = new TextCleaner(text);
        cleanText = tc.getCleanedLatin();

        BlockEntitySign blockEntitySign = (BlockEntitySign) level.getBlockEntity(signLocation);
        String[] signText = blockEntitySign.getText();
        if (clearText) {
            for (int i = 0; i < signText.length; i++) {
                signText[i] = "";
            }
        }

        Log.debug(String.format("Changed block at location (%s) %s => %s", signLocation.toString(), signText[0], cleanText));
        signText[0] = cleanText;

        return blockEntitySign.setText(signText);
    }

    public static boolean populateItemFrame(Level level, String filename, Vector3D frameLocation) {
        if (!(level.getBlockEntity(frameLocation) instanceof BlockEntityItemFrame)) {
            return false;
        }

        ItemMap map = new ItemMap();
        BufferedImage image = null;

        try {
            File filePath = new File(Helper.getImagesDir().toFile(), filename);
            image = ImageIO.read(filePath);
        }
        catch (IOException ioException) {
            //ioException.printStackTrace();
            String nameError = new StringBuilder().append(TextFormat.RED + "No " + filename + "file found in " + Helper.getImagesDir()).append(". Be sure to include the file extension in your command (e.g./addmap image.png, rather than /addmap image.)").toString();
            Log.error(nameError);
            ;
            return false;
        }
        map.setImage(image);

        BlockEntityItemFrame itemFrameEntity = (BlockEntityItemFrame) level.getBlockEntity(frameLocation);
        itemFrameEntity.setItem(map);
        return true;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////
    protected PlayerInteractEvent event;
    protected PlayerInteractEvent.Action action;
    protected Player player;
    protected Block buttonBlock;
    protected GamePosition blockLoc;
    protected GamePosition signLoc;
    protected Level level;
    protected Block signBlock;
    protected String buttonType;
    protected String[] signText;
    protected QuestionIdInfo idInfo;

    protected PlayerInstance playerInstance;
    protected ChildProfile profile;

    protected GamePosition questionPos;

    protected Course course;
    protected Lesson lesson;
    protected QuestionSet questionSet;
    protected Question question;
    protected IAnswer answer;

    protected long questionStartTime;
    protected long questionEndTime;

    protected GamePosition targetPos;

    protected GamePosition teleportPlayer(Player player, GamePosition location, InteractionHandler pendingHandler) {
        //save checkpoint
        saveCheckpoint();

        s_pendingTeleport = pendingHandler;
        return teleportPlayer(player, location);
    }

    protected GamePosition teleportPlayer(Player player, GamePosition location) {
        Log.debug(String.format("Teleporting: %s ", location.toString()));
        player.teleport(location);
        return location;
    }

    protected GamePosition teleportPlayerPostQuestionSet(Player player, GamePosition location) {
        Log.debug(String.format("Teleporting: %s ", location.toString()));
        s_pendingTeleport = null;
        player.teleport(location);
        saveCheckpoint();
        return location;
    }

    protected void resetStartTime() {
        questionStartTime = System.currentTimeMillis();
        questionEndTime = questionStartTime;
    }

    public IAnswer.Feedback getCorrectFeedback() {
        if (answer == null || answer.getCorrectFeedback() == null)
            return new IAnswer.Feedback("Correct!");
        return answer.getCorrectFeedback();
    }

    public IAnswer.Feedback getIncorrectFeedback(IAnswer.Info info) {
        if (answer == null || answer.getIncorrectFeedback(info) == null)
            return new IAnswer.Feedback("Not quite,\nbut try again!");
        return answer.getIncorrectFeedback(info);
    }

    protected void startQuestion() {
        resetStartTime();
    }

    public void endQuestion() {
        questionEndTime = System.currentTimeMillis();
    }

    public long getQuestionAttemptTime() {
        questionEndTime = System.currentTimeMillis();
        return questionEndTime - questionStartTime;
    }

    public String getQuestionText(Question q) {
        return q.prompt;
    }

    public void initHandleInfo(PlayerInteractEvent event) {
        action = event.getAction();
        player = event.getPlayer();
        buttonBlock = event.getBlock();
        blockLoc = new GamePosition(null, buttonBlock.getLocation(), true);
        signLoc = new GamePosition(blockLoc, new Location(0, -2, 0), false); /// buttonBlock.getLocation().add(new Location(0, -2, 0)); //info sign
        level = buttonBlock.getLocation().level;
        signBlock = level.getBlock(signLoc);

        signText = getSignInfo(level, signLoc);
        buttonType = signText[0].split(",")[0]; //Where the button/activity type is stored

        idInfo = new QuestionIdInfo(signText[1].trim());

        playerInstance = PlayerInstance.getPlayer(player.getName());
        if (playerInstance != null)
            profile = playerInstance.getProfile();
        else
            Log.error(String.format("Unable to find player with name: %s", player.getName()));
    }

    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        s_curr = this;

        initHandleInfo(event);

        course = Course.get(idInfo.courseId);
        if (course != null) {
            lesson = course.getLesson(player, idInfo);
        }

    }

    public boolean isCorrectAnswer() {
        Block answerBlock = level.getBlock(signLoc.add(s_answerBlockOffset));
        return answerBlock.getName().equals(s_answerBlockName);
    }

    public void setupQuestion(GamePosition questionLocation) throws InvalidBackendQueryException, IOException {
        /// Just mark the current handler as the one that set up the last question
        s_questionSetup = this;

        startQuestion();
    }

    public void showQuestionSetProgress(Vector3D questionLocation) {
        LessonProgress progress = profile.getCurrentProgress();
        int currentQuestion = progress.currentQuestion();
        int totalQuestions = questionSet.numQuestions(progress.level);
        String holoText = String.format(("§e Question %s / %s"), currentQuestion, totalQuestions);

        //do not update hologram if branching question
        if (currentQuestion > 100) {
            return;
        }

        //get correct hologram offset based on sign facing position
        BlockFace facing =  EntityDirectionHelper.getSignFacingDirection((BlockEntitySign) level.getBlockEntity(questionLocation));
        Vector3D holoLocation = questionLocation;
        switch(facing){
            case NORTH -> holoLocation = questionLocation.add(0.5, 1, 1);
            case SOUTH -> holoLocation = questionLocation.add(0.5, 1, 0);
            case EAST -> holoLocation = questionLocation.add(0, 1, 0.5);
            case WEST -> holoLocation = questionLocation.add(1, 1, 0.5);
        }

        HologramEntity progressHologramEntity = HologramHelper.getHologramEntity(level, "Progress");
        var prevLocation = HologramHelper.getHologramLocation(progressHologramEntity);

        //if previous hologram is not in the same location as the question location then close the hologram
        if (progressHologramEntity != null && prevLocation != holoLocation) {
            progressHologramEntity.closeHologram();
            progressHologramEntity = null;
        }

        if (progressHologramEntity == null) {
            CompoundTag holoTag = HologramHelper.hologramNBT(player, holoLocation);
            HologramHelper.spawnBasicHologram(player, "Progress", holoText, holoTag);

            return;
        }

        HologramHelper.updateHologramText(player, progressHologramEntity, holoText);

    }

    public void removeProgressHologram(){
        HologramEntity progressHologramEntity = HologramHelper.getHologramEntity(level, "Progress");
        if(progressHologramEntity != null) {
            progressHologramEntity.closeHologram();
        }
    }

    protected GamePosition getWorldSpawnPosition(String signText) {
        GamePosition spawnPos = Helper.parseLocation(signText);
        GamePosition spawnPosWorld = spawnPos;

        if (!spawnPos.absolute) {
            spawnPosWorld = signLoc.add(spawnPos);
        }

        return spawnPosWorld;
    }

    public void postTeleport() {
        try {
            setupQuestion(questionPos);
        }
        catch (Exception e) {
            Log.exception(e, "Error setting up next question post teleport!");
        }
        s_pendingTeleport = null;
    }

    public void resetHandlerState(World world) {
    }

    public <T extends IAnswer> T getAnswer(Class<T> type) {
        return type.cast(answer);
    }

    //For resetting MCQ questions command
    public static InteractionHandler getCurrent() {
        return s_questionSetup;
    }

    public Question getQuestion() {
        return question;
    }

    public LessonProgress getLessonProgress() {
        return profile.getCurrentProgress();
    }

    public GamePosition getTargetPos() {
        return targetPos;
    }

    public GamePosition getQuestionPos() {
        return questionPos;
    }

    private void saveCheckpoint(){
        if (User.getCurrent() != null && User.getCurrent().getState() != null) {
            User.getCurrent().getState().updatePos(new Vector3(player.x, player.y, player.z), (float)player.headYaw);
        }
    }
}
