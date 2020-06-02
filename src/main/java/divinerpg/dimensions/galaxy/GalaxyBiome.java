package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.TwilightBiomeBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.awt.*;
import java.util.Random;

public class GalaxyBiome extends TwilightBiomeBase implements IGravityProvider {
    private final Color grassColor;
    private final Color waterColor;
    private final Color foliageColor;

    public GalaxyBiome() {
        super(new BiomeProperties("galaxy"), "galaxy");
        this.topBlock = Blocks.GRASS.getDefaultState();
        this.fillerBlock = Blocks.DIRT.getDefaultState();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableCaveCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.flowers.clear();
        this.decorator.flowersPerChunk = 0;

        grassColor = Color.MAGENTA;
        waterColor = new Color(58, 53, 159, 255);
        foliageColor = new Color(81, 175, 176, 255);
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        super.decorate(worldIn, rand, pos);

        ChunkPos chunkPos = new ChunkPos(pos);

        for (int i = 0; i < 10; i++) {
            int x = chunkPos.x + rand.nextInt(16) + 8;
            int z = chunkPos.z + rand.nextInt(16) + 8;
            int y = 60 + rand.nextInt(100);

            WorldGenAbstractTree worldgenabstracttree = getRandomTreeFeature(rand);
            worldgenabstracttree.setDecorationDefaults();

            BlockPos blockpos = new BlockPos(x, y, z);

            if (worldgenabstracttree.generate(worldIn, rand, blockpos)) {
                worldgenabstracttree.generateSaplings(worldIn, rand, blockpos);
            }
        }
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) {
        return grassColor.getRGB();
    }

    @Override
    public int getWaterColorMultiplier() {
        return waterColor.getRGB();
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
        return foliageColor.getRGB();
    }

    @Override
    public int gravity() {
        return 20;
    }
}
