package divinerpg.dimensions.galaxy;

import divinerpg.dimensions.galaxy.render.GalaxySkyRender;
import divinerpg.registry.ModBiomes;
import divinerpg.registry.ModDimensions;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraftforge.client.IRenderHandler;

import javax.annotation.Nullable;

public class GalaxyWorldProvider extends WorldProvider {
    private final GalaxySkyRender skyRender;

    public GalaxyWorldProvider() {
        skyRender = new GalaxySkyRender();
        biomeProvider = new BiomeProviderSingle(ModBiomes.Galaxy);
    }

    @Override
    public DimensionType getDimensionType() {
        return ModDimensions.galaxyDimension;
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

    @Nullable
    @Override
    public IRenderHandler getSkyRenderer() {
        return skyRender;
    }
}
