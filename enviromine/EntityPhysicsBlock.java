package enviromine;

import enviromine.handlers.EM_PhysManager;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.world.World;

public class EntityPhysicsBlock extends EntityFallingSand
{

	public EntityPhysicsBlock(World world, double x, double y, double z, int id, int meta)
	{
		super(world, x, y, z, id, meta);
		this.setIsAnvil(true);
		EM_PhysManager.schedulePhysUpdate(world, (int)x, (int)y, (int)z, false, true);
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
	}
	
	@Override
	protected void fall(float par1)
	{
		super.fall(par1);
		EM_PhysManager.schedulePhysUpdate(this.worldObj, (int)this.posX, (int)this.posY, (int)this.posZ, false, true);
	}
}
