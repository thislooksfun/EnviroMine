package enviromine.trackers;

public class BlockProperties
{
	boolean ignoreMeta;
	boolean hasPhys;
	
	int id;
	int meta;
	
	int minFall;
	int maxFall;
	int supportDist;
	
	int dropID;
	int dropMeta;
	int dropNum;
	
	boolean enableTemp;
	
	float temp;
	
	public BlockProperties(int id, int meta, boolean hasPhys, boolean ignoreMeta, int minFall, int maxFall, int supportDist, int dropID, int dropMeta, int dropNum, boolean enableTemp, float temp)
	{
		this.id = id;
		this.meta = meta;
		this.hasPhys = hasPhys;
		this.ignoreMeta = ignoreMeta;
		this.minFall = minFall;
		this.maxFall = maxFall;
		this.supportDist = supportDist;
		this.dropID = dropID;
		this.dropMeta = dropMeta;
		this.dropNum = dropNum;
		this.enableTemp = enableTemp;
		this.temp = temp;
	}
}
