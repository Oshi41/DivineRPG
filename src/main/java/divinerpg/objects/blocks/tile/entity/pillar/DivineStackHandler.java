package divinerpg.objects.blocks.tile.entity.pillar;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class DivineStackHandler extends ItemStackHandler implements IStackListener {

    private final List<Consumer<Integer>> callbacks = new ArrayList<>();
    private final BiPredicate<Integer, ItemStack> isSlotValid;
    private final int maxStackSize;

    public DivineStackHandler(int count,
                              Consumer<Integer> onContentChanged,
                              BiPredicate<Integer, ItemStack> isSlotValid) {
        this(count, onContentChanged, isSlotValid, 64);
    }

    public DivineStackHandler(int count,
                              Consumer<Integer> onContentChanged,
                              BiPredicate<Integer, ItemStack> isSlotValid,
                              int maxStackSize) {
        super(count);
        callbacks.add(onContentChanged);
        this.isSlotValid = isSlotValid;
        this.maxStackSize = maxStackSize;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack) && isSlotValid.test(slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return maxStackSize;
    }

    @Override
    protected void onContentsChanged(int slot) {
        callbacks.forEach(x -> x.accept(slot));
    }

    @Override
    public void addListener(Consumer<Integer> onSlotChanged) {
        callbacks.add(onSlotChanged);
    }
}
