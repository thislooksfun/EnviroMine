package enviromine.trackers;

public class ArmorProperties
{
	public int id;
	public float nightTemp;
	public float shadeTemp;
	public float sunTemp;
	public float nightMult;
	public float shadeMult;
	public float sunMult;
	
	public ArmorProperties(int id, float nightTemp, float shadeTemp, float sunTemp, float nightMult, float shadeMult, float sunMult)
	{
		this.id = id;
		this.nightTemp = nightTemp;
		this.shadeTemp = shadeTemp;
		this.sunTemp = sunTemp;
		this.nightMult = nightMult;
		this.shadeMult = shadeMult;
		this.sunMult = sunMult;
	}
}
