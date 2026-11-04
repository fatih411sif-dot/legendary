package com.legendaryblades.item;

import com.legendaryblades.LegendaryBlades;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;

import java.util.function.Function;

public class ModItems {

    // Tambah senjata legendaris baru di sini (satu baris per senjata).
    public static final Item YAGURA_NO_TACHI = register("yagura_no_tachi", props ->
            new YaguraNoTachiItem(props
                    .rarity(Rarity.EPIC)
                    .sword(ToolMaterial.NETHERITE, 3.0f, -2.2f)));

    private static Item register(String name, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(LegendaryBlades.MOD_ID, name));
        Item item = factory.apply(new Item.Properties().setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.accept(YAGURA_NO_TACHI));
    }
}
