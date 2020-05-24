package divinerpg.objects.blocks.tile.entity.base.rituals;

import divinerpg.objects.blocks.tile.entity.TileEntityKingCompressor;
import divinerpg.registry.ItemRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class FuelRitual extends RitualBase implements ITickable {
    private static List<ItemStack> possibleItems = new ArrayList<ItemStack>() {{
        add(ItemRegistry.divineStone.getDefaultInstance());
        add(new ItemStack(Items.GOLDEN_APPLE, 1, 1));
    }};

    private final ItemStack stack;
    private final ITextComponent msg;
    private TileEntityKingCompressor king;

    public FuelRitual(ResourceLocation id, TileEntity tile) {
        super(id, tile);

        stack = possibleItems.get(tile.getWorld().rand.nextInt(possibleItems.size()));

        msg = new TextComponentString(String.format("Use %s as fuel", stack.getDisplayName()));

        if (!(tile instanceof TileEntityKingCompressor)) {
            throw new RuntimeException("Tile should be instance of TileEntityKingCompressor tile");
        }

        king = (TileEntityKingCompressor) tile;
    }

    @Override
    public ITextComponent getDescription() {
        return msg;
    }

    @Override
    public void update() {
        if (isPerformed())
            return;

        if (king == null)
            return;

        for (int i = 0; i < king.getSizeInventory(); i++) {
            ItemStack stack = king.getStackInSlot(i);

            if (ItemStack.areItemsEqual(stack, this.stack)) {
                setIsPerformed(true);
                return;
            }
        }

        setIsPerformed(false);
    }
}
