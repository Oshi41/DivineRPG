package divinerpg.objects.entities.entity.vanilla.dragon;

import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhase;
import divinerpg.objects.entities.entity.vanilla.dragon.phase.base.IPhaseChange;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class DivinePhaseManager {
    private final Map<ResourceLocation, IPhase> phases = new HashMap<>();
    private DivineDragonBase dragon;
    private IPhaseChange change;
    private IPhase phase;

    public DivinePhaseManager(DivineDragonBase dragon, IPhaseChange change) {
        this.dragon = dragon;
        this.change = change;

        setPhase(PhaseRegistry.HOVER);
    }

    /**
     * Gets current executing phase
     *
     * @return
     */
    public IPhase getCurrentPhase() {
        return this.phase;
    }

    /**
     * Get or create phasew from ID
     *
     * @param id
     * @return
     */
    public <T extends IPhase> T getPhase(ResourceLocation id) {
        return (T) phases.computeIfAbsent(id, x -> PhaseRegistry.createNew(dragon, id));
    }

    /**
     * Set phase from id
     *
     * @param id
     */
    public void setPhase(ResourceLocation id) {
        if (change != null) {
            id = change.fixPhase(dragon, phase, id);
        }

        if (phase != null) {
            phase.removeAreaEffect();
        }

        phase = getPhase(id);

        phase.initPhase();

        if (phase != null && !this.dragon.world.isRemote) {
            this.dragon.getDataManager().set(DivineDragonBase.PHASE, id.toString());
        }
    }


}
