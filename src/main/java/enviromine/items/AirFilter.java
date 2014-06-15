package enviromine.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.core.EnviroMine;

public class AirFilter extends Item
{
	public Icon cpIcon;
	
	public AirFilter(int par1)
	{
		super(par1);
		this.setNoRepair();
		this.setTextureName("air_filter");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.cpIcon = par1IconRegister.registerIcon("enviromine:air_filter");
	}
	
	@Override
	/**
	 * Return whether this item is repairable in an anvil.
	 */
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
	{
		return false;
	}
	
	@SideOnly(Side.CLIENT)
	/**
	 * Gets an icon index based on an item's damage value
	 */
	public Icon getIconFromDamage(int par1)
	{
		if(cpIcon != null)
		{
			return this.cpIcon;
		} else
		{
			return super.getIconFromDamage(par1);
		}
	}
}
