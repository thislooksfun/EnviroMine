package enviromine.trackers;

public class EntityProperties
{
	public String name;
	public boolean shouldTrack;
	public boolean dehydration;
	public boolean bodyTemp;
	public boolean airQ;
	public boolean immuneToFrost;
	public boolean immuneToHeat;
	
	public EntityProperties(String name, boolean track, boolean dehydration, boolean bodyTemp, boolean airQ, boolean immuneToFrost, boolean immuneToHeat)
	{
		this.name = name;
		this.shouldTrack = track;
		this.dehydration = dehydration;
		this.bodyTemp = bodyTemp;
		this.airQ = airQ;
		this.immuneToFrost = immuneToFrost;
		this.immuneToHeat = immuneToHeat;
	}
}
