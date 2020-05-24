package divinerpg.utils.tasks;

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TaskFactory<T extends Event> {
    private final Map<UUID, ScheduledTask<IEventTask<T>>> playerTasks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledTask<ITask>> pendings = new ConcurrentHashMap<>();
    private Function<T, UUID> findActorFunc;

    protected TaskFactory(Function<T, UUID> findActorFunc) {
        this.findActorFunc = findActorFunc;
    }

    /**
     * Creating new task from player ID and event
     *
     * @param id
     * @param event
     * @return
     */
    protected abstract IEventTask<T> createTask(UUID id, T event);

    /**
     * Should we proceed current event?
     * Brand new for each player
     *
     * @param event - event
     * @return
     */
    protected abstract boolean shouldProceed(T event);

    /**
     * Gets thread listener from event
     *
     * @param event
     * @return
     */
    protected abstract IThreadListener getListener(T event);

    /**
     * Called after server tick with default delay
     */
    protected void checkPendings() {
        pendings.values().forEach(ScheduledTask::onServerTick);
    }

    /**
     * Get task delay
     *
     * @return
     */
    protected abstract int getDelay();

    /**
     * Should called on every implementation.
     * Create a method with @SubscribeEvent
     *
     * @param event
     */
    public void listen(T event) {
        if (event == null)
            return;

        // getting actor from event
        UUID id = findActorFunc.apply(event);
        if (id == null)
            return;

        // find task on current player
        ScheduledTask<IEventTask<T>> task = playerTasks.get(id);

        if (task != null) {

            // check should we merge work with current event
            if (task.notStarted() && task.getTask().shouldMerge(event)) {
                // merging work and set delay
                task.getTask().merge(event);
            }
        } else {
            if (shouldProceed(event)) {
                IEventTask<T> newTask = createTask(id, event);

                if (newTask != null) {
                    newTask.merge(event);

                    scheduleTask(getListener(event), newTask);
                }
            }
        }
    }

    /**
     * Schedules user created task
     *
     * @param listener
     * @param task
     */
    protected void scheduleTask(IThreadListener listener, IEventTask<T> task) {
        if (listener == null)
            return;

        playerTasks.put(task.getActor(), new ScheduledTask<>(
                listener,
                task,
                finished -> playerTasks.remove(finished.getActor()),
                getDelay()));
    }

    /**
     * Shedules pending task
     *
     * @param listener - execution listener
     * @param task     - pending task
     */
    protected void scheduleTask(IThreadListener listener, ITask task) {
        scheduleTask(listener, task, 0, null);
    }

    /**
     * Shedules pending task
     *
     * @param listener - execution listener
     * @param task     - pending task
     * @param delay    - task delay execution. Mesured NOT in ticks but in pending check cycles.
     *                 So 3 means task will skip 3 cycles and will execute on 4 time
     * @param onFinish - call back on execution finish. Called after task removal from common list
     */
    protected <T extends ITask> void scheduleTask(IThreadListener listener, T task, int delay, @Nullable Consumer<T> onFinish) {
        if (listener == null)
            return;

        Consumer<ITask> afterExecution = iTask -> {
            pendings.remove(iTask.getActor());

            if (onFinish != null) {
                onFinish.accept((T) iTask);
            }
        };

        pendings.put(task.getActor(), new ScheduledTask<>(
                listener,
                task,
                afterExecution,
                delay)
        );
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase != TickEvent.Phase.END)
            return;

        playerTasks.values().forEach(ScheduledTask::onServerTick);

        if (FMLCommonHandler.instance().getMinecraftServerInstance().getTickCounter() % getDelay() == 0) {
            checkPendings();
        }
    }

    /**
     * Search not started task in schedulad work
     *
     * @param id    - id of task
     * @param clazz - task class
     * @param <T1>
     * @return
     */
    @Nullable
    protected <T1 extends ITask> T1 findPendingById(UUID id, Class<T1> clazz) {
        ScheduledTask<IEventTask<T>> task = playerTasks.get(id);
        if (task != null && task.notStarted() && clazz.isInstance(task.getTask())) {
            return (T1) task.getTask();
        }

        ScheduledTask<ITask> scheduledTask = pendings.get(id);
        if (scheduledTask != null && scheduledTask.notStarted() && clazz.isInstance(scheduledTask.getTask())) {
            return (T1) scheduledTask.getTask();
        }

        return null;
    }
}
