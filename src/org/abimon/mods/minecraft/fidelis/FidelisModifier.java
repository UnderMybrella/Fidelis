package org.abimon.mods.minecraft.fidelis;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import scala.actors.threadpool.Arrays;
import tconstruct.modifiers.tools.ModBoolean;

public class FidelisModifier extends ModBoolean {

	public FidelisModifier(ItemStack[] items) {
		super(items, 1, "Fidelis", EnumChatFormatting.DARK_GRAY.toString(), "Soulbound");
	}
	
	public void modify(ItemStack[] input, ItemStack tool){
		super.modify(input, tool);
		tool.getTagCompound().setString("Fidelis", "");	
		tool.getTagCompound().setString("FidelisDisplay", "");
	}

}
