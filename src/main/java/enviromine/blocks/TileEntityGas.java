package enviromine.blocks;

import java.util.ArrayList;
import enviromine.gases.EnviroGasDictionary;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityGas extends TileEntity
{
	public ArrayList<Integer> gases;
	
	public TileEntityGas()
	{
		gases = new ArrayList<Integer>();
		this.gases.add(EnviroGasDictionary.gasFire.gasID);
	}
	
	public void doAllEffects(EntityLivingBase entityLiving)
	{
		if(gases.size() <= 0)
		{
			return;
		}
		
		for(int i = 0; i < gases.size(); i++)
		{
			EnviroGasDictionary.gasList[i].applyEffects(entityLiving);
		}
	}
	
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
	}
	
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
	}
}
