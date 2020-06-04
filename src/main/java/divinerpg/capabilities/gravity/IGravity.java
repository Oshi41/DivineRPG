package divinerpg.capabilities.gravity;

import net.minecraft.world.World;

import javax.annotation.Nonnull;

public interface IGravity {
    /**
     * 1 means normal gravity
     * 0.5 - half lighter than normal
     * more than 1 means stronger gravity, like Jupiter
     *
     * @return
     */
    double getGravityMultiplier();

    /**
     * Setting gravity value
     *
     * @param value
     */
    void setGravityMultiplier(double value);

    /**
     * Applying gravitation for world. Should call every tick
     *
     * @param world
     */
    void applyForWorld(@Nonnull World world);

    /**
     * Max jump height. Default is 3
     *
     * @return
     */
    default double maxNoHarmJumpHeight() {
        return 3 / getGravityMultiplier();
    }
}
