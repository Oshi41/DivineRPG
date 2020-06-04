package divinerpg.dimensions.galaxy;

import divinerpg.registry.BiomeRegistry;
import divinerpg.registry.DimensionRegistry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;

public class GalaxyWorldProvider extends WorldProvider {
    @Override
    protected void init() {
        this.biomeProvider = new BiomeProviderSingle(BiomeRegistry.biomeGalaxy);
        this.nether = false;
        this.hasSkyLight = true;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new GalaxyChunkGenerator(this.world);
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.5F;
    }

    @Override
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    @Override
    public int getAverageGroundLevel() {
        return 70;
    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionRegistry.galaxyDimension;
    }

    @Nullable
    @Override
    public String getSaveFolder() {
        return "Galaxy";
    }
}
