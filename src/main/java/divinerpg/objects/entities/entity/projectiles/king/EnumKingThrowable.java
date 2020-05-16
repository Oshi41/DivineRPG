package divinerpg.objects.entities.entity.projectiles.king;

import net.minecraft.util.math.MathHelper;

public enum EnumKingThrowable {
    /**
     * For regular dragon fireball
     */
    FIREBALL(5, 100),

    /**
     * Anvil, dismount part of player equpment
     */
    ANVIL(3, 100),

    /**
     * Heat seaking projectile, never kills but leaves player 25% percentage
     */
    RAGE(1, 100),

    /**
     * Regular wither shot
     */
    WITHER(5, 100),

    /**
     * Fractile shot
     */
    FRACTILE(4, 100);
    ;
    /**
     * Increase number to randomly select this more often
     */
    public final int weight;

    /**
     * 1..100 value
     * Dragon will use this have not more that that health percentage value
     */
    public final int maxHealthPercantage;


    EnumKingThrowable(int weight, int maxHealthPercantage) {
        this.weight = weight;
        this.maxHealthPercantage = MathHelper.clamp(maxHealthPercantage, 1, 100);
    }
}
