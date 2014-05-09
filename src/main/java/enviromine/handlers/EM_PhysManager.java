package enviromine.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import com.google.common.base.Stopwatch;
import enviromine.EntityPhysicsBlock;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.gui.EM_GuiEnviroMeters;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.StabilityType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEndPortal;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockGlowStone;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLeavesBase;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EM_PhysManager
{
	public static List<String> usedSlidePositions = new ArrayList<String>();
	public static List<String> excluded = new ArrayList<String>();
	public static List<Object[]> physSchedule = new ArrayList<Object[]>();
	public static int updateInterval = 15;
	public static int currentTime = 0;
	
	public static int debugInterval = 15;
	public static int debugTime = 0;
	
	public static int debugUpdatesCaptured = 0;
	
	private static Stopwatch timer = new Stopwatch();
	
	public static void schedulePhysUpdate(World world, int x, int y, int z, boolean updateSelf, boolean exclusions, String type)
	{
		if(world.isRemote || world.getWorldTime() < 1000)
		{
			return;
		}
		
		Object[] entry = new Object[7];
		entry[0] = world;
		entry[1] = x;
		entry[2] = y;
		entry[3] = z;
		entry[4] = updateSelf;
		entry[5] = exclusions;
		entry[6] = type;
		
		physSchedule.add(entry);
	}
	
	public static void scheduleSlideUpdate(World world, int x, int y, int z)
	{
		if(world.isRemote || world.getWorldTime() < 1000)
		{
			return;
		}
		
		Object[] entry = new Object[7];
		entry[0] = world;
		entry[1] = x;
		entry[2] = y;
		entry[3] = z;
		entry[4] = true;
		entry[5] = true;
		entry[6] = "Slide";
		
		physSchedule.add(entry);
	}
	
	public static void updateSurroundingPhys(World world, int x, int y, int z, boolean updateSelf, String type)
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
							callPhysUpdate(world, x + i, y + j, k + z, type);
						} else
						{
							continue;
						}
					} else
					{
						callPhysUpdate(world, x + i, y + j, k + z, type);
					}
				}
			}
		}
	}
	
	public static void updateSurroundingWithExclusions(World world, int x, int y, int z, boolean updateSelf, String type)
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
								callPhysUpdate(world, x + i, y + j, k + z, type);
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
							callPhysUpdate(world, x + i, y + j, k + z, type);
						} else
						{
							continue;
						}
					}
				}
			}
		}
	}
	
	public static void callPhysUpdate(World world, int x, int y, int z, String type)
	{
		if(world.isRemote)
		{
			return;
		}
		
		callPhysUpdate(world, x, y, z, Block.blocksList[world.getBlockId(x, y, z)], world.getBlockMetadata(x, y, z), type);
	}
	
	public static void callPhysUpdate(World world, int x, int y, int z, Block block, int meta, String type)
	{
		if(world.isRemote || block == null)
		{
			return;
		}
		
		int[] blockData = getSurroundingBlockData(world, x, y, z);
		
		boolean waterLogged = false;
		boolean touchingWaterDirect = blockData[2] > 0;//isTouchingLiquid(world, x, y, z, true);
		boolean touchingWater = blockData[3] > 0;//isTouchingLiquid(world, x, y, z, false);
		
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		if(chunk != null)
		{
			waterLogged = (chunk.getBiomeGenForWorldCoords(x & 15, z & 15, world.getWorldChunkManager()).rainfall > 0 && world.isRaining()) || touchingWater;
		}
		
		boolean validSlideType = false;
		boolean emptyBelow = BlockSand.canFallBelow(world, x, y - 1, z);
		
		if(EM_Settings.blockProperties.containsKey("" + block.blockID + "," + world.getBlockMetadata(x, y, z)) || EM_Settings.blockProperties.containsKey("" + block.blockID))
		{
			BlockProperties slideProps;
			
			if(EM_Settings.blockProperties.containsKey("" + block.blockID + "," + world.getBlockMetadata(x, y, z)))
			{
				slideProps = EM_Settings.blockProperties.get("" + block.blockID + "," + world.getBlockMetadata(x, y, z));
			} else
			{
				slideProps = EM_Settings.blockProperties.get("" + block.blockID);
			}
			
			validSlideType = slideProps.slides || (((waterLogged && world.canBlockSeeTheSky(x, y + 1, z)) || touchingWater) && slideProps.wetSlide);
		} else if(block instanceof BlockSand || ((block.blockID == Block.dirt.blockID || block.blockID == Block.blockSnow.blockID) && ((waterLogged && world.canBlockSeeTheSky(x, y + 1, z)) || touchingWater)))
		{
			if(block instanceof BlockAnvil)
			{
				validSlideType = false;
			} else
			{
				validSlideType = true;
			}
		}
		
		if(validSlideType && EM_Settings.enableLandslide)
		{
			if(!(block instanceof BlockSand))
			{
				/*for(int i = -1; i < 2; i++)
				{
					for(int j = -1; j < 2; j++)
					{
						for(int k = -1; k < 2; k++)
						{
							
							Block testBlock = Block.blocksList[world.getBlockId(i + x, j + y, k + z)];
							int stabNum = 0;
							
							if(testBlock != null)
							{
								stabNum = getDefaultStabilityType(testBlock);
							} else
							{
								continue;
							}
							
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
							} else if(stabNum == 3)
							{
								StabilityType strongType = EM_Settings.stabilityTypes.get("strong");
								if(strongType != null && strongType.holdOther)
								{
									return;
								}
							} else if(stabNum == 2)
							{
								StabilityType avgType = EM_Settings.stabilityTypes.get("average");
								if(avgType != null && avgType.holdOther)
								{
									return;
								}
							} else if(stabNum == 1)
							{
								StabilityType looseType;
								if(block.blockID > 175 && EM_Settings.stabilityTypes.containsKey(EM_Settings.defaultStability))
								{
									looseType = EM_Settings.stabilityTypes.get(EM_Settings.defaultStability);
								} else
								{
									looseType = EM_Settings.stabilityTypes.get("loose");
								}
								if(looseType != null && looseType.holdOther)
								{
									return;
								}
							}
							
							if(world.getBlockId(i + x, j + y, k + z) == Block.glowStone.blockID)
							{
								return;
							}
						}
					}
				}*/
				
				if(blockData[4] >= 1)
				{
					return;
				}
			}
			int slideID = block.blockID;
			int slideMeta = meta;
			
			int[] pos = new int[]{x, y, z};
			int[] npos = slideDirection(world, pos, true);
			int[] ppos = slideDirection(world, pos, false);
			
			if(emptyBelow)
			{
				if(!(block instanceof BlockSand))
				{
					EntityPhysicsBlock physBlock = new EntityPhysicsBlock(world, pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5, slideID, slideMeta, false);
					world.spawnEntityInWorld(physBlock);
					EM_PhysManager.schedulePhysUpdate(world, x, y, z, true, true, "Normal");
				}
			} else if(!(pos[0] == npos[0] && pos[1] == npos[1] && pos[2] == npos[2]) && !usedSlidePositions.contains("" + npos[0] + "," + npos[2]))
			{
				//world.setBlock(npos[0], npos[1], npos[2], slideID, slideMeta, 2);
				world.setBlock(x, y, z, 0);
				usedSlidePositions.add("" + npos[0] + "," + npos[2]);
				
				EntityPhysicsBlock physBlock = new EntityPhysicsBlock(world, npos[0] + 0.5, npos[1] + 0.5, npos[2] + 0.5, slideID, slideMeta, false);
				physBlock.isLandSlide = true;
				world.spawnEntityInWorld(physBlock);
				EM_PhysManager.schedulePhysUpdate(world, x, y, z, true, true, "Normal");
				return;
			} else if(!(pos[0] == ppos[0] && pos[1] == ppos[1] && pos[2] == ppos[2]))
			{
				EM_PhysManager.scheduleSlideUpdate(world, x, y, z);
			}
		}
		
		if(isLegalType(world, x, y, z) && blockNotSolid(world, x, y - 1, z, false))
		{
			int dropBlock = block.blockID;//block.idDropped(block.getDamageValue(world, x, y, z), world.rand, 0);
			int dropMeta = -1;
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
			
			if(!defaultDrop)
			{
			} else if(dropBlock <= 0 || block.blockMaterial == Material.glass || block.blockMaterial == Material.ice)
			{
				dropType = 0;
			} else if(dropBlock >= Block.blocksList.length)
			{
				if(dropBlock >= Item.itemsList.length)
				{
					dropType = 0;
				} else if(Item.itemsList[dropBlock] == null)
				{
					dropType = 0;
				} else
				{
					dropType = 2;
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
					if(Block.blocksList[block.blockID] instanceof BlockLeavesBase)
					{
						dropType = 2;
					} else
					{
						dropType = 1;
					}
				}
			}
			
			int minThreshold = 10;
			int maxThreshold = 15;
			int supportDist = 1;
			int yMax = 1;
			
			int stabNum = getDefaultStabilityType(block);
			
			if(isCustom)
			{
				minThreshold = blockProps.minFall;
				maxThreshold = blockProps.maxFall;
				supportDist = blockProps.supportDist;
				if(blockProps.canHang)
				{
					yMax = 2;
				} else
				{
					yMax = 1;
				}
			} else if(stabNum == 3)
			{
				StabilityType strongType = EM_Settings.stabilityTypes.get("strong");
				minThreshold = strongType.minFall;
				maxThreshold = strongType.maxFall;
				supportDist = strongType.supportDist;
				if(strongType.canHang)
				{
					yMax = 2;
				} else
				{
					yMax = 1;
				}
			} else if(stabNum == 2)
			{
				StabilityType avgType = EM_Settings.stabilityTypes.get("average");
				minThreshold = avgType.minFall;
				maxThreshold = avgType.maxFall;
				supportDist = avgType.supportDist;
				if(avgType.canHang)
				{
					yMax = 2;
				} else
				{
					yMax = 1;
				}
			} else if(stabNum == 1)
			{
				StabilityType looseType;
				if(block.blockID > 175 && EM_Settings.stabilityTypes.containsKey(EM_Settings.defaultStability))
				{
					looseType = EM_Settings.stabilityTypes.get(EM_Settings.defaultStability);
				} else
				{
					looseType = EM_Settings.stabilityTypes.get("loose");
				}
				minThreshold = looseType.minFall;
				maxThreshold = looseType.maxFall;
				supportDist = looseType.supportDist;
				if(looseType.canHang)
				{
					yMax = 2;
				} else
				{
					yMax = 1;
				}
			}
			
			if(world.provider.isHellWorld && block.blockMaterial == Material.rock && !isCustom)
			{
				yMax = 2;
			}
			
			int missingBlocks = 0;
			
			if(yMax >= 2)
			{
				missingBlocks = blockData[0];
			} else
			{
				missingBlocks = blockData[1];
			}
			
			/*for(int i = -1; i < 2; i++)
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
						if(world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(i + x, j + y, k + z, i + x + 1, j + y + 1, k + z + 1)).size() > 0)
						{
							if(j < yMax)
							{
								missingBlocks++;
							}
						} else if((blockNotSolid(world, i + x, j + y, k + z, false) || (block.blockMaterial != Material.leaves && world.getBlockMaterial(i + x, j + y, k + z) == Material.leaves)) && !(i == 0 && j < 1 && k == 0))
						{
							if(j < yMax)
							{
								missingBlocks++;
							}
						} else if(world.getBlockId(i + x, j + y, k + z) == Block.glowStone.blockID)
						{
							return;
						}
					}
				}
			}*/
			
			if(yMax == 1)
			{
				missingBlocks += 9;
			}
			
			int dropChance = maxThreshold - missingBlocks;
			
			if(dropChance <= 0)
			{
				dropChance = 1;
			}
			
			boolean supported = hasSupports(world, x, y, z, touchingWaterDirect? supportDist/2 : supportDist);
			//missingBlocks total = 25 - 26
			
			if(missingBlocks > 0 && blockNotSolid(world, x, y - 1, z, false) && !supported)
			{
				if(!world.isRemote && ((missingBlocks > minThreshold && (world.rand.nextInt(dropChance) == 0 || type.equals("Collapse"))) || missingBlocks >= maxThreshold || (touchingWaterDirect && world.rand.nextBoolean())))
				{
					if(dropType == 2)
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
							dropItemstack(world, x, y, z, new ItemStack(dropBlock, dropNum, meta));
						} else if(!isCustom || (isCustom && dropMeta <= -1 && dropNum > 0))
						{
							block.dropBlockAsItem(world, x, y, z, meta, 0);
						}
						world.setBlock(x, y, z, 0);
						schedulePhysUpdate(world, x, y, z, true, true, "Normal");
						return;
					} else if(dropType == 0)
					{
						world.playAuxSFX(2001, x, y, z, block.blockID + (world.getBlockMetadata(x, y, z) << 12));
						
						if(block.blockID == Block.ice.blockID)
						{
							Material mat = world.getBlockMaterial(x, y - 1, z);
							
							if((mat.blocksMovement() || mat.isLiquid()) && !world.provider.isHellWorld)
							{
								world.setBlock(x, y, z, Block.waterMoving.blockID);
							} else
							{
								world.setBlock(x, y, z, 0);
							}
						} else
						{
							world.setBlock(x, y, z, 0);
						}
						
						if(block.blockMaterial != Material.ice || EM_Settings.spreadIce)
						{
							schedulePhysUpdate(world, x, y, z, true, true, "Break");
						}
						return;
					}
					
					if(dropType != 1)
					{
						return;
					}
					if(block.blockID == Block.stone.blockID && EM_Settings.stoneCracks)
					{
						world.setBlock(x, y, z, Block.cobblestone.blockID);
						dropBlock = Block.cobblestone.blockID;
					} else
					{
						world.setBlock(x, y, z, dropBlock, world.getBlockMetadata(x, y, z), 2);
					}
					
					EntityPhysicsBlock entityphysblock;
					if(isCustom && dropMeta > -1)
					{
						entityphysblock = new EntityPhysicsBlock(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, dropMeta, true);
					} else
					{
						entityphysblock = new EntityPhysicsBlock(world, (float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F, dropBlock, world.getBlockMetadata(x, y, z), true);
					}
					world.spawnEntityInWorld(entityphysblock);
					
				} else if(block.blockID == Block.stone.blockID && missingBlocks > minThreshold && !world.isRemote && EM_Settings.stoneCracks)
				{
					world.setBlock(x, y, z, Block.cobblestone.blockID);
				}
			}
		}
	}
	
	/*public static boolean isTouchingLiquid(World world, int x, int y, int z, boolean direct)
	{
		for(int i = x - 1; i <= x + 1; i++)
		{
			for(int j = y; j <= y + 1; j++)
			{
				for(int k = z - 1; k <= z + 1; k++)
				{
					if(direct)
					{
						if(j > y && !(i == x && k == z))
						{
							continue;
						} else if(j == y && i == x && k == z)
						{
							continue;
						} else if(j == y && !(i == x || k == z))
						{
							continue;
						}
					}
					
					Material material = world.getBlockMaterial(i, j, k);
					
					if(material == null)
					{
						continue;
					} else
					{
						if(material.isLiquid())
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}*/
	
	public static int[] getSurroundingBlockData(World world, int x, int y, int z)
	{
		/*
		 * 0 - missing solidBlocks
		 * 1 - missing solidBlocks level or below
		 * 2 - contactLiquids
		 * 3 - allLiquids
		 * 4 - heldUpByOther (0/1)
		 */
		
		int[] data = new int[]{0,0,0,0,0};
		
		for(int i = x - 1; i <= x + 1; i++)
		{
			for(int j = y - 1; j <= y + 1; j++)
			{
				for(int k = z - 1; k <= z + 1; k++)
				{
					Material material = world.getBlockMaterial(i, j, k);
					int blockID = world.getBlockId(i, j, k);
					int metaID = world.getBlockMetadata(i, j, k);
					Block block = Block.blocksList[blockID];
					
					if(blockID == 0)
					{
						if(y < 1)
						{
							data[1] += 1;
						}
						
						data[0] += 1;
						continue;
					}
					
					int stabNum = getDefaultStabilityType(block);
					
					if(material != null && material.isLiquid())
					{
						if(j > y && !(i == x && k == z))
						{
							data[3] += 1;
						} else if(j == y && i == x && k == z)
						{
							data[3] += 1;
						} else if(j == y && !(i == x || k == z))
						{
							data[3] += 1;
						} else
						{
							data[2] += 1;
							data[3] += 1;
						}
					}
					
					BlockProperties blockProps = null;
					
					if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)) || EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
					{
						if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z)))
						{
							blockProps = EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z) + "," + world.getBlockMetadata(i + x, j + y, k + z));
						} else if(EM_Settings.blockProperties.containsKey("" + world.getBlockId(i + x, j + y, k + z)))
						{
							blockProps = EM_Settings.blockProperties.get("" + world.getBlockId(i + x, j + y, k + z));
						}
					}
					
					if(stabNum == 3)
					{
						StabilityType strongType = EM_Settings.stabilityTypes.get("strong");
						if(strongType != null && strongType.holdOther)
						{
							data[4] = 1;
						}
					} else if(stabNum == 2)
					{
						StabilityType avgType = EM_Settings.stabilityTypes.get("average");
						if(avgType != null && avgType.holdOther)
						{
							data[4] = 1;
						}
					} else if(stabNum == 1)
					{
						StabilityType looseType;
						if(block.blockID > 175 && EM_Settings.stabilityTypes.containsKey(EM_Settings.defaultStability))
						{
							looseType = EM_Settings.stabilityTypes.get(EM_Settings.defaultStability);
						} else
						{
							looseType = EM_Settings.stabilityTypes.get("loose");
						}
						if(looseType != null && looseType.holdOther)
						{
							data[4] = 1;
						}
					}
					
					if((blockProps == null && blockID == Block.glowStone.blockID) || (blockProps != null && blockProps.holdsOthers))
					{
						data[4] = 1;
					}
					
					if(world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(i + x, j + y, k + z, i + x + 1, j + y + 1, k + z + 1)).size() > 0)
					{
						if(j < 1)
						{
							data[1] += 1;
						}
						
						data[0] += 1;
					} else if((blockNotSolid(world, i + x, j + y, k + z, false) || (material != Material.leaves && world.getBlockMaterial(i + x, j + y, k + z) == Material.leaves)) && !(i == 0 && j < 1 && k == 0))
					{
						if(j < 1)
						{
							data[1] += 1;
						}
						data[0] += 1;
					} else
					{
						
					}
				}
			}
		}
		
		data[1] += 9;
		
		return data;
	}

	public static boolean hasSupports(World world, int x, int y, int z, int dist)
	{
		for(int i = x - 1; i <= x + 1; i++)
		{
			for(int k = z - 1; k <= z + 1; k++)
			{
				int j = y - 1;
				
				if(!(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves))
				{
					return true;
				}
			}
		}
		for(int i = x + 1; i <= x + dist; i++)
		{
			int k = z;
			
			boolean cancel = false;
			
			for(int j = y - 1; j <= y; j++)
			{
				if(j == y)
				{
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
					{
						cancel = true;
						break;
					} else
					{
						continue;
					}
				} else
				{
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
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
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
					{
						cancel = true;
						break;
					} else
					{
						continue;
					}
				} else
				{
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
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
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
					{
						cancel = true;
						break;
					} else
					{
						continue;
					}
				} else
				{
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
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
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
					{
						cancel = true;
						break;
					} else
					{
						continue;
					}
				} else
				{
					if(blockNotSolid(world, i, j, k, false) || world.getBlockMaterial(i, j, k) == Material.leaves)
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
		if(!par1World.isRemote && par1World.getGameRules().getGameRuleBooleanValue("doTileDrops"))
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
		int meta = world.getBlockMetadata(x, y, z);
		
		if(id == 0)
		{
			return false;
		} else if(EM_Settings.blockProperties.containsKey("" + id + "," + meta) || EM_Settings.blockProperties.containsKey("" + id))
		{
			if(EM_Settings.blockProperties.containsKey("" + id + "," + meta))
			{
				BlockProperties blockProps = EM_Settings.blockProperties.get("" + id + "," + meta);
				return(blockProps.hasPhys && !blockProps.holdsOthers);
			} else
			{
				BlockProperties blockProps = EM_Settings.blockProperties.get("" + id);
				return(blockProps.hasPhys && !blockProps.holdsOthers);
			}
		} else
		{
			if(!(Block.blocksList[id] instanceof BlockMobSpawner) && !(Block.blocksList[id] instanceof BlockLadder) && !(Block.blocksList[id] instanceof BlockWeb) && !(Block.blocksList[id] instanceof BlockGlowStone) && !(Block.blocksList[id] instanceof BlockSign) && !(Block.blocksList[id] instanceof BlockBed) && !(Block.blocksList[id] instanceof BlockDoor) && !(Block.blocksList[id] instanceof BlockAnvil) && !(Block.blocksList[id] instanceof BlockGravel) && !(Block.blocksList[id] instanceof BlockSand) && !(Block.blocksList[id] instanceof BlockPortal) && !(Block.blocksList[id] instanceof BlockEndPortal) && !(Block.blocksList[id] == Block.whiteStone) && !(Block.blocksList[id] instanceof BlockEndPortalFrame) && !(Block.blocksList[id].blockMaterial == Material.vine) && !blockNotSolid(world, x, y, z, false) && Block.blocksList[id].blockHardness != -1F)
			{
				return true;
			} else
			{
				return false;
			}
		}
	}
	
	public static boolean blockNotSolid(World world, int x, int y, int z, boolean isSliding)
	{
		if(world.isAirBlock(x, y, z))
		{
			return true;
		}
		
		int l = world.getBlockId(x, y, z);
		Material material = Block.blocksList[l].blockMaterial;
		
		if(l == Block.fire.blockID)
		{
			return true;
		} else if(material == Material.water || material == Material.lava)
		{
			return !isSliding;
		} else if(Block.blocksList[l].getCollisionBoundingBoxFromPool(world, x, y, z) == null)
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	public static void updateSchedule()
	{
		if(physSchedule.size() >= 4096 && EM_Settings.updateCap <= -1)
		{
			EnviroMine.logger.log(Level.SEVERE, "Physics updates exeeded 4096/tick! Dumping update schedule, things may break.");
			physSchedule.clear();
			return;
		}
		
		if(EnviroMine.proxy.isClient())
		{
			if(debugTime == 0)
			{
				if(!timer.isRunning())
				{
					timer.start();
				}
				debugUpdatesCaptured = 0;
			}
		}
		
		boolean canClear = true;
		if(currentTime >= updateInterval)
		{
			int updateNum = 0;
			
			if(physSchedule.size() <= EM_Settings.updateCap || EM_Settings.updateCap < 0)
			{
				updateNum = physSchedule.size();
			} else
			{
				updateNum = EM_Settings.updateCap;
			}
			
			if(EnviroMine.proxy.isClient())
			{
				debugUpdatesCaptured += updateNum;
			}
			
			for(int i = updateNum - 1; i >= 0; i -= 1)
			{
				Object[] entry = physSchedule.get(i);
				if((Boolean)entry[5])
				{
					canClear = false;
					if(((String)entry[6]).equalsIgnoreCase("Slide"))
					{
						String position = (new StringBuilder()).append((Integer)entry[1]).append(",").append((Integer)entry[2]).append(",").append((Integer)entry[3]).toString();
						if(!excluded.contains(position))
						{
							excluded.add(position);
							callPhysUpdate((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (String)entry[6]);
						}
					} else
					{
						updateSurroundingWithExclusions((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (Boolean)entry[4], (String)entry[6]);
					}
				} else
				{
					System.out.print("Running non excluded update!");
					if(((String)entry[6]).equalsIgnoreCase("Slide"))
					{
						callPhysUpdate((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (String)entry[6]);
					} else
					{
						updateSurroundingPhys((World)entry[0], (Integer)entry[1], (Integer)entry[2], (Integer)entry[3], (Boolean)entry[4], (String)entry[6]);
					}
				}
				physSchedule.remove(i);
			}
			currentTime = 0;
		} else
		{
			currentTime += 1;
		}
		
		if(canClear)
		{
			excluded.clear();
		}
		
		usedSlidePositions.clear();
		
		if(EnviroMine.proxy.isClient() && debugTime >= debugInterval && timer.isRunning())
		{
			timer.stop();
			EM_GuiEnviroMeters.DB_physTimer = timer.toString();
			EM_GuiEnviroMeters.DB_physUpdates = debugUpdatesCaptured;
			timer.reset();
			debugTime = 0;
		} else if(EnviroMine.proxy.isClient() && timer.isRunning())
		{
			debugTime += 1;
		}
	}
	
	public static int[] slideDirection(World world, int[] pos, boolean checkEntities)
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
		
		if(blockNotSolid(world, x + 1, y, z, false) && blockNotSolid(world, x + 1, y - 1, z, true) && (!checkEntities || world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x + 1, y - 2, z, x + 2, y, z + 1)).size() <= 0))
		{
			canSlideDir.add("X+");
		}
		if(blockNotSolid(world, x - 1, y, z, false) && blockNotSolid(world, x - 1, y - 1, z, true) && (!checkEntities || world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x - 1, y - 2, z, x, y, z + 1)).size() <= 0))
		{
			canSlideDir.add("X-");
		}
		if(blockNotSolid(world, x, y, z + 1, false) && blockNotSolid(world, x, y - 1, z + 1, true) && (!checkEntities || world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x, y - 2, z + 1, x + 1, y, z + 2)).size() <= 0))
		{
			canSlideDir.add("Z+");
		}
		if(blockNotSolid(world, x, y, z - 1, false) && blockNotSolid(world, x, y - 1, z - 1, true) && (!checkEntities || world.getEntitiesWithinAABB(EntityPhysicsBlock.class, AxisAlignedBB.getBoundingBox(x, y - 2, z - 1, x + 1, y, z)).size() <= 0))
		{
			canSlideDir.add("Z-");
		}
		
		if(canSlideDir.size() >= 1)
		{
			String slideDir = "";
			
			slideDir = canSlideDir.get(world.rand.nextInt(canSlideDir.size()));
			
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
	
	public static int getDefaultStabilityType(Block block)
	{
		int type = 0;
		
		if(block.blockMaterial == Material.iron || block.blockMaterial == Material.wood || block instanceof BlockObsidian || block.blockID == Block.stoneBrick.blockID || block.blockID == Block.brick.blockID || block.blockID == Block.blockNetherQuartz.blockID)
		{
			type = 3;
		} else if(block.blockMaterial == Material.rock || block.blockMaterial == Material.glass || block.blockMaterial == Material.ice || block instanceof BlockLeavesBase)
		{
			type = 2;
		} else
		{
			type = 1;
		}
		
		return type;
	}
}
