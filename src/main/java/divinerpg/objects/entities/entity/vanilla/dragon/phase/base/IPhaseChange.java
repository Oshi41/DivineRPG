package divinerpg.objects.entities.entity.vanilla.dragon.phase.base;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import net.minecraft.util.ResourceLocation;

@FunctionalInterface
public interface IPhaseChange {
    /**
     * Need to change dragon behavior.
     * Return 'toChange' or any other phase you want to change
     *
     * @param dragon   - dragon entity
     * @param current  - current phase
     * @param toChange - change that will be set
     * @return
     */
    ResourceLocation fixPhase(DivineDragonBase dragon, IPhase current, ResourceLocation toChange);
}
