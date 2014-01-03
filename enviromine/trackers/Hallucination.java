package enviromine.trackers;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;

public class Hallucination
{
	public EntityLivingBase falseEntity;
	public int x;
	public int y;
	public int z;
	public int time;
	public static ArrayList<Hallucination> list = new ArrayList<Hallucination>();
	
	public Hallucination(EntityLivingBase entityLiving)
	{
		Random rand = entityLiving.getRNG();
		
		x = (int)(entityLiving.posX + rand.nextInt(10) - 5);
		y = (int)(entityLiving.posY + rand.nextInt(4) - 2);
		z = (int)(entityLiving.posZ + rand.nextInt(10) - 5);
		EntityCreeper creeper = new EntityCreeper(entityLiving.worldObj);
		falseEntity = creeper;
		creeper.setPosition(x, y, z);
		if(creeper.getCanSpawnHere())
		{
			list.add(this);
			entityLiving.worldObj.spawnEntityInWorld(creeper);
			System.out.println("Spawning hallucination at " + x + ", " + y + ", " + z);
		}
	}

	public static void update()
	{
		if(list.size() >= 1)
		{
			for(int i = list.size() -1; i >= 0; i -= 1)
			{
				Hallucination subject = list.get(i);
				if(subject.time >= 60)
				{
					subject.time += 1;
					subject.falseEntity.setDead();
				} else
				{
					subject.time += 1;
				}
			}
		}
	}
}
