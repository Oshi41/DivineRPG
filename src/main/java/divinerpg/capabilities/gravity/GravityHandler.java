package divinerpg.capabilities.gravity;

public class GravityHandler implements IGravity {
    private double gravity;

    /**
     * NBT ctor
     */
    public GravityHandler() {

    }

    public GravityHandler(double multiplier) {
        gravity = multiplier;
    }

    @Override
    public double getGravityMultiplier() {
        return gravity;
    }

    @Override
    public void setGravityMultiplier(double value) {
        gravity = value;
    }
}
