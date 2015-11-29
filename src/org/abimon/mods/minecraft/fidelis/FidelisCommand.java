package org.abimon.mods.minecraft.fidelis;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import scala.actors.threadpool.Arrays;

public class FidelisCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "fidelis";
	}

	public List addTabCompletionOptions(ICommandSender sender, String[] params)
	{
		if(params.length == 1)
			if(sender instanceof EntityPlayerMP){
				if(params[0].trim().equals(""))
					return Arrays.asList(new String[]{"list", "reload", "bind", "unbind"});
				else if(params[0].trim().startsWith("l"))
					return Arrays.asList(new String[]{"list"});
				else if(params[0].trim().startsWith("r"))
					return Arrays.asList(new String[]{"reload"});
				else if(params[0].trim().startsWith("b"))
					return Arrays.asList(new String[]{"bind"});
				else if(params[0].trim().startsWith("u"))
					return Arrays.asList(new String[]{"unbind"});
			}
			else
				if(params[0].trim().equals(""))
					return Arrays.asList(new String[]{"list", "reload"});
				else if(params[0].trim().startsWith("l"))
					return Arrays.asList(new String[]{"list"});
				else if(params[0].trim().startsWith("r"))
					return Arrays.asList(new String[]{"reload"});
		return null;
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		if(sender instanceof EntityPlayerMP)
			return "fidelis <list|reload|bind|unbind>";
		return "fidelis <list|reload>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params) {
		if(params.length == 0)
			sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
		else{
			String cmd = params[0];
			if(cmd.equalsIgnoreCase("list")){
				sender.addChatMessage(new ChatComponentText("Fidelis Items: "));
				for(ItemStack item : Fidelis.entries.items)
					sender.addChatMessage(new ChatComponentText(item.stackSize + "x " + item.getDisplayName() + ":" + item.getItemDamage() + " - takes " + Fidelis.entries.getLevel(item) + " levels"));
			}
			else if(cmd.equalsIgnoreCase("reload")){
				Fidelis.reload();
				sender.addChatMessage(new ChatComponentText("Reloaded!"));
			}
			else if(sender instanceof EntityPlayerMP){
				EntityPlayerMP player = (EntityPlayerMP) sender;
				if(player.getHeldItem() == null)
					sender.addChatMessage(new ChatComponentText("Not holding an item!"));
				else{
					if(!player.getHeldItem().hasTagCompound())
						player.getHeldItem().setTagCompound(new NBTTagCompound());
					if(cmd.equalsIgnoreCase("bind"))
						if(Fidelis.isSoulbound(player.getHeldItem()))
							sender.addChatMessage(new ChatComponentText("Item already soulbound!"));
						else{
							Fidelis.setFidelisID(player.getHeldItem(), player);
							sender.addChatMessage(new ChatComponentText("Item is now loyal to you!"));
						}
					else if(cmd.equalsIgnoreCase("unbind"))
						if(!Fidelis.isSoulbound(player.getHeldItem()))
							sender.addChatMessage(new ChatComponentText("Item is not soulbound!"));
						else{
							player.getHeldItem().getTagCompound().removeTag("Fidelis");
							player.getHeldItem().getTagCompound().removeTag("FidelisDisplay");
							sender.addChatMessage(new ChatComponentText("Item is now loyal to nobody!"));
						}
				}
			}
		}
	}

}
