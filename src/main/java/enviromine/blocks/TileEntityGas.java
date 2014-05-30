package enviromine.blocks;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import cpw.mods.fml.common.network.PacketDispatcher;
import enviromine.EnviroUtils;
import enviromine.gases.EnviroGas;
import enviromine.gases.EnviroGasDictionary;
import enviromine.handlers.ObjectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class TileEntityGas extends TileEntity
{
	public ArrayList<int[]> gases = new ArrayList<int[]>();;
	Color color = Color.WHITE;
	float opacity = 1.0F;
	float yMax = 1.0F;
	float yMin = 0.0F;
	int amount = 0;
	
	public TileEntityGas()
	{
		//this.addGas(1, 9);
	}
	
	public TileEntityGas(World world)
	{
		this.worldObj = world;
		//this.addGas(1, 9);
	}
	
	public void doAllEffects(EntityLivingBase entityLiving)
	{
		if(gases.size() <= 0)
		{
			return;
		}
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			EnviroGasDictionary.gasList[gasArray[0]].applyEffects(entityLiving, gasArray[1]);
		}
	}

	public void updateColor()
	{
		if(gases.size() <= 0)
		{
			this.color = Color.WHITE;
			return;
		}
		
		Color fCol = null;
		
		for(int i = 0; i < gases.size(); i++)
		{
			if(fCol == null)
			{
				fCol = EnviroGasDictionary.gasList[gases.get(i)[0]].color;
			} else
			{
				int[] gasArray = gases.get(i);
				EnviroGas gas = EnviroGasDictionary.gasList[gasArray[0]];
				float opacity =  gas.getOpacity()*gasArray[1];
				opacity = opacity >= 1.0F? 1.0F : opacity;
				fCol = EnviroUtils.blendColors(fCol.getRGB(), gas.color.getRGB(), opacity / 0.5F);
			}
		}
		
		this.color = fCol;
	}

	public void updateOpacity()
	{
		if(gases.size() <= 0)
		{
			this.opacity = 0F;
			return;
		}
		
		float alpha = 0F;
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			alpha += EnviroGasDictionary.gasList[gasArray[0]].getOpacity()*gasArray[1];
		}
		
		if(alpha >= 1F)
		{
			this.opacity = 1F;
		} else
		{
			this.opacity = alpha;
		}
	}
	
	public void updateSize()
	{
		if(this.amount >= 10)
		{
			yMax = 1.0F;
			yMin = 0.0F;
			return;
		} else if(this.amount <= 0)
		{
			yMax = 0.0F;
			yMin = 0.0F;
			return;
		}
		
		boolean lightGas = false;
		boolean heavyGas = false;
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			float density = EnviroGasDictionary.gasList[gasArray[0]].density;
			
			if(density >= 1F)
			{
				heavyGas = true;
			} else if(density <= -1F)
			{
				lightGas = true;
			}
			
			if(lightGas && heavyGas)
			{
				yMax = 1.0F;
				yMin = 0.0F;
				return;
			}
		}
		
		if(this.amount >= 10)
		{
			yMax = 1.0F;
			yMin = 0.0F;
			return;
		} else if(lightGas)
		{
			yMax = 1.0F;
			yMin = 1.0F - (this.amount/10F);
		} else if(heavyGas)
		{
			yMax = this.amount/10F;
			yMin = 0.0F;
		} else
		{
			yMax = 0.5F + (this.amount/20F);
			yMin = 0.5F - (this.amount/20F);
		}
	}
	
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);
		
		int[] savedGases = par1NBTTagCompound.getIntArray("GasArray");
		
		if(savedGases.length > 0)
		{
			gases = new ArrayList<int[]>();
		}
		
		for(int i = 0; i < savedGases.length; i++)
		{
			this.addGas(savedGases[i], 1);
		}
	}
	
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);
		
		this.updateAmount();
		
		int[] savedGases = new int[this.amount];
		int index = 0;
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			
			for(int j = 0; j < gasArray[1]; j++)
			{
				savedGases[index] = gasArray[0];
				index++;
			}
		}
		
		par1NBTTagCompound.setIntArray("GasArray", savedGases);
	}
	
	public void updateAmount()
	{
		this.amount = getGasQuantity(-1);
	}
	
	public int getGasQuantity(int id)
	{
		int total = 0;
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			if(gasArray[0] == id || id <= -1)
			{
				total += gasArray[1];
			}
		}
		
		return total;
	}
	
	public void updateRender()
	{
		if(this.worldObj == null)
		{
			return;
		}
		if(!this.worldObj.isRemote)
		{
			Packet packet = this.getDescriptionPacket();
			PacketDispatcher.sendPacketToAllAround(this.xCoord, this.yCoord, this.zCoord, 128, this.worldObj.provider.dimensionId, packet);
		} else
		{
			Minecraft.getMinecraft().renderGlobal.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}
	
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.writeToNBT(nbttagcompound);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbttagcompound);
    }
	
	public void addGas(int id, int amount)
	{
		if(amount <= 0)
		{
			return;
		}
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			if(gasArray[0] == id)
			{
				gases.set(i, new int[]{id, gasArray[1] + amount});
				this.updateAmount();
				this.updateColor();
				this.updateOpacity();
				this.updateSize();
				this.updateRender();
				this.sortGasesByDensity();
				return;
			}
		}
		gases.add(new int[]{id, amount});
		this.updateAmount();
		this.updateColor();
		this.updateOpacity();
		this.updateSize();
		this.updateRender();
		this.sortGasesByDensity();
	}

	public void subtractGas(int id, int amount)
	{
		if(amount <= 0)
		{
			return;
		}
		
		for(int i = 0; i < gases.size(); i++)
		{
			int[] gasArray = gases.get(i);
			if(gasArray[0] == id)
			{
				if(gasArray[1] <= amount)
				{
					gases.remove(i);
					break;
				} else
				{
					gases.set(i, new int[]{i, gasArray[1] - amount});
					break;
				}
			}
		}
		
		this.updateAmount();
		this.updateColor();
		this.updateOpacity();
		this.updateSize();
		this.updateRender();
	}
	
	public void burnGases()
	{
		boolean burnt = false;
		
		for(int i = 0; i < gases.size(); i ++)
		{
			int[] gasArray = gases.get(i);
			float vol = EnviroGasDictionary.gasList[gasArray[0]].volitility;
			if(vol > 0)
			{
				gases.set(i, new int[]{EnviroGasDictionary.gasFire.gasID, (int)(gasArray[1] * vol)});
				burnt = true;
			}
		}
		
		if(burnt)
		{
			this.updateAmount();
			this.updateColor();
			this.updateOpacity();
			this.updateSize();
			this.updateRender();
			this.sortGasesByDensity();
		}
	}
	
	public void sortGasesByDensity()
	{
		if(gases.size() <= 1)
		{
			return;
		}
		
		for(int i = 1; i < gases.size(); i++)
		{
			EnviroGas gasA = EnviroGasDictionary.gasList[gases.get(i)[0]];
			EnviroGas gasB = EnviroGasDictionary.gasList[gases.get(i-1)[0]];
			if(gasA.density < gasB.density)
			{
				for(int j = i - 1; j >= 0; j--)
				{
					EnviroGas gasC = EnviroGasDictionary.gasList[gases.get(j)[0]];
					EnviroGas gasD = EnviroGasDictionary.gasList[gases.get(j-1)[0]];
					if(j == 0 || (gasA.density < gasC.density && gasA.density >= gasD.density))
					{
						int[] tmpGas = gases.get(i);
						gases.remove(i);
						gases.add(j, tmpGas);
						break;
					}
				}
			}
		}
	}
	
	public boolean spreadGas()
	{
		if(this.gases.size() <= 0)
		{
			return false;
		}
		
		boolean changed = false;
		
		if(this.worldObj.getBlockId(this.xCoord - 1, this.yCoord, this.zCoord) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord - 1, this.yCoord, this.zCoord) == 0)
		{
			if(this.offLoadGas(this.xCoord - 1, this.yCoord, this.zCoord))
			{
				changed = true;
			}
		}

		if(this.worldObj.getBlockId(this.xCoord + 1, this.yCoord, this.zCoord) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord + 1, this.yCoord, this.zCoord) == 0)
		{
			if(this.offLoadGas(this.xCoord + 1, this.yCoord, this.zCoord))
			{
				changed = true;
			}
		}

		if(this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord - 1) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord - 1) == 0)
		{
			if(this.offLoadGas(this.xCoord, this.yCoord, this.zCoord - 1))
			{
				changed = true;
			}
		}

		if(this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord + 1) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord + 1) == 0)
		{
			if(this.offLoadGas(this.xCoord, this.yCoord, this.zCoord + 1))
			{
				changed = true;
			}
		}

		if(this.worldObj.getBlockId(this.xCoord, this.yCoord - 1, this.zCoord) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord, this.yCoord - 1, this.zCoord) == 0)
		{
			if(this.offLoadGas(this.xCoord, this.yCoord - 1, this.zCoord))
			{
				changed = true;
			}
		}

		if(this.worldObj.getBlockId(this.xCoord, this.yCoord + 1, this.zCoord) == ObjectHandler.gasBlock.blockID || this.worldObj.getBlockId(this.xCoord, this.yCoord + 1, this.zCoord) == 0)
		{
			if(this.offLoadGas(this.xCoord, this.yCoord + 1, this.zCoord))
			{
				changed = true;
			}
		}
		
		if(changed)
		{
			this.updateColor();
			this.updateAmount();
			this.updateOpacity();
			this.updateSize();
		}
		return changed;
	}
	
	public boolean offLoadGas(int i, int j, int k)
	{
		if(gases.size() <= 0)
		{
			return false;
		}
		
		int vDir = j - this.yCoord;
		TileEntity tile = this.worldObj.getBlockTileEntity(i, j, k);
		if(tile == null)
		{
			if(this.worldObj.getBlockId(i, j, k) == 0)
			{
				this.worldObj.setBlock(i, j, k, ObjectHandler.gasBlock.blockID);
			}
			return this.offLoadGas(i, j, k);
		} else if(!(tile instanceof TileEntityGas))
		{
			return false;
		} else
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			
			if(gasTile.amount > this.amount)
			{
				return false;
			} else
			{
				int[] selGas = null;
				
				if(EnviroGasDictionary.gasList[gases.get(0)[0]].density <= -1 && vDir == -1)
				{
					selGas = gases.get(0);
				} else if(EnviroGasDictionary.gasList[gases.get(gases.size()-1)[0]].density >= 1 && vDir == 1)
				{
					selGas = gases.get(gases.size() -1);
				} else
				{
					for(int index = 0; index < gases.size(); i++)
					{
						EnviroGas gasType = EnviroGasDictionary.gasList[gases.get(index)[0]];
						
						if(gasType.density <= -1F && vDir == 1)
						{
							continue;
						} else if(gasType.density >= 1 && vDir == -1)
						{
							continue;
						} else
						{
							selGas = gases.get(index);
							break;
						}
					}
					
					if(selGas == null)
					{
						return false;
					}
				}
				gasTile.addGas(selGas[0], 1);
				this.subtractGas(selGas[0], 1);
				return true;
			}
		}
	}
	
	@Override
	public void onDataPacket(INetworkManager netManager, Packet132TileEntityData packet)
	{
		if(packet.actionType == 0)
		{
			this.readFromNBT(packet.data);
		}
	}
}
