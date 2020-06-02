package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.TwilightBiomeBase;
import net.minecraft.init.Blocks;

public class GalaxyBiome extends TwilightBiomeBase implements IGravityProvider {
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
    public int gravity() {
        return 20;
    }
}
