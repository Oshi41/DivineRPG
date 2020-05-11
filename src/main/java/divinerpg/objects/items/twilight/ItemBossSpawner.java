package divinerpg.objects.items.twilight;

import divinerpg.objects.items.base.ItemMod;
import divinerpg.registry.DivineRPGTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.server.command.TextComponentHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemBossSpawner extends ItemMod {

    private final Predicate<DimensionType> canSpawn;
    private final Function<World, Entity>[] spawnderEntities;
    private final String langKey;

    public ItemBossSpawner(String name, String langKey, Predicate<DimensionType> canSpawn, Function<World, Entity>... spawnedEntities) {
        super(name);
        this.canSpawn = canSpawn;
        this.spawnderEntities = spawnedEntities;
        setMaxStackSize(1);
        this.setCreativeTab(DivineRPGTabs.spawner);

        this.langKey = langKey;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing,
                                      float hitX, float hitY, float hitZ) {

        if (!world.isRemote) {
            if (!canSpawn.test(world.provider.getDimensionType())) {
                TextComponentBase message = TextComponentHelper.createComponentTranslation(player, langKey);
                message.getStyle().setColor(TextFormatting.AQUA);
                player.sendMessage(message);
                return EnumActionResult.FAIL;
            }

            List<Entity> toSpawn = Arrays.stream(spawnderEntities)
                    .map(x -> createEntity(x, player, world, pos))
                    .collect(Collectors.toList());

            if (toSpawn.stream().allMatch(x -> world.getCollisionBoxes(x, x.getEntityBoundingBox()).isEmpty())) {
                toSpawn.forEach(world::spawnEntity);

                if (!player.isCreative())
                    player.getHeldItemMainhand().shrink(toSpawn.size());

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.FAIL;
    }

    protected Entity createEntity(Function<World, Entity> func, EntityPlayer player, World world, BlockPos pos) {
        Entity e = func.apply(world);
        e.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
        return e;
    }
}