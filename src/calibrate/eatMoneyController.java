package calibrate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import controlP5.Button;
import controlP5.CColor;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.Group;
import controlP5.RadioButton;
import controlP5.ScrollableList;
import controlP5.Slider;
import controlP5.Textfield;
import controlP5.Textlabel;
import controlP5.Toggle;
import eatmoney.eatMoneyMain;
import processing.core.PApplet;
import processing.core.PFont;

public class eatMoneyController {

	eatMoneyMain em;
	PApplet parent;
	public ControlP5 cp;
	MyControlListener myListener;
	Group standard, calibrate, pre, camera, interaction, presets;
	
	Toggle monitorT;
	ScrollableList files;
	Textfield tf;
	
	Textlabel warning1;
	
	RadioButton r1, r2;
	Button save;
	public Button fade;
	
	Button[] presetButtons;
	
	public Slider state,damping;
	
	public int nextPreset = 0;
	int lastrunning = 0;
	int coloractive;
	int colorinactive;
	int colorrunning;
	
	PFont font;
	ArrayList<String> oldFiles;
	
	int currentComment = 0;
	
	public saveTag st;
	
	
	public class saveTag{
		
		public float lifetime = 1.f;
		public int num;
		
		public saveTag(int _num) {
			num = _num;
			
		}
		
	}
	
	public eatMoneyController(PApplet _parent, eatMoneyMain _em) {
		parent = _parent;
		em = _em;
		font = em.createFont("Arial",15);
		cp = new ControlP5(parent);
		cp.setPosition(0, 610);
		coloractive = em.color(200,60,60);
		colorinactive = em.color(60,70,60);
		colorrunning = em.color(100,100,120);
		myListener = new MyControlListener();
		loadFiles();
		
		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////preRequisites
		///////////////////////////////////////////////////////////////////
		
		pre = cp.addGroup("prerequisites")
                .setPosition(0,0)
                .setBackgroundHeight(450)
                .setWidth(500)
                .setBackgroundColor(em.color(35))
                .disableCollapse()
                ;
		
		tf = cp.addTextfield("newfile")
		     .setPosition(10,10)
		     .setSize(200,30)
		     .setFont(font)
		     .setFocus(true)
		     .setColor(parent.color(255,255,255))
		     .setGroup(pre)
		     ;
		
		String plCount = String.valueOf(em.Planes);
		
		monitorT = cp.addToggle("Monitors")
		     .setPosition(10,80)
		     .setSize(100,20)
		     .setMode(ControlP5.SWITCH)
		     .setGroup(pre)
		     .setCaptionLabel("Monitors: " + plCount) 
		     .setColorActive(em.color(80,80,80)) 
		     .setColorBackground(em.color(0,250,0)) 
		     ;
	    
	    cp.getController("Monitors").addListener(myListener);
	    
	    cp.addButton("StartNewCalibration")
	     .setValue(0)
	     .setPosition(10,150)
	     .setSize(180,30)
	     .setGroup(pre)
	     ;

	    cp.getController("StartNewCalibration").addListener(myListener);
	   
	    warning1 = cp.addTextlabel("warning1")
                .setText("")
                .setPosition(100,50)
                .setColor(parent.color(255,0,0))
                .setGroup(pre)
                .setVisible(false)
                ;
  
	    files = cp.addScrollableList("Saved Files")
	     .setPosition(10, 350)
	     .setSize(400, 100)
	     .setBarHeight(40)
	     .setItemHeight(40)
	     .addItems(oldFiles)
	     .setGroup(pre);
	
	    cp.getController("Saved Files").addListener(myListener);
	    
		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////calibration planes
		///////////////////////////////////////////////////////////////////
	    
		calibrate = cp.addGroup("calibrate")
                .setPosition(0,0)
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
		
		cp.getController("closeCalibrate").addListener(myListener);
		cp.getController("saveCalibrate").addListener(myListener);	

		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////camera presets
		///////////////////////////////////////////////////////////////////
		
		camera = cp.addGroup("camera")
                .setPosition(0,0)
                .setBackgroundHeight(450)
                .setWidth(1000)
                .setBackgroundColor(em.color(0))
                .disableCollapse()
                ;
		
		cp.addButton("close Presets")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(100,30)
	     .setGroup(camera)
	     ;
		
		for(int i = 0; i < 10; i++) {
			cp.addButton("CamPre_"+i)
		     .setValue(0)
		     .setPosition(120,10+35*i)
		     .setSize(100,30)
		     .setGroup(camera)
		     ;
			cp.getController("CamPre_"+i).addListener(myListener);
		}
	
		for(int i = 0; i < 10; i++) {
		  cp.addButton("savePreset_"+i)
	     .setValue(0)
	     .setPosition(230,10+35*i)
	     .setSize(100,30)
	     .setGroup(camera)
	     ;
		  cp.getController("savePreset_"+i).addListener(myListener);	
		}
		
		camera.setVisible(false);
		
		cp.getController("close Presets").addListener(myListener);	
		
		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////SHOW PRESETS
		///////////////////////////////////////////////////////////////////
		
		presets = cp.addGroup("presets")
                .setPosition(540,0)
                .setBackgroundHeight(450)
                .setWidth(500)
                .setBackgroundColor(em.color(0))
                .disableCollapse()
                ;
		
		presetButtons = new Button[em.presets.allPresets.size()];
		
		for(int i = 0; i<em.presets.allPresets.size();i++) {
			int row = i % 10;
			int col = em.floor(((float)i/(float)10));
			
			Button b = cp.addButton("preset_" +i)
		     .setPosition(20 + (120 *col),10 + 35 * row)
		     .setSize(100,30)
		     .setValue(0)
             .setFont(font)
		     .setGroup(presets)
		     .setColorBackground(colorinactive)
		     ;
			 cp.getController("preset_" +i).addListener(myListener);
			
			 presetButtons[i] = b;
		}
		
		presets.setVisible(false);
		
		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////interaction
		///////////////////////////////////////////////////////////////////
		
		interaction = cp.addGroup("interaction")
                .setPosition(0,0)
                .setBackgroundHeight(450)
                .setWidth(500)
                .setBackgroundColor(em.color(0))
                .disableCollapse()
                ;
		
		cp.addButton("close interaction")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;

		cp.addButton("tracking")
	     .setValue(0)
	     .setPosition(120,10)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;
		
		cp.addButton("reset cloth")
	     .setValue(0)
	     .setPosition(120,50)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;		

		cp.addButton("setAL")
	     .setValue(0)
	     .setPosition(120,140)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;	
		
		cp.addButton("setDL")
	     .setValue(0)
	     .setPosition(120,180)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;	
		
		cp.addButton("setPL")
	     .setValue(0)
	     .setPosition(120,220)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;	
		
		fade = cp.addButton("fade")
	     .setPosition(120,290)
	     .setSize(100,30)
	     .setGroup(interaction)
	     ;	
		
		cp.getController("setPL").addListener(myListener);
		cp.getController("setDL").addListener(myListener);
		cp.getController("setAL").addListener(myListener);
		
		cp.getController("fade").addListener(myListener);
		
		for(int i = 1; i <= 11; i++) {
			cp.addButton("comment_"+i)
		     .setValue(0)
		     .setPosition(230,10+35*i)
		     .setSize(100,30)
		     .setGroup(interaction)
		     ;
			cp.getController("comment_"+i).addListener(myListener);
		}
		
		  state = cp.addSlider("clothstate")
		     .setPosition(350,10)
		     .setSize(20,300)
		     .setRange(0,100)
		     .setValue(0)
		     .setGroup(interaction)
		     ;
		  
		  damping = cp.addSlider("damp")
		     .setPosition(450,10)
		     .setSize(20,300)
		     .setRange(6000,10000)
		     .setValue(9995)
		     .setGroup(interaction)
		     ;	
		  
		cp.getController("tracking").addListener(myListener);	
		cp.getController("close interaction").addListener(myListener);
		cp.getController("clothstate").addListener(myListener);
		cp.getController("damp").addListener(myListener);
		cp.getController("reset cloth").addListener(myListener);
		
		
		interaction.setVisible(false);
		
		///////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////main gui
		///////////////////////////////////////////////////////////////////
		
		standard = cp.addGroup("standard")
                .setPosition(0,0)
                .setBackgroundHeight(450)
                .setWidth(500)
                .setBackgroundColor(em.color(0))
                .disableCollapse()
                ;

		cp.addButton("openCalibrate")
	     .setValue(0)
	     .setPosition(10,10)
	     .setSize(100,30)
	     .setGroup(standard)
	     ;
		
		cp.addButton("new/open file")
	     .setValue(0)
	     .setPosition(120,10)
	     .setSize(100,30)
	     .setGroup(standard)
	     ;
		
		cp.addButton("cam presets")
		 .setValue(0)
	     .setPosition(230,10)
	     .setSize(100,30)
	     .setGroup(standard)
	     ;
		
		cp.addButton("interact")
		 .setValue(0)
	     .setPosition(10,60)
	     .setSize(100,30)
	     .setGroup(standard)
	     ;	
		
		standard.setVisible(false);
		
		 
	 cp.getController("openCalibrate").addListener(myListener);
	 cp.getController("new/open file").addListener(myListener);
	 cp.getController("cam presets").addListener(myListener);
	 cp.getController("interact").addListener(myListener);
	 
	}
	
	class MyControlListener implements ControlListener {
		  public void controlEvent(ControlEvent theEvent) {
			  
		    if(theEvent.getController().getName().equals("openCalibrate")) {
		    	openCalibrate();
		    	calibrate.setVisible(true);
		    	standard.setVisible(false);
		    }
		    else if(theEvent.getController().getName().equals("new/open file")) {
		    	updatePre();
		    	em.runStatus = enums.status.PRE;
		    	calibrate.setVisible(false);
		    	standard.setVisible(false);
		    }
		    else if(theEvent.getController().getName().equals("cam presets")) {
		    	em.runStatus = enums.status.CAMPRE;
		    	camera.setVisible(true);
		    	standard.setVisible(false);
		    } 
		    else if(theEvent.getController().getName().equals("interact")) {
		    	standard.setVisible(false);
		    	interaction.setVisible(true);
		    	presets.setVisible(true);
		    }		    
		    else if(theEvent.getController().getName().contains("comment")) {
		    	String s = theEvent.getController().getName();
		    	s = s.substring(8,s.length());
		    	currentComment = Integer.parseInt(s);
		    	em.startComments(currentComment);
		    }
		    else if(theEvent.getController().getName().contains("clothstate")) {
		    	float f = theEvent.getController().getValue();
		    	em.generalState = (float)f/(float)100.;
		    }
		    else if(theEvent.getController().getName().contains("damp")) {
		    	float f = em.map(theEvent.getController().getValue(),6000,10000,0.6f,1.f); 
		    	em.co.param_cloth_particle.DAMP_VELOCITY  = f;
		    }
		    
		    else if(theEvent.getController().getName().contains("CamPre")) {
		    	String s = theEvent.getController().getName();
		    	s = s.substring(7,8);
		    	em.GP.udp.recallPreset(Integer.parseInt(s));
		    } 
		    else if(theEvent.getController().getName().contains("savePreset")) {
		    	String s = theEvent.getController().getName();
		    	s = s.substring(11,12);
		    	em.GP.udp.savePreset(Integer.parseInt(s));
		    	st = new saveTag(Integer.parseInt(s));
		    }  	    
		    else if(theEvent.getController().getName().equals("close Presets")) {
		    	camera.setVisible(false);
		    	standard.setVisible(true);
		    	em.runStatus = enums.status.RUN;
		    }	
		    else if(theEvent.getController().getName().equals("presets")) {
		    	System.out.println(theEvent.getValue());
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
		    else if(theEvent.getController().getName().equals("Monitors")) {
		    	float i = theEvent.getValue();
		    	em.Planes = (int)i + 1;
		    	String plCount = String.valueOf(em.Planes);
		    	monitorT.setCaptionLabel("Monitors: " + plCount);
		    }
		    else if(theEvent.getController().getName().equals("StartNewCalibration")) {
		    	String name = cp.get(Textfield.class,"newfile").getText();
		    	name = name.replace("�","ae");
		    	name = name.replace("�","ue");
		    	name = name.replace("�","oe");
		    	name = name.replace("�","ss");
		    	name = name.replace(" ","_");
		    	name += ".json";
		    	cp.get(Textfield.class,"newfile").setText(name);
		    	if(name.length() < 2) {
		    		warning1.setText("Projectname to short");
		    		warning1.setVisible(true);
		    	}
		    	else {
			    	pre.setVisible(false);
			    	calibrate.setVisible(true);
			    	em.setupCalibration(name,true);
			    	em.runStatus = enums.status.CALI;
		    	}
		    }
		    else if(theEvent.getController().getName().equals("Saved Files")) {
		    	float n = theEvent.getValue();
		    	String name = cp.get(ScrollableList.class, "Saved Files").getItem((int)n).get("name").toString();
		    	em.setupCalibration(name,false);
		    	pre.setVisible(false);
		    	calibrate.setVisible(true);
		    	em.runStatus = enums.status.CALI;
		    }
		    else if(theEvent.getController().getName().equals("tracking")) {
		    	em.fm.detection = !em.fm.detection;
		    }
		    else if(theEvent.getController().getName().equals("reset cloth")) {
		    	em.co.resetCloth();
		    }
		    
		    else if(theEvent.getController().getName().equals("setAL")) {
		    	em.lightRig.ALset = ! em.lightRig.ALset;
		    }
		    else if(theEvent.getController().getName().equals("setPL")) {
		    	em.lightRig.PLset = ! em.lightRig.PLset;
		    }
		    else if(theEvent.getController().getName().equals("setDL")) {
		    	em.lightRig.DLset = ! em.lightRig.DLset;
		    }
		    else if(theEvent.getController().getName().equals("fade")) {
		    	//System.out.println(theEvent.getValue());
		    	if(em.fader == true) em.fade(false);
		    	else if(em.fader == false) em.fade(true);
		    }
		    else if(theEvent.getController().getName().startsWith("preset_")){
		    	String s = theEvent.getController().getName();
		    	s = s.substring(7, s.length());
		    	firePresetMouse(Integer.parseInt(s));
		    }   
		    else if(theEvent.getController().getName().equals("close interaction")) {
		    	interaction.setVisible(false);
		    	presets.setVisible(false);
		    	standard.setVisible(true);
		    }		    
		  }
	  }
	
	
	
	private void openCalibrate() {
		System.out.println("calibration start");
		em.runStatus = enums.status.CALI;
	}
	private void closeCalibrate() {
		System.out.println("calibration end");
		em.setupMain();
		em.runStatus = enums.status.RUN;
	}
	
	private void updatePre() {
		pre.setVisible(true);
		warning1.setVisible(false);
		for(String s : oldFiles ) {
			cp.get(ScrollableList.class, "Saved Files").removeItem(s);
		}
		loadFiles();
		cp.get(ScrollableList.class, "Saved Files").addItems(oldFiles);
		cp.get(Textfield.class,"newfile").setText("");
	}

	
	private void loadFiles() {
		oldFiles = new ArrayList<String>();		
		
		System.out.println(em.ppath);
		File folder = new File(em.ppath + "\\json");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  if (listOfFiles[i].isFile()) {
			  oldFiles.add(listOfFiles[i].getName());
		  } 
		}	
	}
		
	public void firePreset() {
		em.presets.loadPreset(nextPreset);
		changePreset(1);
	}
	
	public void changePreset(int dir) {
		presetButtons[nextPreset].setColorBackground(colorinactive);
		nextPreset += dir;
		if(nextPreset >= presetButtons.length-1) nextPreset = presetButtons.length-1;
		else if(nextPreset <= 0) nextPreset = 0;
		presetButtons[nextPreset].setColorBackground(coloractive);
	}
	
	public void firePresetMouse(int target) {
		em.presets.loadPreset(target);
		changePresetMouse(target);
	}
	
	
	public void changePresetMouse(int target) {
		presetButtons[nextPreset].setColorBackground(colorinactive);
		nextPreset = target+1;
		if(nextPreset >= presetButtons.length-1) nextPreset = presetButtons.length-1;
		else if(nextPreset <= 0) nextPreset = 0;
		presetButtons[nextPreset].setColorBackground(coloractive);
		
	}
	
	
}
