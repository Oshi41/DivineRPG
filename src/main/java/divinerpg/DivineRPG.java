/**
 * @author NicosaurusRex99
 */
package divinerpg;

import divinerpg.config.GeneralConfig;
import divinerpg.events.enchants.WorldBreakEnchantHandler;
import divinerpg.events.server.SwapFactory;
import divinerpg.utils.UpdateChecker;
import org.apache.logging.log4j.LogManager;

import divinerpg.api.armor.registry.IArmorDescription;
import divinerpg.events.*;
import divinerpg.proxy.CommonProxy;
import divinerpg.registry.*;
import divinerpg.utils.Utils;
import divinerpg.utils.attributes.AttributeFixer;
import divinerpg.utils.multiblock.MultiblockDescription;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.registries.RegistryBuilder;

@Mod(modid = DivineRPG.MODID, name = DivineRPG.NAME, version = DivineRPG.VERSION, updateJSON = DivineRPG.UPDATE_URL)
public class DivineRPG {

    public static final String MODID = "divinerpg";
    public static final String NAME = "DivineRPG";
    public static final String VERSION = "1.6.3.1";
    public static final String UPDATE_URL = "https://raw.githubusercontent.com/NicosaurusRex99/DivineRPG/1.12.2/divinerpg_update.json";

    @Mod.Instance
    public static DivineRPG instance;

    @SidedProxy(serverSide = "divinerpg.proxy.CommonProxy", clientSide = "divinerpg.proxy.ClientProxy")
    public static CommonProxy proxy;

    public static org.apache.logging.log4j.Logger logger;

    public static SimpleNetworkWrapper network = new SimpleNetworkWrapper(MODID);

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public DivineRPG() {
        MinecraftForge.EVENT_BUS.register(this);
        logger = LogManager.getLogger();
    }

    /**
     * Init Methods
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        LiquidRegistry.registerFluids();
        proxy.preInit(event);
        proxy.registerTileEntities();
        MinecraftForge.EVENT_BUS.register(new ArcanaTickHandler());
        MinecraftForge.EVENT_BUS.register(new EventEntityDrop());
        MinecraftForge.EVENT_BUS.register(new WorldBreakEnchantHandler());
        MessageRegistry.initMessages();
        AttributeFixer.patchMaximumHealth();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        proxy.init(e);
        OreDictionaryHandler.registerOreDictionaryEntries();
        TriggerRegistry.registerTriggers();
        SmeltingRecipeRegistry.registerSmeltingRecipes();
        MinecraftForge.EVENT_BUS.register(new GeneralConfig());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        proxy.postInit(e);
        if (Loader.isModLoaded("projecte")) {
            divinerpg.compat.ProjectECompat.init();
        }
        EntitySpawnRegistry.initSpawns();
        Utils.loadHatInformation();
        DimensionHelper.initPortalDescriptions();

        MinecraftForge.EVENT_BUS.register(SwapFactory.instance);

        UpdateChecker.checkForUpdates();
    }

    /**
     * Creating new registry here
     */
    @SubscribeEvent
    public void createRegistries(RegistryEvent.NewRegistry event) {
        logger.info("Creating registries");

        new RegistryBuilder<IArmorDescription>()
                .setName(new ResourceLocation(MODID, "armor_descriptions"))
                .setType(IArmorDescription.class)
                .create();
    }

}