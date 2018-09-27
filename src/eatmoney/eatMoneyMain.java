package eatmoney;

import java.util.ArrayList;

import butler.ButlerObject;
import calibrate.Calibrate;
import calibrate.eatMoneyController;
import controlP5.ControlP5;
import eatmoney.ClothObject.clothCenter;
import enums.ObjectMode;
import enums.Follow;
import enums.mode;
import enums.status;
import gamePTZ.Gamepad;
import midi.MidiMixer;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;
import videoCapture.FaceMark;
import videoCapture.VideoCaptureTool;

public class eatMoneyMain extends PApplet {
	
	//test vars
	public boolean testing = false;
	
	//global Vars
	public String ppath = "";
	PGraphics mc;
	PGraphics layer;
	OscP5 oscP5;
	NetAddress myRemoteLocation;
	ControlP5 mainControl;
	MidiMixer mm;
	
	
	public FaceMark fm;
    VideoCaptureTool vidC;

	public Gamepad GP;
	
	boolean firstinit = false; // if a first calibration was finished
    
	public int mainDisplayWidth = 1920;
	
	//create controller surface
	public eatMoneyController cont;
	
	//calibrate projector
	public Calibrate cal;
	public ArrayList<PShape> allPlanes;
	
	//mouse vars
	boolean mousePress = false;
	float rotx, roty = 0;
	PVector mouseclick = new PVector(0,0,0);
	float dx,dy,sx,sz = 0;
	
	boolean shiftSpace = false;
	float shiftx,shiftz = 0;
	public int Planes = 1;

	//camera vars
	PVector camTarget ;
	PVector camTargetnew ;
	PVector camPos;
	PVector camPosnew;
	float cameraspeed = 0.008f;
	int speedchange = 0;
	float fov = 60; 
	
	boolean noiseMove = false;
	float noiseScale = 0.02f;
	
	public ButlerObject bo;
	public ClothObject co;
	public VideoObject vo;
	public LightRig lightRig;
	displaceTexture dt;
	
	public float generalState = 0.f;
	  
	public status runStatus = status.PRE;
	public mode showMode = mode.cloth;

	PShader glow,side;
	public boolean fader = true;
	float blackfade = 1.f;
	
	
    PVector cameraSlidePos = new PVector(0,0,0);
    PVector cameraNewPos = new PVector(0,0,0);
    PVector middleSlidePos = new PVector(0,0,0);	
	
    PFont fn;
    
    public Presets presets;
    public ClothStates cstates;
    
    boolean showtest = false;
    PImage testTexture;

    
	public static void main(String[] args) {
		PApplet.main("eatmoney.eatMoneyMain");
	}

	public void settings() {
		fullScreen(P3D,SPAN);  
		//smooth(16);
		//size(3840,1080,P3D);
		ppath = sketchPath();
	}
	
	public void setup() {
		frameRate(30);
		fn = createFont("DejaVu Sans Mono", 24);
		textFont(fn);
		myRemoteLocation = new NetAddress("192.168.1.5",8521);
		
		cstates = new ClothStates(this);
		presets = new Presets(this);
		
		cont = new eatMoneyController(this,this);	
		oscP5 = new OscP5(this,7000);	
		
		bo = new ButlerObject(this);
		vo = new VideoObject(this);
		GP = new Gamepad(this);
		mm = new MidiMixer(this);
		
	}
	
	public void setupCalibration(String filename, boolean newCalibration) {
		cal = new Calibrate(this,Planes,filename,newCalibration,layer);	
	}
	
	
	public void setupMain() {
		if(firstinit == false) {

			
			vidC = new VideoCaptureTool(this);
			fm = new FaceMark(this,vidC.cam);
			testTexture = loadImage("calibration\\calibrate_image.jpg");
			
			
		}
		
		Planes = cal.totalcount;

		mc = createGraphics(1920*Planes,height,P3D);
		layer = createGraphics(1920*Planes,height,P2D);
		
		co = new ClothObject(mc,this,this);
		mc.smooth(16);
		
		lightRig = new LightRig(mc,this,this);
		dt = new displaceTexture(this,1920*Planes,height);
		
		glow = loadShader("shader\\glow.glsl"); 
		side = loadShader("shader\\side.glsl");  
	    glow.set("iResolution", (float)mc.width, (float)mc.height);
	
		mc.stroke(0);
		mc.perspective(radians(fov),(float)1920*Planes/(float)height,10,15000);
		 
		for(PShape p : cal.planeObjects) {
			 p.setTexture(layer);
		}
		
		
		camTarget = new PVector(width/2.0f, height/2.0f,0);
		camTargetnew = new PVector(width/2.0f, height/2.0f,0);
		camPos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		camPosnew = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		
		if(firstinit == false) {
			cont.firePreset();
			firstinit = true;
		}
		
	}
	
	public static float easeInOut (float t,float b , float c, float d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	}
	
	public void draw() {
		clear();
		background(0);
		
		if(runStatus == status.PRE) {
			
			
		}
		
		else if(runStatus == status.CALI) {
			  cal.drawPlanes();
			  image(cal.cali,210,0);
			  fill(255,100);
			  noStroke();
			  rect(1920,0,cal.totalcount*1920,1080);
			  image(cal.preview,1920,0,cal.totalcount*1920,1080);
			
		}
		else if(runStatus == status.RUN || runStatus == status.CAMPRE) {
			

			
			GP.update();
			
			if(Math.random() > 0.99) noiseScale = random(0.5f);
			
		  	if(Math.random() > 0.99 && speedchange == 0 && showMode == mode.cloth) cameraspeed = random(0.008f) + 0.004f;
		  	/*
		  	else if(Math.random() > 0.99 && speedchange == 0) {
		  		speedchange = floor(random(1500));
		  	}
		  	speedchange:
		  	if(speedchange != 0) {
		  		if(cameraspeed >= 0.005) cameraspeed-=0.0001;
		  		else if (cameraspeed < 0.0005) cameraspeed += 0.0001;
		  		else { 
		  			speedchange=0;
		  			break speedchange;
		  		}
		  		speedchange--;
		  	}
		  	*/
		  	
		  	co.calcCloth();

		  	PVector middle = new PVector();
		  	 PVector offset = new PVector(0,0,0);

		  	if(showMode == mode.empty) {
		  		middle.x = 0;
		  		middle.y = 0;
		  		middle.z = 0;
		  		cameraNewPos.x = 0;
				cameraNewPos.y = 0;
				cameraNewPos.z = -400;
		  	}
		  	
		  	else if(showMode == mode.cloth) {
		  		if(co.cc == clothCenter.cloth) {
			  		cameraspeed = 0.005f;
			  		middle = co.getClothMiddle();
			  		if(random((float) 1.) > 0.99f) {
			  			cameraspeed = generalState*0.01f;
			  			cameraNewPos.x = middle.x + (random(1000)-500.f);
			  			cameraNewPos.y = random(600)-300.f;
			  			cameraNewPos.z = random(100) + 200.f * -1.f;
			  		}
		  		}
		  		else if(co.cc == clothCenter.pin) {
			  		cameraspeed = 0.008f;
			  		middle = co.getPinMiddle();
			  		if(random((float) 1.) > 0.99f) {
			  			cameraspeed = generalState*0.04f;
			  		}
				    cameraNewPos.x = middle.x+100;
				    cameraNewPos.y = middle.y+100;
				    cameraNewPos.z = middle.z-400;	
				 
		  		}
		  		else if(co.cc == clothCenter.emit) {
		  		cameraspeed = 0.017f;
		  		 middle = co.getEmitMiddle();
		  		 if(co.currentExposes.size() > 0) {
		  			 if(co.currentExposes.get(0).lifetime <= 0) co.cc = clothCenter.cloth;
		  		 }
		  		 cameraNewPos.x = middle.x+100;
				 cameraNewPos.y = middle.y+100;
				 cameraNewPos.z = middle.z-100;
				
		  		}
		  	}
		  	
		  	else if(showMode == mode.comment) {
		  		 middle = co.getCommentMiddle();
		  		 
		  		 cameraNewPos.x = middle.x+400;
				 cameraNewPos.y = middle.y+400;
				 cameraNewPos.z = middle.z-100;
				
		  	}
		  	
		  	else if(showMode == mode.cam) {
		  		 middle = vo.getVideoMiddle();
		  		 float dirx = 1.f, diry = 1.f, dirz = 1.f;
		  		 
		  		 if(middle.x < 0) dirx = -1.f;
		  		 if(middle.y < 0) diry = -1.f;
		  		 if(middle.z < 0) dirz = -1.f;
		  		 
		  		 cameraNewPos.x = middle.x+220*dirx;
				 cameraNewPos.y = middle.y+220*diry;
				 cameraNewPos.z = middle.z+220*dirz;
				 
		  		 
		  	}
		  	
		  	else if(showMode == mode.butler) {
		  		offset = bo.camoffset;
		  		middle = bo.butlerMean;
		  		middle.add(bo.ButlerOffset);
		  		if(random(1.f)>0.99f && bo.displaymode == ObjectMode.run) {
  				  camPosnew = new PVector((random(1.f) *1600)-800.f,(random(1.f) * 200)-70,(random(1.f) *1200)-200.f);
  				  camPosnew.add(bo.ButlerOffset);
  				  cameraspeed = random(0.005f,0.01f);
  				  bo.camoffset.x = random(-400,400);
  				  bo.camoffset.y = random(-40,40);
  				  bo.camoffset.z = random(-290,-110);
		  		}
		  		cameraNewPos.x = middle.x+bo.camoffset.x;
				cameraNewPos.y = middle.y+bo.camoffset.y;
				cameraNewPos.z = middle.z+bo.camoffset.z;
				
		  	}
			
		  	////////////////////////////////////NOISE
		  	float noiseVal = 0.f, noiseVal2 = 0.f, noiseVal3 = 0.f;
		  	float noiseVal4 = 0.f, noiseVal5 = 0.f, noiseVal6 = 0.f;
		  	
		  	if(noiseMove == true) {
		  		noiseVal = noise((frameCount*0.02f)*noiseScale)*150;
		  		noiseVal2 = noise((frameCount*0.04f)*noiseScale)*150;
		  		noiseVal3 = noise((frameCount*0.03f)*noiseScale)*150;
		  		if(showMode != mode.butler) {
		  		noiseVal4 = sin(frameCount * noiseScale*0.056f) * noiseVal*20.f;
		  		noiseVal5 = 1.f-sin(frameCount * noiseScale*0.05f) * noiseVal2*20.f;
		  		noiseVal6 = cos(frameCount * noiseScale*0.04f) * noiseVal3*20.f;
		  		}
		  	}
		  	
		  	
			 mc.beginDraw();
			 mc.pushMatrix();
			 mc.clear();
			 mc.background(0);
			 
		 	 cameraSlidePos.x = lerp(cameraSlidePos.x,cameraNewPos.x+noiseVal4,(float) cameraspeed );
		     cameraSlidePos.y = lerp(cameraSlidePos.y,cameraNewPos.y+noiseVal5,(float) cameraspeed);
		     cameraSlidePos.z = lerp(cameraSlidePos.z,cameraNewPos.z+noiseVal6,(float) cameraspeed);

		    
		     middleSlidePos.x = lerp(middleSlidePos.x,middle.x+noiseVal,(float) cameraspeed);
		     middleSlidePos.y = lerp(middleSlidePos.y,middle.y+noiseVal2,(float) cameraspeed);
		     middleSlidePos.z = lerp(middleSlidePos.z,middle.z+noiseVal3,(float) cameraspeed);
 
		     lightRig.doLight(offset);
		    
		     mc.camera(cameraSlidePos.x, cameraSlidePos.y,cameraSlidePos.z,middleSlidePos.x,middleSlidePos.y,middleSlidePos.z, 0, 1, 0);
		     mc.perspective(PI/2.0f, (float)mc.width/(float)mc.height, 0.1f, 50000.f);
		     
		     //draw phyiscs box
		     //co.drawOutline(mainContent);
		     
		     mc.strokeWeight(2);
		     mc.fill(23);
		     mc.noStroke();
		     mc.sphere(5000);

		     ///////////////////////////////////////CLOTH
		     clothloop:
		     if(co.displaymode != ObjectMode.off) {
		    	 if(co.displaymode == ObjectMode.in && co.easing < 1.f) co.easing += 0.01;
		    	 else if(co.displaymode == ObjectMode.in && co.easing >= 1.) co.displaymode = ObjectMode.run;
		    	 else if(co.easing > 0.f && co.displaymode == ObjectMode.out) {
		    		 co.easing -= 0.01f;
		    		 if(co.easing <= 0.f) {
		    			 co.displaymode = ObjectMode.off;
		    			 break clothloop;
		    		 }
		    	 }
			     co.drawDataPacks(mc);
			     co.drawCloth(mc);
		     }
		     
		     ///////////////////////////////////////BUTLER
		     butlerloop:
		     if(bo.displaymode != ObjectMode.off) {
		    	 if(bo.easing < 1.f && bo.displaymode == ObjectMode.in) bo.easing += 0.02f;
		    	 else if(bo.easing >= 1.f && bo.displaymode == ObjectMode.in) bo.displaymode = ObjectMode.run;
		    	 else if(bo.easing > 0. && bo.displaymode == ObjectMode.out) {
		    		 bo.easing -= 0.02f;
		    		 if(bo.easing <= 0.f) {
		    			 bo.displaymode = ObjectMode.off;
		    			 break butlerloop;
		    		 }
		    	 }
		    	 bo.drawButler(mc);
		     }
		     
		     ///////////////////////////////////////VIDEO  
		     videoloop:
		     if(vo.displaymode != ObjectMode.off) {
		    	 if(vo.easing < 1.f && vo.displaymode == ObjectMode.in) vo.easing += 0.04f;
		    	 else if(vo.easing >= 1.f && vo.displaymode == ObjectMode.in) vo.displaymode = ObjectMode.run;
		    	 else if (vo.displaymode == ObjectMode.run) {
	    				PVector currentCamPos = middleSlidePos;
	    				float d = middle.dist(currentCamPos);
						if(d < 200. && vo.vb.lifetime < 1. && vo.vb.impact == false ) {
							vo.vb.lifetime += 0.05;
							if(vo.vb.lifetime >= 1.) {
								vo.vb.impact = true;
							}
						}
		    	 }
		    	 else if(vo.easing > 0. && vo.displaymode == ObjectMode.out) {
		    		//descale video
		    		 if(vo.vb != null) {
			    		 vo.vb.lifetime -= 0.08;
					    	if(vo.vb.lifetime <= 0.) { 
					    		vo.vb.visible = false;
					    		vo.vb = null;
					    	}
		    		 }
		        else if(vo.vb == null) { 
			    		 vo.easing -= 0.08f; 
			    		 //turn off if eased out
			    		 if(vo.easing <= 0.f) {
			    			 vo.displaymode = ObjectMode.off;
			    			 break videoloop;
			    		 }
		    		 }
		    	 }
		    	 
		    	 vo.drawVideo(mc);
 
		     }
		     
		     ///////////////////////////////////////COMMENTS	
		     commentloop:
		     if(co.userC.displaymode != ObjectMode.off) {

		    	 if(co.userC.easing < 1.f && co.userC.displaymode == ObjectMode.in) co.userC.easing += 0.02f;
		    	 else if(co.userC.easing >= 1.f && co.userC.displaymode == ObjectMode.in) co.userC.displaymode = ObjectMode.run;
		    	 else if(co.userC.easing > 0. && co.userC.displaymode == ObjectMode.out) {
		    		 co.userC.easing -= 0.02f;
		    		 if(co.userC.easing <= 0.f) {
		    			 co.userC.displaymode = ObjectMode.off;
		    			 break commentloop;
		    		 }
		    	 }
		    	 int cwidth = co.userC.cWidth;
		    	 mc.camera(mc.width/2.0f, mc.height/2.0f, (mc.height/2.0f) / tan(PI*30.0f / 180.0f), mc.width/2.0f, mc.height/2.0f,0, 0, 1, 0);
		    	 mc.image(co.drawComments(mc),(mc.width/2.f - ((float)cwidth/2.f) ),0);
		     }	     
		     
		     mc.popMatrix();
	     
		    mc.endDraw();
  
		    ///////////////////////////////////////////////////
		    //////////////MAIN CONTENT
		    ///////////////////////////////////////////////////
		    
		    if(fader==true && blackfade < 1.) {
		    	blackfade += 0.01;
		    	if(blackfade > 0.99) blackfade = 1.f;
		    }

		    else if(fader==false && blackfade > 0.) {
		    	blackfade -= 0.01;
		    	if(blackfade < 0.01) blackfade = 0.f;
		    }		    
		    
		    
		    layer.beginDraw();
		    layer.clear();
			//glow.set("iGlobalTime", frameRate * 0.2f); 
			side.set("iTime", frameRate * 0.2f); 
			side.set("black", blackfade);
			layer.shader(side);
			//layer.shader(glow);
		    layer.image(mc,0,0);
		    //layer.resetShader();
		    if(dt.display == true) {
		    	layer.endDraw();
		    	dt.drawDTexture();
		    	layer.beginDraw();
		    	dt.drawShader(layer); 
		    	dt.lifetime += 0.02;
		    	if(dt.lifetime >= 2.) {
		    		dt.display = false;
		    		dt.lifetime = 0.f;
		    		
		    	}
		    }
		    layer.resetShader();
		    layer.endDraw();

			  if(cal.planeObjects != null && cal.planeObjects.size() > 0) {
				  for(PShape p : cal.planeObjects) {
					  if(showtest == true) p.setTexture(testTexture);
					  else if(showtest == false) p.setTexture(layer);
					  shape(p);
				  }
			  }
			 
			if(showtest == true)  image(testTexture,3840,0,1920,1080);
			else if(showtest == false) image(layer,3840,0,1920,1080);
			  
			
			
			 /* 
			else if(showMode == showMode.cam) {
				image(vidC.getCameraImage(),1920+1920*Planes,0);
				
			}
			else if(showMode == showMode.comment) {
				int cwidth = co.userC.cWidth;
				PGraphics comment = createGraphics(1920,1080,P2D);
				comment.beginDraw();
				image(co.drawComments(comment),(comment.width/2.f - ((float)cwidth/2.f) ),0);
				comment.endDraw();
				image(comment,1920+1920*Planes,0);
			}
			  
			  */
			
		    ///////////////////////////////////////////////////
		    ///////////// CONTROL CONTENT
		    ///////////////////////////////////////////////////
			
			textSize(14);
			  
		    image(layer,0,0,960*Planes,540);
		    image(vidC.getCameraImage(),960,0,960,540);
		  
		    textSize(20);
		    textAlign(RIGHT,TOP);

		    String foc = (GP.udp.focusMode == 0) ? "MF" : "AF";
		    text("focus: " + foc,1900,570);
		    
		    text("detection: " + fm.detection,1900,600);


		    
		    //show save button
		    if(runStatus == status.CAMPRE) {
				if(cont.st != null) {
					pushMatrix();
					fill(255,0,0,128);
					noStroke();
					rect(1920-370,540+130,100,100);
					stroke(0);
					noFill();
					text("preset " + cont.st.num,1920-360,540+140);
					popMatrix();
					cont.st.lifetime -= 0.05;
					if(cont.st.lifetime <= 0.) cont.st = null;				
			 	}			
			 }		  
		}				
		
		
		// SHOW FPS STATUS
		if(runStatus == status.RUN || runStatus == status.CAMPRE) {
			stroke(255,255);
			textSize(15);
			textAlign(LEFT,TOP);
		    text("FPS: " + frameRate + " / BO:" + bo.displaymode + " , " + bo.easing,20,570);
		}
	    
	}
	

	
	void oscEvent(OscMessage theOscMessage) {
		if(runStatus == status.RUN) {
		  if(theOscMessage.addrPattern().equals("/butler")) {
			  if(showMode != mode.butler && theOscMessage.arguments().length != 0) startButler();
			  int take = theOscMessage.get(0).intValue(); 
			   bo.butlerData.openTake(take);
		  }
		  if(theOscMessage.addrPattern().equals("/beep")) {
			  int note = theOscMessage.get(0).intValue(); 
			  System.out.println(note);
			  if(note == 6) {
			  co.shake();
			  }  
		  }
		  
		 }
	}
	
	public void sendOsc(int note) {
		  OscMessage myMessage = new OscMessage("/trigger");  
		  myMessage.add(note); 
		  oscP5.send(myMessage, myRemoteLocation); 
	}
	
	
	//////////////////////////////////////////////////////START AND STOP STATES
	
	public void fade(boolean set) {
		
		fader = set;
	}
	
	public void startButler() {
		//shut down vid if running
		if(showMode == mode.cam) {
			vo.closeVideoBlob();
			vo.displaymode = ObjectMode.out;
			showMode = mode.cloth;
		}
		//shutdown comment if running
		if(showMode == mode.comment) {
			co.userC.displaymode = ObjectMode.out;
			co.stopComment();
			showMode = mode.cloth;
		}
		//
		if(bo.displaymode == ObjectMode.off) {
			 showMode = mode.butler;
			 cameraspeed = 0.04f;
			 bo.displaymode = ObjectMode.in;
			 co.displaymode = ObjectMode.out;
			}
	
	}
	
	
	public void stopButler() {
		if(showMode == mode.butler && (bo.displaymode == ObjectMode.run || bo.displaymode == ObjectMode.in)) {
			 bo.displaymode = ObjectMode.out;
			 co.displaymode = ObjectMode.in;
			 showMode = mode.cloth;
			 co.cc = clothCenter.cloth;
		}
		
	}
	
	public void startComments(int comment) {
		if(showMode == mode.butler) {
			stopButler();
			showMode = mode.cloth; 
		}
		else if(showMode == mode.cam) {
			vo.closeVideoBlob();
			vo.displaymode = ObjectMode.out;
		}
		
		 co.cc = clothCenter.comment;
  		 co.newComment(comment);
		 showMode = mode.comment;
		 cameraspeed = 0.03f;
		 co.userC.displaymode = ObjectMode.in;

	}
	
	public void showVideo(int shader) {
		if(showMode == mode.comment) {
			co.stopComment();
			co.userC.displaymode = ObjectMode.out;
		}
		
		if(showMode == mode.butler) {
			stopButler();
			showMode = mode.cloth;
		}

		if(vo.vb == null ||  vo.vb.impact == false ) {
			 showMode = mode.cam;
			 vo.displaymode = ObjectMode.in;
			 cameraspeed = 0.04f;
			 speedchange = 0;
			 vo.newVideoBlob(1,co.nodecount*co.nodecount,shader);
		  }
	}
	
	public void stopVideo() {
		 vo.closeVideoBlob();
	 	 vo.displaymode = ObjectMode.out;
	}
	
	public void showCloth() {
		if(showMode == mode.cam) {
			vo.closeVideoBlob();
			vo.displaymode = ObjectMode.out;
		}
		if(showMode == mode.comment) {
			co.stopComment();
			co.userC.displaymode = ObjectMode.out;
		}
  		  	
		showMode = mode.cloth;
		co.DISPLAY_MESH = true;
  		co.cc = clothCenter.cloth;
  		co.displaymode = ObjectMode.in;
  		if(bo.displaymode != ObjectMode.off && bo.displaymode != ObjectMode.out) bo.displaymode = ObjectMode.out;
 
	}
	/*
	public void showEmpty() {
		if(showMode == mode.comment) {
			co.stopComment();
		}
		if(showMode == mode.cam) {
			vo.closeVideoBlob();
			vo.displaymode = ObjectMode.out;
		}
		if(co.displaymode != ObjectMode.off)  co.displaymode = ObjectMode.out;
			 if(bo.displaymode != ObjectMode.off)  bo.displaymode = ObjectMode.out;
			 showMode = mode.empty;
	}
	*/
	public void changeCloth(int id) {
		cstates.loadState(id);
	}
	
	
	public void keyPressed() {
		  switch(key) {

		  case '1' : showCloth();
		  		     return;		 
		  case 'q' : co.reState();
			 		 return;  			 
		  case 'w' : co.shake();
					 return;
		  case 'e' : co.reshake();
					 return;
		  case 'r' : co.mark();
			 	     return;
		  case 't' : co.textureAlpha = (co.textureAlpha == 0.f) ? 1.f : 0.f;
		  			 return;				 		 
			 		 
		  case 'a' : mm.switchCh(0);
		  			 return;
		  case 's' : mm.switchCh(1);
			 		 return;		 
		  case 'd' : mm.switchCh(2);
		  			 return;
		  case 'f' : mm.switchCh(3);
		  		     return;		  			 

		  case 'g' : mm.switchPreset(0);
		  			 return;
		  case 'h' : mm.switchPreset(1);
		     	     return;
		     	     
		  case 'c' : showtest = !showtest;
		  			 return;
		  }  
	  }
	
	void showInsert(int i) {
		dt.contentID = i;
		dt.display = true;
		dt.setupTex();
	}

	public void mousePressed(){
		if(runStatus == status.RUN) {
			if (mouseButton == LEFT && showMode == mode.cloth) {
				  // co.cc = clothCenter.random();
			  }	  
		}
		
		else if(runStatus == status.CALI && (mouseX>210 && mouseX<cal.cali.width+210) && (mouseY > 0 && mouseY < cal.cali.height)) {
		   cal.mouseP = true;
		   int col = get(mouseX, mouseY);
		   cal.currenthandle = red(col);
		}
		
	}
	
	public void mouseReleased(){
		if(runStatus == status.RUN) {
		   if (mouseButton == LEFT){
			mousePress = false;
			rotx += dy;
		    roty += dx;
		    dx = 0;
		    dy = 0;
		   }
		  else if (mouseButton == RIGHT){
			shiftSpace = false;
		    shiftx += sx;
		    shiftz += sz;
		    sx = 0;
		    sz = 0;
		  }
		}
		else if(runStatus == status.CALI) {
			cal.mouseP = false;
		}
	}
	
	
	
	
	
}


