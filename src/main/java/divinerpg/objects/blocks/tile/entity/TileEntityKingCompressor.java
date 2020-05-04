package divinerpg.objects.blocks.tile.entity;

import com.google.common.collect.Sets;
import divinerpg.api.DivineAPI;
import divinerpg.api.Reference;
import divinerpg.api.armor.ArmorEquippedEvent;
import divinerpg.config.Config;
import divinerpg.objects.blocks.tile.container.KingCompressorContainer;
import divinerpg.objects.blocks.tile.entity.base.IFuelProvider;
import divinerpg.objects.blocks.tile.entity.base.ModUpdatableTileEntity;
import divinerpg.objects.blocks.tile.entity.pillar.DivineStackHandler;
import divinerpg.registry.ModBlocks;
import divinerpg.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.*;
import java.util.stream.Collectors;

public class TileEntityKingCompressor extends ModUpdatableTileEntity implements IFuelProvider, ITickable, IInteractionObject {

    //region Fields
    /**
     * List of possible fuels
     */
    public static final Map<Item, Integer> fuelMap = new HashMap<Item, Integer>() {{
        put(ModItems.shadowStone, 500);
        put(ModItems.divineStone, 250);
        put(ModItems.arlemiteIngot, (int) Math.ceil(500 / 3.0));
    }};
    private final ResourceLocation id = new ResourceLocation(Reference.MODID, "king_compressor");

    /**
     * Id's of fuel slots
     */
    private final Set<Integer> fuelSlots;

    /**
     * Output slot id
     */
    private final int outputSlot;
    /**
     * List of absorbed sets
     */
    private final List<ResourceLocation> absorbedSets = new ArrayList<>();
    /**
     * Should keep inventory
     */
    public boolean keepInventory;
    /**
     * Current burning ticks
     */
    private int burnTime;
    /**
     * Gets current cooking ticks
     */
    private int cookTime;
    /**
     * Main container
     */
    private ItemStackHandler container;
    /**
     * Limit of absorbed sets amount
     */
    private int setsLimit;
    /**
     * Oprimizing. Check if we really have recipe for smelting
     */
    private boolean hasRecipe;
    //endregion

    public TileEntityKingCompressor() {
        burnTime = 0;
        cookTime = 0;
        fuelSlots = new HashSet<Integer>() {{
            add(6);
        }};
        outputSlot = 7;

        container = new DivineStackHandler(8, this::recheckRecipe, this::isItemValidForSlot);

        setsLimit = (int) (DivineAPI.getArmorDescriptionRegistry().getKeys().size() * (Config.kingCreationPercentage / 100.0));
    }

    // region IFuelProvider
    @Override
    public boolean needFuel() {
        return true;
    }

    @Override
    public int consumeFuel() {
        for (Integer id : fuelSlots) {
            ItemStack stack = getStackInSlot(id);
            if (fuelMap.containsKey(stack.getItem())) {
                int result = fuelMap.get(stack.getItem());

                decrStackSize(id, 1);
                return result;
            }
        }

        return 0;
    }

    @Override
    public void onFinished() {
        if (canMakeKingSet()) {
            absorbedSets.clear();

            // todo get boss summon item
        } else {
            absorbedSets.addAll(getSetsToApply());

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                setInventorySlotContents(slot.getSlotIndex(), ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public boolean haveItemsToSmelt() {
        return hasRecipe;
    }

    @Override
    public int getBurningTicks() {
        return burnTime;
    }

    @Override
    public void setBurningTicks(int value) {
        burnTime = value;
    }

    @Override
    public int getCurrentCookTime() {
        return cookTime;
    }

    @Override
    public void setCookTime(int value) {
        cookTime = value;
    }

    @Override
    public int getCookTimeLength() {
        return 500;
    }

    @Override
    public void changeBurnState(boolean isBurning) {
        TileEntity tileentity = world.getTileEntity(pos);

        Block block = isBurning
                ? ModBlocks.king_compression
                : ModBlocks.king_compression_still;

        if (world.getBlockState(pos).getBlock() != block) {
            keepInventory = true;
            world.setBlockState(pos, block.getDefaultState(), 3);
            keepInventory = false;

            if (tileentity != null) {
                tileentity.validate();
                world.setTileEntity(pos, tileentity);
            }
        }
    }

    @Override
    public IItemHandlerModifiable getInventoryRef() {
        return container;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return player.world.getTileEntity(pos) == this && player.getDistanceSq(pos) < 64;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (fuelSlots.contains(index)) {
            return fuelMap.containsKey(stack.getItem());
        }

        if (canMakeKingSet())
            return false;

        return index != outputSlot;
    }

    @Override
    public String getName() {
        return String.format("tile.%s.name", id.getResourcePath());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    // endregion

    // region NBT

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        container.deserializeNBT(compound.getCompoundTag("container"));

        setBurningTicks(compound.getInteger("burn"));
        setCookTime(compound.getInteger("cook"));

        absorbedSets.clear();
        NBTBase raw = compound.getTag("absorbed");
        if (raw instanceof NBTTagList) {
            ((NBTTagList) raw).forEach(x -> {
                if (x instanceof NBTTagString) {
                    absorbedSets.add(new ResourceLocation(((NBTTagString) x).getString()));
                }
            });
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound result = super.writeToNBT(compound);

        result.setTag("container", container.serializeNBT());
        result.setInteger("burn", getBurningTicks());
        result.setInteger("cook", getCurrentCookTime());

        NBTTagList sets = new NBTTagList();
        absorbedSets.forEach(x -> sets.appendTag(new NBTTagString(x.toString())));

        result.setTag("absorbed", sets);

        return result;
    }

    // endregion

    @Override
    public void update() {
        updateBurningTick();
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new KingCompressorContainer(playerInventory, this);
    }

    @Override
    public String getGuiID() {
        return id.toString();
    }


    private void recheckRecipe(int slot) {
        // should call anyway
        markDirty();

        hasRecipe = !getSetsToApply().isEmpty();
    }

    private Set<ResourceLocation> getSetsToApply() {
        Map<EntityEquipmentSlot, ItemStack> input = Arrays.stream(EntityEquipmentSlot.values()).collect(Collectors.toMap(x -> x, x -> getStackInSlot(x.getSlotIndex())));

        if (input.values().stream().allMatch(ItemStack::isEmpty))
            return Sets.newHashSet();

        ArmorEquippedEvent event = new ArmorEquippedEvent(input);
        MinecraftForge.EVENT_BUS.post(event);
        Set<ResourceLocation> confirmed = event.getConfirmed();
        confirmed.removeAll(absorbedSets);

        return confirmed;
    }

    /**
     * Condition if we can make an king set
     *
     * @return
     */
    public boolean canMakeKingSet() {
        return absorbedSets.size() >= setsLimit;
    }

    /**
     * Gets absorbed list
     *
     * @return
     */
    public Set<String> getAbsorbedSets() {
        return absorbedSets.stream().map(ResourceLocation::toString).collect(Collectors.toSet());
    }

    /**
     * Returns size of armor
     *
     * @return
     */
    public int getLimit() {
        return setsLimit;
    }
}
