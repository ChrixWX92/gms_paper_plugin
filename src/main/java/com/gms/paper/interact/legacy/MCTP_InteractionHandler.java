package com.gms.paper.interact.legacy;

import cn.nukkit.event.player.PlayerInteractEvent;
import com.gms.paper.data.GamePosition;
import com.gms.paper.error.InvalidBackendQueryException;
import com.gms.paper.interact.InteractionHandler;
import com.gms.paper.util.Helper;

import java.io.IOException;

public class MCTP_InteractionHandler extends InteractionHandler {
    @Override
    public void handle(PlayerInteractEvent event) throws IOException, InvalidBackendQueryException {
        super.handle(event);

        /// TODO: Wire this up with backend
        String questionInfo[] = signText[0].split(","); // "R" or "W" (Right or wrong)
        String line2 = signText[1]; // Relative co-ordinates of correct tp location OR Relative co-ordinates of incorrect tp location eg "10 0 10"
        Integer msgId = Integer.parseInt(signText[2]); // Message to send to player (maybe change to index from list of phrases)
        String ticketAmount = signText[3].trim();

        if (!((ticketAmount == "") || (ticketAmount == null))) {
            int ticketReward = Integer.parseInt(ticketAmount); // Ticket reward
            if (ticketReward > 0) {
                //TicketHUD.ticketChange(ticketReward);
                profile.earnTickets(ticketReward);
//                tickets = tickets + ticketReward;
                player.sendMessage("You've earned " + ticketReward + " tickets! You now have " + profile.tickets + " tickets.");
            }
            else if (ticketReward == -1) {
                profile.spendTickets(ticketReward);
                player.sendMessage("A wrong answer loses 1 ticket, you now have " + profile.tickets + " tickets");
            }
        }

        if ((line2.length() > 2)) {
            GamePosition spawnPos = Helper.parseLocation(line2);
            GamePosition spawnPosWorld = signLoc.add(spawnPos);
            teleportPlayer(player, spawnPosWorld);
        }

        String[] phraseBank = getPhraseBank();
        Helper.setPlayerTitle(player, phraseBank[msgId]);
    }
}
