package com.arclighttw.tinyreactors.blocks;

import java.util.List;
import java.util.Random;

import com.arclighttw.tinyreactors.inits.Registry.IItemProvider;
import com.arclighttw.tinyreactors.inits.Registry.IModelRegistrar;
import com.arclighttw.tinyreactors.items.ItemReactorVent;
import com.arclighttw.tinyreactors.main.Reference;
import com.arclighttw.tinyreactors.properties.EnumVentState;
import com.arclighttw.tinyreactors.properties.EnumVentTier;
import com.arclighttw.tinyreactors.tiles.TileEntityReactorVent;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockReactorVent extends BlockReactorComponent implements IItemProvider, IModelRegistrar
{
	public BlockReactorVent()
	{
		super(Material.IRON, "reactor_vent");
		setDefaultState(blockState.getBaseState().withProperty(EnumVentTier.PROPERTY, EnumVentTier.IRON).withProperty(EnumVentState.PROPERTY, EnumVentState.CLOSED));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityReactorVent();
	}
	
	@Override
	public ItemBlock getItemBlock()
	{
		return new ItemReactorVent(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels()
	{
		for(EnumVentTier tier : EnumVentTier.values())
		{
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), tier.ordinal(), new ModelResourceLocation(new ResourceLocation(Reference.ID, "reactor_vent_" + tier.getName() + "_closed"), "inventory"));
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 3 + tier.ordinal(), new ModelResourceLocation(new ResourceLocation(Reference.ID, "reactor_vent_" + tier.getName() + "_open"), "inventory"));
		}
	}
	
	@Override
	public int damageDropped(IBlockState state)
	{
		return getMetaFromState(state) % 3;
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(EnumVentTier.PROPERTY, EnumVentTier.values()[meta % 3]).withProperty(EnumVentState.PROPERTY, meta > 2 ? EnumVentState.OPEN : EnumVentState.CLOSED);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		int meta = state.getValue(EnumVentTier.PROPERTY).ordinal();
		meta += state.getValue(EnumVentState.PROPERTY) == EnumVentState.OPEN ? 3 : 0;
		
		return meta;
	}

	@Override
	public BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] { EnumVentTier.PROPERTY, EnumVentState.PROPERTY });
    	}
	
	@Override
	public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items)
	{
		for(int i = 0; i < EnumVentTier.values().length; i++)
			items.add(new ItemStack(this, 1, i));
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(!player.getHeldItem(hand).isEmpty())
			return false;
		
		if(!world.isRemote && player.isSneaking())
		{
			TileEntity tile = world.getTileEntity(pos);
			
			if(tile == null || !(tile instanceof TileEntityReactorVent))
				return false;
			
			EnumVentState ventState = state.getValue(EnumVentState.PROPERTY);
			world.setBlockState(pos, state.withProperty(EnumVentState.PROPERTY, ventState == EnumVentState.OPEN ? EnumVentState.CLOSED : EnumVentState.OPEN));
		}
		
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile == null || !(tile instanceof TileEntityReactorVent))
			return;
		
		TileEntityReactorVent vent = (TileEntityReactorVent)tile;
		
		if(!vent.isRunning())
			return;
		
		for(int i = 0; i < vent.getTier().getParticleCount(); i++)
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, true, pos.getX() + 0.1F + (rand.nextFloat() * 0.8F), pos.getY() + 1F, pos.getZ() + 0.1F + (rand.nextFloat() * 0.8F), 0F, 0.05F, 0F);
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced)
	{
		tooltip.add(String.format("Operational Heat Offset: %.2f C", (getStateFromMeta(stack.getItemDamage()).getValue(EnumVentTier.PROPERTY).getHeatOffset() * 10 / 500)));
	}
}
