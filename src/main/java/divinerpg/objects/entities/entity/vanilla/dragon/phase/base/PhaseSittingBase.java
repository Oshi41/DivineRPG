package divinerpg.objects.entities.entity.vanilla.dragon.phase.base;

import divinerpg.objects.entities.entity.vanilla.dragon.DivineDragonBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;

public abstract class PhaseSittingBase extends PhaseBase {
    public PhaseSittingBase(DivineDragonBase dragon) {
        super(dragon);
    }

    public boolean getIsStationary() {
        return true;
    }

    /**
     * Normally, just returns damage. If dragon is sitting and src is an arrow, arrow is enflamed and zero damage
     * returned.
     * Like its burning the arrows with flame
     */
    public float getAdjustedDamage(MultiPartEntityPart pt, DamageSource src, float damage) {
        if (src.getImmediateSource() instanceof EntityArrow) {
            src.getImmediateSource().setFire(1);
            return 0.0F;
        } else {
            return super.getAdjustedDamage(pt, src, damage);
        }
    }
}
