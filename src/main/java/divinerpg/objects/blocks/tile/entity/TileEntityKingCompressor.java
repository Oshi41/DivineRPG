package divinerpg.objects.blocks.tile.entity;

import com.google.common.collect.Sets;
import divinerpg.DivineRPG;
import divinerpg.api.DivineAPI;
import divinerpg.api.armor.ArmorEquippedEvent;
import divinerpg.config.Config;
import divinerpg.enums.ParticleType;
import divinerpg.objects.blocks.tile.container.KingCompressorContainer;
import divinerpg.objects.blocks.tile.entity.base.IFuelProvider;
import divinerpg.objects.blocks.tile.entity.base.rituals.RitualRegistry;
import divinerpg.objects.blocks.tile.entity.multiblock.TileEntityDivineMultiblock;
import divinerpg.objects.blocks.tile.entity.pillar.IStackListener;
import divinerpg.objects.blocks.tile.entity.pillar.TileEntityPedestal;
import divinerpg.registry.ItemRegistry;
import divinerpg.utils.PositionHelper;
import divinerpg.utils.multiblock.MultiblockDescription;
import divinerpg.utils.multiblock.StructureMatch;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IInteractionObject;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
//    public static final Map<Item, Integer> fuelMap = new HashMap<Item, Integer>() {{
//        put(ItemRegistry.shadowStone, 500);
//        put(ItemRegistry.divineStone, 250);
//        put(ItemRegistry.arlemiteIngot, (int) Math.ceil(500 / 3.0));
//    }};
    private static final ResourceLocation id = new ResourceLocation(DivineRPG.MODID, "king_compressor");

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

    @SideOnly(Side.CLIENT)
    private boolean firstTick = true;
    //endregion

    public TileEntityKingCompressor() {
        super(MultiblockDescription.instance.findById(new ResourceLocation(DivineRPG.MODID, "king_compressor")),
                id.toString(), null);
        burnTime = 0;
        cookTime = 0;
        fuelSlots = new HashSet<Integer>() {{
            add(6);
        }};
        outputSlot = 7;

        container = new EmptyHandler();
        setsLimit = (int) (DivineAPI.getArmorDescriptionRegistry().getKeys().size() * (Config.maxSetsPercentage / 100.0));

        setsLimit = 3;
        setCurrentRitual(RitualRegistry.KILL_ANGRY_MOB);
    }

    // region IFuelProvider
    @Override
    public boolean needFuel() {
        return false;
    }

    @Override
    public int consumeFuel() {
//        for (int i = 0; i < getInventoryRef().getSlots(); i++) {
//            ItemStack stack = getStackInSlot(i);
//
//            Integer burntime = fuelMap.get(stack.getItem());
//            if (burntime != null) {
//
//                decrStackSize(i, 1);
//                return burntime;
//            }
//        }

        return 0;
    }

    @Override
    public void onFinished() {
        if (!isConstructed())
            return;

        recreateContainer(getMultiblockMatch());

        if (canMakeKingSet()) {
            absorbedSets.clear();

            if (!world.isRemote) {
                EnumFacing forwards = getMultiblockMatch().up;

                if (world.rand.nextBoolean()) {
                    forwards = forwards.getOpposite();
                }

                AxisAlignedBB area = getMultiblockMatch().area;

                double length = area.getAverageEdgeLength() / 3 * 2;

                EntityItem cage = new EntityItem(world,
                        (area.maxX - area.minX) / 2.0 + forwards.getFrontOffsetX() * length,
                        (area.maxY - area.minY) / 2.0 + forwards.getFrontOffsetY() * length,
                        (area.maxZ - area.minZ) / 2.0 + forwards.getFrontOffsetZ() * length,
                        ItemRegistry.forgotten_cage.getDefaultInstance()
                );

                cage.motionX = forwards.getFrontOffsetX() * 0.3;
                cage.motionY = forwards.getFrontOffsetY() * 0.3;
                cage.motionZ = forwards.getFrontOffsetZ() * 0.3;

                cage.setEntityInvulnerable(true);
                cage.setNoDespawn();

                world.spawnEntity(cage);
            }

        } else if (haveItemsToSmelt()) {
            absorbedSets.addAll(getSetsToApply());
        }

        if (canMakeKingSet() || haveItemsToSmelt()) {
            for (int i = 0; i < container.getSlots(); i++) {
                container.setStackInSlot(i, ItemStack.EMPTY);
            }

            setNextRitual();
        }
    }

    @Override
    public void onStart() {

    }

    @Override
    public boolean haveItemsToSmelt() {
        if (!wasRitualPerformed())
            return false;

        return hasRecipe || canMakeKingSet();
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
        return !canMakeKingSet();
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
    public void onBuilt(@Nonnull StructureMatch match) {
        recreateContainer(match);
    }

    @Override
    public void onDestroy(@Nonnull StructureMatch match) {
        container = new EmptyHandler();
        world.setBlockToAir(getPos());
    }

    @Override
    public void update() {
        updateBurningTick();

        if (world.isRemote) {

            if (firstTick) {
                firstTick = false;

                if (!isConstructed())
                    recheckStructure();
            }

            spawnParticles();
        }

        if (getWorld().getTotalWorldTime() % 20 == 1
                && getCurrentRitual() instanceof ITickable) {
            ((ITickable) getCurrentRitual()).update();
        }
    }

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new KingCompressorContainer(playerInventory, this);
    }

    @Override
    public String getGuiID() {
        return id.toString();
    }

    @Override
    public void click(EntityPlayer player) {
        super.click(player);

        if (world.isRemote) {
            ITextComponent msg;

            if (hasRecipe
                    && !wasRitualPerformed()
                    && getCurrentRitual() != null) {
                // description about needed ritual
                msg = getCurrentRitual().getDescription();

                msg.getStyle().setColor(TextFormatting.RED);
            } else {
                // sets information
                msg = new TextComponentString(
                        String.format("%s of %s was collected",
                                absorbedSets.size(),
                                setsLimit));

                for (ResourceLocation set : absorbedSets) {
                    msg.appendText("\n" + set.toString());
                }
            }

            player.sendMessage(msg);
        }
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

    private void recreateContainer(@Nonnull StructureMatch match) {
        // find all pedestals staying on solid ground
        List<TileEntityPedestal> pillars = PositionHelper.findTiles(world, match.area.grow(1), TileEntityPedestal.class)
                .stream()
                .filter(x -> world.getBlockState(x.getPos().down()).isSideSolid(world, x.getPos().down(), EnumFacing.UP))
                .collect(Collectors.toList());

        container = new CombinedInvWrapper(pillars
                .stream()
                .map(TileEntityPedestal::getInventory)
                .toArray(IItemHandlerModifiable[]::new)
        );

        pillars.stream().filter(x -> x.getInventory() instanceof IStackListener)
                .forEach(x -> ((IStackListener) x.getInventory()).addListener(this::recheckRecipe));

        recheckRecipe(0);
    }

    @SideOnly(Side.CLIENT)
    private void spawnParticles() {
        if (getMultiblockMatch() == null) {
            if (constructedOnServer) {
                recheckStructure();
            }

            return;
        }

        AxisAlignedBB area = getMultiblockMatch().area;

        Random rand = world.rand;

        // above contruction
        Vec3d centerPos = area.getCenter();

        Vec3d center = centerPos.addVector(2 - rand.nextDouble() * 3, (area.maxY - area.minY) / 2 + 2, 2 - rand.nextDouble() * 3);

        ParticleType particleType = ParticleType.values()[1 + rand.nextInt(5)];

        for (int i = 0; i < 15; i++) {
            DivineRPG.proxy.spawnParticle(
                    world,
                    particleType,
                    center.x,
                    center.y,
                    center.z,
                    rand.nextFloat() * 2 - rand.nextFloat() * 2,
                    rand.nextFloat() * 3,
                    rand.nextFloat() * 2 - rand.nextFloat() * 2
            );
        }

        // perform actual work
        if (hasRecipe && getBurningTicks() % 20 == 1) {
            List<EnumFacing> directions = Arrays.asList(EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST);
            int length = (int) (area.getAverageEdgeLength() / 3 * 2);

            for (int i = 0; i < 4; i++) {
                EnumFacing finger = directions.get(rand.nextInt(directions.size()));
                EnumFacing thumb = rand.nextBoolean()
                        ? finger.rotateYCCW()
                        : finger.rotateYCCW().getOpposite();

                BlockPos temp = PositionHelper.translateOffset(new BlockPos(centerPos.x, centerPos.y, centerPos.z), finger, thumb,
                        -length * 2,
                        length,
                        length);

                Vec3d position = new Vec3d(temp).addVector(rand.nextDouble() * 2, rand.nextDouble() * 2, rand.nextDouble() * 2);

                this.world.playSound(null,
                        position.x, position.y, position.z,
                        SoundEvents.ENTITY_GENERIC_EXPLODE,
                        SoundCategory.BLOCKS,
                        4.0F,
                        (1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F);

                this.world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE,
                        position.x,
                        position.y,
                        position.z,
                        1.0D,
                        0.0D,
                        0.0D);
            }
        }
    }

    /**
     * Set new ritual
     */
    private void setNextRitual() {
        setCurrentRitual(RitualRegistry.getRandom(this));
    }
}
