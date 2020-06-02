package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.galaxy.render.GalaxySkyRender;
import divinerpg.registry.BiomeRegistry;
import divinerpg.registry.DimensionRegistry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;

public class GalaxyWorldProvider extends WorldProvider {
    private final GalaxySkyRender skyRender;

    public GalaxyWorldProvider() {
        skyRender = new GalaxySkyRender();
        biomeProvider = new BiomeProviderSingle(BiomeRegistry.biomeGalaxy);
    }

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
    public DimensionType getDimensionType() {
        return DimensionRegistry.galaxyDimension;
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
    public boolean shouldMapSpin(String entity, double x, double z, double rotation) {
        return true;
    }

    @Override
    public boolean isDaytime() {
        return true;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    //    @Nullable
//    @Override
//    public IRenderHandler getSkyRenderer() {
//        return skyRender;
//    }
}
