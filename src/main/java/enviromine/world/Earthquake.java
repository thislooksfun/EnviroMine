package enviromine.world;

import java.util.ArrayList;
import enviromine.core.EM_Settings;
import enviromine.handlers.EM_PhysManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class Earthquake
{
	public static ArrayList<Earthquake> pendingQuakes = new ArrayList<Earthquake>();
	public static int tickCount = 0;
	
	World world;
	int posX;
	int posZ;
	
	int length;
	int width;
	
	ArrayList<int[]> ravineMask = new ArrayList<int[]>(); // 2D array containing x,z coordinates of blocks within the ravine
	
	public Earthquake(World world, int i, int k, int l, int w)
	{
		this.world = world;
		this.posX = i;
		this.posZ = k;
		this.length = l;
		this.width = w;
		
		this.markRavine();
		pendingQuakes.add(this);
	}
	
	public void markRavine()
	{
		float angle = world.rand.nextFloat()*4 - 2;
		
		for(int i = -length/2; i < length/2; i++)
		{
			int fx = Math.round(Math.abs(angle) > 1F? i*angle : i);
			int fz = Math.round(Math.abs(angle) > 1F? i : i*angle);
			int widthFactor = MathHelper.ceiling_double_int(Math.cos(i/(length/3D))*width);
			
			if(Math.abs(angle) <= 1F)
			{
				for(int z = fx - widthFactor/2; z < fx + widthFactor/2; z++)
				{
					this.ravineMask.add(new int[]{fx + posX, z + posZ});
				}
			} else
			{
				for(int x = fx - widthFactor/2; x < fx + widthFactor/2; x++)
				{
					this.ravineMask.add(new int[]{x + posX, fz + posZ});
				}
			}
		}
	}
	
	public boolean removeBlock()
	{
		for(int y = 1; y < world.getActualHeight(); y++)
		{
			for(int i = this.ravineMask.size() - 1; i >= 0; i--)
			{
				int[] pos = this.ravineMask.get(i);
				
				int x = pos[0];
				int z = pos[1];
				
				if(world.getBlockMaterial(x, y, z) == Material.rock || world.getBlockMaterial(x, y, z) == Material.ground || world.getBlockMaterial(x, y, z) == Material.grass)
				{
					if(y < 8)
					{
						world.setBlock(x, y, z, Block.lavaMoving.blockID);
						
						if(EM_Settings.enablePhysics)
						{
							EM_PhysManager.schedulePhysUpdate(world, x, y, z, false, "Quake");
						}
						return true;
					} else
					{
						world.setBlockToAir(x, y, z);
						
						if(EM_Settings.enablePhysics)
						{
							EM_PhysManager.schedulePhysUpdate(world, x, y, z, false, "Quake");
						}
						return true;
					}
				}
				
				if(world.getTopSolidOrLiquidBlock(x, z) < 16)
				{
					ravineMask.remove(i);
				}
			}
		}
		
		return false;
	}
	
	public void removeAll()
	{
		for(int y = 1; y < world.getActualHeight(); y++)
		{
			for(int i = 0; i < this.ravineMask.size(); i++)
			{
				int[] pos = this.ravineMask.get(i);
				
				int x = pos[0];
				int z = pos[1];
				
				if(y < 8)
				{
					world.setBlock(x, y, z, Block.lavaMoving.blockID);
				} else
				{
					world.setBlockToAir(x, y, z);
				}
			}
		}
		
		this.ravineMask.clear();
	}
	
	public static void updateEarthquakes()
	{
		/*if(tickCount >= 1)
		{
			tickCount = 0;
		} else
		{
			tickCount++;
			return;
		}*/
		
		for(int i = pendingQuakes.size() - 1; i >= 0; i--)
		{
			Earthquake quake = pendingQuakes.get(i);
			
			if(!quake.removeBlock() || quake.ravineMask.size() <= 0)
			{
				pendingQuakes.remove(i);
			}
		}
	}
}
