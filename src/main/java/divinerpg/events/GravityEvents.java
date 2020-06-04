package divinerpg.events;

import divinerpg.DivineRPG;
import divinerpg.capabilities.gravity.GravityProvider;
import divinerpg.capabilities.gravity.IGravity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

public class GravityEvents {
    @Nullable
    private IGravity getFromWorld(World world) {
        return world != null && world.hasCapability(GravityProvider.GravityCapability, null)
                ? world.getCapability(GravityProvider.GravityCapability, null)
                : null;
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent e) {
        IGravity gravity = getFromWorld(e.getEntity().getEntityWorld());
        if (gravity == null)
            return;

        e.setDistance((float) (e.getDistance() - gravity.maxNoHarmJumpHeight()));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        IGravity gravity = getFromWorld(e.world);

        if (gravity != null) {
            gravity.applyForWorld(e.world);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END
                || e.side != Side.CLIENT
                || DivineRPG.proxy.getPlayer() == null)
            return;

        if (net.minecraft.client.Minecraft.getMinecraft().isGamePaused())
            return;

        World world = DivineRPG.proxy.getPlayer().getEntityWorld();

        IGravity gravity = getFromWorld(world);
        if (gravity != null) {
            gravity.applyForWorld(world);
        }
    }
}
