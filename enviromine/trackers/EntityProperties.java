package enviromine.trackers;

public class EntityProperties
{
	public String name;
	public boolean dehydration;
	public boolean bodyTemp;
	public boolean airQ;
	public boolean immuneToFrost;
	
	public EntityProperties(String name, boolean dehydration, boolean bodyTemp, boolean airQ, boolean immuneToFrost)
	{
		this.name = name;
		this.dehydration = dehydration;
		this.bodyTemp = bodyTemp;
		this.airQ = airQ;
		this.immuneToFrost = immuneToFrost;
	}
}
