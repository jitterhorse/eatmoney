package butler;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

import javax.swing.plaf.synth.SynthSpinnerUI;

import eatmoney.eatMoneyMain;

public class readData{
  
  public static enum State{
	  none,
	  mix1,
	  mix2,
	  inmix
  }

  State state = State.none;
  eatMoneyMain em;
  private DataInputStream[] in = new DataInputStream[2];
  private String[] path = new String[2];
  private int[] frame = {0,0};
  private int[] maxCount = {0,0};
  public int[] isPlaying = {0,0};
  private int lastNew = 0;
  
  boolean shownoise = false;
  float transtitionSpeed = 0.15f;
  
  AudioTool [] audio;
  
  int idlestate = 189;
  
  float visible = 0.f; //mix in and out
  float transition = 0.f; // 0.0=idle <-> 1.0=action
  
  public float [] deformMatrix = new float[8];
  
  Butler butler;
  
  //init class
  public readData( eatMoneyMain _em, Butler _butler) {
	this.em = _em;
	this.butler = _butler;
	audio = new AudioTool[2];
	audio[0] = new AudioTool(em);
	audio[1] = new AudioTool(em);
	//openTake(idlestate);
  }
  
  public void initButler() {
	  openTake(idlestate);
  }
  
  public void stopButler() {
	  audio[0].stopSound();
	  audio[1].stopSound();
  }
 
  
  public void openTake(int p) {
	
	int delay = butler.butlerDelays.get(p).intValue();
	  
	int target = 0;  
	if(this.state == State.mix1) target = 1;
	else if(this.state == State.inmix) {
		target = Math.abs(lastNew-1);
		audio[target].stopSound();
		isPlaying[target] = 0;
	}
	lastNew = target;
		
	frame[target] = 0;
	maxCount[target] = 0;
    this.path[target] = em.ppath + "\\data\\recording_" + p;
    try{ 
	    Stream<Path> files = Files.list(Paths.get(this.path[target]));
	    maxCount[target] = (int)files.count()-1; //one is soundfile 
	    files.close();
	    this.audio[target].playSound(this.path[target],delay);

	    isPlaying[target] =  1;
	    if(isPlaying[0] == 1 && isPlaying[1] == 1) this.state = State.inmix;  
	    else if(isPlaying[0] == 1) this.state = State.mix1;
	    else if(isPlaying[1] == 1) this.state = State.mix2;
	    else if(isPlaying[0] == 0 && isPlaying[1] == 0) this.state = State.none; 

     }
     catch (IOException e) {
    	e.printStackTrace();
    }
    
  }
  
  int[] readFrame(){  
	int[] savedData = new int[868352/4];  	  
	
	//make the transition  
	if(this.state == State.inmix) {
		float direction = 1.f;
		if(lastNew == 0) direction = -1.f;
		transition += transtitionSpeed * direction;
		if(transition >= 1.) {
			transition = 1.f;
			this.audio[0].stopSound();
			this.state = State.mix2;
			isPlaying[0]= 0;
		}
		else if((transition <= 0.)) {
			transition = 0.f;
			this.audio[1].stopSound();
			this.state = State.mix1;
			isPlaying[1]= 0;
		}
	}

	//check witch player is playing
	for(int i = 0; i < isPlaying.length; i++) {
		if(isPlaying[i] == 1) {
			//playhead position
			int pos = this.audio[i].getPosition();
			int leng = this.audio[i].getTime();
			frame[i] = (int)(((float)pos/(float)leng)* (float)maxCount[i]);
			if(frame[i] >= maxCount[i]*0.95 && state != State.inmix) {
				System.out.println("reset to idle state");
				this.openTake(idlestate);
			}
			
			//if end of file = stopit
		    if(frame[i] >= maxCount[i]) {
		    	System.out.println("stop playback " + i);
		    	this.audio[i].stopSound();
		    	frame[i] = maxCount[i]-1;
		    	isPlaying[i] = 0;
		    }
		    
			try {
				in[i] = new DataInputStream(new FileInputStream(this.path[i]+"\\file_" + frame[i] + ".txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}	
	}
	
	//if only one is playing
	if(this.state == State.mix1 || this.state == State.mix2 ) {
		int target = 0;
		if(this.state == State.mix2) target = 1;
		
		byte[] buf = new byte[868352];
	    try {
			in[target].read(buf,0,868352);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    for(int i = 0; i < buf.length/4;i++){
	    	savedData[i] = buf[4 * i] << 24 | (buf[4 * i + 1] & 0xFF) << 16 | (buf[4 * i + 2] & 0xFF) << 8 | (buf[4 * i + 3] & 0xFF) ;
	    	if(shownoise == true) savedData[i] *= this.getNoise(i)+1;
	    }  
	}
	//if both are playing = inTransition
	else if(this.state == State.inmix) {
		byte[] buf = new byte[868352];
		byte[] buf2 = new byte[868352];
		
	    try {
			in[0].read(buf,0,868352);
			in[1].read(buf2,0,868352);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    for(int i = 0; i < buf.length/4;i++){
	    	int val1 = buf[4 * i] << 24 | (buf[4 * i + 1] & 0xFF) << 16 | (buf[4 * i + 2] & 0xFF) << 8 | (buf[4 * i + 3] & 0xFF) ;
	    	int val2 = buf2[4 * i] << 24 | (buf2[4 * i + 1] & 0xFF) << 16 | (buf2[4 * i + 2] & 0xFF) << 8 | (buf2[4 * i + 3] & 0xFF) ;
	    	//int valout1 = val1;
	    	//int valout2 = val2;
	    	//if(!(val1 < em.maxRange && val1 > em.minRange) && (val2 < em.maxRange && val2 > em.minRange)) valout1 = val2;
	    	//if((val1 < em.maxRange && val1 > em.minRange) && !(val2 < em.maxRange && val2 > em.minRange)) valout2 = val1;
	    	savedData[i] = (int)em.lerp(val1,val2,transition);
	    	if(shownoise == true) {
	    		savedData[i] *= this.getNoise(i)+1;
	    	}
	    	
	    	
	    }  
	}


   return savedData;
  }
  
  public float getNoise(int i) {
	  int row = i / 424;
	  int col = i / 512;
	  
	  int block = (row/20) + (col/20) ;
	  return em.map(em.noise(block*0.001f*em.frameCount),0,1,-0.1f,0.1f);
  }
  
  public void toggleNoise() {
	  shownoise = !shownoise; 
	  System.out.println("noise: " + shownoise);
  }
  
  
  
}