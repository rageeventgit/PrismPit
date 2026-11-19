package net.rageevent.hypixel.player;

import dev.prism.api.entity.Player;
import lombok.*;
import lombok.experimental.Accessors;
import net.rageevent.hypixel.game.RenownPerk;
import net.rageevent.hypixel.game.StandardPerk;
import net.rageevent.hypixel.player.enums.AdminSetting;
import net.rageevent.hypixel.player.enums.PlayerLevel;
import net.rageevent.hypixel.player.enums.PlayerPrestige;
import net.rageevent.hypixel.player.enums.PlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(force = true)
@Data
@Builder
@Accessors(fluent = true)
public class UserData {

    private final UUID uniqueId;
    private String username;
    private PlayerRank rank;

    private int level;
    private PlayerPrestige prestige;

    private int experience;

    private double gold;
    private int bounty;
    private int renown;

    private List<RenownPerk> renownPerks;
    private List<StandardPerk> standardPerks;

    private List<AdminSetting> adminSettings;

    public int flattenLevel() {
        return (level * 10) / 10;
    }

    public static UserData fresh(Player player) {
        return new UserData(
                player.uuid(),
                player.username(),
                PlayerRank.NORMAL,
                1,
                PlayerPrestige.P0,
                PlayerLevel.LEVEL_0.xp(),
                100.0,
                0,
                0,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("UserData{");
        sb.append("uniqueId=").append(uniqueId);
        sb.append(", username='").append(username).append('\'');
        sb.append(", rank=").append(rank);
        sb.append(", level=").append(level);
        sb.append(", prestige=").append(prestige);
        sb.append(", experience=").append(experience);
        sb.append(", gold=").append(gold);
        sb.append(", bounty=").append(bounty);
        sb.append(", renown=").append(renown);
        sb.append(", renownPerks=").append(renownPerks);
        sb.append(", standardPerks=").append(standardPerks);
        sb.append(", adminSettings=").append(adminSettings);
        sb.append('}');
        return sb.toString();
    }
}

