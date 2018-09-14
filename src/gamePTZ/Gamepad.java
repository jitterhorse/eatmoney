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


public class Gamepad {

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
	
	//slider 0 & 1 is camera leftright & camera updown
	//button 6 & 8 are camera zoomin zoomout
	
	 
	public Gamepad(eatMoneyMain _emm)  {
		
		  emm = _emm;
		  ControlIO control;
		  control = ControlIO.getInstance(emm);	
		  List<ControlDevice> ctrl = control.getDevices();
		  System.out.println(ctrl);

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

			udp = new UDPClient();	
	}
	
	public void update() {
		for(int i = 0; i < slider.length; i++) {
			sliderValues[i] = round1(slider[i].getValue(),1);
		}
		for(int i = 0; i < buttons.length; i++) {
			buttonValues[i] = buttons[i].pressed();	
		}
		
		//move camera
		if(((sliderValues[0] != 0.0f || sliderValues[1] != 0.0f) &&  (this.udp.ismovingLR == false || this.udp.ismovingUD == false))
			|| (this.udp.ismovingLR == true || this.udp.ismovingUD == true && sliderValues[0] == 0 && sliderValues[1] == 0) ) {
			this.udp.moveNew(sliderValues[1], sliderValues[0]);
			System.out.println("move cam" + sliderValues[1] + " / " + sliderValues[0]);
		}
		//zoom camera
		if(buttonValues[8] == true && this.udp.isZoomIn == false) {
			System.out.println("in zoom");
			this.udp.zoom("in");
		}
		else if(buttonValues[6] == true && this.udp.isZoomOut == false) {
			System.out.println("out zoom");
			this.udp.zoom("out");
		}
		else if(buttonValues[8] == false && buttonValues[6] == false && ( this.udp.isZoomIn == true || this.udp.isZoomOut == true)) {
			System.out.println("stop zoom");
			this.udp.zoom("stop");
		}
		
		if(buttonValues[0] == true && focusstate == false) {
			focusstate = true;
			this.udp.autoFocus();
		}
		else if(buttonValues[0] == false && focusstate == true) {
			focusstate = false;
			
		}
		
		
		
		//load and save states
		/*
		if(buttonValues[0] == true && savestate == false) {
			savestate = true;
			this.udp.savePreset();
		}
		
		else if(buttonValues[0] == false && savestate == true) {
			savestate = false;
		}
		
		
		if(buttonValues[1] == true && recallstate == false) {
			recallstate = true;
			this.udp.recallPreset();
		}
		
		else if(buttonValues[1] == false && recallstate == true) {
			recallstate = false;
		}
		*/
		
	}
	
	public static float round1(float value, int scale) {
	    return (float) (Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale));
	}


}
