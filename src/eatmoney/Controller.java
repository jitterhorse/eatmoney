package eatmoney;

import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Group;
import processing.core.PApplet;

public class Controller {

	eatMoneyMain em;
	PApplet parent;
	ControlP5 cp;
	MyControlListener myListener;
	Group standard, calibrate;
	
	public Controller(PApplet _parent, eatMoneyMain _em) {
		parent = _parent;
		em = _em;
		cp = new ControlP5(parent);
		cp.setPosition(0, 0);
		
		//Standard gui
		
		standard = cp.addGroup("standard")
                .setPosition(0,560)
                .setBackgroundHeight(450)
                .setWidth(500)
                .setBackgroundColor(em.color(0))
                .disableCollapse()
                ;
		
		
		
		cp.addButton("openCalibrate")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(180,30)
	     .setGroup(standard)
	     ;
		
		//Calibration gui
		
		calibrate = cp.addGroup("calibrate")
                .setPosition(0,560)
                .setBackgroundHeight(450)
                .setWidth(200)
                .setBackgroundColor(em.color(35))
                .disableCollapse()
                ;
		cp.addButton("saveCalibrate")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(180,30)
	     .setGroup(calibrate)
	     ;
		cp.addButton("closeCalibrate")
	     .setValue(0)
	     .setPosition(10,50)
	     .setSize(180,30)
	     .setGroup(calibrate)
	     ;
		calibrate.setVisible(false);
		
		 myListener = new MyControlListener();
		 cp.getController("openCalibrate").addListener(myListener);
		 cp.getController("closeCalibrate").addListener(myListener);
		 cp.getController("saveCalibrate").addListener(myListener);
	}
	
	class MyControlListener implements ControlListener {
		  public void controlEvent(ControlEvent theEvent) {
		    if(theEvent.getController().getName().equals("openCalibrate")) {
		    	openCalibrate();
		    	calibrate.setVisible(true);
		    	standard.setVisible(false);
		    }
		    else if(theEvent.getController().getName().equals("closeCalibrate")) {
		    	closeCalibrate();
		    	calibrate.setVisible(false);
		    	standard.setVisible(true);
		    }
		    else if(theEvent.getController().getName().equals("saveCalibrate")) {
		    	em.cal.renderPlanes();
		    	em.cal.saveData();
		    }
		  }
	  }
	
	private void openCalibrate() {
		System.out.println("calibration start");
		em.calibrate = true;
	}
	private void closeCalibrate() {
		System.out.println("calibration end");
		em.calibrate = false;
	}	
}
