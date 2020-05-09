package divinerpg.utils.tasks;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.function.Consumer;

public class ScheduledTask<T extends Event> {
    private final ITask<T> task;
    private IThreadListener listener;
    private Consumer<ITask<T>> onFinish;
    private boolean isExecuting;
    private int delay;

    public ScheduledTask(IThreadListener listener, ITask<T> task, Consumer<ITask<T>> onFinish, int delay) {
        this.listener = listener;
        this.task = task;
        this.onFinish = onFinish;
        this.delay = delay;
    }

    public ITask<T> getTask() {
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
