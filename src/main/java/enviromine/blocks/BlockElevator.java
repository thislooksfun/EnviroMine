package enviromine.blocks;

import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockElevator extends Block
{
	public BlockElevator(int par1, Material par2Material)
	{
		super(par1, par2Material);
		this.setHardness(5.0F);
		this.setStepSound(Block.soundMetalFootstep);
		this.setUnlocalizedName("enviromine.block.elevator");
		LanguageRegistry.addName(this, "Elevator");
	}
}
