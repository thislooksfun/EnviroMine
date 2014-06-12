package enviromine.core.sounds;

import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class HeavyBreathing 
{
	@ForgeSubscribe
	public void onSound(SoundLoadEvent event) 
	{
		// You add them the same way as you add blocks.
		event.manager.addSound(EM_Settings.ID +":hit.ogg");
	}
}
