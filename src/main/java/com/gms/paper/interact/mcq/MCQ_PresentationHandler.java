package com.gms.paper.interact.mcq;

import org.bukkit.World;
import org.bukkit.entity.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.NukkitRunnable;
import com.gms.paper.Main;
import com.gms.paper.data.Question;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.interact.tpqs.TPQS_Answer;
import com.gms.paper.level.gslevels.GSLevel;
import com.gms.paper.util.EntityDirectionHelper;
import com.gms.paper.util.Helper;
import com.gms.paper.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MCQ_PresentationHandler {

    private MCQ_InteractionHandler interactionHandler;
    private Player player;
    private Level world;

    private List<Location> displayLocations;

    private HashMap<Location, Integer> cleanUpLocations = new HashMap<>();
    private List<AnswerBlockData> setUpData = new ArrayList<>();


    private class AnswerBlockData {
        public Block displayWallBlock;
        public BlockFace displayDirection;
        public Block displayBlock;
        public Block button;

        public AnswerBlockData(Block displayWallBlock, BlockFace displayDirection, Block displayBlock, Block button) {
            this.displayWallBlock = displayWallBlock;
            this.displayDirection = displayDirection;
            this.displayBlock = displayBlock;
            this.button = button;
        }
    }

    // different presentation types for MCQ questions (only one atm)
    enum PresentationType {
        SCATTER(0);

        private int value;

        private PresentationType(int value) {
            this.value = value;
        }
    }


    public MCQ_PresentationHandler(Player player, MCQ_InteractionHandler interactionHandler) {
        this.interactionHandler = interactionHandler;
        this.player = player;
        this.world = player.getWorld();
    }

    /***
     * Call this function from an MCQ handler to set up the presentation of the question in the physical space.
     * @param setupType - presentation style to set up
     * @param signLocations - sign locations to get the sign and its data needed to set up the display
     */
    public void setupQuestion(PresentationType setupType, List<Location> signLocations, Question question) {

        switch (setupType) {
            case SCATTER:
                setupScatteredAnswers(signLocations, question);
                break;
        }
    }

    private void setupScatteredAnswers(List<Location> signLocations, Question question) {
        Log.debug(String.format("Set up MCQ presentation: SCATTER"));

        // if answer blocks were removed then bring them back.
        resetAnswerBlocks();

        //sort sign locations based on index. This is important since the answers need to correspond with the index. Order of set up is important.
        Location[] sortedSignLocations = sortSignLocations(signLocations);

        // find display locations
        displayLocations = findDisplayLocations(sortedSignLocations);

        //populate display locations
        populateDisplay(displayLocations, question);

        //clean up unused signs
        cleanUpAnswerBlock(sortedSignLocations);

        if (displayLocations == null || displayLocations.size() == 0) {
            Log.error("No display locations found");
            return;
        }
        if (displayLocations.get(0) == null) {
            Log.error(String.format("Question display location (index 0) should never be null [Content ID : %s]", question.contentId));
            return;
        }

        Location questionDisplayLocation = displayLocations.get(0);

        //if extra prompt available
        populateExtraPrompt(question, questionDisplayLocation);

        //show progress hologram
        interactionHandler.showQuestionSetProgress(questionDisplayLocation);
    }

    private Location[] sortSignLocations(List<Location> signLocations) {
        Location[] sortedSignLocations = new Location[signLocations.size()];

        for (var signLocation : signLocations) {
            BlockEntitySign sign = (BlockEntitySign) world.getBlockEntity(signLocation);
            String[] signText = interactionHandler.getSignInfo(sign);

            String signIndexText = signText[2];
            if (signIndexText.matches("[0-9]+")) { //test if string is numeric
                int signIndex = Integer.parseInt(signIndexText);
                if (!(signIndex > sortedSignLocations.length - 1)) {
                    sortedSignLocations[signIndex] = signLocation;
                }
                else {
                    Log.error(String.format("Error sorting signs based on index: Index value missing [Index: %d, num of signs %d]", signIndex, sortedSignLocations.length));
                }
            }
        }

        return sortedSignLocations;
    }

    private List<Location> findDisplayLocations(Location[] signLocations) {
        // find display locations

        if (signLocations == null) {
            Log.error("Could not find any data signs in the ground");
            return null;
        }

        //sign can be on either side of a block
        Vector3[] displayOffsets = new Vector3[]{
                new Vector3(0, 2, 1),
                new Vector3(0, 2, -1),
                new Vector3(1, 2, 0),
                new Vector3(-1, 2, 0),

                //question signs can be higher
                new Vector3(0, 4, 1),
                new Vector3(0, 4, -1),
                new Vector3(1, 4, 0),
                new Vector3(-1, 4, 0),

        };

        List<Location> displayLocations = new ArrayList<Location>();

        for (int i = 0; i < signLocations.length; i++) {

            if (signLocations[i] == null) {
                Log.error(String.format("Missing QA indexes : [index %s]", i));
                continue;
            }

            for (var displayOffset : displayOffsets) {
                Location displayLocation = signLocations[i].add(displayOffset);
                BlockEntity block = world.getBlockEntity(displayLocation);

                //does this display sign belong to this data sign underneath. Display has to belong to this block and not adjacent block.
                // belonging is checked with the facing direction and offset direction
                if (block != null) {
                    BlockFace displayDirection = getDisplayDirection(block);

                    if (displayDirection != null) {
                        //check facing direction of the sign/item frame and compare with the offset direction
                        if (isDisplayValid(displayDirection, displayOffset)) {
                            displayLocations.add(displayLocation);
                            //addDirectionTag(signLocations[i], displayDirection); // add display direction tag to the data sign. (for removing and adding block)
                            break;
                        }
                    }
                }
            }
        }
        return displayLocations;
    }

    private BlockFace getDisplayDirection(BlockEntity blockEntity) {
        BlockFace displayDirection = null;

        if (blockEntity instanceof BlockEntitySign) {
            displayDirection = EntityDirectionHelper.getSignFacingDirection((BlockEntitySign) blockEntity);
        }
        if (blockEntity instanceof BlockEntityItemFrame) {
            displayDirection = EntityDirectionHelper.getItemFrameFacingDirection((BlockEntityItemFrame) blockEntity);
        }

        return displayDirection;
    }

    //check if display sign found is on the corresponding block
    private boolean isDisplayValid(BlockFace displayDirection, Vector3D displayOffset) {
        //display offset should match the facing direction of the sign
        switch (displayDirection) {
            case NORTH:
                if (displayOffset.z == -1) {
                    return true;
                }
                break;
            case SOUTH:
                if (displayOffset.z == 1) {
                    return true;
                }
                break;
            case EAST:
                if (displayOffset.x == 1) {
                    return true;
                }
                break;
            case WEST:
                if (displayOffset.x == -1) {
                    return true;
                }
                break;

        }
        Log.debug("Display does not belong to this block");
        return false;
    }


    // ISSUE: adding a tag to signs is breaking. Text on the sign disappears if unload and reloaded. (i.e go far away and come back)
/*    private void addDirectionTag(Location signLocation, BlockFace displayDirection) {
        BlockEntity dataSign = level.getBlockEntity(signLocation);
        if(dataSign.namedTag.getString("Direction") != null) {
            dataSign.namedTag = new CompoundTag().putString("Direction", displayDirection.toString().toUpperCase());
        }

        //Log.debug(String.format("String direction : %s", dataSign.namedTag.get("Direction")));
    }*/

    private void populateDisplay(List<Location> displayLocations, Question question) {

        if (displayLocations.size() == 0) {
            Log.error("display locations are null");
            return;
        }

        var sortedAnswer = interactionHandler.getAnswer(TPQS_Answer.class);

        //Currently, signs/text only for question
        if (!InteractionHandler.populateSign(world, Helper.formatQuestionSign(question.prompt), displayLocations.get(0), true)) {
            Log.error(String.format("Error populating question sign : index: %d", 0));
        }

        for (int i = 1; i < displayLocations.size() && i < sortedAnswer.answerOptions.size() + 1; i++) {
            TPQS_Answer.Item ansOption = sortedAnswer.answerOptions.get(i - 1);

            Location displayLocation = displayLocations.get(i);

            //if answer has @ symbol at the start then it's an image with item frame expected
            if (ansOption.answer.startsWith("@")) {
                InteractionHandler.populateItemFrame(world, ansOption.answer.substring(1) + ".png", displayLocation);

            }
            else if (ansOption.answer.isEmpty()) {

                cleanUpLocations.put(displayLocation, i);

            }
            else {// otherwise, it's text and a sign is expected
                if (!InteractionHandler.populateSign(player.getWorld(), Helper.formatAnswerSign(ansOption.answer), displayLocation, true)) {
                    Log.error(String.format("Error populating sign index: %d", i));
                }
            }
        }


        Log.debug(String.format("Finished setting up questionId: %s [Correct Index: %d => %d]", question.contentId, sortedAnswer.orgCorrectAnswerIndex, sortedAnswer.correctAnswerIndex));

    }

    // expecting extra prompt display to be a sign. Can expand this to item frames if needed.
    private void populateExtraPrompt(Question question, Location questionLocation) {

        Vector3D extraPromptDisplayOffset = new Vector3(0, 1, 0);

        Location extraPromptLocation = questionLocation.subtract(extraPromptDisplayOffset);
        BlockEntity extraPromptDisplayEntity = world.getBlockEntity(extraPromptLocation);

        if (question.getExtraPrompt() != null && extraPromptDisplayEntity != null) {

            if (!interactionHandler.populateSign(world, Helper.formatQuestionSign(question.getExtraPrompt()), extraPromptLocation, true)) {
                Log.error(String.format("Error populating extra prompt: %s", extraPromptDisplayEntity));
            }
        }
    }


    // get the location of the data sign from the display location index
    // find the block (wall block) on which the sign/item frame is hanging by using the y valve of the display and the x and z of the data sign
    // find the button location going one up on the wall block.
    // remove the button, display, and the wall block in this order.

    private void cleanUpAnswerBlock(Location[] sortedSignLocations) {

        if (!cleanUpLocations.isEmpty()) {
            for (Location displayLoc : cleanUpLocations.keySet()) {

                //get locations of blocks to remove
                Location dataSignLoc = sortedSignLocations[cleanUpLocations.get(displayLoc)]; //display locations will have the same index as the sorted signs
                Location blockWallLoc = new Location(dataSignLoc.x, displayLoc.y, dataSignLoc.z);
                Location buttonLoc = new Location(blockWallLoc.x, blockWallLoc.y + 1, blockWallLoc.getZ());

                //Get blocks to remove
                //main block with the sign and button
                Block displayWallBlock = world.getBlock(blockWallLoc);
                //sign location and direction
                BlockEntity displayEntity = world.getBlockEntity(displayLoc);
                BlockFace displayDirection = getDisplayDirection(displayEntity);
                Block displayBlock = displayEntity.getBlock();
                //button
                Block buttonBlock = world.getBlock(buttonLoc);

                // store information before removing it
                AnswerBlockData answerBlockData = new AnswerBlockData(
                        displayWallBlock,
                        displayDirection,
                        displayBlock,
                        buttonBlock);

                setUpData.add(answerBlockData);


                new NukkitRunnable() {
                    @Override
                    public void run() {

                        world.setBlock(buttonLoc, new BlockAir(), true, true);
                        world.setBlock(displayLoc, new BlockAir(), true, true);
                        world.setBlock(blockWallLoc, new BlockAir(), true, true);

                    }
                }.runTaskLater(Main.s_plugin, 1);

               /* // remove blocks
                Timer timer = new Timer();
                TimerTask removeTask = new RemoveAnswerBlocks(buttonLoc, displayLoc, blockWallLoc);
                timer.schedule(removeTask, 2);*/

            }
        }
    }

/*    private class RemoveAnswerBlocks extends TimerTask {

        Location buttonLoc;
        Location displayLoc;
        Location blockWallLoc;

        public RemoveAnswerBlocks(Location buttonLoc, Location displayLoc, Location blockWallLoc){
            this.buttonLoc = buttonLoc;
            this.displayLoc = displayLoc;
            this.blockWallLoc = blockWallLoc;
        }

        @Override
        public void run() {

            level.setBlock(buttonLoc, new BlockAir(), true, true);
            level.setBlock(displayLoc, new BlockAir(), true, true);
            level.setBlock(blockWallLoc, new BlockAir(), true, true);
        }
    }*/

    private void resetAnswerBlocks() {
        if (!setUpData.isEmpty()) {

            for (AnswerBlockData data : setUpData) {
                // get removed block and add that back in
                world.setBlock(data.displayWallBlock.getLocation(), data.displayWallBlock, true, true);

                // add button on top
                world.setBlock(data.button.getLocation(), data.button, true, true);

                // add sign/item frame in the stored direction
                data.displayBlock.place(data.displayBlock.toItem(), data.displayBlock, null, data.displayDirection, 0, 0, 0, player);

                BlockEntity blockE = world.getBlockEntity(data.displayBlock.getLocation());
                if (blockE == null) {
                    Log.error(String.format("Block entity is null : %s ", data.displayBlock));
                }

            }

            cleanUpLocations.clear();
            setUpData.clear();
        }

    }

    private void clearDisplaySignText(List<Location> displayLocations) {
        GSLevel gsLevel = Main.getGsLevelManager().getCurrent();

        if (gsLevel != null) {
            for (Location loc : displayLocations) {
                var entity = gsLevel.getBlockEntity(loc);
                if (entity != null) {
                    if (entity instanceof BlockEntitySign) {
                        InteractionHandler.populateSign(gsLevel, "", entity.getLocation());
                    }
                }
            }
        }
    }


    //reset the state after the last question is answered in the question set
    // and before a new question set has started
    public void resetPresentationState(World world) {
        /// TODO: This is more of a hack. We need to do a bit of cleanup on keeping handler states
        if (this.world != null)
            this.world = this.world;

        resetAnswerBlocks();
        clearDisplaySignText(displayLocations);
        interactionHandler.removeProgressHologram();
    }
}