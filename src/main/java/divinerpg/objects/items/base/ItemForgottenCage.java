package divinerpg.objects.items.base;

import divinerpg.DivineRPG;
import divinerpg.objects.entities.entity.vanilla.AncientKingEntity;
import divinerpg.objects.items.twilight.ItemBossSpawner;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Function;

public class ItemForgottenCage extends ItemBossSpawner {
    public ItemForgottenCage() {
        super("forgotten_cage", "message.ancient_king", x -> x.getName().contains(DivineRPG.MODID), AncientKingEntity::new);
    }

    @Override
    protected Entity createEntity(Function<World, Entity> func, EntityPlayer player, World world, BlockPos pos) {
        return new AncientKingEntity(world, pos.up(5));
    }
}
