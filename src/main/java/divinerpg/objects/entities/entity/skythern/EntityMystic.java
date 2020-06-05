package divinerpg.objects.entities.entity.skythern;

import divinerpg.enums.BulletType;
import divinerpg.objects.entities.entity.EntityMageBase;
import divinerpg.registry.LootTableRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityMystic extends EntityMageBase {

    public EntityMystic(World worldIn) {
        super(worldIn, BulletType.MYSTIC_SHOT);
    }

    @Override
    protected ResourceLocation getLootTable() {
        return LootTableRegistry.ENTITIES_MYSTIC;
    }
}