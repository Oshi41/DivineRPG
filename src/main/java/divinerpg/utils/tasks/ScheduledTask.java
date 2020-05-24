package divinerpg.utils.tasks;

import net.minecraft.util.IThreadListener;

import java.util.function.Consumer;

public class ScheduledTask<T extends ITask> {
    private final T task;
    private IThreadListener listener;
    private Consumer<T> onFinish;
    private boolean isExecuting;
    private int delay;

    public ScheduledTask(IThreadListener listener, T task, Consumer<T> onFinish, int delay) {
        this.listener = listener;
        this.task = task;
        this.onFinish = onFinish;
        this.delay = delay;
    }

    public T getTask() {
        return task;
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
