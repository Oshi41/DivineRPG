package divinerpg.objects.blocks.tile.entity;

import com.google.common.collect.Sets;
import divinerpg.api.DivineAPI;
import divinerpg.api.Reference;
import divinerpg.api.armor.ArmorEquippedEvent;
import divinerpg.config.Config;
import divinerpg.objects.blocks.tile.container.KingCompressorContainer;
import divinerpg.objects.blocks.tile.entity.base.IFuelProvider;
import divinerpg.objects.blocks.tile.entity.multiblock.TileEntityDivineMultiblock;
import divinerpg.objects.blocks.tile.entity.pillar.IStackListener;
import divinerpg.objects.blocks.tile.entity.pillar.TileEntityPillar;
import divinerpg.registry.ModItems;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.MultiblockDescription;
import divinerpg.utils.multiblock.StructureMatch;
import net.minecraft.entity.EntityLiving;
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
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.items.wrapper.EmptyHandler;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class TileEntityKingCompressor extends TileEntityDivineMultiblock implements IFuelProvider, ITickable, IInteractionObject {
    //region Fields
    /**
     * List of possible fuels
     */
    public static final Map<Item, Integer> fuelMap = new HashMap<Item, Integer>() {{
        put(ModItems.shadowStone, 500);
        put(ModItems.divineStone, 250);
        put(ModItems.arlemiteIngot, (int) Math.ceil(500 / 3.0));
    }};
    private static final ResourceLocation id = new ResourceLocation(Reference.MODID, "king_compressor");

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
    private IItemHandlerModifiable container;
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
        super(MultiblockDescription.instance.findById(new ResourceLocation(Reference.MODID, "king_compressor")),
                id.toString(), null);
        burnTime = 0;
        cookTime = 0;
        fuelSlots = new HashSet<Integer>() {{
            add(6);
        }};
        outputSlot = 7;

        container = new EmptyHandler();

        setsLimit = (int) (DivineAPI.getArmorDescriptionRegistry().getKeys().size() * (Config.kingCreationPercentage / 100.0));
    }

    // region IFuelProvider
    @Override
    public boolean needFuel() {
        return false;
    }

    @Override
    public int consumeFuel() {
        for (int i = 0; i < getInventoryRef().getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);

            Integer burntime = fuelMap.get(stack.getItem());
            if (burntime != null) {

                decrStackSize(i, 1);
                return burntime;
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
        return 1000;
    }

    @Override
    public void changeBurnState(boolean isBurning) {
        // ignored
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

        // container.deserializeNBT(compound.getCompoundTag("container"));

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

        // result.setTag("container", container.serializeNBT());
        result.setInteger("burn", getBurningTicks());
        result.setInteger("cook", getCurrentCookTime());

        NBTTagList sets = new NBTTagList();
        absorbedSets.forEach(x -> sets.appendTag(new NBTTagString(x.toString())));

        result.setTag("absorbed", sets);

        return result;
    }

    // endregion

    @Override
    public void onBuilt(@Nonnull StructureMatch match){
        super.onBuilt(match);

        List<TileEntityPillar> pillars = PositionHelper.findTiles(world, match.area, TileEntityPillar.class);

        container = new CombinedInvWrapper(pillars
                .stream()
                .map(TileEntityPillar::getInventory)
                .toArray(IItemHandlerModifiable[]::new)
        );

        pillars.stream().filter(x -> x.getInventory() instanceof IStackListener)
                .forEach(x -> ((IStackListener) x.getInventory()).addListener(this::recheckRecipe));
    }

    @Override
    public void onDestroy(@Nonnull StructureMatch match) {
        super.onDestroy(match);

        container = new EmptyHandler();
    }

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
        Map<EntityEquipmentSlot, ItemStack> input = new HashMap<>();

        for (int i = 0; i < getInventoryRef().getSlots(); i++) {
            ItemStack slot = getStackInSlot(i);
            input.put(EntityLiving.getSlotForItemStack(slot), slot);
        }

        if (input.isEmpty() || input.values().stream().allMatch(ItemStack::isEmpty))
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
