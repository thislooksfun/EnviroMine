package enviromine.handlers;

import java.util.ArrayList;
import java.util.List;
import enviromine.core.EM_Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockGlowStone;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EM_PhysManager
{
	public static List<String> excluded = new ArrayList<String>();
	public static List<Object[]> physSchedule = new ArrayList<Object[]>();
	public static int updateInterval = 1;
	public static int currentTime = 0;
	
	public static void schedulePhysUpdate(World world, int x, int y, int z, boolean updateSelf, boolean exclusions)
	{
		if(world.isRemote)
		{
			return;
		}
		
		Object[] entry = new Object[6];
		entry[0] = world;
		entry[1] = x;
		entry[2] = y;
		entry[3] = z;
		entry[4] = updateSelf;
		entry[5] = exclusions;
		
		physSchedule.add(entry);
	}
	
	public static void updateSurroundingPhys(World world, int x, int y, int z, boolean updateSelf)
	{
		if(world.isRemote)
		{
			return;
		}
		
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				for(int k = -1; k <= 1; k++)
				{
					if(i == 0 && j == 0 && k == 0)
					{
						if(updateSelf)
						{
							callPhysUpdate(world, x + i, y + j, k + z);
						} else
						{
							continue;
						}
					} else
					{
						callPhysUpdate(world, x + i, y + j, k + z);
					}
				}
			}
		}
	}
	
	public static void updateSurroundingWithExclusions(World world, int x, int y, int z, boolean updateSelf)
	{
		if(world.isRemote)
		{
			return;
		}
		
		for(int i = -1; i <= 1; i++)
		{
			for(int j = -1; j <= 1; j++)
			{
				for(int k = -1; k <= 1; k++)
				{
					String position = (new StringBuilder()).append(x + i).append(",").append(y + j).append(",").append(z + k).toString();
					if(i == 0 && j == 0 && k == 0)
					{
						if(updateSelf)
						{
							if(!excluded.contains(position))
							{
								excluded.add(position);
								callPhysUpdate(world, x + i, y + j, k + z);
							} else
							{
								continue;
							}
						} else
						{
							excluded.add(position);
							continue;
						}
					} else
					{
						if(!excluded.contains(position))
						{
							excluded.add(position);
							callPhysUpdate(world, x + i, y + j, k + z);
						} else
						{
							continue;
						}
					}
				}
			}
		}
	}
	
	public static void callPhysUpdate(World world, int x, int y, int z)
	{
		if(world.isRemote)
		{
			return;
		}
		
		callPhysUpdate(world, x, y, z, Block.blocksList[world.getBlockId(x, y, z)], world.getBlockMetadata(x, y, z));
	}
	
	public static void callPhysUpdate(World world, int x, int y, int z, Block block, int meta)
	{
		if(world.isRemote)
		{
			return;
		}
		
		if(isLegalType(world, x, y, z))
    	{
    		int dropBlock = block.idDropped(block.getDamageValue(world, x, y, z), world.rand, 0);/*idDropped(damage value, random, quantity)*/
    		int dropMeta = -1;//block.damageDropped(block.getDamageValue(world, x, y, z));
    		int dropNum = -1;
    		int dropType = 0;
    		
    		boolean isCustom = false;
    		Object[] blockProps = null;
    		
    		if(EM_Settings.blockProperties.containsKey(block.blockID))
    		{
    			blockProps = EM_Settings.blockProperties.get(block.blockID);
    			if((Integer)blockProps[1] == world.getBlockMetadata(x, y, z) || (Integer)blockProps[1] == -1)
    			{
    				isCustom = true;
    				
        			if((Integer)blockProps[2] == 0)
        			{
        				dropType = 0;
        				dropBlock = 0;
        				dropMeta = 0;
        				dropNum = 0;
        			} else if(Block.blocksList[(Integer)blockProps[2]] != null && (Integer)blockProps[4] <= 0)
        			{
        				dropType = 1;
        				dropBlock = (Integer)blockProps[2];
        				if((Integer)blockProps[3] <= -1)
        				{
        					dropMeta = -1;
        				} else
        				{
        					dropMeta = (Integer)blockProps[3];
        				}
        				dropNum = 0;
        			} else if(Item.itemsList[(Integer)blockProps[2]] != null && (Integer)blockProps[4] > 0)
        			{
        				dropType = 2;
        				dropBlock = (Integer)blockProps[2];
        				if((Integer)blockProps[3] <= -1)
        				{
        					dropMeta = -1;
        				} else
        				{
        					dropMeta = (Integer)blockProps[3];
        				}
        				dropNum = (Integer)blockProps[4];
        			} else
        			{
        				dropType = 0;
        				dropBlock = 0;
        				dropMeta = -1;
        				dropNum = -1;
        			}
    			}
    		}
    		
			if(isCustom)
			{
			} else if(dropBlock <= 0)
    		{
    			dropType = 0;
    		} else if(dropBlock >= 4096)
    		{
    			if(dropBlock >= 32000)
    			{
        			dropType = 0;
    			} else if(Item.itemsList[dropBlock] == null)
    			{
        			dropType = 0;
    			}
    		} else if(Block.blocksList[dropBlock] == null && Item.itemsList[dropBlock] == null)
    		{
    			dropType = 0;
    		} else
    		{
    			if(Item.itemsList[dropBlock] != null && !(Item.itemsList[dropBlock] instanceof ItemBlock))
    			{
    				dropType = 2;
    			} else if(Block.blocksList[dropBlock] != null)
    			{
    				dropType = 1;
    			}
    		}
        	
    		int minThreshold = 10;
    		int maxThreshold = 15;
    		int supportDist = 1;
    		
    		if(isCustom)
    		{
    			minThreshold = (Integer)blockProps[8];
    			maxThreshold = (Integer)blockProps[9];
    		} else if(block.blockMaterial == Material.iron || block.blockMaterial == Material.wood || block instanceof BlockObsidian)
    		{
    			minThreshold = 22;
    			maxThreshold = 25;
        		supportDist = 5;
    		} else if(block.blockMaterial == Material.rock || block.blockMaterial == Material.glass || block instanceof BlockIce || block instanceof BlockLeaves)
    		{
        		minThreshold = 15;
        		maxThreshold = 22;
        		supportDist = 3;
    		}
    		
    		int missingBlocks = 0;
    		
    		for(int i = -1; i < 2; i++)
    		{
    			for(int j = -1; j < 1; j++)
    			{
    				for(int k = -1; k < 2; k++)
    				{
    					if(blockNotSolid(world, i + x, j + y, k + z) && !(i == 0 && j < 1 && k == 0))
    					{
        					missingBlocks++;
    					} else if(i == 0 && j == 1 && k == 0 && (block.blockMaterial == Material.rock || block.blockMaterial == Material.ground))
    					{
    						missingBlocks += 2;
    					} else if(world.getBlockId(i + x, j + y, k + z) == Block.glowStone.blockID)
    					{
    						return;
    					}
    				}
    			}
    		}
    		
    		missingBlocks += 9;
    		
    		int dropChance = maxThreshold - missingBlocks;
    		
    		if(dropChance <= 0)
    		{
    			dropChance = 1;
    		}
    		
    		boolean supported = hasSupports(world, x, y, z, supportDist);
    		//missingBlocks total = 25 - 26
    		
    		if(missingBlocks > 0 && blockNotSolid(world, x, y - 1, z) && !supported)
    		{
    			if (!world.isRemote && ((missingBlocks > minThreshold && world.rand.nextInt(dropChance) == 0) || missingBlocks >= maxThreshold))
    			{
    	        	if(dropType == 2 && block.quantityDropped(world.rand) > 0 && !(block instanceof BlockOre || block instanceof BlockRedstoneOre))
    	        	{
    	        		world.playAuxSFX(2001, x, y, z, block.blockID + (world.getBlockMetadata(x, y, z) << 12));
    	        		if(isCustom && dropMeta > -1)
    	        		{
    	        			if(dropNum >= 1)
    	        			{
    	        				dropItemstack(world, x, y, z, new ItemStack(dropBlock, dropNum, dropMeta));
    	        			}
    	        		} else if(isCustom && dropNum >= 1)
    	        		{
	        				dropItemstack(world, x, y, z, new ItemStack(dropBlock, dropNum, block.getDamageValue(world, x, y, z)));
    	        		} else if(!isCustom || (isCustom && dropMeta <= -1 && dropNum != 0))
    	        		{
    	        			block.dropBlockAsItem(world, x, y, z, block.getDamageValue(world, x, y, z),1);
    	        		}
    	        		world.setBlock(x, y, z, 0);
        				schedulePhysUpdate(world, x, y, z, false, true);
    	        		return;
    	        	} else if(block.quantityDropped(world.rand) <= 0 || dropType == 0)
	    	        {
    	        		world.playAuxSFX(2001, x, y, z, block.blockID + (world.getBlockMetadata(x, y, z) << 12));
    	        		
    	        		if(block.blockID == Block.ice.blockID)
    	        		{
    	        			world.setBlock(x, y, z, Block.waterMoving.blockID);
    	        		} else
    	        		{
        	        		world.setBlock(x, y, z, 0);
    	        		}
        				schedulePhysUpdate(world, x, y, z, false, true);
    	        		return;
    	        	} else if(block instanceof BlockOre || block instanceof BlockRedstoneOre)
    	        	{
    	        		dropType = 1;
    	        		if(block.blockID == Block.oreNetherQuartz.blockID)
    	        		{
    	        			dropBlock = Block.netherrack.blockID;
    	        		} else
    	        		{
    	        			dropBlock = Block.cobblestone.blockID;
    	        		}
    	        	}
    	        	
    	        	world.setBlock(x, y, z, dropBlock, world.getBlockMetadata(x, y, z), 2);
    	        	
    				EntityFallingSand entityfallingsand;
    				if(isCustom && dropMeta != -1)
    				{
    					entityfallingsand = new EntityFallingSand(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, dropMeta);
        			} else
    				{
    					entityfallingsand = new EntityFallingSand(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, world.getBlockMetadata(x, y, z));
    				}
    				world.spawnEntityInWorld(entityfallingsand);
    				
    				schedulePhysUpdate(world, x, y, z, false, true);

    			} else if(!world.isRemote && missingBlocks > minThreshold)
        		{
        			if(block.blockID == Block.stone.blockID)
        			{
    	        		world.setBlock(x, y, z, Block.cobblestone.blockID);
        			}
        		}
    		}
    	}
	}
	

    public static boolean hasSupports(World world, int x, int y, int z, int dist)
	{
    	for(int i = x + 1; i <= x + dist; i++)
    	{
        	int k = z;
        	
    		boolean cancel = false;
    		
    		for(int j = y - 1; j <= y; j++)
    		{
    			if(j == y)
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				continue;
	    			} else
	    			{
	    				return true;
	    			}
    			}
    		}
    		
    		if(cancel)
    		{
    			break;
    		}
    	}
    	
    	for(int i = x - 1; i >= x - dist; i--)
    	{
        	int k = z;
        	
    		boolean cancel = false;
    		
    		for(int j = y - 1; j <= y; j++)
    		{
    			if(j == y)
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				continue;
	    			} else
	    			{
	    				return true;
	    			}
    			}
    		}
    		
    		if(cancel)
    		{
    			break;
    		}
    	}
    	
    	for(int k = z + 1; k <= z + dist; k++)
    	{
        	int i = x;
        	
    		boolean cancel = false;
    		
    		for(int j = y - 1; j <= y; j++)
    		{
    			if(j == y)
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				continue;
	    			} else
	    			{
	    				return true;
	    			}
    			}
    		}
    		
    		if(cancel)
    		{
    			break;
    		}
    	}
    	
    	for(int k = z - 1; k >= z - dist; k--)
    	{
        	int i = x;
        	
    		boolean cancel = false;
    		
    		for(int j = y - 1; j <= y; j++)
    		{
    			if(j == y)
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k))
	    			{
	    				continue;
	    			} else
	    			{
	    				return true;
	    			}
    			}
    		}
    		
    		if(cancel)
    		{
    			break;
    		}
    	}
    	
    	return false;
	}

	protected static void dropItemstack(World par1World, int par2, int par3, int par4, ItemStack par5ItemStack)
    {
        if (!par1World.isRemote && par1World.getGameRules().getGameRuleBooleanValue("doTileDrops"))
        {
            float f = 0.7F;
            double d0 = (double)(par1World.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d1 = (double)(par1World.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            double d2 = (double)(par1World.rand.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
            EntityItem entityitem = new EntityItem(par1World, (double)par2 + d0, (double)par3 + d1, (double)par4 + d2, par5ItemStack);
            entityitem.delayBeforeCanPickup = 10;
            par1World.spawnEntityInWorld(entityitem);
        }
    }

	public static boolean isLegalType(World world, int x, int y, int z)
	{
		int id = world.getBlockId(x, y, z);
		
		if(id == 0)
		{
			return false;
		} else
		{
			if(
			!(Block.blocksList[id] instanceof BlockMobSpawner) && 
			!(Block.blocksList[id] instanceof BlockLadder) && 
			!(Block.blocksList[id] instanceof BlockWeb) && 
			!(Block.blocksList[id] instanceof BlockGlowStone) && 
			!(Block.blocksList[id] instanceof BlockSign) && 
			!(Block.blocksList[id] instanceof BlockBed) && 
			!(Block.blocksList[id] instanceof BlockDoor) && 
			//!(Block.blocksList[id] instanceof BlockChest) && 
			//!(Block.blocksList[id] instanceof BlockEnderChest) && 
			//!(Block.blocksList[id] instanceof BlockDispenser) && 
			//!(Block.blocksList[id] instanceof BlockFurnace) && 
			!(Block.blocksList[id] instanceof BlockAnvil) && 
			!(Block.blocksList[id] instanceof BlockGravel) && 
			!(Block.blocksList[id] instanceof BlockSand) &&
			!(Block.blocksList[id] instanceof BlockPortal) &&
			!(Block.blocksList[id] instanceof BlockEndPortal) &&
			!(Block.blocksList[id] == Block.whiteStone) &&
			!(Block.blocksList[id] instanceof BlockEndPortalFrame) &&
			!(Block.blocksList[id].blockMaterial == Material.vine) && 
			!blockNotSolid(world, x, y, z) && 
			Block.blocksList[id].blockHardness != -1F
			) 
			{
				return true;
			} else
			{
				return false;
			}
		}
	}

	public static boolean blockNotSolid(World world, int x, int y, int z)
	{
        int l = world.getBlockId(x, y, z);
        
		if (world.isAirBlock(x, y, z))
        {
            return true;
        }
        else if (l == Block.fire.blockID)
        {
            return true;
        }
        else if(Block.blocksList[l].getCollisionBoundingBoxFromPool(world, x, y, z) == null)
        {
        	return true;
        } else
        {
            Material material = Block.blocksList[l].blockMaterial;
            return material == Material.water ? true : material == Material.lava;
        }
	}
	
	public static void updateSchedule()
	{
		boolean canClear = true;
		if(currentTime >= updateInterval)
		{
			for(int i = physSchedule.size() - 1; i >= 0; i -= 1)
			{
				Object[] entry = physSchedule.get(i);
				if((Boolean)entry[5])
				{
					canClear = false;
					updateSurroundingWithExclusions((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (Boolean)entry[4]);
				} else
				{
					updateSurroundingPhys((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (Boolean)entry[4]);
				}
				physSchedule.remove(i);
			}
		} else
		{
			currentTime += 1;
		}
		
		if(canClear)
		{
			excluded.clear();
		}
	}
}
