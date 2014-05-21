package enviromine.blocks;

import java.util.ArrayList;
import enviromine.gases.EnviroGas;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityGas extends TileEntity
{
	public ArrayList<Integer> gases = new ArrayList<Integer>();
	
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
	}
	
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
	}
}
