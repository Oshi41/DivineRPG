package divinerpg.registry;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class SpawnRegistry {

    public static void registerSpawn() {
        addOverworldSpawn(EntitiesRegistry.entrhralled_dramcryx, 70, 3, 4);
        addOverworldSpawn(EntitiesRegistry.rotatick, 70, 3, 4);
        addOverworldSpawn(EntitiesRegistry.grue, 30, 1, 4);
        addOverworldSpawn(EntitiesRegistry.cavelops, 70, 1, 4);
        addOverworldSpawn(EntitiesRegistry.ender_spider, 4, 1, 4);
        addOverworldSpawn(EntitiesRegistry.cave_crawler, 70, 2, 3);
        addOverworldSpawn(EntitiesRegistry.miner, 5, 1, 1);
        addOverworldSpawn(EntitiesRegistry.eye, 30, 1, 4);
        addOverworldSpawn(EntitiesRegistry.rainbour, 1, 1, 1);

        addOverworldSpawn(EntitiesRegistry.jack_o_man, 5, 1, 1);

        addMonterInBiomes(EntitiesRegistry.desert_crawler, 50, 1, 4, BiomeDictionary.Type.SANDY);
        addMonterInBiomes(EntitiesRegistry.saguaro_worm, 20, 1, 4, BiomeDictionary.Type.SANDY);
        addMonterInBiomes(EntitiesRegistry.arid_warrior, 35, 1, 4, BiomeDictionary.Type.SANDY);

        addMonterInBiomes(EntitiesRegistry.crab, 100, 4, 4, BiomeDictionary.Type.BEACH);
        addMonterInBiomes(EntitiesRegistry.king_crab, 10, 4, 4, BiomeDictionary.Type.BEACH);

        addMonterInBiomes(EntitiesRegistry.koblin, 5, 1, 1, BiomeDictionary.Type.PLAINS);

        addMonterInBiomes(EntitiesRegistry.jungle_dramcryx, 80, 1, 4, BiomeDictionary.Type.JUNGLE);
        addMonterInBiomes(EntitiesRegistry.jungle_spider, 80, 1, 4, BiomeDictionary.Type.JUNGLE);
        addMonterInBiomes(EntitiesRegistry.jungle_bat, 50, 1, 4, BiomeDictionary.Type.JUNGLE);

        addMonterInBiomes(EntitiesRegistry.cyclops, 10, 2, 4, BiomeDictionary.Type.PLAINS, BiomeDictionary.Type.MOUNTAIN);

        addMonterInBiomes(EntitiesRegistry.frost, 50, 1, 4, BiomeDictionary.Type.SNOWY);
        addMonterInBiomes(EntitiesRegistry.glacon, 30, 1, 4, BiomeDictionary.Type.SNOWY);
        addToSpawn(EntitiesRegistry.glacon, EntityClassification.CREATURE, filter(true, BiomeDictionary.Type.SNOWY),
                30, 1, 4);

        addMonterInBiomes(EntitiesRegistry.hell_spider, 50, 1, 1, BiomeDictionary.Type.NETHER);
        addMonterInBiomes(EntitiesRegistry.scorcher, 7, 4, 4, BiomeDictionary.Type.NETHER);
        addMonterInBiomes(EntitiesRegistry.wildfire, 50, 1, 1, BiomeDictionary.Type.NETHER);
        addMonterInBiomes(EntitiesRegistry.hell_bat, 50, 1, 1, BiomeDictionary.Type.NETHER);
        addInBiomes(EntitiesRegistry.hell_pig, EntityClassification.CREATURE, 25, 5, 50, BiomeDictionary.Type.NETHER);

        addToSpawn(EntitiesRegistry.ender_spider, EntityClassification.MONSTER, filter(true, BiomeDictionary.Type.END),
                2, 1, 4);
        addMonterInBiomes(EntitiesRegistry.ender_watcher, 10, 4, 4, BiomeDictionary.Type.END);
        addMonterInBiomes(EntitiesRegistry.ender_triplets, 1, 1, 4, BiomeDictionary.Type.END);

        addMonterInBiomes(EntitiesRegistry.pumpkin_spider, 20, 1, 1, BiomeDictionary.Type.FOREST);

        /////////
        // Eden
        ////////
        List<Biome.SpawnListEntry> edenMonsters = BiomeRegisty.EDEN.getSpawns(EntityClassification.MONSTER);
        List<Biome.SpawnListEntry> edenCreatures = BiomeRegisty.EDEN.getSpawns(EntityClassification.CREATURE);

        Arrays.asList(new Biome.SpawnListEntry(EntitiesRegistry.eden_tomo, 20, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.eden_cadillion, 20, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.weak_cori, 1, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.bunny, 20, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.sun_archer, 6, 4, 4)
        ).forEach(x -> {
            edenCreatures.add(x);
            edenMonsters.add(x);
        });

        edenMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.greenfeet, 20, 4, 4));
        edenMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.madivel, 20, 4, 4));


        ////////////
        // Wildwood
        ////////////
        List<Biome.SpawnListEntry> wildwoodMonsters = BiomeRegisty.WILDWOOD.getSpawns(EntityClassification.MONSTER);
        List<Biome.SpawnListEntry> wildwoodCreatures = BiomeRegisty.WILDWOOD.getSpawns(EntityClassification.CREATURE);

        Arrays.asList(new Biome.SpawnListEntry(EntitiesRegistry.wildwood_tomo, 4, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.wildwood_cadillion, 4, 4, 4),
                new Biome.SpawnListEntry(EntitiesRegistry.epiphite, 1, 2, 2),
                new Biome.SpawnListEntry(EntitiesRegistry.behemoth, 1, 1, 1)
        ).forEach(x -> {
            wildwoodMonsters.add(x);
            wildwoodCreatures.add(x);
        });

        wildwoodCreatures.add(new Biome.SpawnListEntry(EntitiesRegistry.moon_wolf, 4, 4, 4));

        wildwoodMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.wildwood_golem, 3, 4, 4));
        wildwoodMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.verek, 4, 4, 4));
        wildwoodMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.mage, 2, 4, 4));

        /////////////
        // Apalachia
        /////////////
        List<Biome.SpawnListEntry> apalachiaMonsters = BiomeRegisty.APALACHIA.getSpawns(EntityClassification.MONSTER);

        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.apalachia_tomo, 2, 4, 4));
        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.apalachia_cadillion, 2, 4, 4));
        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.apalachia_golem, 2, 4, 4));
        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.enchanted_archer, 2, 4, 4));
        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.enchanted_warrior, 2, 4, 4));
        apalachiaMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.spellbinder, 1, 4, 4));

        ///////////////
        // Skythern
        ///////////////
        List<Biome.SpawnListEntry> skythernMonsters = BiomeRegisty.SKYTHERN.getSpawns(EntityClassification.MONSTER);

        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.advanced_cori, 1, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.skythern_golem, 4, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.mystic, 2, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.samek, 4, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.skythern_fiend, 4, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.skythern_archer, 4, 4, 4));
        skythernMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.megalith, 1, 4, 4));

        ///////////////
        // Mortum
        //////////////
        List<Biome.SpawnListEntry> mortumMonsters = BiomeRegisty.MORTUM.getSpawns(EntityClassification.MONSTER);

        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.mortum_cadillion, 2, 4, 4));
        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.twilight_archer, 2, 4, 4));
        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.demon_of_darkness, 2, 4, 4));
        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.basilisk, 2, 4, 4));
        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.sorcerer, 1, 4, 4));
        mortumMonsters.add(new Biome.SpawnListEntry(EntitiesRegistry.soul_stealer, 2, 4, 4));
    }

    private static void addOverworldSpawn(EntityType type, int weight, int min, int max) {
        addOverworldSpawn(type, EntityClassification.MONSTER, weight, min, max);
    }

    private static void addOverworldSpawn(EntityType type, EntityClassification entityClass, int weight, int min, int max) {
        EntitySpawnPlacementRegistry.register(type, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::func_223324_d);

        Stream<Biome> vanillaBiomes = filter(false, BiomeDictionary.Type.END, BiomeDictionary.Type.NETHER).filter(x -> x.getRegistryName().getNamespace().equals("minecraft"));

        addToSpawn(type, entityClass, vanillaBiomes, weight, min, max);
    }

    private static void addMonterInBiomes(EntityType type, int weight, int min, int max, BiomeDictionary.Type... types) {
        addInBiomes(type, EntityClassification.MONSTER, weight, min, max, types);
    }

    private static void addInBiomes(EntityType type, EntityClassification classification, int weight, int min, int max, BiomeDictionary.Type... types) {
        EntitySpawnPlacementRegistry.register(type, EntitySpawnPlacementRegistry.PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, MonsterEntity::func_223324_d);

        addToSpawn(type, classification, filter(true, types), weight, min, max);
    }

    private static Stream<Biome> filter(boolean isInclude, BiomeDictionary.Type... types) {
        Stream<Biome> result = ForgeRegistries.BIOMES.getValues().stream();

        if (types != null) {
            result = result.filter(b -> isInclude == Arrays.stream(types).anyMatch(x -> BiomeDictionary.hasType(b, x)));
        }

        return result;
    }


    private static void addToSpawn(EntityType type, EntityClassification clazz, Stream<Biome> biomes, int weight, int min, int max) {
        biomes.forEach(x -> x.getSpawns(clazz).add(new Biome.SpawnListEntry(type, weight, min, max)));
    }
}