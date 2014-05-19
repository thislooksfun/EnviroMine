package enviromine.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.Item;

public class ItemAirFilter extends Item
{
	public ItemAirFilter(int par1)
	{
		super(par1);
		this.setTextureName("airFilter");
	}

	public void registerIcons(IconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(this.getIconString());
	}
}
