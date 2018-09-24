package gamePTZ;

import java.util.List;

import org.gamecontrolplus.ControlButton;
import org.gamecontrolplus.ControlDevice;
import org.gamecontrolplus.ControlIO;
import org.gamecontrolplus.ControlSlider;

import eatmoney.eatMoneyMain;

// R12 zoomin zoomout
//mode== red
//right knob pan+tilt cam
//1 = force AF
//2 = force 


public class Gamepad{

	eatMoneyMain emm;
	ControlDevice cd;
	
	int numS, numB;
	
	ControlButton[] buttons;
	boolean [] buttonValues;
	
	ControlSlider[] slider;
	float [] sliderValues;
	
	public UDPClient udp;
	 
	boolean savestate = false;
	boolean recallstate = false;
	boolean focusstate = false;
	 
	boolean changepreset = false;
	boolean firepreset = false;
	 
	boolean irisScan = false;
	
	int currHandle = 0;
	 
	//slider 0 & 1 is camera leftright & camera updown
	//button 6 & 8 are camera zoomin zoomout
	
	 
	public Gamepad(eatMoneyMain _emm)  {
		
		  emm = _emm;
		  ControlIO control;
		  control = ControlIO.getInstance(emm);	
		  List<ControlDevice> ctrl = control.getDevices();
		  //System.out.println(ctrl);

	      cd = control.getDevice(5);
	      
	      numB = cd.getNumberOfButtons();
	      buttons = new ControlButton[numB];
	      buttonValues = new boolean[numB];
	      
	      numS = cd.getNumberOfSliders();
	      slider = new ControlSlider[numS];
	      sliderValues = new float[numS];
	      
	      
	      for(int i = 0; i < numB; i++) {
				buttons[i] = cd.getButton(i);
				buttonValues[i] = buttons[i].pressed();
			}
			for(int i = 0; i < numS; i++) {
				slider[i] = cd.getSlider(i);
				sliderValues[i] = slider[i].getValue();
			}

			udp = new UDPClient(this);	
	}
	

	
	
	public void update() {
		

		for(int i = 0; i < slider.length; i++) {
			sliderValues[i] = round1(slider[i].getValue(),1);	
		}
		for(int i = 0; i < buttons.length; i++) {
			buttonValues[i] = buttons[i].pressed();	
			//if(buttonValues[i] == true) System.out.println(i);
		}

		
		//move camera
		if(((sliderValues[0] != 0.0f || sliderValues[1] != 0.0f) &&  (this.udp.ismovingLR == false || this.udp.ismovingUD == false))
			|| (this.udp.ismovingLR == true || this.udp.ismovingUD == true && (sliderValues[0] == 0 && sliderValues[1] == 0)) && currHandle != 1 ) {
			this.udp.moveNew(sliderValues[1], sliderValues[0],2);
			currHandle = 2;
		}
		
		else if(((sliderValues[3] != 0.0f || sliderValues[4] != 0.0f) &&  (this.udp.ismovingLR == false || this.udp.ismovingUD == false))
				|| (this.udp.ismovingLR == true || this.udp.ismovingUD == true && (sliderValues[3] == 0 && sliderValues[4] == 0)) && currHandle != 2 ) {
				this.udp.moveNew(sliderValues[4], sliderValues[3],1);	
				currHandle = 1;
			}
		
		//zoom camera
		if(buttonValues[8] == true && this.udp.isZoomIn == false) {
			//System.out.println("in zoom");
			this.udp.zoom("in");
		}
		else if(buttonValues[6] == true && this.udp.isZoomOut == false) {
			//System.out.println("out zoom");
			this.udp.zoom("out");
		}
		else if((buttonValues[8] == false && buttonValues[6] == false) && ( this.udp.isZoomIn == true || this.udp.isZoomOut == true)) {
			//System.out.println("stop zoom");
			this.udp.zoom("stop");
		}
		
		//button 2 switch focus
		if(buttonValues[2] == true && focusstate == false) {
			focusstate = true;
			this.udp.switchFocus();
		}
		else if(buttonValues[2] == false && focusstate == true) {
			focusstate = false;
			
		}
		//button 1 focus in
		if(buttonValues[1] == true && this.udp.focusIn == false) {
			this.udp.focus("near");
		}
		
		//button 3 focus out
		if(buttonValues[3] == true && this.udp.focusOut == false) {
			this.udp.focus("far");
		}

		else if((buttonValues[1] == false && buttonValues[3] == false) && ( this.udp.focusIn == true || this.udp.focusOut == true)) {
			this.udp.focus("stop");
		}
		
		// change preset with pad
		if(buttonValues[0] == true && changepreset == false) {
			changepreset = true;
			int val = 0;
			if(buttons[0].getValue() == 2.0) val = -1;
			else if(buttons[0].getValue() == 6.0) val = 1;
			emm.cont.changePreset(val);
		}
		else if(buttonValues[0] == false && changepreset == true) {
			changepreset = false;
		}
		
		// fire preset with pad
		if(buttonValues[7] == true && firepreset == false) {
			firepreset = true;
			emm.cont.firePreset();
		}
		else if(buttonValues[7] == false && firepreset == true) {
			firepreset = false;
		}
		// fire irisScan with pad
		if(buttonValues[5] == true && irisScan == false && emm.vo.vb.shootIris == false) {
			emm.vo.iris.reset();
			irisScan = true;
			emm.vo.vb.shootIris = true;
		}
		else if(buttonValues[5] == false && irisScan == true) {
			irisScan = false;
		}
		
	}
	
	public static float round1(float value, int scale) {
	    return (float) (Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale));
	}




}
