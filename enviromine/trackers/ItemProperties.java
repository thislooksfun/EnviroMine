package enviromine.trackers;

public class ItemProperties
{
	public int id;
	public int meta;
	
	public boolean enableTemp;
	
	public float ambTemp;
	public float ambAir;
	public float ambSanity;
	public float ambHydration;
	
	public float effTemp;
	public float effAir;
	public float effSanity;
	public float effHydration;
	
	public ItemProperties(int id, int meta, boolean enableTemp, float ambTemp, float ambAir, float ambSanity, float ambHydration, float effTemp, float effAir, float effSanity, float effHydration)
	{
		this.id = id;
		this.meta = meta;
		this.enableTemp = enableTemp;
		
		this.ambTemp = ambTemp;
		this.ambAir = ambAir;
		this.ambSanity = ambSanity;
		this.ambHydration = ambHydration;
		
		this.effTemp = effTemp;
		this.effAir = effAir;
		this.effSanity = effSanity;
		this.effHydration = effHydration;
	}
}
