package org.abimon.mods.minecraft.fidelis;

import baubles.api.BaublesApi;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BaubleLoyalty 
{
	public static final String BAUBLES = "BAUBLES_INV";
	
	public static void playerTick(EntityPlayer player){
		
	}

	public static void onDeath(EntityPlayer player) {
		IInventory inv = BaublesApi.getBaubles(player);
		ItemStack[] items = new ItemStack[inv.getSizeInventory()];
		for(int i = 0; i < items.length; i++)
			items[i] = inv.getStackInSlot(i);
		Fidelis.setFidelisNBT(player, BAUBLES, Fidelis.setItems(Fidelis.getSoulboundItems(Fidelis.getID(player), items)));
		for(int i = 0; i < items.length; i++)
			inv.setInventorySlotContents(i, items[i]);
	}

	public static void onJoin(EntityPlayer player) {
		ItemStack[] inv = Fidelis.getItems(Fidelis.getFidelisNBT(player).getCompoundTag(BAUBLES));
		IInventory baublesInv = BaublesApi.getBaubles(player);
		for(int i = 0; i < inv.length; i++)
			if(inv[i] != null)
				if(baublesInv.getStackInSlot(i) == null){
					baublesInv.setInventorySlotContents(i, inv[i].copy());
					inv[i] = null;
				}
		
		for(ItemStack item : inv)
			if(item != null){
				EntityItem eItem = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, item);
				player.worldObj.spawnEntityInWorld(eItem);
				item = null;
			}
		Fidelis.getFidelisNBT(player).removeTag(BAUBLES);
	}
}
