package divinerpg.objects.blocks.tile.entity.pillar;

import java.util.function.Consumer;

public interface IStackListener {

    /**
     * Subscribe on listener
     *
     * @param onSlotChanged
     */
    void addListener(Consumer<Integer> onSlotChanged);
}
