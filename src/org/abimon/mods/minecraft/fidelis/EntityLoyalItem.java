package org.abimon.mods.minecraft.fidelis;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class EntityLoyalItem extends EntityItem {

	public EntityLoyalItem(EntityItem item) {
		super(item.worldObj, item.posX, item.posY, item.posZ, item.getEntityItem().copy());
		this.motionX = item.motionX;
		this.motionY = item.motionY;
		this.motionZ = item.motionZ;
		this.age = item.age;
		this.delayBeforeCanPickup = item.delayBeforeCanPickup;
		System.out.println("Overriding " + item);
	}

    public void setDead()
    {
		super.setDead();
    	ItemStack item = getEntityItem();
    	System.out.println("This thing");
    	System.out.println(this.age);
    	if(item == null ||  item.stackSize <= 0 || !Fidelis.isSoulbound(item));
    	else
    	{
    			for(Object ply : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
    			{
    				EntityPlayer player = (EntityPlayer) ply;
    				if(Fidelis.getID(player).equalsIgnoreCase(Fidelis.getFidelisID(item)))
    				{
    					if(player.inventory.addItemStackToInventory(item))
    						if(item.stackSize <= 0)
    							item = null;
    					if(item != null){
    						this.setEntityItemStack(item);
    						this.setPosition(player.posX, player.posY, player.posZ);
    						Fidelis.setHealth(EntityItem.class, this, 50);
    						this.isDead = false;
    					}
    				}
    			}
    	}
    }
}
