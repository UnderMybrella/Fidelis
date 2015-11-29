package org.abimon.mods.minecraft.fidelis;

import java.util.LinkedList;

import net.minecraft.item.ItemStack;

public class FidelisEntry 
{
	LinkedList<ItemStack> items = new LinkedList<ItemStack>();
	LinkedList<Integer> levels = new LinkedList<Integer>();
	LinkedList<Integer> quantities = new LinkedList<Integer>();
	
	public void add(ItemStack item, int level, int quantity){
		items.add(item);
		levels.add(level);
		quantities.add(quantity);
	}
	
	public void clear(){
		items.clear();
		levels.clear();
		quantities.clear();
	}
	
	public boolean contains(ItemStack item){
		for(ItemStack itemsItem : items)
			if(itemsItem.getItem() == item.getItem())
				if(itemsItem.getItemDamage() == item.getItemDamage())
					return true;
		return false;
	}
	
	public int getLevel(ItemStack item){
		for(int i = 0; i < items.size(); i++)
		{
			ItemStack itemsItem = items.get(i);
			if(itemsItem.getItem() == item.getItem())
				if(itemsItem.getItemDamage() == item.getItemDamage())
					return levels.get(i);
		}
		return -1;
	}
	
	public int getQuantity(ItemStack item){
		for(int i = 0; i < items.size(); i++)
		{
			ItemStack itemsItem = items.get(i);
			if(itemsItem.getItem() == item.getItem())
				if(itemsItem.getItemDamage() == item.getItemDamage())
					return quantities.get(i);
		}
		return -1;
	}
}
