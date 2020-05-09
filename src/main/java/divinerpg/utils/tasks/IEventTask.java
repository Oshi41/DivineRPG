package divinerpg.utils.tasks;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public interface IEventTask<T extends Event> extends ITask {

    /**
     * Merge with new event.
     * Should contains logic os scheduling work here, because
     * it's calling in factory after constructor
     *
     * @param event
     */
    void merge(T event);

    /**
     * Checks wherever work was scheduled
     *
     * @param event
     * @return
     */
    boolean shouldMerge(T event);

}
