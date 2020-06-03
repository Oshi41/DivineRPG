package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.TwilightBiomeBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.awt.*;
import java.util.Random;

public class GalaxyBiome extends TwilightBiomeBase implements IGravityProvider {
    private final int grassColor;
    private final int waterColor;
    private final int foliageColor;

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
        this.decorator.gravelPatchesPerChunk = 0;
        this.decorator.sandPatchesPerChunk = 0;
        this.decorator.clayPerChunk = 0;

        grassColor = Color.MAGENTA.getRGB();
        waterColor = new Color(58, 53, 159, 255).getRGB();
        foliageColor = new Color(81, 175, 176, 255).getRGB();
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (TerrainGen.decorate(worldIn, rand, chunkPos, DecorateBiomeEvent.Decorate.EventType.TREE)) {
            for (int j2 = 0; j2 < 5; ++j2) {
                int x = rand.nextInt(16) + 8;
                int z = rand.nextInt(16) + 8;
                int y = 60 + rand.nextInt(100);

                WorldGenAbstractTree worldgenabstracttree = BIG_TREE_FEATURE;
                worldgenabstracttree.setDecorationDefaults();
                BlockPos blockpos = chunkPos.getBlock(x, y, z);

                if (worldgenabstracttree.generate(worldIn, rand, blockpos)) {
                    worldgenabstracttree.generateSaplings(worldIn, rand, blockpos);
                }
            }
        }

        if (TerrainGen.decorate(worldIn, rand, chunkPos, DecorateBiomeEvent.Decorate.EventType.GRASS)) {
            for (int i3 = 0; i3 < 2; ++i3) {
                int x = rand.nextInt(8) + 8;
                int z = rand.nextInt(8) + 8;
                int y = rand.nextInt(100) + 60;
                getRandomWorldGenForGrass(rand).generate(worldIn, rand, chunkPos.getBlock(x, y, z));
            }
        }
    }

    @Override
    public int getGrassColorAtPos(BlockPos pos) {
        return grassColor;
    }

    @Override
    public int getWaterColorMultiplier() {
        return waterColor;
    }

    @Override
    public int getFoliageColorAtPos(BlockPos pos) {
        return foliageColor;
    }

    @Override
    public int gravity() {
        return 20;
    }
}
