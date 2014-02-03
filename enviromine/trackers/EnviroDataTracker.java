package enviromine.trackers;

import java.math.BigDecimal;
import java.math.RoundingMode;

import enviromine.EnviroDamageSource;
import enviromine.EnviroPotion;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.handlers.EM_StatusManager;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

public class EnviroDataTracker
{
	public EntityLivingBase trackedEntity;
	
	public float prevBodyTemp = 37F;
	public float prevHydration = 100F;
	public float prevAirQuality = 100;
	public float prevSanity = 100F;
	
	public float airQuality;
	
	public float bodyTemp;
	public float airTemp;
	
	public float hydration;
	
	public float sanity;
	
	public int attackDelay = 1;
	public int curAttackTime = 0;
	public boolean isDisabled = false;
	public int itemUse = 0;
	
	public int frostbiteLevel = 0;
	
	public boolean brokenLeg = false;
	public boolean brokenArm = false;
	public boolean bleedingOut = false;
	
	public int timeBelow10 = 0;
	
	public int updateTimer = 0;
	
	public EnviroDataTracker(EntityLivingBase entity)
	{
		trackedEntity = entity;
		airQuality = 100F;
		bodyTemp = 37F;
		hydration = 100F;
		sanity = 100F;
	}
	
	public void updateData()
	{
		prevBodyTemp = bodyTemp;
		prevAirQuality = airQuality;
		prevHydration = hydration;
		prevSanity = sanity;
		
		updateTimer = 0;
		
		if(trackedEntity == null)
		{
			EM_StatusManager.removeTracker(this);
			return;
		}
		
		if(trackedEntity.isDead)
		{
			if(trackedEntity instanceof EntityPlayer)
			{
				EntityPlayer player = EM_StatusManager.findPlayer(((EntityPlayer)trackedEntity).username);
				
				if(player == null)
				{
					EM_StatusManager.saveAndRemoveTracker(this);
				} else
				{
					trackedEntity = player;
					this.loadNBTTags();
				}
			} else
			{
				EM_StatusManager.removeTracker(this);
				return;
			}
		}
		
		if(!(trackedEntity instanceof EntityPlayer) && !EM_Settings.trackNonPlayer_actual)
		{
			EM_StatusManager.saveAndRemoveTracker(this);
		}
		
		int i = MathHelper.floor_double(trackedEntity.posX);
		int k = MathHelper.floor_double(trackedEntity.posZ);
		
		if(!trackedEntity.worldObj.getChunkFromBlockCoords(i, k).isChunkLoaded)
		{
			return;
		}
		
		float[] enviroData = EM_StatusManager.getSurroundingData(trackedEntity, 5);
		
		// Air checks
		airQuality += enviroData[0];
		
		if(airQuality <= 0F)
		{
			airQuality = 0;
		}
		
		if(airQuality >= 100F)
		{
			airQuality = 100F;
		}
		
		// Temperature checks
		airTemp = enviroData[1];
		float tnm = enviroData[4];
		float tpm = enviroData[5];
		
		if(bodyTemp - airTemp > 0)
		{
			if(bodyTemp - airTemp >= tnm)
			{
				bodyTemp -= tnm;
			} else
			{
				bodyTemp = airTemp;
			}
		} else if(bodyTemp - airTemp < 0)
		{
			
			if(bodyTemp - airTemp <= -tpm)
			{
				bodyTemp += tpm;
			} else
			{
				bodyTemp = airTemp;
			}
		}
		
		// Hydration checks
		if(hydration > 0F && (enviroData[6] == 1 || !(trackedEntity instanceof EntityAnimal)))
		{
			if(bodyTemp >= 38.01F)
			{
				dehydrate(0.05F);
				
				if(hydration >= 50.01F)
				{
					bodyTemp -= 0.01F;
				}
			}
			
			dehydrate(0.05F + enviroData[3]);
		} else if(enviroData[6] == -1 && trackedEntity instanceof EntityAnimal)
		{
			hydrate(0.05F);
		} else if(hydration <= 0F)
		{
			hydration = 0;
		}
		
		// Sanity checks
		if(sanity < 0F)
		{
			sanity = 0F;
		}
		
		if(enviroData[7] < 0F)
		{
			if(sanity + enviroData[7] >= 0F)
			{
				sanity += enviroData[7];
			} else
			{
				sanity = 0F;
			}
		} else if(enviroData[7] > 0F)
		{
			if(sanity + enviroData[7] <= 100F)
			{
				sanity += enviroData[7];
			} else
			{
				sanity = 100F;
			}
		}
		
		if((trackedEntity.getHealth() <= 2F || bodyTemp <= 32F || bodyTemp >= 41F) && sanity >= 1F)
		{
			sanity -= 0.1F;
		} else if((trackedEntity.getHealth() <= 2F || bodyTemp <= 32F || bodyTemp >= 41F) && sanity <= 1F)
		{
			sanity = 0F;
		}
		
		if(bodyTemp <= 10F)
		{
			timeBelow10 += 1;
		} else
		{
			timeBelow10 = 0;
		}
		
		//Check for custom properties
		boolean enableAirQ = true;
		boolean enableBodyTemp = true;
		boolean enableHydrate = true;
		boolean enableFrostbite = true;
		boolean enableHeat = true;
		if(EntityList.getEntityString(trackedEntity) != null)
		{
			if(EM_Settings.livingProperties.containsKey(EntityList.getEntityString(trackedEntity).toLowerCase()))
			{
				EntityProperties livingProps = EM_Settings.livingProperties.get(EntityList.getEntityString(trackedEntity).toLowerCase());
				enableHydrate = livingProps.dehydration;
				enableBodyTemp = livingProps.bodyTemp;
				enableAirQ = livingProps.airQ;
				enableFrostbite = !livingProps.immuneToFrost;
				enableHeat = !livingProps.immuneToHeat;
			} else if((trackedEntity instanceof EntitySheep) || (trackedEntity instanceof EntityWolf))
			{
				enableFrostbite = false;
			} else if(trackedEntity instanceof EntityChicken)
			{
				enableHeat = false;
			}
		}
		
		//Reset Disabled Values
		if(!EM_Settings.enableAirQ || !enableAirQ)
		{
			airQuality = 100F;
		}
		if(!EM_Settings.enableBodyTemp || !enableBodyTemp)
		{
			bodyTemp = 37F;
		}
		if(!EM_Settings.enableHydrate || !enableHydrate)
		{
			hydration = 100F;
		}
		if(!EM_Settings.enableSanity || !(trackedEntity instanceof EntityPlayer))
		{
			sanity = 100F;
		}
		
		// Camel Pack Stuff
		ItemStack plate = trackedEntity.getCurrentItemOrArmor(3);
		
		if(plate != null)
		{
			if(plate.itemID == EnviroMine.camelPack.itemID)
			{
				if(plate.getItemDamage() < plate.getMaxDamage() && hydration <= 99F)
				{
					plate.setItemDamage(plate.getItemDamage() + 1);
					hydration += 1F;
					
					if(bodyTemp >= 37.1F)
					{
						bodyTemp -= 0.1F;
					}
				}
			}
		}
		
		// Fix floating point errors
		this.fixFloatinfPointErrors();
		
		// Apply side effects
		if(curAttackTime >= attackDelay)
		{
			if(airQuality <= 0)
			{
				trackedEntity.attackEntityFrom(EnviroDamageSource.suffocate, 2.0F);
			}
			
			if(bodyTemp >= 39F && enableHeat && (enviroData[6] == 1 || !(trackedEntity instanceof EntityAnimal)))
			{
				if(bodyTemp >= 43F)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200, 2));
				} else if(bodyTemp >= 41F)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200, 1));
				} else
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.heatstroke.id, 200, 0));
				}
			}
			
			if(bodyTemp <= 35F && enableFrostbite && (enviroData[6] == 1 || !(trackedEntity instanceof EntityAnimal)))
			{
				if(bodyTemp <= 30F)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.hypothermia.id, 200, 2));
				} else if(bodyTemp <= 32F)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.hypothermia.id, 200, 1));
				} else
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.hypothermia.id, 200, 0));
				}
			}
			
			if(((airTemp <= 10F && timeBelow10 >= 60 && enableFrostbite) || frostbiteLevel >= 1))
			{
				if(timeBelow10 >= 120 || frostbiteLevel >= 2)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.frostbite.id, 200, 1));
					
					if(frostbiteLevel <= 2)
					{
						frostbiteLevel = 2;
					}
				} else
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.frostbite.id, 200, 0));
					
					if(frostbiteLevel <= 1)
					{
						frostbiteLevel = 1;
					}
				}
			}
			
			if(bodyTemp >= 75F || (enviroData[2] == 1 && bodyTemp >= 50))
			{
				trackedEntity.setFire(10);
			}
			
			if(hydration <= 1.0F)
			{
				trackedEntity.attackEntityFrom(EnviroDamageSource.dehydrate, 2.0F);
			}
			
			if(sanity <= 10F)
			{
				trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.insanity.id, 600, 2));
			} else if(sanity <= 25F)
			{
				trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.insanity.id, 600, 1));
			} else if(sanity <= 50F)
			{
				trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.insanity.id, 600, 0));
			}
			
			curAttackTime = 0;
		} else
		{
			curAttackTime += 1;
		}
		
		EnviroPotion.checkAndApplyEffects(trackedEntity);
		
		if(trackedEntity instanceof EntityPlayer)
		{
			if(((EntityPlayer)trackedEntity).capabilities.isCreativeMode)
			{
				bodyTemp = prevBodyTemp;
				airQuality = prevAirQuality;
				hydration = prevHydration;
				sanity = prevSanity;
			}
		}
		
		this.fixFloatinfPointErrors();
		EM_StatusManager.saveTracker(this);
	}
	
	public void fixFloatinfPointErrors()
	{
		airQuality = new BigDecimal(String.valueOf(airQuality)).setScale(2, RoundingMode.HALF_UP).floatValue();
		bodyTemp = new BigDecimal(String.valueOf(bodyTemp)).setScale(3, RoundingMode.HALF_UP).floatValue();
		airTemp = new BigDecimal(String.valueOf(airTemp)).setScale(3, RoundingMode.HALF_UP).floatValue();
		hydration = new BigDecimal(String.valueOf(hydration)).setScale(2, RoundingMode.HALF_UP).floatValue();
		sanity = new BigDecimal(String.valueOf(sanity)).setScale(2, RoundingMode.HALF_UP).floatValue();
	}
	
	public static boolean isLegalType(EntityLivingBase entity)
	{
		String name = EntityList.getEntityString(entity);
		
		if(EM_Settings.livingProperties.containsKey(EntityList.getEntityString(entity)))
		{
			return EM_Settings.livingProperties.get(EntityList.getEntityString(entity)).shouldTrack;
		}
		
		if(entity.isEntityUndead() || entity instanceof EntityMob)
		{
			return false;
		} else if(name == "Enderman")
		{
			return false;
		} else if(name == "Villager")
		{
			return false;
		} else if(name == "Slime")
		{
			return false;
		} else if(name == "Ghast")
		{
			return false;
		} else if(name == "Squid")
		{
			return false;
		} else if(name == "Blaze")
		{
			return false;
		} else if(name == "LavaSlime")
		{
			return false;
		} else if(name == "SnowMan")
		{
			return false;
		} else if(name == "MushroomCow")
		{
			return false;
		} else if(name == "WitherBoss")
		{
			return false;
		} else if(name == "EnderDragon")
		{
			return false;
		} else if(name == "VillagerGolem")
		{
			return false;
		} else
		{
			if(entity instanceof EntityPlayer)
			{
				EnviroDataTracker tracker = EM_StatusManager.lookupTracker(entity);
				
				if(tracker != null)
				{
					return false;
				} else
				{
					return true;
				}
			} else
			{
				return true;
			}
		}
	}
	
	public void hydrate(float amount)
	{
		if(hydration >= 100F - amount)
		{
			hydration = 100.0F;
		} else
		{
			hydration += amount;
		}
		
		this.fixFloatinfPointErrors();
		
		if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
		{
			EM_StatusManager.syncMultiplayerTracker(this);
		}
	}
	
	public void dehydrate(float amount)
	{
		if(hydration >= amount)
		{
			hydration -= amount;
		} else
		{
			hydration = 0F;
		}
		
		this.fixFloatinfPointErrors();
		
		if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
		{
			EM_StatusManager.syncMultiplayerTracker(this);
		}
	}
	
	public void loadNBTTags()
	{
		NBTTagCompound tags = trackedEntity.getEntityData();
		
		if(tags.hasKey("ENVIRO_AIR"))
		{
			airQuality = tags.getFloat("ENVIRO_AIR");
		}
		if(tags.hasKey("ENVIRO_HYD"))
		{
			hydration = tags.getFloat("ENVIRO_HYD");
		}
		if(tags.hasKey("ENVIRO_TMP"))
		{
			bodyTemp = tags.getFloat("ENVIRO_TMP");
		}
		if(tags.hasKey("ENVIRO_SAN"))
		{
			sanity = tags.getFloat("ENVIRO_SAN");
		}
	}
	
	public int getAndIncrementItemUse()
	{
		itemUse += 1;
		return itemUse;
	}
	
	public void resetItemUse()
	{
		itemUse = 0;
	}
	
	public void resetData()
	{
		airQuality = 100F;
		bodyTemp = 37F;
		hydration = 100F;
		sanity = 100F;
	}
}
