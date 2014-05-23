package enviromine.blocks;

import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class BlockGas extends Block implements ITileEntityProvider
{
	public Icon gasIcon;
	public BlockGas(int par1, Material par2Material)
	{
		super(par1, par2Material);
	}
	
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	public boolean canCollideCheck(int par1, boolean par2)
    {
        return false;
    }
	
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }
    
    public boolean isBlockReplaceable(World world, int x, int y, int z)
    {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public int getBlockColor()
    {
    	return 0;
    }
    
    public int getRenderBlockPass()
    {
        return 1;
    }
    
    public void updateTick(World world, int x, int y, int z, Random rand)
    {
    }
	
	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if(tile instanceof TileEntityGas && entity instanceof EntityLivingBase)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			gasTile.doAllEffects((EntityLivingBase)entity);
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityGas();
	}
	
	public Icon getIcon(int par1, int par2)
    {
		return gasIcon;
    }
	
	public void registerIcons(IconRegister par1IconRegister)
    {
		this.gasIcon = par1IconRegister.registerIcon("enviromine:block_gas");
    }
}
