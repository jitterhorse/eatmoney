package eatmoney;
import ddf.minim.*;

public class AudioTool {

	boolean isPlaying = false;
	float gain = 0.f;
	
	// for playing back
	AudioOutput out;
	AudioPlayer player;
	eatMoneyMain em;
	Minim minim;
	
	public AudioTool(eatMoneyMain _em) {
		em = _em;
		minim = new Minim(em);	
		
		out = minim.getLineOut( Minim.MONO );

	}
	
	public void playSound(String path) {
		isPlaying = true;
		player =  minim.loadFile(path + "\\sound.wav");
		player.play();
	}
	
	public int getPosition() {
		if(isPlaying == true) {
			return player.position();
		}
		else {
			return 0;
		}
	}
	
	public int getTime() {
		if(isPlaying == true) {
			return player.length();
		}
		else return 0;
	}
	
	public void stopSound() {
		if(isPlaying == true) {
			player.close();
			isPlaying = false;
		}
	}

	
	
}
