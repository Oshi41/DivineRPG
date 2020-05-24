package divinerpg.objects.blocks.tile.entity.base.rituals;

import divinerpg.DivineRPG;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RitualRegistry {
    public static final ResourceLocation KILL_ANGRY_MOB = new ResourceLocation(DivineRPG.MODID, "kill_angry_mob");


    private static final Map<ResourceLocation, Function<TileEntity, ? extends IRitualDescription>> ritualsMap = new HashMap<>();

    static {
        register(KILL_ANGRY_MOB, entity -> new KillEntityRitual(KILL_ANGRY_MOB,
                entity,
                x -> {
                    if (!(x instanceof EntityLiving))
                        return false;

                    EntityLiving deadMob = (EntityLiving) x;

                    return deadMob.getAttackTarget() != null || deadMob.getRevengeTarget() != null;
                },
                "msg.kill_angry_mob_in_radius",
                4));
    }

    /**
     * Registeres new ritual
     *
     * @param id   - id of ritual
     * @param func - creation func
     */
    public static void register(ResourceLocation id, Function<TileEntity, ? extends IRitualDescription> func) {
        if (ritualsMap.containsKey(id)) {
            DivineRPG.logger.warn(String.format("This ritual  will be owerwritten: %s", id.toString()));
        }

        ritualsMap.put(id, func);
    }

    /**
     * Creates new ritual instance
     *
     * @param id     - id of ritual
     * @param entity - binded tile enrity
     * @param clazz  - class of ritual
     * @param <T>
     * @return
     */
    public static <T extends IRitualDescription> T createById(ResourceLocation id, TileEntity entity, Class<T> clazz) {
        if (id == null || entity == null || !ritualsMap.containsKey(id))
            return null;

        IRitualDescription description = ritualsMap.get(id).apply(entity);
        if (clazz.isInstance(description)) {

            if (description.isEventListener()) {
                MinecraftForge.EVENT_BUS.register(description);
            }

            return (T) description;
        }

        return null;
    }
}
