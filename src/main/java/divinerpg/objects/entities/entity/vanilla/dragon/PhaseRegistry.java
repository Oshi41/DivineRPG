package divinerpg.objects.entities.entity.vanilla.dragon;

import divinerpg.api.Reference;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.*;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhase;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PhaseRegistry {
    public static final ResourceLocation HOVER = new ResourceLocation(Reference.MODID, "hover");
    public static final ResourceLocation STRIFE_PLAYER = new ResourceLocation(Reference.MODID, "strife_player");
    public static final ResourceLocation DYING = new ResourceLocation(Reference.MODID, "dying");
    public static final ResourceLocation LANDING_APPROACH = new ResourceLocation(Reference.MODID, "landing_approach");
    public static final ResourceLocation TAKEOFF = new ResourceLocation(Reference.MODID, "takeoff");
    public static final ResourceLocation SITTING_FLAMING = new ResourceLocation(Reference.MODID, "sitting_flaming");
    public static final ResourceLocation LANDING = new ResourceLocation(Reference.MODID, "landing");
    public static final ResourceLocation CHARGING_PLAYER = new ResourceLocation(Reference.MODID, "charging_player");
    public static final ResourceLocation SITTING_SCANNING = new ResourceLocation(Reference.MODID, "sitting_scanning");
    public static final ResourceLocation SITTING_ATTACKING = new ResourceLocation(Reference.MODID, "sitting_attacking");

    /**
     * List of all phases
     */
    private final static Map<ResourceLocation, Function<DivineDragonBase, ? extends IPhase>> allPhases = new HashMap<ResourceLocation, Function<DivineDragonBase, ? extends IPhase>>() {{
        put(HOVER, PhaseHover::new);
        put(STRIFE_PLAYER, PhaseStrafePlayer::new);
        put(DYING, PhaseDying::new);
        put(LANDING_APPROACH, PhaseLanding::new);
        put(TAKEOFF, PhaseTakeoff::new);
        put(SITTING_FLAMING, PhaseSittingFlaming::new);
        put(LANDING, PhaseLanding::new);
        put(CHARGING_PLAYER, PhaseChargingPlayer::new);
        put(SITTING_SCANNING, PhaseSittingScanning::new);
        put(SITTING_ATTACKING, PhaseSittingAttacking::new);
    }};

    public static void register(Function<DivineDragonBase, ? extends IPhase> func, ResourceLocation name) {
        if (allPhases.containsKey(name)) {
            throw new RuntimeException("That key for dragon phase was already registered: " + name.toString());
        }

        allPhases.put(name, func);
    }

    /**
     * Creates new instance from phase ID
     *
     * @param dragon
     * @param id
     * @param <T>
     * @return
     */
    public static <T extends IPhase> T createNew(DivineDragonBase dragon, ResourceLocation id) {
        Function<DivineDragonBase, ? extends IPhase> function = allPhases.get(id);

        if (function == null) {
            throw new RuntimeException("Seems like this dragon phase isn't registered: " + id.toString());
        }

        return (T) function.apply(dragon);
    }
}
