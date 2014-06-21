package enviromine.blocks.models;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelElevatorBottom extends ModelBase
{
	//fields
	ModelRenderer bottom_platform;
	ModelRenderer bottom_bars;
	
	public ModelElevatorBottom()
	{
		textureWidth = 128;
		textureHeight = 64;
		
		bottom_platform = new ModelRenderer(this, 0, 0);
		bottom_platform.addBox(0F, 0F, 0F, 16, 3, 16);
		bottom_platform.setRotationPoint(-8F, 21F, -8F);
		bottom_platform.setTextureSize(128, 64);
		bottom_platform.mirror = true;
		setRotation(bottom_platform, 0F, 0F, 0F);
		bottom_bars = new ModelRenderer(this, 64, 0);
		bottom_bars.addBox(0F, 0F, 0F, 16, 13, 16);
		bottom_bars.setRotationPoint(-8F, 8F, -8F);
		bottom_bars.setTextureSize(128, 64);
		bottom_bars.mirror = true;
		setRotation(bottom_bars, 0F, 0F, 0F);
	}
	
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		bottom_platform.render(f5);
		bottom_bars.render(f5);
	}
	
	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
	
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
	{
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
	
}
