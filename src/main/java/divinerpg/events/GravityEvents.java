package divinerpg.events;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import divinerpg.DivineRPG;
import divinerpg.capabilities.gravity.GravityAffectedEvent;
import divinerpg.capabilities.gravity.GravityChangedEvent;
import divinerpg.capabilities.gravity.GravityProvider;
import divinerpg.capabilities.gravity.IGravity;
import divinerpg.enums.ParticleType;
import divinerpg.utils.GravityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GravityEvents {
    /**
     * List of gravity chunks
     */
    private final Cache<World, Cache<IGravity, Boolean>> gravityChunks;
    /**
     * List of entities with own gravity
     */
    private final Cache<IGravity, Boolean> gravityEntities;
    /**
     * List of tiles with
     */
    private final Cache<World, Cache<IGravity, Boolean>> tiles;

    public GravityEvents() {
        tiles = CacheBuilder.newBuilder().weakKeys().build();
        gravityChunks = CacheBuilder.newBuilder().weakKeys().build();
        gravityEntities = CacheBuilder.newBuilder().weakKeys().build();
    }

    // region Add/remove handlers

    @SubscribeEvent
    public void onEntityGravityChanged(final GravityChangedEvent<Entity> e) {
        IGravity cap = e.getCap();

        if (cap.getGravityMultiplier() == 1) {
            gravityEntities.invalidate(cap);
        } else {
            gravityEntities.put(cap, true);
        }
    }

    @SubscribeEvent
    public void onWorldGravityChanged(final GravityChangedEvent<Chunk> e) {
        IGravity cap = e.getCap();
        World world = e.getObject().getWorld();

        Cache<IGravity, Boolean> set = gravityChunks.getIfPresent(world);
        if (set == null) {
            set = CacheBuilder.newBuilder().weakKeys().build();
            gravityChunks.put(world, set);
        }


        if (cap.getGravityMultiplier() == 1) {
            set.invalidate(cap);
        } else {
            set.put(cap, true);
        }
    }

    @SubscribeEvent
    public void onTileGravityChanged(final GravityChangedEvent<TileEntity> e) {
        IGravity cap = e.getCap();
        World world = e.getObject().getWorld();

        Cache<IGravity, Boolean> set = tiles.getIfPresent(world);
        if (set == null) {
            set = CacheBuilder.newBuilder().weakKeys().build();
            gravityChunks.put(world, set);
        }

        if (cap.getGravityMultiplier() == 1) {
            set.invalidate(cap);
        } else {
            set.put(cap, true);
        }
    }

    // endregion

    // region Gravity events

    @SubscribeEvent
    public void onFall(LivingFallEvent e) {
        double jumpHeight = Stream.of(
                getCap(e.getEntity().getEntityWorld().getChunkFromBlockCoords(e.getEntity().getPosition())),
                getCap(e.getEntity()))
                .filter(Objects::nonNull)
                .mapToDouble(IGravity::maxNoHarmJumpHeight)
                .average()
                .orElse(0);

        if (jumpHeight <= 0)
            return;

        e.setDistance((float) (e.getDistance() - jumpHeight));
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

    // endregion

    // region tick action

    private void applyForWorld(World world) {
        if (world.profiler != null) {
            world.profiler.startSection("WorldGravity");
        }

        Cache<IGravity, Boolean> chunks = gravityChunks.getIfPresent(world);
        if (chunks != null && chunks.size() > 0) {
            chunks.asMap().keySet().forEach(gravity -> {
                ICapabilityProvider owner = gravity.getOwner();
                if (owner instanceof Chunk) {
                    applyForChunk((Chunk) owner, gravity);
                }
            });
        }

        Cache<IGravity, Boolean> tiles = this.tiles.getIfPresent(world);
        if (tiles != null && tiles.size() > 0) {
            chunks.asMap().keySet().forEach(gravity -> {
                ICapabilityProvider owner = gravity.getOwner();
                if (owner instanceof TileEntity) {
                    applyForTile((TileEntity) owner, gravity);
                }
            });
        }

        Map<Entity, IGravity> entities = gravityEntities.asMap().keySet()
                .stream()
                .filter(x -> x.getOwner() instanceof Entity)
                .filter(x -> Objects.equals(world, ((Entity) x.getOwner()).world))
                .collect(Collectors.toMap(x -> ((Entity) x.getOwner()), x -> x));

        entities.forEach((entity, iGravity) -> {
            GravityAffectedEvent<Entity> event = new GravityAffectedEvent<>(Entity.class, entity, iGravity);
            MinecraftForge.EVENT_BUS.post(event);
            applyForEvent(event, iGravity);
        });

        if (world.getTotalWorldTime() % (20 * 60) == 1) {
            cleanUp();
        }

        if (world.profiler != null) {
            world.profiler.endSection();
        }
    }

    private void applyForTile(TileEntity owner, IGravity gravity) {
        if (owner == null
                || gravity == null
                || gravity.getGravityMultiplier() == 1
                || owner.getWorld() == null
                || !owner.getWorld().isBlockLoaded(owner.getPos())) {
            return;
        }

        if (owner.getWorld().profiler != null) {
            owner.getWorld().profiler.startSection("TileGravity");
        }

        GravityAffectedEvent<TileEntity> event = new GravityAffectedEvent<>(TileEntity.class, owner, gravity);
        MinecraftForge.EVENT_BUS.post(event);
        applyForEvent(event, gravity);

        if (owner.getWorld().profiler != null) {
            owner.getWorld().profiler.endSection();
        }
    }

    private void applyForChunk(Chunk chunk, IGravity gravity) {
        if (chunk == null || !chunk.isLoaded() || gravity == null || gravity.getGravityMultiplier() == 1)
            return;

        if (chunk.getWorld().profiler != null) {
            chunk.getWorld().profiler.startSection("ChunkGravity");
        }

        if (chunk.getWorld().isRemote)
            addParticles(chunk, gravity);

        GravityAffectedEvent<Chunk> event = new GravityAffectedEvent<>(Chunk.class, chunk, gravity);
        MinecraftForge.EVENT_BUS.post(event);
        applyForEvent(event, gravity);

        if (chunk.getWorld().profiler != null) {
            chunk.getWorld().profiler.endSection();
        }
    }

    private void applyForEvent(GravityAffectedEvent e, IGravity gravity) {
        if (e.isCanceled())
            return;

        Set<Entity> entities = e.getAffectedEntities();
        if (entities.isEmpty())
            return;

        entities.forEach(x -> changeVelocity(x, gravity));
    }

    private void cleanUp() {
        gravityChunks.cleanUp();
        gravityEntities.cleanUp();
        tiles.cleanUp();

        gravityChunks.getAllPresent(gravityChunks.asMap().keySet())
                .forEach((world, cache) -> cache.cleanUp());

        tiles.getAllPresent(tiles.asMap().keySet())
                .forEach((world, cache) -> cache.cleanUp());
    }
    // endregion

    private void changeVelocity(Entity entity, IGravity cap) {
        if (cap == null || entity == null || cap.getGravityMultiplier() == 1)
            return;

        double gravity = GravityUtils.getGravity(entity);
        if (gravity == 0)
            return;

        double gravityTick = gravity * cap.getGravityMultiplier();
        entity.motionY += gravity - gravityTick;
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
