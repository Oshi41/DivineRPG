package divinerpg.registry;

import divinerpg.DivineRPG;
import divinerpg.config.GeneralConfig;
import divinerpg.dimensions.apalachia.WorldProviderApalachia;
import divinerpg.dimensions.arcana.WorldProviderArcana;
import divinerpg.dimensions.eden.WorldProviderEden;
import divinerpg.dimensions.galaxy.GalaxyWorldProvider;
import divinerpg.dimensions.iceika.WorldProviderIceika;
import divinerpg.dimensions.mortum.WorldProviderMortum;
import divinerpg.dimensions.skythern.WorldProviderSkythern;
import divinerpg.dimensions.vethea.VetheaWorldProvider;
import divinerpg.dimensions.wildwood.WorldProviderWildWood;
import divinerpg.structure.legacy.DivineLegacyStructureStart;
import divinerpg.structure.legacy.DivineStructureComponent;
import divinerpg.structure.template.DivineStructureComponentTemplate;
import divinerpg.structure.template.DivineStructureStart;
import net.minecraft.world.DimensionType;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.DimensionManager;

public class DimensionRegistry {
    public static DimensionType edenDimension;
    public static DimensionType wildwoodDimension;
    public static DimensionType apalachiaDimension;
    public static DimensionType skythernDimension;
    public static DimensionType mortumDimension;
    public static DimensionType iceikaDimension;
    public static DimensionType arcanaDimension;
    public static DimensionType vetheaDimension;
    public static DimensionType galaxyDimension;

    public static void init() {
        registerStructures();
        registerDimensionTypes();
        registerDimensions();
    }

    private static void registerStructures() {
        MapGenStructureIO.registerStructure(DivineStructureStart.class, "DrpgSS");
        MapGenStructureIO.registerStructureComponent(DivineStructureComponentTemplate.class, "DrpgCmp");

        MapGenStructureIO.registerStructure(DivineLegacyStructureStart.class, "DrpgLegacySS");
        MapGenStructureIO.registerStructureComponent(DivineStructureComponent.class, "DrpgLegacyCmp");
    }

    private static void registerDimensionTypes() {
        edenDimension = DimensionType.register(DivineRPG.MODID + ":eden", "_eden", GeneralConfig.dimensionIDs.edenDimensionID,
                WorldProviderEden.class, false);
        wildwoodDimension = DimensionType.register(DivineRPG.MODID + ":wildwood", "_wildwood",
                GeneralConfig.dimensionIDs.wildwoodDimensionID, WorldProviderWildWood.class, false);
        apalachiaDimension = DimensionType.register(DivineRPG.MODID + ":apalachia", "_apalachia",
                GeneralConfig.dimensionIDs.apalachiaDimensionID, WorldProviderApalachia.class, false);
        skythernDimension = DimensionType.register(DivineRPG.MODID + ":skythern", "_skythern",
                GeneralConfig.dimensionIDs.skythernDimensionID, WorldProviderSkythern.class, false);
        mortumDimension = DimensionType.register(DivineRPG.MODID + ":mortum", "_mortum", GeneralConfig.dimensionIDs.mortumDimensionID,
                WorldProviderMortum.class, false);
        iceikaDimension = DimensionType.register(DivineRPG.MODID + ":iceika", "_iceika", GeneralConfig.dimensionIDs.iceikaDimensionID,
                WorldProviderIceika.class, false);
        arcanaDimension = DimensionType.register(DivineRPG.MODID + ":arcana", "_arcana", GeneralConfig.dimensionIDs.arcanaDimensionID,
                WorldProviderArcana.class, false);
        vetheaDimension = DimensionType.register(DivineRPG.MODID + ":vethea", "_vethea", GeneralConfig.dimensionIDs.vetheaDimensionID,
        		VetheaWorldProvider.class, false); //VetheaWorldProvider = new, WorldProviderVethea = old
        galaxyDimension = DimensionType.register(DivineRPG.MODID + ":galaxy", "_galaxy", GeneralConfig.dimensionIDs.galaxyDimensionID,
                GalaxyWorldProvider.class, false);
    }

    private static void registerDimensions() {
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.edenDimensionID, edenDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.wildwoodDimensionID, wildwoodDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.apalachiaDimensionID, apalachiaDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.skythernDimensionID, skythernDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.mortumDimensionID, mortumDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.iceikaDimensionID, iceikaDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.arcanaDimensionID, arcanaDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.vetheaDimensionID, vetheaDimension);
        DimensionManager.registerDimension(GeneralConfig.dimensionIDs.galaxyDimensionID, galaxyDimension);
    }
}