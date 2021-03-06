package divinerpg.utils;

import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import divinerpg.registry.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class PositionHelper {
    public static RayTraceResult rayTrace(EntityPlayer player, double blockReachDistance, int partialTicks) {
        Vec3d vec3d = player.getPositionEyes(partialTicks);
        Vec3d vec3d1 = player.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.addVector(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return player.world.rayTraceBlocks(vec3d, vec3d2, false, false, true);
    }

    /**
     * Returns correct poses to perform ray trace
     *
     * @param player             - shooter
     * @param blockReachDistance - rich distance
     * @return
     */
    public static Tuple<Vec3d, Vec3d> getRayTraceVecs(Entity player, double blockReachDistance) {
        Vec3d startVec = new Vec3d(player.posX, player.posY + (double) player.getEyeHeight(), player.posZ);
        Vec3d endVec = startVec.add(player.getLook(0).scale(blockReachDistance));

        return new Tuple<>(startVec, endVec);
    }

    public static RayTraceResult rayTrace(EntityPlayer e, boolean stopOnLiquid) {
        Tuple<Vec3d, Vec3d> vecs = getRayTraceVecs(e, getBlockReachDistance(e));

        return e.world.rayTraceBlocks(vecs.getFirst(),
                vecs.getSecond(),
                stopOnLiquid,
                false,
                true);
    }

    public static double getBlockReachDistance(EntityPlayer player) {
        if (player.world.isRemote) {
            return net.minecraft.client.Minecraft.getMinecraft().playerController.getBlockReachDistance();
        }

        return player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
    }

    public static void moveBullet(EntityPlayer player, EntityThrowable bullet) {
        if (player == null || bullet == null)
            return;

        Vec3d vector = player.getLookVec().scale(2).add(bullet.getPositionVector());
        bullet.setPosition(vector.x, vector.y, vector.z);
    }

    /**
     * Performs ray trace and return hitted entity
     *
     * @param player - shooter
     * @param range  - block range distance
     * @return
     */
    @Nullable
    public static Entity rayTrace(Entity player, float range) {
        // Some magic number
        double fix = 0.3;

        Tuple<Vec3d, Vec3d> vecs = getRayTraceVecs(player, range);

        Vec3d start = vecs.getFirst();
        Vec3d end = vecs.getSecond();

        AxisAlignedBB cube = new AxisAlignedBB(start, end);

        for (Entity entity : player.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player, cube)) {
            RayTraceResult optional = entity.getEntityBoundingBox().grow(1).calculateIntercept(start, end);
            if (optional != null && start.distanceTo(optional.hitVec) <= range) {
                return entity;
            }
        }

        return null;
    }

    public static BlockPos searchInRadius(World world, BlockPos center, int diameter, Predicate<BlockPos> action) {
        return searchInRadius(world, center, new BlockPos(diameter, diameter, diameter), action);
    }

    /**
     * Searches in range from nearest an further
     *
     * @param center - search center
     * @param range  - range
     * @param action - action on every pos
     */
    public static BlockPos searchInRadius(World world, BlockPos center, BlockPos range, Predicate<BlockPos> action) {
        for (int xRadius = 0; xRadius <= Math.floor(range.getX() / 2.0); xRadius++) {
            for (int yRadius = 0; yRadius <= Math.floor(range.getY() / 2.0); yRadius++) {
                for (int zRadius = 0; zRadius <= Math.floor(range.getZ() / 2.0); zRadius++) {

                    for (int x = -1; x <= 1; x += 2) {
                        for (int y = -1; y <= 1; y += 2) {
                            for (int z = -1; z <= 1; z += 2) {

                                BlockPos pos = center.add(x * xRadius, y * yRadius, z * zRadius);

                                if (world.isOutsideBuildHeight(pos))
                                    continue;

                                if (action.test(pos)) {
                                    return pos;
                                }
                            }
                        }
                    }
                }
            }

        }
        return center;
    }

    /**
     * Check if any blocl belong to structure area
     *
     * @param area  - current area
     * @param poses - list of structure block
     * @return
     */
    public static boolean containsInArea(AxisAlignedBB area, BlockPos... poses) {
        if (poses == null || poses.length < 1 || area == null)
            return false;

        for (BlockPos pos : poses) {
            if (pos.getX() >= area.minX && pos.getX() <= area.maxX) {
                if (pos.getY() >= area.minY && pos.getY() <= area.maxY) {
                    if (pos.getZ() >= area.minZ && pos.getZ() <= area.maxZ) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * copy of BlockPattern.translateOffset
     *
     * @param pos
     * @param finger
     * @param thumb
     * @param palmOffset
     * @param thumbOffset
     * @param fingerOffset
     * @return
     */
    public static BlockPos translateOffset(BlockPos pos, EnumFacing finger, EnumFacing thumb, int palmOffset, int thumbOffset, int fingerOffset) {
        if (finger != thumb && finger != thumb.getOpposite()) {
            Vec3i vec3i = new Vec3i(finger.getFrontOffsetX(), finger.getFrontOffsetY(), finger.getFrontOffsetZ());
            Vec3i vec3i1 = new Vec3i(thumb.getFrontOffsetX(), thumb.getFrontOffsetY(), thumb.getFrontOffsetZ());
            Vec3i vec3i2 = vec3i.crossProduct(vec3i1);
            return pos.add(vec3i1.getX() * -thumbOffset + vec3i2.getX() * palmOffset + vec3i.getX() * fingerOffset, vec3i1.getY() * -thumbOffset + vec3i2.getY() * palmOffset + vec3i.getY() * fingerOffset, vec3i1.getZ() * -thumbOffset + vec3i2.getZ() * palmOffset + vec3i.getZ() * fingerOffset);
        } else {
            throw new IllegalArgumentException("Invalid forwards & up combination");
        }
    }

    /**
     * Search tile in radius
     *
     * @param world - world
     * @param area  - search area
     * @param clazz - class of entity
     * @param <T>
     * @return
     */
    @Nullable
    public static <T> T findTile(World world, AxisAlignedBB area, Class<T> clazz) {
        Iterator<BlockPos> iterator = BlockPos.getAllInBox(new BlockPos(area.minX, area.minY, area.minZ), new BlockPos(area.maxX, area.maxY, area.maxZ)).iterator();

        while (iterator.hasNext()) {
            TileEntity entity = world.getTileEntity(iterator.next());

            if (clazz.isInstance(entity)) {
                return (T) entity;
            }
        }

        return null;
    }

    /**
     * Searches for tiles in radisu
     *
     * @param world
     * @param area
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> findTiles(World world, AxisAlignedBB area, Class<T> clazz) {
        Iterator<BlockPos> iterator = BlockPos.getAllInBox(new BlockPos(area.minX, area.minY, area.minZ), new BlockPos(area.maxX, area.maxY, area.maxZ)).iterator();
        List<T> result = new ArrayList<>();

        while (iterator.hasNext()) {
            TileEntity entity = world.getTileEntity(iterator.next());

            if (clazz.isInstance(entity)) {
                result.add((T) entity);
            }
        }

        return result;
    }

    /**
     * Searching tile in multistructure
     *
     * @param world  - world
     * @param pos    - pos of controller/structure block
     * @param clazz  - class of controller block
     * @param result - list of tiles
     * @param cache  - loading cache
     * @param <T>    - type of TileEntity
     * @return
     */
    public static <T> List<T> findTilesInStructureBlocks(World world,
                                                         BlockPos pos,
                                                         Class<T> clazz,
                                                         @Nullable List<T> result,
                                                         @Nullable Set<BlockPos> checkedPos,
                                                         @Nullable LoadingCache<BlockPos, BlockWorldState> cache) {
        if (result == null) {
            result = new ArrayList<>();
        }

        if (checkedPos == null) {
            checkedPos = Sets.newHashSet();
        }

        if (cache == null) {
            cache = BlockPattern.createLoadingCache(world, true);
        }

        checkedPos.add(pos);

        TileEntity tileEntity = world.getTileEntity(pos);

        if (clazz.isInstance(tileEntity)) {
            result.add((T) tileEntity);
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = pos.offset(facing);
            Block block = cache.getUnchecked(offset).getBlockState().getBlock();
            tileEntity = world.getTileEntity(offset);

            boolean canSearchFurther = false;

            if (clazz.isInstance(tileEntity)) {
                result.add((T) tileEntity);
                canSearchFurther = true;
            }

            if (block == BlockRegistry.structure_block) {
                canSearchFurther = true;
            }

            // can go futher but we havet ever been there
            if (canSearchFurther && !checkedPos.contains(offset)) {
                findTilesInStructureBlocks(world, offset, clazz, result, checkedPos, cache);
            }
        }

        return result;
    }

    /**
     * Scan area for blocks
     *
     * @param area
     * @return
     */
    public static Iterator<BlockPos> forEveryBlock(AxisAlignedBB area, Predicate<BlockPos> predicate) {
        return StreamSupport.stream(BlockPos.getAllInBox(new BlockPos(area.minX, area.minY, area.minZ), new BlockPos(area.maxX, area.maxY, area.maxZ))
                .spliterator(), false)
                .filter(predicate)
                .iterator();
    }

    /**
     * Deep seach for connected blocks
     *
     * @param pos    - current pos
     * @param block  - block to search
     * @param result - set resut
     * @param cache  - possible loading cache (optimization)
     * @return
     */
    public static Set<BlockPos> search(BlockPos pos, Block block, Set<BlockPos> result, LoadingCache<BlockPos, BlockWorldState> cache) {

        IBlockState state = cache.getUnchecked(pos).getBlockState();

        if (state.getBlock() == block) {
            result.add(pos);
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos offset = pos.offset(facing);
            if (!result.contains(offset)) {
                state = cache.getUnchecked(offset).getBlockState();
                if (state.getBlock() == block) {
                    search(offset, block, result, cache);
                }
            }
        }

        return result;
    }
}