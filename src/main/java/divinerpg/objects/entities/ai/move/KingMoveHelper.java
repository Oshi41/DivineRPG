package divinerpg.objects.entities.ai.move;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class KingMoveHelper extends GhastLikeMoveHelper {

    public KingMoveHelper(EntityLiving ghast) {
        super(ghast);
    }

    @Override
    protected void moveEntity(EntityLiving e, double xMovement, double yMovement, double zMovement, double distanceSquared) {
        World world = e.getEntityWorld();
        BlockPos position = e.getPosition();
        int minHeight = world.getHeight(position.getX(), position.getZ()) + getMinHeight(world.rand);

        yMovement += minHeight - position.getY();
        distanceSquared = xMovement * 2 + yMovement * 2 + zMovement * 2;

        super.moveEntity(e, xMovement, yMovement, zMovement, distanceSquared);
    }

    private int getMinHeight(Random random){
        return 5 + random.nextInt(5);
    }
}
