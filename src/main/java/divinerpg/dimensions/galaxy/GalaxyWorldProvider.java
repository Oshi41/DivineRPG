package divinerpg.dimensions.galaxy;

import divinerpg.capabilities.gravity.GravityProvider;
import divinerpg.registry.BiomeRegistry;
import divinerpg.registry.DimensionRegistry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
        return 0.0F;
    }

    /**
     * Returns array with sunrise/sunset colors
     */
    @Nullable
    @SideOnly(Side.CLIENT)
    public float[] calcSunriseSunsetColors(float celestialAngle, float partialTicks) {
        return null;
    }


    /**
     * Returns true if the given X,Z coordinate should show environmental fog.
     */
    @SideOnly(Side.CLIENT)
    public boolean doesXZShowFog(int x, int z) {
        return false;
    }

    @Override
    public boolean isDaytime() {
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

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities() {
        return new GravityProvider(0.2);
    }
}
