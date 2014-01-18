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
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;

public class EnviroDataTracker
{
	public EntityLivingBase trackedEntity;
	public float airQuality;
	public float maxQuality = 100;
	
	public float bodyTemp;
	public float airTemp;
	
	public float hydration;
	
	public float sanity;
	
	public int attackDelay = 1;
	public int curAttackTime = 0;
	public boolean isDisabled = false;
	public int itemUse = 0;
	
	public int frostBiteTime = 0;
	public boolean frozenHands = false;
	public boolean frozenLegs = false;
	
	public EnviroDataTracker(EntityLivingBase entity)
	{
		trackedEntity = entity;
		airQuality = maxQuality;
		bodyTemp = 20F;
		hydration = 100F;
		sanity = 100F;
	}
	
	public void updateData()
	{
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
		
		if(airQuality >= maxQuality)
		{
			airQuality = maxQuality;
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
			if(bodyTemp > 30F)
			{
				dehydrate(0.05F);
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
		
		if(trackedEntity.getHealth() <= 2F && sanity >= 1F)
		{
			sanity -= 1F;
		} else if(trackedEntity.getHealth() <= 2F && sanity <= 1F)
		{
			sanity = 0F;
		}
		
		//Check for custom properties
		boolean enableAirQ = true;
		boolean enableBodyTemp = true;
		boolean enableHydrate = true;
		boolean enableFrostbite = true;
		if(EM_Settings.livingProperties.containsKey(trackedEntity.getClass().getSimpleName()))
		{
			EntityProperties livingProps = EM_Settings.livingProperties.get(trackedEntity.getClass().getSimpleName());
			enableHydrate = livingProps.dehydration;
			enableBodyTemp = livingProps.bodyTemp;
			enableAirQ = livingProps.airQ;
			enableFrostbite = !livingProps.immuneToFrost;
		}
		
		//Reset Disabled Values
		if(!EM_Settings.enableAirQ || !enableAirQ)
		{
			airQuality = 100F;
		}
		if(!EM_Settings.enableBodyTemp || !enableBodyTemp)
		{
			bodyTemp = 20F;
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
					
					if(bodyTemp >= 21F)
					{
						bodyTemp -= 1F;
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
			
			if(bodyTemp <= -5F && !(trackedEntity instanceof EntitySheep) && !(trackedEntity instanceof EntityWolf) && enableFrostbite)
			{
				if(bodyTemp <= -10F)
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.frostbite.id, 200, 1));
				} else
				{
					trackedEntity.addPotionEffect(new PotionEffect(EnviroPotion.frostbite.id, 200, 0));
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
		this.fixFloatinfPointErrors();
		EM_StatusManager.saveTracker(this);
	}
	
	public void fixFloatinfPointErrors()
	{
		airQuality = new BigDecimal(String.valueOf(airQuality)).setScale(2, RoundingMode.HALF_UP).floatValue();
		bodyTemp = new BigDecimal(String.valueOf(bodyTemp)).setScale(2, RoundingMode.HALF_UP).floatValue();
		hydration = new BigDecimal(String.valueOf(hydration)).setScale(2, RoundingMode.HALF_UP).floatValue();
		sanity = new BigDecimal(String.valueOf(sanity)).setScale(2, RoundingMode.HALF_UP).floatValue();
	}
	
	public static boolean isLegalType(EntityLivingBase entity)
	{
		String name = EntityList.getEntityString(entity);
		
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
		}else if(name == "WitherBoss")
		{
			return false;
		} else if(name == "EnderDragon")
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
		airQuality = maxQuality;
		bodyTemp = 20F;
		hydration = 100F;
		sanity = 100F;
	}
}
