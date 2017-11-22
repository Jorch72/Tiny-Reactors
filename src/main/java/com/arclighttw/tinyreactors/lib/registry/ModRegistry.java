package com.arclighttw.tinyreactors.lib.registry;

import java.lang.reflect.Field;
import java.util.Locale;

import com.arclighttw.tinyreactors.lib.items.IItemProvider;
import com.arclighttw.tinyreactors.lib.models.IModelProvider;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class ModRegistry
{
	private final InternalRegistry registry;
	
	public ModRegistry()
	{
		registry = new InternalRegistry();
	}
	
	public void register(Class<?> clazz)
	{
		try
		{
			for(Field field : clazz.getDeclaredFields())
			{
				Object obj = field.get(null);
				String name = field.getName().toLowerCase(Locale.ENGLISH);
				
				if(obj instanceof Block)
					register((Block)obj, name);
				
				if(obj instanceof Item)
					register((Item)obj, name);
				
				if(obj instanceof IRecipe)
					register((IRecipe)obj, name);
				
				if(obj instanceof SoundEvent)
					register((SoundEvent)obj, name);
			}
		}
		catch(IllegalAccessException e)
		{
		}
	}
	
	private void register(Block block, String name)
	{
		registry.registerBlock(block, name);
		
		ItemBlock item;
		
		if(block instanceof IItemProvider)
			item = ((IItemProvider)block).createItemBlock();
		else
			item = new ItemBlock(block);
		
		register(item, name);
		
		if(block instanceof ITileEntityProvider)
			registry.registerTileEntity(((ITileEntityProvider)block).createNewTileEntity(null, -1), name);
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			if(block instanceof IModelProvider)
				registry.registerModel(((IModelProvider)block).createModel(), name, "normal");
		}
	}
	
	private void register(Item item, String name)
	{
		registry.registerItem(item, name);
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
			if(item instanceof IModelProvider)
				registry.registerModel(((IModelProvider)item).createModel(), name, "inventory");
		}
	}
	
	private void register(IRecipe recipe, String name)
	{
		registry.registerRecipe(recipe, name);
	}
	
	private void register(SoundEvent sound, String name)
	{
		registry.registerSound(sound, name);
	}
}
