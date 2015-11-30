package org.abimon.mods.minecraft.fidelis;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraft.entity.Entity;
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
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import tconstruct.library.modifier.IModifyable;
import net.minecraft.command.ServerCommandManager;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemPickupEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = Fidelis.MODID, name = Fidelis.NAME)
public class Fidelis {

	public static final String MODID = "Fidelis";
	public static final String NAME = "Fidelis";

	private static File config;

	public static final String MAIN = "MAIN_INV";
	public static final String ARMOR = "ARMOR_INV";

	public static FidelisEntry entries = new FidelisEntry();

	public static LinkedList<ItemStack> blacklisted = new LinkedList<ItemStack>();

	public static boolean BAUBLES_LOADED = false;
	public static boolean MODIFIERS_LOADED = false;
	public static boolean HIJACK_ITEMS = false;

	@EventHandler
	public void preinit(FMLPreInitializationEvent event){
		config = new File(event.getSuggestedConfigurationFile().getAbsolutePath().replace(MODID, NAME).replace("cfg", "json"));

		reload();

		BAUBLES_LOADED = Loader.isModLoaded("Baubles");

		//System.out.println("Is Baubles Loaded? " + BAUBLES_LOADED);

		MinecraftForge.EVENT_BUS.register(this);
		FMLCommonHandler.instance().bus().register(this);
	}

	@EventHandler
	public void serverStarted(FMLServerStartedEvent event){
		((ServerCommandManager) MinecraftServer.getServer().getCommandManager()).registerCommand(new FidelisCommand());
	}

	public static void reload(){
		if(!config.exists())
		{
			try{
				config.createNewFile();
				PrintStream out = new PrintStream(config);
				writeDefault(out);
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
				if(json.has("fidelis"))
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
				if(json.has("blacklistedItems"))
					for(JsonElement element : json.get("blacklistedItems").getAsJsonArray())
					{
						try{
							JsonObject obj = element.getAsJsonObject();
							String item = obj.has("item") ? obj.get("item").getAsString() : "minecraft:apple";
							int damage = obj.has("damage") ? obj.get("damage").getAsInt() : 0;

							ItemStack itemstack = new ItemStack((Item) ((Block.blockRegistry.containsKey(item) ? Item.getItemFromBlock((Block) Block.blockRegistry.getObject(item)) : Item.itemRegistry.containsKey(item) ? Item.itemRegistry.getObject(item) : Items.apple)), 1, damage);
							blacklisted.add(itemstack);
						}catch(Throwable th){};
					}
				if(json.has("hijackItems"))
					HIJACK_ITEMS = json.get("hijackItems").getAsBoolean();
				if(Loader.isModLoaded("TConstruct"))
					TConstructLoyalty.load(json);
			}
			catch(Throwable th){
				th.printStackTrace();
			}
		}
	}

	public static void writeDefault(PrintStream out){
		out.println("{");
		out.println("\t\"fidelis\":[");
		out.println("\t\t{");
		out.println("\t\t\t\"item\":\"minecraft:nether_star\",");
		out.println("\t\t\t\"damage\":0,");
		out.println("\t\t\t\"quantity\":1,");
		out.println("\t\t\t\"levels\":2");
		out.println("\t\t}");
		out.println("\t],");
		out.println("\t\"blacklistedItems\":[");
		out.println("\t\t{");
		out.println("\t\t\t\"item\":\"minecraft:stick\",");
		out.println("\t\t\t\"damage\":0");
		out.println("\t\t}");
		out.println("\t],");
		out.println("\t\"modifiers\":[");
		out.println("\t\t[");
		out.println("\t\t\t{");
		out.println("\t\t\t\t\"item\":\"minecraft:nether_star\",");
		out.println("\t\t\t\t\"damage\":0");
		out.println("\t\t\t}");
		out.println("\t\t]");
		out.println("\t],");
		out.println("\t\"hijackItems\":true");
		out.println("}");
		out.close();
	}

	@SubscribeEvent
	public void onPickup(EntityItemPickupEvent event){
		ItemStack item = event.item.getEntityItem();
		if(item == null)
			return;
		if(isSoulbound(item)){
			String fidelis = getFidelisID(item);
			String id = getID(event.entityPlayer);
			if(!fidelis.equals("") && !fidelis.equals(id))
			{
				event.setCanceled(true);
				if(event.entityPlayer.ticksExisted % 40 == 0)
					event.entityPlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_RED + "This item is not yours. it belongs to " + getOwner(item)));
				return;
			}
		}
	}

	@SubscribeEvent
	public void anvilUpdate(AnvilUpdateEvent event){
		if(isSoulbound(event.left))
			return;
		if(MODIFIERS_LOADED)
			if(event.left.getItem() instanceof IModifyable)
				return;

		ItemStack copy = event.left.copy();
		copy.stackSize = 1;
		copy.setTagCompound(new NBTTagCompound());
		for(ItemStack blacklist : blacklisted)
		{
			ItemStack blackCopy = blacklist.copy();
			blackCopy.stackSize = 1;
			blackCopy.setTagCompound(new NBTTagCompound());
			if(ItemStack.areItemStacksEqual(blackCopy, copy))
				return;
		}


		ItemStack right = event.right;
		if(entries.contains(right))
		{
			event.cost += entries.getLevel(right);
			event.materialCost = entries.getQuantity(right);
			if(event.materialCost > event.right.stackSize)
			{
				event.setCanceled(true);
				return;
			}
			if(event.output == null)
				event.output = event.left.copy();
			if(!event.output.hasTagCompound())
				event.output.setTagCompound(new NBTTagCompound());
			event.output.getTagCompound().setString("Fidelis", "");
			event.output.getTagCompound().setString("FidelisDisplay", "");
		}
	}

	public static ItemStack[] getItems(NBTTagCompound nbt){
		if(nbt == null)
			return new ItemStack[0];
		int count = nbt.getInteger("ItemCount");
		ItemStack[] items = new ItemStack[count];
		for(int i = 0; i < count; i++)
			items[i] = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Item-" + i));
		return items;
	}

	public static NBTTagCompound setItems(ItemStack[] item){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("ItemCount", item.length);
		for(int i = 0; i < item.length; i++)
			nbt.setTag("Item-" + i, item[i] != null ? item[i].writeToNBT(new NBTTagCompound()) : new NBTTagCompound());
		return nbt;
	}

	public static ItemStack[] getSoulboundItems(String idString, ItemStack[] items){
		ItemStack[] inv = new ItemStack[items.length];
		for(int i = 0; i < items.length; i++)
			if(items[i] == null)
				inv[i] = null;
			else if(!isSoulbound(items[i]))
				inv[i] = null;
			else if(getFidelisID(items[i]).equals(idString))
			{
				inv[i] = items[i].copy();
				items[i] = null;
			}
			else
				inv[i] = null;
		return inv;
	}

	public static void setFidelisNBT(EntityPlayer player, String tag, NBTTagCompound nbt) {
		NBTTagCompound playerNBT = player.getEntityData();
		if(playerNBT != null)
		{	
			NBTTagCompound persistedNBT = playerNBT.getCompoundTag(player.PERSISTED_NBT_TAG);
			if(!playerNBT.hasKey(player.PERSISTED_NBT_TAG))
				player.getEntityData().setTag(player.PERSISTED_NBT_TAG, persistedNBT);
			if(persistedNBT != null)
			{
				if(persistedNBT.hasKey(MODID))
					player.getEntityData().getCompoundTag(player.PERSISTED_NBT_TAG).getCompoundTag(Fidelis.MODID).setTag(tag, nbt);
				else
				{
					player.getEntityData().getCompoundTag(player.PERSISTED_NBT_TAG).setTag(Fidelis.MODID, new NBTTagCompound());
					player.getEntityData().getCompoundTag(player.PERSISTED_NBT_TAG).getCompoundTag(Fidelis.MODID).setTag(tag, nbt);
				}
			}
		}
	}

	public static NBTTagCompound getFidelisNBT(EntityPlayer player){
		NBTTagCompound playerNBT = player.getEntityData();
		if(playerNBT != null)
		{	
			NBTTagCompound persistedNBT = playerNBT.getCompoundTag(player.PERSISTED_NBT_TAG);
			if(persistedNBT != null)
				return persistedNBT.getCompoundTag(Fidelis.MODID);
		}
		return null;
	}

	@SubscribeEvent
	public void onJoin(EntityJoinWorldEvent event){
		//		if(!event.entity.worldObj.isRemote)
		//			return;
		if(event.entity instanceof EntityPlayerMP){
			EntityPlayerMP player = (EntityPlayerMP) event.entity;
			ItemStack[] mainInv = getItems(getFidelisNBT(player).getCompoundTag(MAIN));
			for(int i = 0; i < mainInv.length; i++)
				if(mainInv[i] != null)
					if(player.inventory.mainInventory[i] == null){
						player.inventory.mainInventory[i] = mainInv[i].copy();
						mainInv[i] = null;
					}
			for(ItemStack item : mainInv)
				if(item != null){
					EntityItem eItem = new EntityItem(event.world, player.posX, player.posY, player.posZ, item);
					event.world.spawnEntityInWorld(eItem);
					item = null;
				}
			getFidelisNBT(player).removeTag(MAIN);

			ItemStack[] armorInv = getItems(getFidelisNBT(player).getCompoundTag(ARMOR));
			for(int i = 0; i < armorInv.length; i++)
				if(armorInv[i] != null)
					if(player.inventory.armorInventory[i] == null){
						player.inventory.armorInventory[i] = armorInv[i].copy();
						armorInv[i] = null;
					}

			for(ItemStack item : armorInv)
				if(item != null){
					EntityItem eItem = new EntityItem(event.world, player.posX, player.posY, player.posZ, item);
					event.world.spawnEntityInWorld(eItem);
					item = null;
				}

			getFidelisNBT(player).removeTag(ARMOR);

			if(BAUBLES_LOADED)
				BaubleLoyalty.onJoin(player);
		}
		else if(event.entity.getClass().getName().equalsIgnoreCase(EntityItem.class.getName()) && HIJACK_ITEMS)
		{
			EntityItem item = (EntityItem) event.entity;
			ItemStack stack = item.getEntityItem();
			if(stack == null)
				return;
			if(Fidelis.isSoulbound(stack))
			{
				EntityLoyalItem itm = new EntityLoyalItem(item);
				event.entity.worldObj.spawnEntityInWorld(itm);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDeath(LivingDeathEvent event){
		if(event.entityLiving instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			ItemStack[] mainInv = getSoulboundItems(getID(player), player.inventory.mainInventory);
			ItemStack[] armorInv = getSoulboundItems(getID(player), player.inventory.armorInventory);
			setFidelisNBT(player, MAIN, setItems(mainInv));
			setFidelisNBT(player, ARMOR, setItems(armorInv));
			if(BAUBLES_LOADED)
				BaubleLoyalty.onDeath(player);
		}
	}

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event){
		if(event.entityPlayer != null && event.entityPlayer.getHeldItem() != null)
			if(event.entityPlayer.getHeldItem().hasTagCompound())
				if(event.entityPlayer.getHeldItem().getTagCompound().hasKey("Fidelis"))
				{
					String fidelisID = event.entityPlayer.getHeldItem().getTagCompound().getString("Fidelis");
					if(event.entityPlayer.getGameProfile().getId().toString().equals(fidelisID))
						return;
					else if(fidelisID.equals(""))
					{
						event.entityPlayer.getHeldItem().getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
						event.entityPlayer.getHeldItem().getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
					}
					else
						event.setCanceled(true);
				}
	}

	public static String getOwner(ItemStack item){
		if(item.hasTagCompound())
			if(item.getTagCompound().hasKey("Fidelis"))
				return item.getTagCompound().getString("Fidelis");
		return "";
	}
	public static boolean isSoulbound(ItemStack item){
		if(item.hasTagCompound())
			if(item.getTagCompound().hasKey("Fidelis"))
				return true;
		return false;
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void info(ItemTooltipEvent event){
		if(event.itemStack.hasTagCompound()){
			if(event.itemStack.getTagCompound().hasKey("Fidelis"))
			{
				String fidelisID = getFidelisID(event.itemStack);
				if(getID(event.entityPlayer).equals(fidelisID))
					event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to you");
				else if(!fidelisID.equals(""))
					event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Loyal to someone else (" + event.itemStack.getTagCompound().getString("FidelisDisplay") + ")");
				else
					event.toolTip.add(EnumChatFormatting.DARK_GRAY + "Waiting to bind to a loyal soul");

				if(event.itemStack.getItem().hasCustomEntity(event.itemStack) && HIJACK_ITEMS){
					event.toolTip.add(EnumChatFormatting.RED + "WARNING: Item may not return if 'killed' (Dropped in Lava, onto cactus)");
					event.toolTip.add(EnumChatFormatting.RED + "The item will still return if you die with it, however");
				}
			}
		}
	}

	@SubscribeEvent
	public void tick(LivingEvent.LivingUpdateEvent event)
	{
		if(event.entity instanceof EntityPlayer){
			EntityPlayer player = (EntityPlayer) event.entity;
			for(int i = 0; i < player.inventory.armorInventory.length; i++)
			{
				ItemStack item = player.inventory.armorInventory[i];
				if(item == null)
					continue;
				if(isSoulbound(item))
					if(getOwner(item).equals("")){
						item = setFidelisID(item, player);
						player.inventory.armorInventory[i] = item;
					}
					else if(!getID(player).equals(getFidelisID(item)))
					{
						player.inventory.armorInventory[i] = null;
						if(!event.entity.worldObj.isRemote){
							EntityItem entity = new EntityItem(player.worldObj, player.posX, player.posY, player.posZ, item);
							entity.delayBeforeCanPickup = 40;
							player.worldObj.spawnEntityInWorld(entity);
						}
					}
			}

			for(int i = 0; i < player.inventory.mainInventory.length; i++)
			{
				ItemStack item = player.inventory.mainInventory[i];
				if(item == null)
					continue;
				if(isSoulbound(item))
					if(getOwner(item).equals("")){
						item = setFidelisID(item, player);
						player.inventory.mainInventory[i] = item;
					}
			}
		}
	}

	@SubscribeEvent
	public void itemExpire(ItemExpireEvent event){
		//TODO: Add items to inventory upon expire
		ItemStack item = event.entityItem.getEntityItem();
		if(item == null)
			return;
		if(isSoulbound(item)){
			for(Object ply : MinecraftServer.getServer().getConfigurationManager().playerEntityList)
			{
				EntityPlayer player = (EntityPlayer) ply;
				if(getID(player).equalsIgnoreCase(getFidelisID(item)))
				{
					if(player.inventory.addItemStackToInventory(item))
						if(item.stackSize <= 0)
							item = null;
				}
			}
		}

		if(item != null)
			event.setCanceled(true);

	}

	public static void setHealth(Entity entity, int health){
		try{
			Field field = entity.getClass().getDeclaredField("health");
			field.setAccessible(true);
			field.setInt(entity, health);
		}
		catch(Throwable th){
			System.err.println("Error setting " + entity + "'s health to " + health + ": " + th);
		}
	}

	public static void setHealth(Class<?> clazz, Entity entity, int health){
		try{
			Field field = clazz.getDeclaredField("health");
			field.setAccessible(true);
			field.setInt(entity, health);
		}
		catch(Throwable th){
			System.err.println("Error setting " + entity + "'s health to " + health + ": " + th);
		}
	}

	public static String getID(EntityPlayer player){
		if(player == null)
			return "";
		if(MinecraftServer.getServer().isServerInOnlineMode())
			return player.getGameProfile().getId().toString();
		else
			return player.getDisplayName();
	}

	public static String getFidelisID(ItemStack item){
		if(MinecraftServer.getServer().isServerInOnlineMode())
			return item.getTagCompound().getString("Fidelis");
		else
			return item.getTagCompound().getString("FidelisDisplay");
	}

	public static ItemStack setFidelisID(ItemStack item, EntityPlayer player){
		if(MinecraftServer.getServer().isServerInOnlineMode())
			item.getTagCompound().setString("Fidelis", player.getGameProfile().getId().toString());
		item.getTagCompound().setString("FidelisDisplay", player.getDisplayName());
		return item;
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
					event.item.getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
					event.item.getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
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
					event.item.getTagCompound().setString("Fidelis", event.entityPlayer.getGameProfile().getId().toString());
					event.item.getTagCompound().setString("FidelisDisplay", event.entityPlayer.getDisplayName());
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
				}
				else{
					event.setCanceled(true);
					event.useItem = Result.DENY;
				}
			}
	}

}
