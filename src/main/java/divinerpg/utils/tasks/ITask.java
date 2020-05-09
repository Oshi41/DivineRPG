package divinerpg.utils.tasks;

import java.util.UUID;

public interface ITask {
    UUID getActor();

    /**
     * Do the main work
     */
    void execute();
}
