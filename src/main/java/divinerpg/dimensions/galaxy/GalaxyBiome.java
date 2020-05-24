package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.TwilightBiomeBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class GalaxyBiome extends TwilightBiomeBase {
    private final WorldGenerator meteorGen = new IslandGenerator(Blocks.STONE.getDefaultState(), 6, 20);

    public GalaxyBiome() {
        super(new BiomeProperties("galaxy"), "galaxy");

//        this.genTree = new EdenTree(false, 3);
//        this.genLargeTree = new LargeTwilightTree(false, 7,
//                BlockRegistry.edenLog.getDefaultState(),
//                BlockRegistry.edenLeaves.getDefaultState(),
//                BlockRegistry.edenGrass);
//        this.genConeUp = new WorldGenConeUp(BlockRegistry.divineMossStone);
//        this.genLakes = new WorldGenLakes(Blocks.WATER);
//        this.brush = new WorldGenTwilightSinglePlants(BlockRegistry.edenBrush, BlockRegistry.edenGrass);
//        this.bloom = new WorldGenTwilightSinglePlants(BlockRegistry.sunbloom, BlockRegistry.edenGrass);
//        this.blossom = new WorldGenTwilightSinglePlants(BlockRegistry.sunBlossom,
//                BlockRegistry.edenGrass);

        this.topBlock = Blocks.GRASS.getDefaultState();
        this.fillerBlock = Blocks.DIRT.getDefaultState();
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableCaveCreatureList.clear();
        this.spawnableWaterCreatureList.clear();
        this.flowers.clear();
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
    }

    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        super.decorate(worldIn, rand, pos);

        ChunkPos chunkPos = new ChunkPos(pos);

        generate(worldIn, rand, chunkPos.x, chunkPos.z, 2, 10, 230, meteorGen);
    }
}
