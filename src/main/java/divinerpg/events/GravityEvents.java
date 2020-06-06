package divinerpg.events;

import divinerpg.DivineRPG;
import divinerpg.capabilities.gravity.GravityProvider;
import divinerpg.capabilities.gravity.IGravity;
import divinerpg.enums.ParticleType;
import divinerpg.utils.GravityUtils;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GravityEvents {
    /**
     * Gravity chunk by dimension
     */
    private final Map<Integer, Set<ChunkPos>> gravityChunks = new HashMap<>();

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load e) {
        IGravity gravity = getCap(e.getChunk());
        if (gravity == null)
            return;

        Set<ChunkPos> poses = gravityChunks.computeIfAbsent(e.getChunk().getWorld().provider.getDimension(),
                integer -> new ConcurrentSet<>());

        poses.add(new ChunkPos(e.getChunk().x, e.getChunk().z));
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload e) {
        Set<ChunkPos> set = gravityChunks.get(e.getChunk().getWorld().provider.getDimension());
        if (set != null && !set.isEmpty()) {
            set.remove(new ChunkPos(e.getChunk().x, e.getChunk().z));
        }
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent e) {
        IGravity gravity = getCap(e.getEntity().getEntityWorld().getChunkFromBlockCoords(e.getEntity().getPosition()));
        if (gravity == null)
            return;

        e.setDistance((float) (e.getDistance() - gravity.maxNoHarmJumpHeight()));
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        applyForWorld(e.world);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END
                || e.side != Side.CLIENT
                || DivineRPG.proxy.getPlayer() == null)
            return;

        if (net.minecraft.client.Minecraft.getMinecraft().isGamePaused())
            return;

        applyForWorld(DivineRPG.proxy.getPlayer().world);
    }

    private void applyForWorld(World world) {
        Set<ChunkPos> set = gravityChunks.get(world.provider.getDimension());
        if (set == null || set.isEmpty())
            return;

        Map<IGravity, List<Entity>> chunkEntities = new HashMap<>();

        set.stream()
                .map(x -> x.getBlock(0, 0, 0))
                .filter(world::isBlockLoaded)
                .map(world::getChunkFromBlockCoords)
                .distinct()
                .forEach(x -> {
                    IGravity gravity = getCap(x);
                    if (gravity == null || gravity.getGravityMultiplier() == 1)
                        return;

                    List<Entity> entities = Arrays.stream(x.getEntityLists()).flatMap(Collection::stream).collect(Collectors.toList());
                    if (entities.isEmpty())
                        return;

                    chunkEntities.put(gravity, entities);

                    if (world.isRemote) {
                        addParticles(x, gravity);
                    }
                });

        chunkEntities.forEach((cap, entities) -> {
            for (Entity entity : entities) {
                double gravity = GravityUtils.getGravity(entity);
                if (gravity == 0)
                    continue;

                double gravityTick = gravity * cap.getGravityMultiplier();
                entity.motionY += gravity - gravityTick;
            }
        });
    }

    @SideOnly(Side.CLIENT)
    private void addParticles(Chunk chunk, IGravity cap) {
        if (cap.getGravityMultiplier() <= 1)
            return;


        int speed = -2;
        ParticleType type = ParticleType.MORTUM_PORTAL;

        ChunkPos chunkPos = new ChunkPos(chunk.x, chunk.z);

        Random rand = chunk.getWorld().rand;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                for (int y = chunk.getLowestHeight(); y <= chunk.getTopFilledSegment(); y++) {
                    BlockPos pos = chunkPos.getBlock(x, y, z);

                    DivineRPG.proxy.spawnParticle(
                            chunk.getWorld(),
                            type,
                            pos.getX(),
                            pos.getY() + (rand.nextDouble() * 2) - (rand.nextDouble() * 2),
                            pos.getZ(),
                            0,
                            speed,
                            0);
                }
            }
        }
    }

    @Nullable
    private IGravity getCap(ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(GravityProvider.GravityCapability, null)
                ? provider.getCapability(GravityProvider.GravityCapability, null)
                : null;
    }
}
