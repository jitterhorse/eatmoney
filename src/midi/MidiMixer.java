package midi;

import processing.core.PApplet;
import themidibus.MidiBus;

public class MidiMixer {

	MidiBus myBusOut;

	 public MidiMixer(PApplet _parent){
		 myBusOut = new MidiBus(_parent,-1, "USB2MIDI"); 
	}
	
	
	
	public void switchCh(int ch){
		  int channel = 0;
		  int layer = 13;
		  myBusOut.sendControllerChange(channel, layer, ch);      
	  }
	 
	
	public void switchPreset(int _preset){
	      myBusOut.sendMessage(192, _preset);
	     
	}


}
