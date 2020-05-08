package divinerpg.events.server;

import divinerpg.objects.blocks.tile.entity.multiblock.IMultiStructure;
import divinerpg.utils.multiblock.Matcher;
import divinerpg.utils.tasks.ITask;
import divinerpg.utils.tasks.ScheduledTask;
import divinerpg.utils.tasks.TaskFactory;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SwapFactory extends TaskFactory<EntityStruckByLightningEvent> {
    public final static SwapFactory instance = new SwapFactory();
    private final Map<IMultiStructure, Block> possiblePoses = new ConcurrentHashMap<>();

    protected SwapFactory() {
        super(x -> x.getLightning().getUniqueID());

        possiblePoses.put(
                new Matcher()
                        .aisle("xxx")
                        .where('x', Blocks.ANVIL.getDefaultState(), Blocks.OBSIDIAN.getDefaultState())
                        .build(),
                Blocks.ANVIL);

        possiblePoses.put(
                new Matcher()
                        .aisle(
                                "non",
                                "ooo",
                                "non"
                        )
                        .aisle(
                                "non",
                                "olo",
                                "nbn"
                        )
                        .aisle(
                                "non",
                                "oao",
                                "non"
                        )
                        .where('n', Blocks.NETHER_BRICK.getDefaultState(), Blocks.QUARTZ_BLOCK.getDefaultState())
                        .where('o', Blocks.OBSIDIAN.getDefaultState(), Blocks.GRASS.getDefaultState())
                        .where('l', Blocks.LAVA.getDefaultState(), Blocks.WATER.getDefaultState())
                        .where('a', Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState())
                        .where('b', Blocks.IRON_BARS.getDefaultState(), Blocks.OAK_FENCE.getDefaultState())
                        .build(),
                Blocks.ANVIL);
    }

    @Override
    protected ITask<EntityStruckByLightningEvent> createTask(UUID id, EntityStruckByLightningEvent event) {
        return new SwapTask(event.getLightning().getEntityWorld(), id, possiblePoses);
    }

    @Override
    protected boolean shouldProceed(EntityStruckByLightningEvent event) {
        ScheduledTask<EntityStruckByLightningEvent> task = playerTasks
                .values()
                .stream()
                .filter(x -> x.shouldMerge(event))
                .findFirst()
                .orElse(null);

        if (task != null) {
            task.getTask().merge(event);
            return false;
        }

        if (isNearMultistructure(event.getLightning().getEntityWorld(), event.getLightning().getPosition()))
            return true;

        return false;
    }

    @Override
    protected IThreadListener getListener(EntityStruckByLightningEvent event) {
        return event.getLightning().getServer();
    }

    @Override
    protected int getDelay() {
        return 20;
    }

    @SubscribeEvent
    public void listen(EntityStruckByLightningEvent e) {
        super.listen(e);
    }

    /**
     * Searches avtivating block near
     *
     * @param world
     * @param pos
     * @return
     */
    private boolean isNearMultistructure(World world, BlockPos pos) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    Block block = world.getBlockState(pos.add(i, j, k)).getBlock();

                    if (possiblePoses.entrySet().stream().anyMatch(x -> x.getValue() == block))
                        return true;
                }
            }
        }

        return false;
    }
}
