package divinerpg.registry;

import divinerpg.DivineRPG;
import divinerpg.objects.blocks.tile.entity.base.rituals.FuelRitual;
import divinerpg.objects.blocks.tile.entity.base.rituals.IRitualDescription;
import divinerpg.objects.blocks.tile.entity.base.rituals.KillEntityRitual;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public class RitualRegistry {
    public static final ResourceLocation KILL_ANGRY_MOB = new ResourceLocation(DivineRPG.MODID, "kill_angry_mob");
    public static final ResourceLocation NEED_FUEL = new ResourceLocation(DivineRPG.MODID, "need_fuel");


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
                new TextComponentString("Kill angry mob near the structure"),
                4));

        register(NEED_FUEL, x -> new FuelRitual(NEED_FUEL, x));
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

    /**
     * Gets random ritual
     *
     * @param entity - tile entity
     * @return
     */
    public static IRitualDescription getRandom(TileEntity entity) {
        Random rand = entity.getWorld().rand;

        int index = rand.nextInt(ritualsMap.size());
        ResourceLocation id = ritualsMap.keySet().stream().skip(index).findFirst().orElse(KILL_ANGRY_MOB);

        return ritualsMap.get(id).apply(entity);
    }
}
