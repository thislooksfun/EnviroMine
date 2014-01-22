package enviromine.handlers;

import java.util.ArrayList;
import java.util.List;

import enviromine.EntityPhysicsBlock;
import enviromine.core.EM_Settings;
import enviromine.trackers.BlockProperties;
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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

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
		if(world.isRemote || block == null)
		{
			return;
		}

        boolean waterLogged = false;
		
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
        if(chunk != null)
        {
        	waterLogged = chunk.getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager()).rainfall > 0 && world.isRaining();
        }
        
		if(world.getBlockId(x, y - 1, z) != 0 && (block instanceof BlockSand || (block.blockID == Block.dirt.blockID && waterLogged && y >= 48 && world.canBlockSeeTheSky(x, y, z))))
		{
			if(EM_Settings.enableLandslide == false) { return; } // If Landslides Disable stop here
    		if(block.blockID == Block.dirt.blockID)
    		{
	    		for(int i = -1; i < 2; i++)
	    		{
	    			for(int j = -1; j < 1; j++)
	    			{
	    				for(int k = -1; k < 2; k++)
	    				{
	    					if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)) || EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
	    					{
	    						if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
	    						{
	    							if(EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)).holdsOthers)
	    							{
	    								return;
	    							}
	    						} else if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)))
	    						{
	    							if(EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z)).holdsOthers)
	    							{
	    								return;
	    							}
	    						}
	    					}
	    					if(world.getBlockId(i + x, j + y, k + z) == Block.glowStone.blockID)
	    					{
								return;
	    					}
	    				}
	    			}
	    		}
    		}
			int slideID = block.blockID;
			int slideMeta = meta;
			
			int[] pos = new int[]{x,y,z};
			int[] npos =  slideDirection(world, pos);
			
			if(!(pos[0] == npos[0] && pos[1] == npos[1] && pos[2] == npos[2]))
			{
				world.setBlock(npos[0], npos[1], npos[2], slideID, slideMeta, 2);
				world.setBlock(x, y, z, 0);
				
				EntityPhysicsBlock physBlock = new EntityPhysicsBlock(world, npos[0]+0.5, npos[1]+0.5, npos[2]+0.5, slideID, slideMeta, false);
				physBlock.isLandSlide = true;
				world.spawnEntityInWorld(physBlock);
        		EM_PhysManager.schedulePhysUpdate(world, x, y, z, true, false);
				return;
			}
		}
		
		if(isLegalType(world, x, y, z))
    	{
    		int dropBlock = block.idDropped(block.getDamageValue(world, x, y, z), world.rand, 0);/*idDropped(damage value, random, quantity)*/
    		int dropMeta = -1;//block.damageDropped(block.getDamageValue(world, x, y, z));
    		int dropNum = -1;
    		int dropType = 0;
    		
    		boolean isCustom = false;
    		boolean defaultDrop = true;
    		BlockProperties blockProps = null;
    		
    		if(EM_Settings.blockProperties.containsKey("" + block.blockID + "," + world.getBlockMetadata(x, y, z)) || EM_Settings.blockProperties.containsKey("" + block.blockID))
    		{
    			if(EM_Settings.blockProperties.containsKey("" + block.blockID + "," + world.getBlockMetadata(x, y, z)))
    			{
    				blockProps = EM_Settings.blockProperties.get("" + block.blockID + "," + world.getBlockMetadata(x, y, z));
    			} else
    			{
    				blockProps = EM_Settings.blockProperties.get("" + block.blockID);
    			}
    			
    			if(blockProps.meta == world.getBlockMetadata(x, y, z) || blockProps.meta == -1)
    			{
    				isCustom = true;
    				defaultDrop = false;
    				
    				if(blockProps.dropID < 0)
    				{
        				dropType = 1;
        				defaultDrop = true;
        				dropNum = blockProps.dropNum;
    				} else if(blockProps.dropID == 0)
        			{
        				dropType = 0;
        				dropBlock = 0;
        				dropMeta = 0;
        				dropNum = 0;
        			} else if(Block.blocksList[blockProps.dropID] != null && blockProps.dropNum <= 0)
        			{
        				dropType = 1;
        				dropBlock = blockProps.dropID;
        				if(blockProps.dropMeta <= -1)
        				{
        					dropMeta = -1;
        				} else
        				{
        					dropMeta = blockProps.dropMeta;
        				}
        				dropNum = 0;
        			} else if(Item.itemsList[blockProps.dropID] != null && blockProps.dropNum > 0)
        			{
        				dropType = 2;
        				dropBlock = blockProps.dropID;
        				if(blockProps.dropMeta <= -1)
        				{
        					dropMeta = -1;
        				} else
        				{
        					dropMeta = blockProps.dropMeta;
        				}
        				dropNum = blockProps.dropNum;
        			} else
        			{
        				dropType = 0;
        				dropBlock = 0;
        				dropMeta = -1;
        				dropNum = -1;
        			}
    			}
    		}
    		
			if(!defaultDrop)
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
    			minThreshold = blockProps.minFall;
    			maxThreshold = blockProps.maxFall;
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
    			for(int j = -1; j < 2; j++)
    			{
    				for(int k = -1; k < 2; k++)
    				{
    					if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)) || EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
    					{
    						if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
    						{
    							if(EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)).holdsOthers)
    							{
    								return;
    							}
    						} else if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)))
    						{
    							if(EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z)).holdsOthers)
    							{
    								return;
    							}
    						}
    					}
    					if((blockNotSolid(world, i + x, j + y, k + z) || (block.blockMaterial != Material.leaves && world.getBlockMaterial(i, j, k) == Material.leaves)) && !(i == 0 && j < 1 && k == 0))
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
    		
    		//missingBlocks += 9;
    		
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
    	        	
    				EntityPhysicsBlock entityphysblock;
    				if(isCustom && dropMeta > -1)
    				{
    					entityphysblock = new EntityPhysicsBlock(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, dropMeta, false);
        			} else
    				{
        				entityphysblock = new EntityPhysicsBlock(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, world.getBlockMetadata(x, y, z), false);
    				}
    				world.spawnEntityInWorld(entityphysblock);
    				
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
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
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
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
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
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
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
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
	    			{
	    				cancel = true;
	    				break;
	    			} else
	    			{
	    				continue;
	    			}
    			} else
    			{
	    			if(blockNotSolid(world, i, j, k) && world.getBlockMaterial(i, j, k) != Material.leaves)
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
	
	public static int[] slideDirection(World world, int[] pos)
	{
		if(pos.length != 3)
		{
			return pos;
		}
		
		int[] npos = new int[3];
		
		int x = pos[0];
		int y = pos[1];
		int z = pos[2];
		
		npos[0] = x;
		npos[1] = y;
		npos[2] = z;
		
    	ArrayList<String> canSlideDir = new ArrayList<String>();
    	
    	if(world.getBlockId(x + 1, y, z) == 0 && world.getBlockId(x + 1, y - 1, z) == 0 && world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x + 1, y - 2, z, x + 2, y, z + 1)).size() <= 0)
    	{
    		canSlideDir.add("X+");
    	}
    	if(world.getBlockId(x - 1, y, z) == 0 && world.getBlockId(x - 1, y - 1, z) == 0 && world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x - 1, y - 2, z, x, y, z + 1)).size() <= 0)
    	{
    		canSlideDir.add("X-");
    	}
    	if(world.getBlockId(x, y, z + 1) == 0 && world.getBlockId(x, y - 1, z + 1) == 0 && world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x + 1, y - 2, z + 1, x + 1, y, z + 2)).size() <= 0)
    	{
    		canSlideDir.add("Z+");
    	}
    	if(world.getBlockId(x, y, z - 1) == 0 && world.getBlockId(x, y - 1, z - 1) == 0 && world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x + 1, y - 2, z - 1, x + 1, y, z)).size() <= 0)
    	{
    		canSlideDir.add("Z-");
    	}
    	
    	if(canSlideDir.size() >= 1)
    	{
    		String slideDir = "";
    		
    		if(canSlideDir.size() == 1)
    		{
    			slideDir = canSlideDir.get(0);
    		} else
    		{
    			slideDir = canSlideDir.get(world.rand.nextInt(canSlideDir.size() - 1));
    		}
    		
    		if(slideDir == "X+")
    		{
    			npos[0] = x + 1;
    		} else if(slideDir == "X-")
    		{
    			npos[0] = x - 1;
    		} else if(slideDir == "Z+")
    		{
    			npos[2] = z + 1;
    		} else if(slideDir == "Z-")
    		{
    			npos[2] = z - 1;
    		}
    	}
    	
    	return npos;
	}
}
