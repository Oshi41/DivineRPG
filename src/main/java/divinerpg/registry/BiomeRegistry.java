package divinerpg.registry;

import divinerpg.DivineRPG;
import divinerpg.dimensions.apalachia.BiomeApalachia;
import divinerpg.dimensions.arcana.BiomeArcana;
import divinerpg.dimensions.eden.BiomeEden;
import divinerpg.dimensions.galaxy.GalaxyBiome;
import divinerpg.dimensions.galaxy.GalaxyWorldProvider;
import divinerpg.dimensions.iceika.BiomeIceika;
import divinerpg.dimensions.mortum.BiomeMortum;
import divinerpg.dimensions.skythern.BiomeSkythern;
import divinerpg.dimensions.vethea.BiomeVethea;
import divinerpg.dimensions.wildwood.BiomeWildWood;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = DivineRPG.MODID)
@ObjectHolder(Reference.MODID)
public class BiomeRegistry {

    @ObjectHolder("eden")
    public static final Biome biomeEden = new BiomeEden();
    @ObjectHolder("wildwood")
    public static final Biome biomeWildwood = new BiomeWildWood();
    @ObjectHolder("apalachia")
    public static final Biome biomeApalachia = new BiomeApalachia();
    @ObjectHolder("skythern")
    public static final Biome biomeSkythern = new BiomeSkythern();
    @ObjectHolder("mortum")
    public static final Biome biomeMortum = new BiomeMortum();
    @ObjectHolder("iceika")
    public static final Biome biomeIceika = new BiomeIceika();
    @ObjectHolder("arcana")
    public static final Biome biomeArcana = new BiomeArcana();
    @ObjectHolder("vethea")
    public static final Biome biomeVethea = new BiomeVethea();
    @ObjectHolder("galaxy")
    public static Biome biomeGalaxy = new GalaxyBiome();

    @SubscribeEvent
    public static void registerBiomes(RegistryEvent.Register<Biome> event) {
        IForgeRegistry<Biome> registry = event.getRegistry();

        registry.registerAll(
                biomeEden,
                biomeWildwood,
                biomeApalachia,
                biomeSkythern,
                biomeMortum,
                biomeIceika,
                biomeArcana,
                biomeVethea,
                biomeGalaxy
        );

        addBiomeTypes();
    }

    public static void addBiomeTypes() {
        BiomeDictionary.addTypes(biomeEden, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeWildwood, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeApalachia, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeSkythern, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeMortum, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeIceika, Type.MAGICAL, Type.SNOWY);
        BiomeDictionary.addTypes(biomeArcana, Type.MAGICAL);
        BiomeDictionary.addTypes(biomeVethea, Type.MAGICAL, Type.SPOOKY);
        BiomeDictionary.addTypes(biomeGalaxy, Type.MAGICAL);
    }
}