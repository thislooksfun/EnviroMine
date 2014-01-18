package enviromine.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cpw.mods.fml.common.network.PacketDispatcher;
import enviromine.EnviroPotion;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.trackers.ArmorProperties;
import enviromine.trackers.BlockProperties;
import enviromine.trackers.EnviroDataTracker;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class EM_StatusManager
{
	public static List<String[]> trackedEntities = new ArrayList<String[]>();
	public static List<EnviroDataTracker> trackerList = new ArrayList<EnviroDataTracker>();
	public static int ticksSinceUpdate = 0;
	public static int updateInterval = 15;
	
	public static void addToManager(EnviroDataTracker tracker)
	{
		trackerList.add(tracker);
	}
	
	public static void updateTrackers()
	{
		if(EnviroMine.proxy.isClient() && Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			if(Minecraft.getMinecraft().getIntegratedServer().getServerListeningThread().isGamePaused() && !EnviroMine.proxy.isOpenToLAN())
			{
				return;
			}
		}
		
		for(int i = 0; i < trackerList.size(); i += 1)
		{
			EnviroDataTracker tracker = trackerList.get(i);
			tracker.updateData();
			if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
			{
				syncMultiplayerTracker(tracker);
			}
		}
	}
	
	public static void syncMultiplayerTracker(EnviroDataTracker tracker)
	{
		String dataString = "";
		if(tracker.trackedEntity instanceof EntityPlayer)
		{
			dataString = ("" + ((EntityPlayer)tracker.trackedEntity).username + "," + tracker.airQuality + "," + tracker.bodyTemp + "," + tracker.hydration + "," + tracker.sanity + "," + tracker.airTemp);
		} else
		{
			return;
			//dataString = ("" + tracker.trackedEntity.entityId + "," + tracker.airQuality + "," + tracker.bodyTemp + "," + tracker.hydration + "," + tracker.sanity);
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(bos);
		
		try
		{
	        outputStream.writeBytes(dataString);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = EM_Settings.Channel;
		packet.data = bos.toByteArray();
		packet.length = bos.size();
		PacketDispatcher.sendPacketToAllPlayers(packet);
	}
	
	public static EnviroDataTracker lookupTracker(EntityLivingBase entity)
	{
		for(int i = 0; i < trackerList.size(); i += 1)
		{
			if(trackerList.get(i).trackedEntity == entity)
			{
				return trackerList.get(i);
			} else
			{
				continue;
			}
		}
		return null;
	}
	
	public static EnviroDataTracker lookupTrackerFromID(int id)
	{
		for(int i = 0; i < trackerList.size(); i += 1)
		{
			if(trackerList.get(i) == null)
			{
				continue;
			} else if(trackerList.get(i).trackedEntity != null)
			{
				if(trackerList.get(i).trackedEntity.entityId == id)
				{
					return trackerList.get(i);
				} else
				{
					continue;
				}
			}
		}
		return null;
	}
	
	public static EnviroDataTracker lookupTrackerFromUUID(UUID id)
	{
		for(int i = 0; i < trackerList.size(); i += 1)
		{
			if(trackerList.get(i) == null)
			{
				continue;
			} else if(trackerList.get(i).trackedEntity != null)
			{
				if(trackerList.get(i).trackedEntity.getUniqueID() == id)
				{
					return trackerList.get(i);
				} else
				{
					continue;
				}
			}
		}
		return null;
	}
	
	public static EnviroDataTracker lookupPlayerTrackerFromName(String user)
	{
		for(int i = 0; i < trackerList.size(); i += 1)
		{
			if(trackerList.get(i) == null)
			{
				continue;
			} else if(trackerList.get(i).trackedEntity instanceof EntityPlayer)
			{
				if(((EntityPlayer)trackerList.get(i).trackedEntity).username.equals(user))
				{
					return trackerList.get(i);
				} else
				{
					continue;
				}
			} else
			{
				continue;
			}
		}
		return null;
	}
	
	public static void updateTimer()
	{
		if(ticksSinceUpdate >= updateInterval)
		{
			updateTrackers();
			ticksSinceUpdate = 0;
		} else
		{
			ticksSinceUpdate += 1;
		}
	}
	
	public static float[] getSurroundingData(EntityLivingBase entityLiving, int range)
	{
		float[] data = new float[8];
		
		float sanityRate = 0;
		
		float quality = 0;
		int leaves = 0;
		
		float dropSpeed = 0.1F;
		float riseSpeed = 0.1F;
		
		float temp = -999F;
		float cooling = 0;
		float dehydrateBonus = 0.0F;
		int canBurn = 0;
		int animalHostility = 0;
		boolean nearLava = false;
		
		boolean isCustom = false;
		BlockProperties blockProps = null;
		
		int i = MathHelper.floor_double(entityLiving.posX);
		int j = MathHelper.floor_double(entityLiving.posY);
		int k = MathHelper.floor_double(entityLiving.posZ);
		
		if(entityLiving.worldObj == null)
		{
			return data;
		}
		
		Chunk chunk = entityLiving.worldObj.getChunkFromBlockCoords(i, k);
		
		if(chunk == null)
		{
			return data;
		}
		
		BiomeGenBase biome = chunk.getBiomeGenForWorldCoords(i & 15, k & 15, entityLiving.worldObj.getWorldChunkManager());
		
		if(biome == null)
		{
			return data;
		}
		
		boolean isDay = entityLiving.worldObj.isDaytime();
		
		int lightLev = 0;
		
		if(j > 0)
		{
			if(j >= 256)
			{
				lightLev = 15;
			} else
			{
				lightLev = chunk.getSavedLightValue(EnumSkyBlock.Sky, i & 0xf, j, k & 0xf);
			}
		}
		
		for(int x = -range; x <= range; x++)
		{
			for(int y = -range; y <= range; y++)
			{
				for(int z = -range; z <= range; z++)
				{
					int id = 0;
					int meta = 0;

					id = entityLiving.worldObj.getBlockId(i + x, j + y, k + z);
					
					if(id != 0)
					{
						meta = entityLiving.worldObj.getBlockMetadata(i + x, j + y, k + z);
					}
					
					if(EM_Settings.blockProperties.containsKey(id))
					{
						blockProps = EM_Settings.blockProperties.get(id);
						
						if(blockProps.meta == meta || blockProps.meta == -1)
						{
							isCustom = true;
						}
					}
					
					if(isCustom && blockProps != null)
					{
						if(quality <= blockProps.air)
						{
							quality = blockProps.air;
						}
						if(temp <= blockProps.temp)
						{
							temp = blockProps.temp;
						}
						if((sanityRate <= blockProps.sanity && blockProps.sanity > 0F) || (sanityRate >= blockProps.sanity && blockProps.sanity < 0 && sanityRate <= 0))
						{
							sanityRate = blockProps.sanity;
						}
						
					} else if((id == Block.lavaMoving.blockID || id == Block.lavaStill.blockID) && quality > -1F)
					{
						if(quality > -1)
						{
							quality = -1;
						}
						if(temp < 100F)
						{
							temp = 100F;
						}
						nearLava = true;
						canBurn = 1;
					} else if(id == Block.fire.blockID)
					{
						if(quality > -0.5F)
						{
							quality = -0.5F;
						}
						if(temp < 50F)
						{
							temp = 50F;
						}
					} else if((id == Block.torchWood.blockID || id == Block.furnaceBurning.blockID) && quality > -0.25F)
					{
						if(quality > -0.25F)
						{
							quality = -0.25F;
						}
						if(temp < 20F)
						{
							temp = 20F;
						}
					} else if(id == Block.leaves.blockID || id == Block.plantYellow.blockID || id == Block.plantRed.blockID || id == Block.waterlily.blockID || id == Block.grass.blockID)
					{
						if(id == Block.plantRed.blockID || id == Block.plantYellow.blockID)
						{
							sanityRate = 1F;
						}
						leaves += 1;
					} else if((id == Block.netherrack.blockID || id == Block.cloth.blockID) && quality >= 0)
					{
						if(temp < 10F)
						{
							temp = 10F;
						}
					} else if(id == Block.waterMoving.blockID || id == Block.waterStill.blockID || (id == Block.cauldron.blockID && meta > 0))
					{
						animalHostility = -1;
					} else if(id == Block.snow.blockID)
					{
						cooling += 0.05F;
					} else if(id == Block.blockSnow.blockID || id == Block.ice.blockID)
					{
						cooling += 0.1F;
					} else if(id == Block.flowerPot.blockID && (meta == 1 || meta == 2))
					{
						if(meta == 1 || meta == 2)
						{
							sanityRate = 1F;
							leaves += 1;
						} else if(meta != 0 && !(meta >= 7 && meta <= 10))
						{
							leaves += 1;
						}
					}
				}
			}
		}
		
		quality += (leaves * 0.1F);
		
		if(lightLev > 1 && !entityLiving.worldObj.provider.isHellWorld)
		{
			quality = 2F;
			
			if(isDay)
			{
				sanityRate = 2F;
			}
		} else
		{
			if(sanityRate == 0)
			{
				sanityRate = -0.1F;
			}
		}
		
		if(j > 0 && j < 256)
		{
			if(chunk.getSavedLightValue(EnumSkyBlock.Block, i & 0xf, j, k & 0xf) > 1 && sanityRate <= 0 && !entityLiving.worldObj.provider.isHellWorld)
			{
				sanityRate = 0F;
			}
		}
		
		if(entityLiving.posY > 48 && !entityLiving.worldObj.provider.isHellWorld)
		{
			quality = 2F;
		}
		
		float bTemp = biome.temperature;
		
		if(entityLiving.posY <= 16)
		{
			bTemp = 1.0F;
		}
		
		if(bTemp > 1.0F)
		{
			bTemp = 30F + ((bTemp - 1F) * 20);
		} else if(bTemp < -1.0F)
		{
			bTemp = -30F + ((bTemp + 1F) * 20);
		} else
		{
			bTemp *= 30;
		}
		
		if(biome.getEnableSnow())
		{
			bTemp -= 5F;
		}
		
		bTemp -= cooling;
		
		if(entityLiving instanceof EntityPlayer)
		{
			if(((EntityPlayer)entityLiving).isPlayerSleeping())
			{
				bTemp += 5F;
			}
		}
		
		if(entityLiving.worldObj.isRaining() && entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && biome.rainfall != 0.0F)
		{
			bTemp -= 10F;
			dropSpeed = 0.5F;
			animalHostility = -1;
		}
		
		if(!entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && isDay && !entityLiving.worldObj.isRaining())
		{
			bTemp -= 2.5F;
		}
		
		if(entityLiving.posY > 127)
		{
			bTemp -= 5F;
		}
		
		if(!isDay && bTemp > 0F)
		{
			bTemp /= 2;
		} else if(!isDay && bTemp <= 0F)
		{
			bTemp -= 10F;
		}
		
		{
			ItemStack helmet = entityLiving.getCurrentItemOrArmor(4);
			ItemStack plate = entityLiving.getCurrentItemOrArmor(3);
			ItemStack legs = entityLiving.getCurrentItemOrArmor(2);
			ItemStack boots = entityLiving.getCurrentItemOrArmor(1);
			
			float tempMultTotal = 0F;
			
			if(helmet != null)
			{
				if(EM_Settings.armorProperties.containsKey(helmet.itemID))
				{
					ArmorProperties props = EM_Settings.armorProperties.get(helmet.itemID);
					
					if(isDay)
					{
						if(entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && bTemp > 0F)
						{
							tempMultTotal += (props.sunMult - 1.0F);
							bTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							bTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						bTemp += props.nightTemp;
					}
				}
			}
			if(plate != null)
			{
				if(EM_Settings.armorProperties.containsKey(plate.itemID))
				{
					ArmorProperties props = EM_Settings.armorProperties.get(plate.itemID);
					
					if(isDay)
					{
						if(entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && bTemp > 0F)
						{
							tempMultTotal += (props.sunMult - 1.0F);
							bTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							bTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						bTemp += props.nightTemp;
					}
				}
			}
			if(legs != null)
			{
				if(EM_Settings.armorProperties.containsKey(legs.itemID))
				{
					ArmorProperties props = EM_Settings.armorProperties.get(legs.itemID);
					
					if(isDay)
					{
						if(entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && bTemp > 0F)
						{
							tempMultTotal += (props.sunMult - 1.0F);
							bTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							bTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						bTemp += props.nightTemp;
					}
				}
			}
			if(boots != null)
			{
				if(EM_Settings.armorProperties.containsKey(boots.itemID))
				{
					ArmorProperties props = EM_Settings.armorProperties.get(boots.itemID);
					
					if(isDay)
					{
						if(entityLiving.worldObj.canBlockSeeTheSky(i, j, k) && bTemp > 0F)
						{
							tempMultTotal += (props.sunMult - 1.0F);
							bTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							bTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						bTemp += props.nightTemp;
					}
				}
			}
		}
		
		if(temp > bTemp)
		{
			temp = (bTemp + temp) / 2;
		} else
		{
			temp = bTemp;
		}
		
		if((entityLiving.worldObj.getBlockId(i, j, k) == Block.waterStill.blockID || entityLiving.worldObj.getBlockId(i, j, k) == Block.waterMoving.blockID) && entityLiving.posY > 48)
		{
			if(biome.getEnableSnow())
			{
				temp -= 10F;
			}
			dropSpeed = 1.0F;
		}
		
		if(biome.biomeName == BiomeGenBase.hell.biomeName || nearLava)
		{
			riseSpeed = 0.25F;
			dehydrateBonus += 0.05F;
			if(animalHostility == 0)
			{
				animalHostility = 1;
			}
			
			if(biome.biomeName == BiomeGenBase.hell.biomeName && quality <= -0.25F)
			{
				quality = -0.25F;
			}
		}
		
		if(biome.getIntRainfall() == 0 && isDay)
		{
			dehydrateBonus += 0.05F;
			if(animalHostility == 0)
			{
				animalHostility = 1;
			}
		}
		
		if(entityLiving.worldObj.getBlockId(i, j, k) == Block.lavaStill.blockID || entityLiving.worldObj.getBlockId(i, j, k) == Block.lavaMoving.blockID)
		{
			temp = 100F;
			riseSpeed = 5.0F;
		} else if(entityLiving.isBurning())
		{
			if(temp <= 75F)
			{
				temp = 75;
			}
			riseSpeed = 1.0F;
		}
		
		data[0] = quality;
		data[1] = temp;
		data[2] = canBurn;
		data[3] = dehydrateBonus;
		data[4] = dropSpeed;
		data[5] = riseSpeed;
		data[6] = animalHostility;
		data[7] = sanityRate/10;
		return data;
	}
	
	public static void removeTracker(EnviroDataTracker tracker)
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			if(trackerList.get(i) == tracker)
			{
				trackerList.get(i).isDisabled = true;
				trackerList.remove(i);
			}
		}
	}
	
	public static void saveAndRemoveTracker(EnviroDataTracker tracker)
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			if(trackerList.get(i) == tracker)
			{
				tracker.isDisabled = true;
				NBTTagCompound tags = tracker.trackedEntity.getEntityData();
				tags.setFloat("ENVIRO_AIR", tracker.airQuality);
				tags.setFloat("ENVIRO_HYD", tracker.hydration);
				tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
				tags.setFloat("ENVIRO_SAN", tracker.sanity);
				trackerList.get(i).isDisabled = true;
				trackerList.remove(i);
			}
		}
	}
	
	public static void saveTracker(EnviroDataTracker tracker)
	{
		NBTTagCompound tags = tracker.trackedEntity.getEntityData();
		tags.setFloat("ENVIRO_AIR", tracker.airQuality);
		tags.setFloat("ENVIRO_HYD", tracker.hydration);
		tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
		tags.setFloat("ENVIRO_SAN", tracker.sanity);
	}
	
	public static void removeAllTrackers()
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			trackerList.get(i).isDisabled = true;
			trackerList.remove(i);
		}
	}
	
	public static void saveAndDeleteAllTrackers()
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			EnviroDataTracker tracker = trackerList.get(i);
			NBTTagCompound tags = tracker.trackedEntity.getEntityData();
			tags.setFloat("ENVIRO_AIR", tracker.airQuality);
			tags.setFloat("ENVIRO_HYD", tracker.hydration);
			tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
			tags.setFloat("ENVIRO_SAN", tracker.sanity);
			trackerList.get(i).isDisabled = true;
			trackerList.remove(i);
		}
	}
	
	public static void createFX(EntityLivingBase entityLiving)
	{
		float rndX = (entityLiving.getRNG().nextFloat() * entityLiving.width*2) - entityLiving.width;
		float rndY = entityLiving.getRNG().nextFloat() * entityLiving.height;
		float rndZ = (entityLiving.getRNG().nextFloat() * entityLiving.width*2) - entityLiving.width;
		EnviroDataTracker tracker = null;
		
		if(entityLiving instanceof EntityPlayer && !(entityLiving instanceof EntityPlayerMP))
		{
			tracker = EM_StatusManager.lookupPlayerTrackerFromName(((EntityPlayer)entityLiving).username);
			rndY = -rndY;
		} else
		{
			tracker = EM_StatusManager.lookupTrackerFromID(entityLiving.entityId);
		}
		
		if(tracker != null)
		{
			if(tracker.bodyTemp >= 30F)
			{
				entityLiving.worldObj.spawnParticle("dripWater", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}
			
			if(tracker.trackedEntity.isPotionActive(EnviroPotion.insanity))
			{
				entityLiving.worldObj.spawnParticle("portal", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	public static void saveAndDeleteWorldTrackers(World world)
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			EnviroDataTracker tracker = trackerList.get(i);
			if(tracker.trackedEntity.worldObj == world)
			{
				NBTTagCompound tags = tracker.trackedEntity.getEntityData();
				tags.setFloat("ENVIRO_AIR", tracker.airQuality);
				tags.setFloat("ENVIRO_HYD", tracker.hydration);
				tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
				tags.setFloat("ENVIRO_SAN", tracker.sanity);
				trackerList.get(i).isDisabled = true;
				trackerList.remove(i);
			}
		}
	}

	public static void saveAllWorldTrackers(World world)
	{
		for(int i = trackerList.size() - 1; i >= 0; i -= 1)
		{
			EnviroDataTracker tracker = trackerList.get(i);
			if(tracker.trackedEntity.worldObj == world)
			{
				NBTTagCompound tags = tracker.trackedEntity.getEntityData();
				tags.setFloat("ENVIRO_AIR", tracker.airQuality);
				tags.setFloat("ENVIRO_HYD", tracker.hydration);
				tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
				tags.setFloat("ENVIRO_SAN", tracker.sanity);
			}
		}
	}

	public static EntityPlayer findPlayer(String username)
	{
		World[] worlds = new World[3];
		
		if(EnviroMine.proxy.isClient())
		{
			if(Minecraft.getMinecraft().isIntegratedServerRunning())
			{
				worlds = MinecraftServer.getServer().worldServers;
			} else
			{
				worlds[0] = Minecraft.getMinecraft().thePlayer.worldObj;
			}
		} else
		{
			worlds = MinecraftServer.getServer().worldServers;
		}
		
		for(int i = worlds.length - 1; i >= 0; i -= 1)
		{
			if(worlds[i] == null)
			{
				continue;
			}
			EntityPlayer player = worlds[i].getPlayerEntityByName(username);
			
			if(player != null)
			{
				if(!player.isDead)
				{
					System.out.println("Found player in dimension " + worlds[i].getWorldInfo().getVanillaDimension());
					return player;
				}
			}
		}
		
		return null;
	}
}
