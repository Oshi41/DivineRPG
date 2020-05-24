package divinerpg.objects.blocks.tile.entity.base.rituals;

import divinerpg.objects.blocks.tile.entity.multiblock.IMultiblockTile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

public class KillEntityRitual extends RitualBase {
    private final Predicate<Entity> canAccept;
    private final ITextComponent msg;
    private final double maxDistance;
    private final IMultiblockTile tile;

    public KillEntityRitual(ResourceLocation id,
                            TileEntity entity,
                            Predicate<Entity> canAccept,
                            ITextComponent msg,
                            double maxDistance) {
        super(id, entity);
        this.canAccept = canAccept;
        this.msg = msg;
        this.maxDistance = maxDistance;

        if (!(entity instanceof IMultiblockTile)) {
            throw new RuntimeException("Tile entity should be an IMultiblockTile instance!");
        }

        this.tile = ((IMultiblockTile) entity);
    }

    @SubscribeEvent
    public void processEvent(LivingDeathEvent event) {
        if (canAccept == null
                || tile == null
                || isPerformed()) {
            MinecraftForge.EVENT_BUS.unregister(this);
            return;
        }

        // is not constructred
        if (tile.getMultiblockMatch() == null)
            return;

        // player is the killer
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) {
            return;
        }

        if (!canAccept.test(event.getEntity()))
            return;

        AxisAlignedBB area = tile.getMultiblockMatch().area;
        Vec3d center = new Vec3d(area.minX + (area.maxX - area.minX) * 0.5D, area.minY + (area.maxY - area.minY) * 0.5D, area.minZ + (area.maxZ - area.minZ) * 0.5D);
        Vec3d entityPosition = new Vec3d(event.getEntity().getPosition());

        RayTraceResult traceResult = area.calculateIntercept(center, entityPosition);

        if (traceResult != null) {
            if (traceResult.hitVec.distanceTo(entityPosition) <= maxDistance) {
                setIsPerformed(true);

                confirmRutual(event.getSource().getTrueSource());
            } else {
                event.getSource().getTrueSource().sendMessage(new TextComponentString("Too far away..."));
            }
        }
    }

    @Override
    public ITextComponent getDescription() {
        return msg;
    }

    @Override
    public boolean isEventListener() {
        return true;
    }
}
