package divinerpg.objects.entities.entity.arcana;

import divinerpg.DivineRPG;
import divinerpg.objects.entities.entity.EntityDivineRPGVillager;
import divinerpg.proxy.GUIHandler;
import divinerpg.registry.ModItems;
import divinerpg.utils.LocalizeUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;

public class EntityLordVatticus extends EntityDivineRPGVillager {
    private static final String[] MESSAGE = { "message.vatticus.feel", "message.vatticus.noend",
            "message.vatticus.strength", "message.vatticus.discover", "message.vatticus.magic" };

    public EntityLordVatticus(World world) {
        super(world);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!this.world.isRemote) {
            player.openGui(DivineRPG.instance, GUIHandler.VATICUS_GUI_ID, this.world, getEntityId(), 0, 0);
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void extraInteract(EntityPlayer player) {
        player.sendMessage(LocalizeUtils.getChatComponent(LocalizeUtils.normal("entity.divinerpg.lord_vatticus.name") + ": "
                + LocalizeUtils.normal(MESSAGE[rand.nextInt(MESSAGE.length)])));
    }

    @Override
    protected boolean canDespawn() {
        return true;
    }

    @Override
    public void addRecipies(MerchantRecipeList list) {
        list.addAll(getAllRecipies());
    }

    public static MerchantRecipeList getAllRecipies(){
        MerchantRecipeList list = new MerchantRecipeList();
        list.add(new MerchantRecipe(new ItemStack(ModItems.collector, 16), new ItemStack(ModItems.chargedCollector)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 8), new ItemStack(ModItems.kormaHelmet)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 8), new ItemStack(ModItems.kormaChestplate)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 8), new ItemStack(ModItems.kormaLeggings)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 8), new ItemStack(ModItems.kormaBoots)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 10), new ItemStack(ModItems.vemosHelmet)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 10), new ItemStack(ModItems.vemosChestplate)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 10), new ItemStack(ModItems.vemosLeggings)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 10), new ItemStack(ModItems.vemosBoots)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 2), new ItemStack(ModItems.staffOfEnrichment)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 2), new ItemStack(ModItems.wizardsBook)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 1), new ItemStack(ModItems.weakArcanaPotion, 4)));
        list.add(
                new MerchantRecipe(new ItemStack(ModItems.arcanium, 2), new ItemStack(ModItems.strongArcanaPotion, 4)));
        list.add(new MerchantRecipe(new ItemStack(ModItems.arcanium, 20), new ItemStack(ModItems.orbOfLight)));
        return list;
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.posY < 40.0D && super.getCanSpawnHere();
    }
}