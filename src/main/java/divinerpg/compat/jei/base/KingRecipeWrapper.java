package divinerpg.compat.jei.base;

import divinerpg.api.DivineAPI;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class KingRecipeWrapper implements IRecipeWrapper {
    private List<List<ItemStack>> slotItems = new ArrayList<>();

    public KingRecipeWrapper() {
        Map<EntityEquipmentSlot, List<ItemStack>> map = new LinkedHashMap<>();

        List<EntityEquipmentSlot> slots = Arrays.asList(
                EntityEquipmentSlot.HEAD,
                EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS,
                EntityEquipmentSlot.FEET,
                EntityEquipmentSlot.MAINHAND,
                EntityEquipmentSlot.OFFHAND
        );

        DivineAPI
                .getArmorDescriptionRegistry()
                .getValuesCollection()
                .forEach(x -> {
                    for (EntityEquipmentSlot slot : slots) {
                        List<ItemStack> items = x.getPossibleItems(slot).stream()
                                .sorted(Comparator.comparing(l -> l.getRegistryName().toString()))
                                .map(ItemStack::new)
                                .collect(Collectors.toList());

                        List<ItemStack> stacks = map.computeIfAbsent(slot, s -> new ArrayList<>());
                        stacks.addAll(items);
                    }
                });


        for (EntityEquipmentSlot slot : slots) {
            slotItems.add(map.get(slot));
        }

        // TileEntityKingCompressor.fuelMap.size();
        // slotItems.add(TileEntityKingCompressor.fuelMap.keySet().stream().map(ItemStack::new).collect(Collectors.toList()));
    }


    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, slotItems);
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Items.BOOK));
    }
}
