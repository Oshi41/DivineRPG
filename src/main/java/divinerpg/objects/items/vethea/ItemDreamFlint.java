package divinerpg.objects.items.vethea;

//public class ItemDreamFlint extends ItemMod {
//
//	public ItemDreamFlint() {
//		super("dream_flint", DivineRPGTabs.spawner);
//		setMaxStackSize(1);
//	}
//
//	@Override
//	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
//    {
//		ItemStack stack = player.getHeldItem(hand);
//        WreckForm wreckform = new WreckForm(world);
//        RaglokGogdure raglok = new RaglokGogdure(world);
//        Karos karos = new Karos(world);
//        Quadro quadro = new Quadro(world);
//		Block block = world.getBlockState(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).getBlock();
//		RayTraceResult rtr = PositionHelper.rayTrace(player,4, 1);
//		int x2 = rtr.getBlockPos().getX(), y2 = rtr.getBlockPos().getY(), z2 = rtr.getBlockPos().getZ();
//		if(world.getBlockState(pos.up())==Blocks.AIR.getDefaultState());{
//		if(!world.isRemote){
//			if(block == ModBlocks.wreckAltar){
//				wreckform.setLocationAndAngles(x2 + 0.5F, y2 + 1, z2 + 0.5F, 0.0F, 0.0F);
//				    world.spawnEntity(wreckform);
//				    if(!player.capabilities.isCreativeMode) stack.shrink(1);
//				return EnumActionResult.PASS;
//			}
//			if(block == ModBlocks.raglokAltar){
//				raglok.setLocationAndAngles(x2 + 0.5F, y2 + 1, z2 + 0.5F, 0.0F, 0.0F);
//				    world.spawnEntity(raglok);
//				    if(!player.capabilities.isCreativeMode) stack.shrink(1);
//				return EnumActionResult.PASS;
//			}
//			if(block == ModBlocks.karosAltar){
//				karos.setLocationAndAngles(x2 + 0.5F, y2 + 1, z2 + 0.5F, 0.0F, 0.0F);
//				    world.spawnEntity(karos);
//				    if(!player.capabilities.isCreativeMode) stack.shrink(1);
//				return EnumActionResult.PASS;
//			}
//			if(block == ModBlocks.quadroticAltar){
//				quadro.setLocationAndAngles(x2 + 0.5F, y2 + 1, z2 + 0.5F, 0.0F, 0.0F);
//				    world.spawnEntity(quadro);
//				    if(!player.capabilities.isCreativeMode) stack.shrink(1);
//				return EnumActionResult.PASS;
//			}
//			if(block == ModBlocks.quadroticAltar){
//				quadro.setLocationAndAngles(x2 + 0.5F, y2 + 1, z2 + 0.5F, 0.0F, 0.0F);
//				    world.spawnEntity(quadro);
//				    if(!player.capabilities.isCreativeMode) stack.shrink(1);
//				return EnumActionResult.PASS;
//			}
//			return EnumActionResult.FAIL;
//		}
//		}
//		return EnumActionResult.FAIL;
//	}
//}