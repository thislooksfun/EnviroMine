package enviromine.blocks;

import java.awt.Color;
import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import enviromine.EnviroUtils;
import enviromine.handlers.ObjectHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
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
	
	public void onBlockAdded(World par1World, int par2, int par3, int par4)
	{
		super.onBlockAdded(par1World, par2, par3, par4);
		
		if(par1World.getBlockId(par2, par3, par4) == this.blockID)
		{
			par1World.scheduleBlockUpdate(par2, par3, par4, this.blockID, this.tickRate(par1World));
		}
	}
	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return gasTile.color.getRGB();
		} else
		{
			return Color.WHITE.getRGB();
		}
	}
	
	public float getOpacity(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			return ((TileEntityGas)tile).opacity;
		} else
		{
			return 1F;
		}
	}
	
	public void swtichIgnitionState(World world, int i, int j, int k)
	{
        TileEntity tile = world.getBlockTileEntity(i, j, k);
        
        if(this.blockID == ObjectHandler.gasBlock.blockID)
        {
            world.setBlock(i, j, k, ObjectHandler.fireGasBlock.blockID);
        } else
        {
            world.setBlock(i, j, k, ObjectHandler.gasBlock.blockID);
        }
        
        if (tile != null)
        {
            tile.validate();
            world.setBlockTileEntity(i, j, k, tile);
        }
	}
	
	public int tickRate(World world)
	{
		if(this.blockID  == ObjectHandler.fireGasBlock.blockID)
		{
			return 5;
		} else
		{
			return 10;
		}
	}
	
	@Override
	public int getRenderColor(int meta)
	{
		return 16777215;
	}
	
	public int getRenderType()
	{
		return ObjectHandler.renderGasID;
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
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int i, int j, int k, int side)
	{
		double yMax = this.getMaxY(blockAccess, i, j, k);
		double yMin = this.getMinY(blockAccess, i, j, k);
		float opacity = this.getOpacity(blockAccess, i, j, k);
		
		if(opacity <= 0.1F)
		{
			return false;
		}
		
		int[] sideCoord = EnviroUtils.getAdjacentBlockCoordsFromSide(i, j, k, side);
		if(blockAccess.getBlockId(sideCoord[0], sideCoord[1], sideCoord[2]) == this.blockID)
		{
			double sideYMax = this.getMaxY(blockAccess, sideCoord[0], sideCoord[1], sideCoord[2]);
			double sideYMin = this.getMinY(blockAccess, sideCoord[0], sideCoord[1], sideCoord[2]);
			
			if(this.getOpacity(blockAccess, sideCoord[0], sideCoord[1], sideCoord[2]) <= 0.1F)
			{
				return true;
			} else if(side > 1) // Sides
			{
				
				if(sideYMin > yMin || sideYMax < yMax)
				{
					return true;
				} else
				{
					return false;
				}
			} else if(side == 0) // Bottom
			{
				if(sideYMax != 1.0F || yMin != 0.0F)
				{
					return true;
				} else
				{
					return false;
				}
			} else if(side == 1) // Top
			{
				if(yMax != 1.0F || sideYMin != 0.0F)
				{
					return true;
				} else
				{
					return false;
				}
			} else
			{
				return true;
			}
		} else
		{
			if(side == 0 && yMin != 0.0F)
			{
				return true;
			} else if(side == 1 && yMax != 1.0F)
			{
				return true;
			} else
			{
				return !blockAccess.isBlockOpaqueCube(sideCoord[0], sideCoord[1], sideCoord[2]);
			}
		}
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
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		
		if(tile == null || !(tile instanceof TileEntityGas))
		{
			return;
		} else
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			
			if(gasTile.spreadGas())
			{
				world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate(world));
			}
			
			if(gasTile.gases.size() == 0)
			{
				world.setBlockToAir(x, y, z);
			}
		}
	}
	
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockID)
	{
		world.scheduleBlockUpdate(x, y, z, this.blockID, this.tickRate(world));
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
		TileEntityGas tile = new TileEntityGas(world);
		return tile;
	}
	
	public Icon getIcon(int par1, int par2)
	{
		return gasIcon;
	}
	
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.gasIcon = par1IconRegister.registerIcon("enviromine:block_gas");
	}
	
	/**
	 * Return whether this block can drop from an explosion.
	 */
	public boolean canDropFromExplosion(Explosion par1Explosion)
	{
		return false;
	}
	
	public double getMinY(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);
		
		if(tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return (double)gasTile.yMin;
		} else
		{
			return 0D;
		}
	}
	
	public double getMaxY(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);
		
		if(tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return (double)gasTile.yMax;
		} else
		{
			return 1D;
		}
	}
	
}
