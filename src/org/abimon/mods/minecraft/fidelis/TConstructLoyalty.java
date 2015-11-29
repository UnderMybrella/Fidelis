package org.abimon.mods.minecraft.fidelis;

import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import tconstruct.library.crafting.ModifyBuilder;

public class TConstructLoyalty {

	public static void load(JsonObject json) {					
		if(json.has("modifiers"))
			for(JsonElement element : json.get("modifiers").getAsJsonArray())
			{
				try{
					JsonArray array = element.getAsJsonArray();
					LinkedList<ItemStack> items = new LinkedList<ItemStack>();
					for(JsonElement elem : array){ 
						JsonObject obj = elem.getAsJsonObject();
						String item = obj.has("item") ? obj.get("item").getAsString() : "minecraft:apple";
						int damage = obj.has("damage") ? obj.get("damage").getAsInt() : 0;

						ItemStack itemstack = new ItemStack((Item) ((Block.blockRegistry.containsKey(item) ? Item.getItemFromBlock((Block) Block.blockRegistry.getObject(item)) : Item.itemRegistry.containsKey(item) ? Item.itemRegistry.getObject(item) : Items.apple)), 1, damage);
						items.add(itemstack);
					}
					ModifyBuilder.registerModifier(new FidelisModifier(items.toArray(new ItemStack[0])));
					Fidelis.MODIFIERS_LOADED = true;

				}catch(Throwable th){};
			}

	}

}
