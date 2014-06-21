package enviromine.world.features;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.core.EnviroMine;
import enviromine.gases.EnviroGasDictionary;
import enviromine.handlers.ObjectHandler;

public class WorldFeatureGenerator implements IWorldGenerator
{
	public static ArrayList<int[]> pendingMines = new ArrayList<int[]>();
	
	public WorldFeatureGenerator()
	{
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(world.isRemote)
		{
			return;
		}
		
		/*if(random.nextInt(1000) == 0)
		{
			
		} else */
		{
			for(int i = random.nextInt(5) + 5; i >= 0; i--)
			{
				//GenGasPocket(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
			}
		}
	}
	
	public void GenGasPocket(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		int rX = (chunkX * 16) + random.nextInt(16);
		int rY = random.nextInt(32);
		int rZ = (chunkZ * 16) + random.nextInt(16);
		
		if(world.getBlockId(rX, rY, rZ) == 0)
		{
			Block bBlock = Block.blocksList[world.getBlockId(rX, rY - 1, rZ)];
			if(rY < 16 && rY > 0)
			{
				if(bBlock != null && bBlock.blockMaterial == Material.water)
				{
					world.setBlock(rX, rY, rZ, ObjectHandler.gasBlock.blockID);
					TileEntity tile = world.getBlockTileEntity(rX, rY, rZ);
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.hydrogenSulfide.gasID, 5);
						EnviroMine.logger.log(Level.INFO, "Generation hydrogen sulfide at (" + rX + "," + rY + "," + rZ + ")");
					}
				} else if(bBlock != null && (bBlock.blockMaterial == Material.lava || bBlock.blockMaterial == Material.fire))
				{
					world.setBlock(rX, rY, rZ, ObjectHandler.gasBlock.blockID);
					TileEntity tile = world.getBlockTileEntity(rX, rY, rZ);
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.carbonMonoxide.gasID, 10);
						gasTile.addGas(EnviroGasDictionary.sulfurDioxide.gasID, 10);
						EnviroMine.logger.log(Level.INFO, "Generation carbon monoxide at (" + rX + "," + rY + "," + rZ + ")");
					}
				} else
				{
					world.setBlock(rX, rY, rZ, ObjectHandler.gasBlock.blockID);
					TileEntity tile = world.getBlockTileEntity(rX, rY, rZ);
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.sulfurDioxide.gasID, 10);
						gasTile.addGas(EnviroGasDictionary.carbonDioxide.gasID, 20);
						EnviroMine.logger.log(Level.INFO, "Generation sulfur dioxide at (" + rX + "," + rY + "," + rZ + ")");
					}
				}
			} else
			{
				world.setBlock(rX, rY, rZ, ObjectHandler.gasBlock.blockID);
				TileEntity tile = world.getBlockTileEntity(rX, rY, rZ);
				
				if(tile instanceof TileEntityGas)
				{
					TileEntityGas gasTile = (TileEntityGas)tile;
					gasTile.addGas(EnviroGasDictionary.carbonDioxide.gasID, 30);
					EnviroMine.logger.log(Level.INFO, "Generation carbon dioxide at (" + rX + "," + rY + "," + rZ + ")");
				}
			}
		}
	}
	
	public void SavePendingMines()
	{
	}
	
	public void LoadPendingMines()
	{
	}
}
