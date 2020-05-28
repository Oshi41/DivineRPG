package divinerpg.objects.blocks.tile.entity.base.rituals;

import divinerpg.registry.RitualRegistry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public interface IRitualConsumer {
    /**
     * Gets current ritual
     *
     * @return
     */
    @Nullable
    IRitualDescription getCurrentRitual();

    /**
     * Set current ritual
     *
     * @param description
     */
    void setCurrentRitual(IRitualDescription description);

    /**
     * If instance of interface is Tile entity, method will create new Ritual instance from ID
     *
     * @param id
     */
    default void setCurrentRitual(ResourceLocation id) {
        if (this instanceof TileEntity) {
            setCurrentRitual(RitualRegistry.createById(id, (TileEntity) this, IRitualDescription.class));
        }
    }

    /**
     * Was ritual performed
     *
     * @return
     */
    default boolean wasRitualPerformed() {
        return getCurrentRitual() == null || getCurrentRitual().isPerformed();
    }
}
