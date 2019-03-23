package naturix.divinerpg.objects.items.iceika;

import java.util.List;

import javax.annotation.Nullable;

import naturix.divinerpg.objects.items.base.ItemBase;
import naturix.divinerpg.registry.DRPGCreativeTabs;
import naturix.divinerpg.registry.DRPGSoundHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

public class SerenadeIce extends ItemBase {

	public SerenadeIce(String name) {
		super(name);
		this.setCreativeTab(DRPGCreativeTabs.ranged);
		setMaxDamage(100);
		setMaxStackSize(1);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		tooltip.add("Slows mobs for 5 seconds");
		tooltip.add(stack.getMaxDamage() - stack.getItemDamage() + " uses left");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer entity, EnumHand hand) {
		ActionResult<ItemStack> ar = super.onItemRightClick(world, entity, hand);

		if (!world.isRemote) {
			// worldgen.spawnEntity(new EntitySerenadeOfIce(worldgen, entity));
			world.playSound(entity, entity.getPosition(), DRPGSoundHandler.SERENADE, SoundCategory.MASTER, 1, 1);

			entity.getHeldItem(hand).damageItem(1, entity);
		}
		return ar;
	}
}