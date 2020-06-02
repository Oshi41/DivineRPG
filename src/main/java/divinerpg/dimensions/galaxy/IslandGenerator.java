package divinerpg.dimensions.galaxy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IslandGenerator extends WorldGenerator {

    private final IBlockState state;
    private final IBlockState top;
    private final int minSize;
    private final int maxSize;

    public IslandGenerator(IBlockState state, IBlockState top, int minSize, int maxSize) {

        this.state = state;
        this.top = top;
        this.minSize = MathHelper.clamp(minSize, 1, maxSize - 1);
        this.maxSize = MathHelper.clamp(maxSize, minSize + 1, 30);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        final int radius = (rand.nextInt(maxSize - minSize) + minSize) / 2;
        float f = radius;

        int minOffset = (radius * 2) + 1;
        BlockPos correctPosition = position.add(minOffset, 0, minOffset);

        List<BlockPos> islandPoses = new ArrayList<>();

        for (int i = 0; f > 0.5F; --i) {
            for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                    if ((float) (j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
                        BlockPos current = correctPosition.add(j, i, k);
                        this.setBlockAndNotifyAdequately(worldIn, current, state);
                        islandPoses.add(current);
                    }
                }
            }

            f = (float) ((double) f - ((double) rand.nextInt(2) + 0.5D));
        }

        islandPoses.stream().filter(worldIn::canSeeSky)
                .forEach(x -> this.setBlockAndNotifyAdequately(worldIn, x, top));

        return true;
    }
}
