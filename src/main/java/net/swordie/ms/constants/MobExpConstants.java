package net.swordie.ms.constants;

import net.swordie.ms.ServerConfig;

import java.util.List;

public class MobExpConstants {
    public record MobExpRate(
        int minLevel,
        int rate
    ) {}

    public static int MOB_EXP_BASE_RATE = ServerConfig.getInt("mob.exp.base.rate", 10);
    public static int LEVEL_10_EXP_RATE = ServerConfig.getInt("mob.exp.rate.per.level.10", MOB_EXP_BASE_RATE);

    public static List<MobExpRate> MOB_EXP_RATE_PER_MIN_LEVEL = List.of(
        new MobExpRate(1, 1),
        new MobExpRate(10, LEVEL_10_EXP_RATE)
    );

    /**
     * Searches for the {@link MobExpRate} with the highest minLevel for the given character level
     *
     * @param level the level of the character
     * @return exp rate found for the given level, with a minimum value of 1
     */
    public static int getRateForCharacterLevel(int level) {
        int rate = 1;
        for (MobExpRate mobExpRate : MOB_EXP_RATE_PER_MIN_LEVEL) {
            if (mobExpRate.minLevel() <= level) {
                rate = mobExpRate.rate();
            } else {
                break; // because list is sorted from lowest to highest
            }
        }

        return Math.max(rate, 1);

    }
}
