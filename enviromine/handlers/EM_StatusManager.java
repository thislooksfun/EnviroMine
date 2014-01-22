package enviromine.handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;

public class EM_StatusManager
{
	public static HashMap<String,EnviroDataTracker> trackerList = new HashMap<String,EnviroDataTracker>();
	public static int ticksSinceUpdate = 0;
	public static int updateInterval = 15;
	
	public static void addToManager(EnviroDataTracker tracker)
	{
		if(tracker.trackedEntity instanceof EntityPlayer)
		{
			trackerList.put("" + tracker.trackedEntity.getEntityName(), tracker);
		} else
		{
			trackerList.put("" + tracker.trackedEntity.entityId, tracker);
		}
	}
	
	public static void updateTracker(EnviroDataTracker tracker)
	{
		if(tracker == null)
		{
			return;
		}
		
		if(EnviroMine.proxy.isClient() && Minecraft.getMinecraft().isIntegratedServerRunning())
		{
			if(Minecraft.getMinecraft().getIntegratedServer().getServerListeningThread().isGamePaused() && !EnviroMine.proxy.isOpenToLAN())
			{
				return;
			}
		}
		
		tracker.updateTimer += 1;
		
		if(tracker.updateTimer >= 30)
		{
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
		if(entity instanceof EntityPlayer)
		{
			if(trackerList.containsKey("" + entity.getEntityName()))
			{
				return trackerList.get("" + entity.getEntityName());
			} else
			{
				return null;
			}
		} else
		{
			if(trackerList.containsKey("" + entity.entityId))
			{
				return trackerList.get("" + entity.entityId);
			} else
			{
				return null;
			}
		}
	}
	
	public static EnviroDataTracker lookupTrackerFromUsername(String username)
	{
		return trackerList.get(username);
	}
	
	public static float[] getSurroundingData(EntityLivingBase entityLiving, int range)
	{
		float[] data = new float[8];
		
		float sanityRate = 0;
		
		float quality = 0;
		int leaves = 0;
		
		float dropSpeed = 0.002F;
		float riseSpeed = 0.002F;
		
		float temp = -999F;
		float cooling = 0;
		float dehydrateBonus = 0.0F;
		int unused = 0;
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
					
					if(EM_Settings.blockProperties.containsKey("" + id + "," + meta) || EM_Settings.blockProperties.containsKey("" + id))
					{
						if(EM_Settings.blockProperties.containsKey("" + id + "," + meta))
						{
							blockProps = EM_Settings.blockProperties.get("" + id + "," + meta);
						} else
						{
							blockProps = EM_Settings.blockProperties.get("" + id);
						}
						
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
						if(temp <= blockProps.temp && blockProps.enableTemp)
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
						if(temp < 200F)
						{
							temp = 200F;
						}
						nearLava = true;
					} else if(id == Block.fire.blockID)
					{
						if(quality > -0.5F)
						{
							quality = -0.5F;
						}
						if(temp < 100F)
						{
							temp = 100F;
						}
					} else if((id == Block.torchWood.blockID || id == Block.furnaceBurning.blockID) && quality > -0.25F)
					{
						if(quality > -0.25F)
						{
							quality = -0.25F;
						}
						if(temp < 50F)
						{
							temp = 50F;
						}
					} else if(id == Block.leaves.blockID || id == Block.plantYellow.blockID || id == Block.plantRed.blockID || id == Block.waterlily.blockID || id == Block.grass.blockID)
					{
						if(id == Block.plantRed.blockID || id == Block.plantYellow.blockID)
						{
							sanityRate = 1F;
						}
						leaves += 1;
					} else if(id == Block.netherrack.blockID && quality >= 0)
					{
						if(temp < 25F)
						{
							temp = 25F;
						}
					} else if(id == Block.waterMoving.blockID || id == Block.waterStill.blockID || (id == Block.cauldron.blockID && meta > 0))
					{
						animalHostility = -1;
					} else if(id == Block.snow.blockID)
					{
						cooling += 0.01F;
					} else if(id == Block.blockSnow.blockID || id == Block.ice.blockID)
					{
						cooling += 0.05F;
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
		
		float bTemp = biome.temperature * 2.25F;
		
		if(bTemp > 1.5F)
		{
			bTemp = 30F + ((bTemp - 1F) * 10);
		} else if(bTemp < -1.5F)
		{
			bTemp = -30F + ((bTemp + 1F) * 10);
		} else
		{
			bTemp *= 20;
		}
		
		if(entityLiving.posY <= 48)
		{
			if(bTemp < 20F)
			{
				bTemp += (50 * (1 - (entityLiving.posY/48)));
			} else
			{
				bTemp += (20 * (1 - (entityLiving.posY/48)));
			}
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
			dropSpeed = 0.05F;
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
			if(biome.rainfall == 0.0F)
			{
				bTemp /= 9;
			} else
			{
				bTemp /= 2;
			}
		} else if(!isDay && bTemp <= 0F)
		{
			bTemp -= 10F;
		}
		
		if((entityLiving.worldObj.getBlockId(i, j, k) == Block.waterStill.blockID || entityLiving.worldObj.getBlockId(i, j, k) == Block.waterMoving.blockID) && entityLiving.posY > 48)
		{
			if(biome.getEnableSnow())
			{
				temp -= 10F;
			}
			dropSpeed = 0.1F;
		}
		
		List mobList = entityLiving.worldObj.getEntitiesWithinAABBExcludingEntity(entityLiving, AxisAlignedBB.getBoundingBox(entityLiving.posX - 2, entityLiving.posY - 2, entityLiving.posZ - 2, entityLiving.posX + 3, entityLiving.posY + 3, entityLiving.posZ + 3));
		
		Iterator iterator = mobList.iterator();
		
		float avgEntityTemp = 0.0F;
		int validEntities = 0;
		
		while(iterator.hasNext())
		{
			Entity mob = (Entity)iterator.next();
			
			if(!(mob instanceof EntityLivingBase))
			{
				continue;
			}
			EnviroDataTracker mobTrack = lookupTracker((EntityLivingBase)mob);
			
			if(mobTrack != null)
			{
				avgEntityTemp += mobTrack.bodyTemp;
				validEntities += 1;
			} else
			{
				continue;
			}
		}
		
		if(validEntities > 0)
		{
			avgEntityTemp /= validEntities;
			
			if(bTemp < avgEntityTemp)
			{
				bTemp = (bTemp + avgEntityTemp)/2;
			}
		}
		
		{
			ItemStack helmet = entityLiving.getCurrentItemOrArmor(4);
			ItemStack plate = entityLiving.getCurrentItemOrArmor(3);
			ItemStack legs = entityLiving.getCurrentItemOrArmor(2);
			ItemStack boots = entityLiving.getCurrentItemOrArmor(1);
			
			float tempMultTotal = 0F;
			float addTemp = 0F;
			
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
							addTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							addTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						addTemp += props.nightTemp;
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
							addTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							addTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						addTemp += props.nightTemp;
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
							addTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							addTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						addTemp += props.nightTemp;
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
							addTemp += props.sunTemp;
						} else
						{
							tempMultTotal += (props.shadeMult - 1.0F);
							addTemp += props.shadeTemp;
						}
					} else
					{
						tempMultTotal += (props.nightMult - 1.0F);
						addTemp += props.nightTemp;
					}
				}
			}
			
			bTemp *= (1F + tempMultTotal);
			bTemp += addTemp;
		}
		
		if(temp > bTemp)
		{
			temp = (bTemp + temp) / 2;
		} else
		{
			temp = bTemp;
		}
		
		if(biome.biomeName == BiomeGenBase.hell.biomeName || nearLava || biome.rainfall == 0.0F)
		{
			riseSpeed = 0.005F;
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
			temp = 200F;
			riseSpeed = 1.0F;
		} else if(entityLiving.isBurning())
		{
			if(temp <= 75F)
			{
				temp = 75;
			}
			riseSpeed = 0.5F;
		}
		
		data[0] = quality;
		data[1] = temp;
		data[2] = unused;
		data[3] = dehydrateBonus;
		data[4] = dropSpeed;
		data[5] = riseSpeed;
		data[6] = animalHostility;
		data[7] = sanityRate/10;
		return data;
	}
	
	public static void removeTracker(EnviroDataTracker tracker)
	{
		if(trackerList.containsValue(tracker))
		{
			tracker.isDisabled = true;
			if(tracker.trackedEntity instanceof EntityPlayer)
			{
				trackerList.remove(((EntityPlayer)tracker.trackedEntity).username);
			} else
			{
				trackerList.remove(tracker.trackedEntity.entityId);
			}
		}
	}
	
	public static void saveAndRemoveTracker(EnviroDataTracker tracker)
	{
		if(trackerList.containsValue(tracker))
		{
			tracker.isDisabled = true;
			NBTTagCompound tags = tracker.trackedEntity.getEntityData();
			tags.setFloat("ENVIRO_AIR", tracker.airQuality);
			tags.setFloat("ENVIRO_HYD", tracker.hydration);
			tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
			tags.setFloat("ENVIRO_SAN", tracker.sanity);
			if(tracker.trackedEntity instanceof EntityPlayer)
			{
				trackerList.remove(((EntityPlayer)tracker.trackedEntity).username);
			} else
			{
				trackerList.remove(tracker.trackedEntity.entityId);
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
		Iterator<EnviroDataTracker> iterator = trackerList.values().iterator();
		while(iterator.hasNext())
		{
			EnviroDataTracker tracker = iterator.next();
			tracker.isDisabled = true;
		}
		
		trackerList.clear();
	}
	
	public static void saveAndDeleteAllTrackers()
	{
		Iterator<EnviroDataTracker> iterator = trackerList.values().iterator();
		while(iterator.hasNext())
		{
			EnviroDataTracker tracker = iterator.next();
			NBTTagCompound tags = tracker.trackedEntity.getEntityData();
			tags.setFloat("ENVIRO_AIR", tracker.airQuality);
			tags.setFloat("ENVIRO_HYD", tracker.hydration);
			tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
			tags.setFloat("ENVIRO_SAN", tracker.sanity);
			tracker.isDisabled = true;
		}
		trackerList.clear();
	}

	public static void saveAndDeleteWorldTrackers(World world)
	{
		HashMap<String,EnviroDataTracker> tempList = new HashMap<String,EnviroDataTracker>(trackerList);
		Iterator<EnviroDataTracker> iterator = tempList.values().iterator();
		while(iterator.hasNext())
		{
			EnviroDataTracker tracker = iterator.next();
			
			if(tracker.trackedEntity.worldObj == world)
			{
				NBTTagCompound tags = tracker.trackedEntity.getEntityData();
				tags.setFloat("ENVIRO_AIR", tracker.airQuality);
				tags.setFloat("ENVIRO_HYD", tracker.hydration);
				tags.setFloat("ENVIRO_TMP", tracker.bodyTemp);
				tags.setFloat("ENVIRO_SAN", tracker.sanity);
				tracker.isDisabled = true;
				if(tracker.trackedEntity instanceof EntityPlayer)
				{
					trackerList.remove(((EntityPlayer)tracker.trackedEntity).username);
				} else
				{
					trackerList.remove(tracker.trackedEntity.entityId);
				}
			}
		}
	}

	public static void saveAllWorldTrackers(World world)
	{
		HashMap<String,EnviroDataTracker> tempList = new HashMap<String,EnviroDataTracker>(trackerList);
		Iterator<EnviroDataTracker> iterator = tempList.values().iterator();
		while(iterator.hasNext())
		{
			EnviroDataTracker tracker = iterator.next();
			
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
					return player;
				}
			}
		}
		
		return null;
	}
	
	public static void createFX(EntityLivingBase entityLiving)
	{
		float rndX = (entityLiving.getRNG().nextFloat() * entityLiving.width*2) - entityLiving.width;
		float rndY = entityLiving.getRNG().nextFloat() * entityLiving.height;
		float rndZ = (entityLiving.getRNG().nextFloat() * entityLiving.width*2) - entityLiving.width;
		EnviroDataTracker tracker = lookupTracker(entityLiving);
		
		if(entityLiving instanceof EntityPlayer && !(entityLiving instanceof EntityPlayerMP))
		{
			rndY = -rndY;
		}
		
		if(tracker != null)
		{
			if(tracker.bodyTemp >= 38F)
			{
				entityLiving.worldObj.spawnParticle("dripWater", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}
			
			if(tracker.trackedEntity.isPotionActive(EnviroPotion.insanity))
			{
				entityLiving.worldObj.spawnParticle("portal", entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 0.0D, 0.0D, 0.0D);
			}
		}
	}
}
