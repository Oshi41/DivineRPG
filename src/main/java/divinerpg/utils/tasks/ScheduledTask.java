package divinerpg.utils.tasks;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.function.Consumer;

public class ScheduledTask<T extends Event> {
    private final IEventTask<T> task;
    private IThreadListener listener;
    private Consumer<IEventTask<T>> onFinish;
    private boolean isExecuting;
    private int delay;

    public ScheduledTask(IThreadListener listener, IEventTask<T> task, Consumer<IEventTask<T>> onFinish, int delay) {
        this.listener = listener;
        this.task = task;
        this.onFinish = onFinish;
        this.delay = delay;
    }

    public IEventTask<T> getTask() {
        return task;
    }

    /**
     * Can merge new events only if not already executing.
     * Avoid cascading execution
     *
     * @param event
     * @return
     */
    public boolean shouldMerge(T event) {
        return notStarted() && getTask().shouldMerge(event);
    }

    /**
     * Was
     *
     * @return
     */
    public boolean notStarted() {
        return !isExecuting;
    }

    public void onServerTick() {
        delay--;
        if (delay >= 0)
            return;

        // already schedule executing and can't stop this
        isExecuting = true;

        listener.addScheduledTask(() -> {
            try {
                task.execute();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                onFinish.accept(task);
            }
        });
    }
}
