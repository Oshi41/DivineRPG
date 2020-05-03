package divinerpg.objects.blocks.tile.entity.pillar;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class DivineStackHandler extends ItemStackHandler {

    private final Consumer<Integer> onContentChangedCallBack;
    private final BiPredicate<Integer, ItemStack> isSlotValid;

    public DivineStackHandler(int count, Consumer<Integer> onContentChanged, BiPredicate<Integer, ItemStack> isSlotValid) {
        super(count);
        this.onContentChangedCallBack = onContentChanged;
        this.isSlotValid = isSlotValid;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return super.isItemValid(slot, stack) && isSlotValid.test(slot, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        onContentChangedCallBack.accept(slot);
    }
}
