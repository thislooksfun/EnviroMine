package enviromine.trackers;

public class RotProperties
{
	public int id;
	public int meta;
	public int rotID;
	public int rotMeta;
	public float days = 10.0F;
	
	public RotProperties(int id, int meta, int rotID, int rotMeta, float days)
	{
		this.id = id;
		this.meta = meta;
		this.rotID = rotID;
		this.rotMeta = rotMeta;
		this.days = days;
	}
}
