package divinerpg.dimensions.galaxy;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nullable;
import java.util.*;

public class GalaxyChunkGenerator implements IChunkGenerator {
    private final Map<String, MapGenStructure> generators = new HashMap<>();
    private final World world;
    private final Random rand;

    public GalaxyChunkGenerator(World world) {
        this.world = world;
        rand = world.rand;
    }

    @Override
    public Chunk generateChunk(int x, int z) {
        world.profiler.startSection("GalaxyChunkGeneration");

        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        ChunkPrimer primer = new ChunkPrimer();

        generateChunkBlocks(primer, new ChunkPos(x, z));

        generators.forEach((s, structure) -> structure.generate(world, x, z, primer));
        Chunk chunk = new Chunk(this.world, primer, x, z);
        chunk.generateSkylightMap();
        world.profiler.endSection();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
        BlockPos pos = new ChunkPos(x, z).getBlock(0, 0, 0);

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(
                true,
                this,
                this.world,
                this.rand,
                x,
                z,
                false);

        Biome biome = this.world.getBiome(pos);
        biome.decorate(world, rand, pos);

        net.minecraftforge.event.ForgeEventFactory.onChunkPopulate(
                false,
                this,
                this.world,
                this.rand,
                x,
                z,
                false);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        // used only for only ocean monuments
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        Biome biome = this.world.getBiomeProvider().getBiome(pos);
        return biome.getSpawnableList(creatureType);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        MapGenStructure structure = generators.get(structureName);
        if (structure != null) {
            return structure.getNearestStructurePos(worldIn, position, findUnexplored);
        }

        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
        generators.forEach((s, structure) -> structure.generate(chunkIn.getWorld(), x, z, null));
    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        MapGenStructure structure = generators.get(structureName);

        if (structure != null) {
            return structure.isInsideStructure(pos);
        }

        return false;
    }


    private void generateChunkBlocks(ChunkPrimer primer, ChunkPos chunkPos) {
        int minSize = 10;
        int maxSize = 14;

        int altitude = 60;
        double waveSize = 16;

        double height = (Math.cos(chunkPos.x / waveSize) * Math.cos(chunkPos.z / waveSize)) * altitude;
        int minHeight = 40;


        generateIsland(primer,
                (int) (altitude + height + minHeight + rand.nextInt(20)),
                Blocks.GRASS.getDefaultState(),
                Blocks.DIRT.getDefaultState(),
                (rand.nextInt(maxSize - minSize) + minSize) / 2);
    }

    private void generateIsland(ChunkPrimer primer, int y, IBlockState top, IBlockState main, final int radius) {
        float f = radius;

        BlockPos correctPosition;

        {
            int x = radius;
            int z = radius;

            if (radius * 2 < 16) {
                x += rand.nextInt(16 - radius * 2);
                z += rand.nextInt(16 - radius * 2);
            }

            correctPosition = new BlockPos(x, y, z);
        }

        List<BlockPos> islandPoses = new ArrayList<>();

        for (int i = 0; f > 0.5F; --i) {
            for (int j = MathHelper.floor(-f); j <= MathHelper.ceil(f); ++j) {
                for (int k = MathHelper.floor(-f); k <= MathHelper.ceil(f); ++k) {
                    if ((float) (j * j + k * k) <= (f + 1.0F) * (f + 1.0F)) {
                        BlockPos current = correctPosition.add(j, i, k);
                        islandPoses.add(current);
                        primer.setBlockState(current.getX(), current.getY(), current.getZ(), main);
                    }
                }
            }

            f = (float) ((double) f - ((double) rand.nextInt(2) + 0.5D));
        }

        if (top == null || top == main)
            return;

        for (BlockPos pos : islandPoses) {
            if (primer.getBlockState(pos.getX(), pos.getY() + 1, pos.getZ()).getMaterial() == Material.AIR) {
                primer.setBlockState(pos.getX(), pos.getY(), pos.getZ(), top);
            }
        }
    }
}
