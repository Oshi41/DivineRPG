package divinerpg.dimensions.galaxy;

/**
 * Should be an instance of Biome.
 */
public interface IGravityProvider {

    /**
     * 100 means normal gravity
     * 0 means no gravity
     * Negative numbers should work as levitation effect
     * More than 100 means more gravity like Jupiter
     *
     * @return
     */
    default int gravity() {
        return 100;
    }
}
