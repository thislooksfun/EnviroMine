package enviromine.blocks;

import java.util.ArrayList;
import java.util.Iterator;
import enviromine.gases.EnviroGasDictionary;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityGas extends TileEntity
{
	public ArrayList<Integer> gases = new ArrayList<Integer>();
	
	public void doAllEffects(EntityLivingBase entityLiving)
	{
		Iterator<Integer> iterator = gases.iterator();
		
		while(iterator.hasNext())
		{
			int gasID = iterator.next();
			EnviroGasDictionary.gasList[gasID].applyEffects(entityLiving);
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
