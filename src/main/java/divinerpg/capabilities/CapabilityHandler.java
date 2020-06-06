package divinerpg.capabilities;

import divinerpg.DivineRPG;
import divinerpg.api.arcana.ArcanaProvider;
import divinerpg.api.armor.cap.ArmorProvider;
import divinerpg.capabilities.gravity.GravityProvider;
import divinerpg.registry.DimensionRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {
    public static final ResourceLocation ARCANA_CAP = new ResourceLocation(DivineRPG.MODID, "arcana");
    public static final ResourceLocation ArmorCapabilityID = new ResourceLocation(DivineRPG.MODID, "armor");
    public static final ResourceLocation GravityCapabilityID = new ResourceLocation(DivineRPG.MODID, "gravity");

    @SubscribeEvent
    public void attachEntityCapability(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            event.addCapability(ARCANA_CAP, new ArcanaProvider());
            event.addCapability(ArmorCapabilityID, new ArmorProvider((EntityPlayer) event.getObject()));
        }
    }

    @SubscribeEvent
    public void attachWorldCapability(final AttachCapabilitiesEvent<Chunk> event) {
        if (event.getObject() != null) {
            int dimension = event.getObject().getWorld().provider.getDimension();

            if (dimension == DimensionRegistry.galaxyDimension.getId()) {
                double multiplier = event.getObject().getWorld().rand.nextInt(400) == 0
                        ? 1.5
                        : 0.2;

                event.addCapability(GravityCapabilityID, new GravityProvider(multiplier));
            }
        }
    }
}