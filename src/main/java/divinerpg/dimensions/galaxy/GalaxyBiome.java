package divinerpg.dimensions.galaxy;

import divinerpg.DivineRPG;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.awt.*;
import java.util.Random;

public class GalaxyBiome extends Biome {
    private final int grassColor;
    private final int waterColor;
    private final int foliageColor;

    public GalaxyBiome() {
        super(new BiomeProperties("Galaxy")
                .setRainDisabled());
        setRegistryName(DivineRPG.MODID, "galaxy");

        spawnableWaterCreatureList.clear();
        spawnableCaveCreatureList.clear();
        spawnableMonsterList.clear();
        spawnableCreatureList.clear();
        flowers.clear();
        decorator.flowersPerChunk = 0;
        decorator.grassPerChunk = 0;
        topBlock = Blocks.GRASS.getDefaultState();
        fillerBlock = Blocks.DIRT.getDefaultState();


        grassColor = Color.MAGENTA.getRGB();
        waterColor = new Color(58, 53, 159, 255).getRGB();
        foliageColor = new Color(81, 175, 176, 255).getRGB();
    }


    @Override
    public void decorate(World worldIn, Random rand, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);

        if (rand.nextInt(10) == 1 && TerrainGen.decorate(worldIn, rand, chunkPos, DecorateBiomeEvent.Decorate.EventType.TREE)) {
            int x = rand.nextInt(16) + 8;
            int z = rand.nextInt(16) + 8;
            int y = worldIn.getHeight(x + pos.getX(), z + pos.getZ());

            WorldGenAbstractTree worldgenabstracttree = BIG_TREE_FEATURE;
            worldgenabstracttree.setDecorationDefaults();
            BlockPos blockpos = chunkPos.getBlock(x, y, z);

            if (worldgenabstracttree.generate(worldIn, rand, blockpos)) {
                worldgenabstracttree.generateSaplings(worldIn, rand, blockpos);
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
    public int getModdedBiomeFoliageColor(int original) {
        return foliageColor;
    }

    @Override
    public int getModdedBiomeGrassColor(int original) {
        return grassColor;
    }

    @Override
    public int getWaterColorMultiplier() {
        return waterColor;
    }
}
