package divinerpg.dimensions.galaxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class IslandGenerator extends WorldGenerator {

    private final IBlockState state;
    private final int minSize;
    private final int maxSize;

    public IslandGenerator(IBlockState state, int minSize, int maxSize) {

        this.state = state;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        float f = (float) rand.nextInt(maxSize - minSize) + minSize;

        for (int i = 0; f > 0.5F; --i) {
            for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                    if ((float) (j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
                        this.setBlockAndNotifyAdequately(worldIn, position.add(j, i, k), state);
                    }
                }
            }

            f = (float) ((double) f - ((double) rand.nextInt(2) + 0.5D));
        }

        return true;
    }
}
