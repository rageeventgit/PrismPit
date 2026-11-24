package net.rageevent.hypixel.game.ui;

import dev.prism.api.entity.Player;
import dev.prism.api.scoreboard.Animation;
import dev.prism.api.scoreboard.Board;
import dev.prism.api.scoreboard.Line;
import dev.prism.api.text.Style;
import dev.prism.api.text.Text;
import dev.prism.api.text.TextColor;
import dev.prism.api.world.Instance;
import lombok.RequiredArgsConstructor;
import net.rageevent.hypixel.player.UserData;
import net.rageevent.hypixel.player.data.PlayerTracker;
import net.rageevent.hypixel.player.enums.PlayerPrestige;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
public class GameBoard {


    public static final int BOARD_REFRESH_RATE = 20;

    private final PlayerTracker playerTracker;
    //TODO: Add combat tagging, and somehow assists.

    public void show(Player player) {
        player.showBoard(buildBoard());
    }

    private Board buildBoard() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
        String date = dateFormat.format(new Date());
        return Board.builder()
                .title(CustomAnimations.sweep("THE HYPIXEL PIT", 3))
                .line(Text.of(date, TextColor.GRAY).appendSpace().append(Text.of("p1t", TextColor.DARK_GRAY)))
                .blank()
                .rows(BOARD_REFRESH_RATE, player -> {
                    List<Text> rows = new ArrayList<>();
                    UserData userData = playerTracker.getOrCreate(player);

                    PlayerPrestige playerPrestige = userData.prestige();
                    if (playerPrestige != PlayerPrestige.P0)
                        rows.addFirst(Text.of("Prestige: ").append(Text.of(playerPrestige.name(), TextColor.YELLOW)));


                    Text neededXp = Text.of("Needed XP: ");

                    if (userData.level() == 120)
                        neededXp = neededXp.append(Text.of("MAXED!", TextColor.AQUA).bold());
                    else
                        neededXp = neededXp.append(Text.of(String.format("%,d", userData.neededExperience()), TextColor.AQUA));

                    Text levelText = Text.of("[", playerPrestige.bracketColor())
                            .append(userData.levelText())
                            .append(Text.of("]", playerPrestige.bracketColor()));

                    rows.add(Text.of("Level: ").append(levelText));
                    rows.add(neededXp);
                    return rows;
                })
                .blank()
                .line(Line.dynamic(player -> Text.of("Gold: ")
                        .append(Text.of(String.format("%,.0fg", playerTracker.getOrCreate(player).gold()), TextColor.GOLD))))
                .blank()
                .rows(BOARD_REFRESH_RATE, player -> {
                    List<Text> rows = new ArrayList<>();
                    UserData userData = playerTracker.getOrCreate(player);

                    Text statusText = Text.of("Idling", TextColor.GREEN);

                    if (userData.bounty() > 0) {
                        statusText = Text.of("Bountied", TextColor.RED);
                        rows.add(Text.of("Bounty: ").append(Text.of(String.format("%,dg", playerTracker.getOrCreate(player).bounty()), TextColor.GOLD)));
                    }
                    rows.addFirst(Text.of("Status: ").append(statusText));
                    return rows;
                })
                .blank()
                .line("<yellow>www.hypixel.net")
                .build();
    }

    private static class CustomAnimations {

        private static Animation sweep(final String text, final int frameTicks) {
            final Style bold = Style.EMPTY.bold(true);
            final Text still = Text.of(text, bold.color(TextColor.YELLOW));
            final Text lit = Text.of(text, bold.color(TextColor.WHITE));

            final int[] cps = text.codePoints().toArray();
            final Text[] sweep = new Text[cps.length];
            for (int i = 0; i < cps.length; i++) {
                Text frame = Text.of("", bold);
                if (i > 0) {
                    frame = frame.append(Text.of(new String(cps, 0, i), TextColor.WHITE));                       // passed
                }
                frame = frame.append(Text.of(new String(cps, i, 1), TextColor.GOLD));                           // the highlight
                if (i + 1 < cps.length) {
                    frame = frame.append(Text.of(new String(cps, i + 1, cps.length - i - 1), TextColor.YELLOW)); // still to come
                }
                sweep[i] = frame;
            }

            return Animation.of(frameTicks, still).hold(4)                            // 5 frames at rest
                    .then(Animation.of(frameTicks, sweep))                                // the sweep
                    .then(Animation.of(frameTicks, lit, still, lit, still, lit, still))   // blink 3 times
                    .then(Animation.of(frameTicks, still).hold(9));                       // 10 frames at rest
        }
    }

}
