package org.abimon.mods.minecraft.fidelis;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;

import com.apple.concurrent.Dispatch.Priority;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Fidelis.MODID, name = Fidelis.NAME)
public class Fidelis {

	public static final String MODID = "um_fidelis";
	public static final String NAME = "Fidelis";

	private static File config;

	public FidelisEntry entries = new FidelisEntry();

	public static HashMap<String, ItemStack[]> MAIN = new HashMap<String, ItemStack[]>();
	public static HashMap<String, ItemStack[]> BAUBLES = new HashMap<String, ItemStack[]>();
	public static HashMap<String, ItemStack[]> ARMOR = new HashMap<String, ItemStack[]>();

	@EventHandler
	public void preinit(FMLPreInitializationEvent event){
		config = new File(event.getSuggestedConfigurationFile().getAbsolutePath().replace(MODID, NAME).replace("cfg", "json"));
		if(!config.exists())
		{
			try{
				config.createNewFile();
				PrintStream out = new PrintStream(config);
				out.println("{");
				out.println("\t\"fidelis:\":[");
				out.println("\t\t{");
				out.println("\t\t\t\"item\":\"minecraft:nether_star\",");
				out.println("\t\t\t\"damage\":0,");
				out.println("\t\t\t\"quantity\":1,");
				out.println("\t\t\t\"levels\":2");
				out.println("\t\t}");
				out.println("\t]");
				out.println("}");
				out.close();
			}
			catch(Throwable th){}
		}
		MinecraftForge.EVENT_BUS.register(this);
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		if(!config.exists())
		{
			try{
				config.createNewFile();
				PrintStream out = new PrintStream(config);
				out.println("{");
				out.println("\t\"fidelis\":[");
				out.println("\t\t{");
				out.println("\t\t\t\"item\":\"minecraft:nether_star\",");
				out.println("\t\t\t\"damage\":0,");
				out.println("\t\t\t\"quantity\":1,");
				out.println("\t\t\t\"levels\":2");
				out.println("\t\t}");
				out.println("\t]");
				out.println("}");
				out.close();
			}
			catch(Throwable th){}
		}

		if(config.exists()){
			try{
				FileInputStream in = new FileInputStream(config);
				byte[] data = new byte[in.available()];
				in.read(data);
				in.close();

				String s = new String(data);
				JsonObject json = (JsonObject) new JsonParser().parse(s);
				for(JsonElement element : json.get("fidelis").getAsJsonArray())
				{
					try{
						JsonObject obj = element.getAsJsonObject();
						String item = obj.has("item") ? obj.get("item").getAsString() : "minecraft:apple";
						int damage = obj.has("damage") ? obj.get("damage").getAsInt() : 0;
						int quantity = obj.has("quantity") ? obj.get("quantity").getAsInt() : 1;
						int levels = obj.has("levels") ? obj.get("levels").getAsInt() : 0;

						ItemStack itemstack = new ItemStack((Item) ((Block.blockRegistry.containsKey(item) ? Item.getItemFromBlock((Block) Block.blockRegistry.getObject(item)) : Item.itemRegistry.containsKey(item) ? Item.itemRegistry.getObject(item) : Items.apple)), 1, damage);
						entries.add(itemstack, levels, quantity);
					}catch(Throwable th){};
				}
			}
			catch(Throwable th){
				th.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void anvilUpdate(AnvilUpdateEvent event){
		ItemStack right = event.right;
		if(entries.contains(right))
		{
			event.cost += entries.getLevel(right);
			event.materialCost = entries.getQuantity(right);
			if(event.output == null)
				event.output = event.left.copy();
			if(!event.output.hasTagCompound())
				event.output.setTagCompound(new NBTTagCompound());
			if(!event.output.getTagCompound().hasKey("Fidelis")){
				event.output.getTagCompound().setString("Fidelis", "");
				event.output.getTagCompound().setString("FidelisDisplay", "");
			}
		}
	}
	
	@SubscribeEvent
	public void onJoin(EntityJoinWorldEvent event){
		if(event.entity instanceof EntityPlayerMP){
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			if(MinecraftServer.getServer().isServerInOnlineMode()){
				ItemStack[] inv = MAIN.get(player.getGameProfile().getId().toString());
				if(inv == null)
					return;
				for(int i = 0; i < inv.length; i++)
					if(inv[i] != null)
						if(player.inventory.getStackInSlot(i) == null){
							player.inventory.setInventorySlotContents(i, inv[i].copy());
							inv[i] = null;
						}
				for(ItemStack item : inv)
					if(item != null){
						EntityItem eItem = new EntityItem(event.world, player.posX, player.posY, player.posZ, item);
						event.world.spawnEntityInWorld(eItem);
						item = null;
					}
				MAIN.put(player.getGameProfile().getId().toString(), null);
			}else{
				ItemStack[] inv = MAIN.get(player.getDisplayName());				
				if(inv == null)
					return;
				for(int i = 0; i < inv.length; i++)
					if(inv[i] != null)
						if(player.inventory.getStackInSlot(i) == null){
							player.inventory.setInventorySlotContents(i, inv[i].copy());
							inv[i] = null;
						}
				for(ItemStack item : inv)
					if(item != null){
						EntityItem eItem = new EntityItem(event.world, player.posX, player.posY, player.posZ, item);
						event.world.spawnEntityInWorld(eItem);
					}
				MAIN.put(player.getDisplayName(), null);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDeath(LivingDeathEvent event){
		if(event.entityLiving instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			ItemStack[] inv = new ItemStack[player.inventory.getSizeInventory()];
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack item = player.inventory.getStackInSlot(i);
				if(item == null)
					continue;
				if(item.hasTagCompound())
					if(item.getTagCompound().hasKey("Fidelus"))
						if(MinecraftServer.getServer().isServerInOnlineMode()){
							if(player.getGameProfile().getId().toString().equals(item.getTagCompound().getString("Fidelus")))
							{
								inv[i] = item.copy();
								player.inventory.setInventorySlotContents(i, null);
							}
						}else{
							if(player.getDisplayName().equals(item.getTagCompound().getString("FidelisDisplay")))
							{
								inv[i] = item.copy();
								player.inventory.setInventorySlotContents(i, null);
							}
						}

			}

			if(MinecraftServer.getServer().isServerInOnlineMode()){
				MAIN.put(player.getGameProfile().getId().toString(), inv);
			}else{
				MAIN.put(player.getDisplayName(), inv);
			}
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void info(ItemTooltipEvent event){
		if(event.itemStack.hasTagCompound())
			if(event.itemStack.getTagCompound().hasKey("Fidelis"))
			{
				String fidelisID = event.itemStack.getTagCompound().getString("Fidelis");
				if(MinecraftServer.getServer().isServerInOnlineMode()){
					if(event.entityPlayer.getGameProfile().getId().toString().equals(fidelisID))
						event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to you");
					else
						event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to someone else (" + event.itemStack.getTagCompound().getString("FidelisDisplay") + ")");
				}else{
					if(event.entityPlayer.getDisplayName().equals(event.itemStack.getTagCompound().getString("FidelisDisplay")))
						event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to you");
					else
						event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to someone else (" + event.itemStack.getTagCompound().getString("FidelisDisplay") + ")");
				}
			}
	}

	@SubscribeEvent
	public void use(PlayerUseItemEvent.Start event){
		if(event.item.hasTagCompound())
			if(event.item.getTagCompound().hasKey("Fidelis"))
			{
				String fidelisID = event.item.getTagCompound().getString("Fidelis");
				if(event.entityPlayer.getGameProfile().getId().toString().equals(fidelisID))
					return;
				else if(fidelisID.equals(""))
				{
					System.err.println("SETTING FIDELIS ID");
					event.item.getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
					event.item.getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
					event.setCanceled(true);
					event.duration = -1;
				}
				else{
					event.setCanceled(true);
					event.duration = -1;
				}
			}
	}

	@SubscribeEvent
	public void use(PlayerUseItemEvent.Tick event){
		if(event.item.hasTagCompound())
			if(event.item.getTagCompound().hasKey("Fidelis"))
			{
				String fidelisID = event.item.getTagCompound().getString("Fidelis");
				if(event.entityPlayer.getGameProfile().getId().toString().equals(fidelisID))
					return;
				else if(fidelisID.equals(""))
				{
					System.err.println("SETTING FIDELIS ID 2.0");
					event.item.getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
					event.item.getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
					event.setCanceled(true);
				}
				else
					event.setCanceled(true);
			}
	}

	@SubscribeEvent
	public void use(PlayerInteractEvent event){
		ItemStack item = event.entityPlayer.getEquipmentInSlot(0);
		if(item == null)
			return;
		if(item.hasTagCompound())
			if(item.getTagCompound().hasKey("Fidelis"))
			{
				String fidelisID = item.getTagCompound().getString("Fidelis");
				if(event.entityPlayer.getGameProfile().getId().toString().equals(fidelisID))
					return;
				else if(fidelisID.equals(""))
				{
					item.getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
					item.getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
					event.setCanceled(true);
					event.useItem = Result.DENY;
				}
				else{
					event.setCanceled(true);
					event.useItem = Result.DENY;
				}
			}
	}

}
