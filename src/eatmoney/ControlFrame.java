package eatmoney;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import processing.core.PApplet;

public class ControlFrame extends PApplet{

	int w, h;
	PApplet parent;
	ControlP5 cp5;
	eatMoneyMain em;
	MyControlListener myListener;
	boolean calibrate = false;
	
	public ControlFrame(PApplet _parent,eatMoneyMain _em) {
		super();
		em = _em;
		parent = _parent;
		PApplet.runSketch(new String[]{this.getClass().getName()}, this);

	}

	public void settings() {
	    size(200, 800);
	  }

	  public void setup() {
	    surface.setLocation(10, 10);
	    cp5 = new ControlP5(this);
	    cp5.addButton("openCalibrate")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(180,30)
	     ;
	    
	    myListener = new MyControlListener();
	    cp5.getController("openCalibrate").addListener(myListener);
	  }
	  
	  class MyControlListener implements ControlListener {
		  public void controlEvent(ControlEvent theEvent) {
		    if(theEvent.getController().getName().equals("openCalibrate")) {
		    	//em.mapping.openCalibration();
		    	calibrate = true;
		    }
		  }
	  }
	  
	  public void draw() {
			clear();
		    background(40);
	  }
	  
}
