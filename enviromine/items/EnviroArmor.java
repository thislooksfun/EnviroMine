package enviromine.items;

import enviromine.core.EnviroMine;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

public class EnviroArmor extends ItemArmor //implements ITextureProvider, IArmorTextureProvider
{
    public EnviroArmor(int par1, EnumArmorMaterial par2EnumArmorMaterial, int par3, int par4)
	{
		super(par1, par2EnumArmorMaterial, par3, par4);
	}

	public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer)
    {
	    if(itemID == EnviroMine.camelPack.itemID)
	    {
	    	return "enviroMine:textures/models/armor/camelpack_layer_1.png";
	    } else
	    {
	    	return null;
	    }
    }
}
