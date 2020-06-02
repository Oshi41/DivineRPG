package divinerpg.registry;

import divinerpg.DivineRPG;
import divinerpg.potions.GravityPotion;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
@GameRegistry.ObjectHolder("divinerpg")
public class PotionRegistry {
    @GameRegistry.ObjectHolder("gravity_effect")
    public static final Potion Gravity = null;

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Potion> e) {
        IForgeRegistry<Potion> registry = e.getRegistry();

        registry.register(new GravityPotion().setRegistryName(DivineRPG.MODID, "gravity_effect"));
    }
}
