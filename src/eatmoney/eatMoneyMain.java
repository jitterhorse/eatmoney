package eatmoney;

import java.util.ArrayList;

import butler.ButlerObject;
import calibrate.Calibrate;
import calibrate.eatMoneyController;
import controlP5.ControlP5;
import enums.Follow;
import enums.mode;
import enums.status;
import gamePTZ.Gamepad;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PShader;
import videoCapture.FaceMark;
import videoCapture.VideoCaptureTool;

public class eatMoneyMain extends PApplet {
	
	//global Vars
	public String ppath = "";
	PGraphics mc;
	PGraphics layer;
	OscP5 oscP5;
	NetAddress myRemoteLocation;
	ControlP5 mainControl;

	public FaceMark fm;
    VideoCaptureTool vidC;

    
	public Gamepad GP;
	
	boolean firstinit = false; // if a first calibration was finished
    
	public int mainDisplayWidth = 1920;
	
	//create controller surface
	eatMoneyController cont;
	
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
	float cameraspeed = 0.02f;
	float fov = 60; 
	
	ButlerObject bo;
	ClothObject co;
	LightRig lightRig;
	displaceTexture dt;
	
	boolean viewFPS = true;
	  
	public status runStatus = status.PRE;
	public mode showMode = mode.cam;

	PShader glow;
	Follow follow = Follow.cloth;
	
    PVector cameraSlidePos = new PVector(0,0,0);
    PVector cameraNewPos = new PVector(0,0,0);
    PVector middleSlidePos = new PVector(0,0,0);	
	
	
	public static void main(String[] args) {
		PApplet.main("eatmoney.eatMoneyMain");
	}

	public void settings() {
		fullScreen(P3D,SPAN);  
		//size(3840,1080,P3D);
		ppath = sketchPath();
	}
	
	public void setup() {
		frameRate(30);

		cont = new eatMoneyController(this,this);	
		oscP5 = new OscP5(this,7000);	
		bo = new ButlerObject(this);
	
		GP = new Gamepad(this);

	}
	
	public void setupCalibration(String filename, boolean newCalibration) {
		cal = new Calibrate(this,Planes,filename,newCalibration,layer);	
	}
	
	
	public void setupMain() {
		if(firstinit == false) {
			Planes = cal.totalcount;
			
			
			mc = createGraphics(1920*Planes,height,P3D);
			layer = createGraphics(1920*Planes,height,P2D);
			
			co = new ClothObject(mc,this,this);
			mc.smooth(8);
			
			vidC = new VideoCaptureTool(this);
			fm = new FaceMark(this,vidC.cam);
			
			lightRig = new LightRig(mc,this);
			dt = new displaceTexture(this,1920*Planes,height);
			
			glow = loadShader("shader\\glow.glsl"); 
		    glow.set("iResolution", (float)mc.width, (float)mc.height);
			
			
			mc.stroke(0);
			mc.perspective(radians(fov),(float)1920*Planes/(float)height,10,15000);
			 
			for(PShape p : cal.planeObjects) {
				 p.setTexture(layer);
			}
			firstinit = true;
		}

		camTarget = new PVector(width/2.0f, height/2.0f,0);
		camTargetnew = new PVector(width/2.0f, height/2.0f,0);
		camPos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		camPosnew = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
	}
	
	
	public void draw() {
		 
		GP.update();
		
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
			
		  	if(Math.random() > 0.99) cameraspeed = random(0.1f) + 0.02f;
			  
		  	co.calcCloth();

		  	PVector middle = new PVector();
		  	
		  	if(follow == Follow.cloth) {
		  		cameraspeed = 0.02f;
		  		middle = co.getClothMiddle();
		  		cameraNewPos.x = 0;
				cameraNewPos.y = 0;
				cameraNewPos.z = -400;
		  	}	   
		  	else if(follow == Follow.pin) {  
		  		cameraspeed = 0.02f;
		  		middle = co.getPinMiddle();
			    cameraNewPos.x = middle.x+100;
			    cameraNewPos.y = middle.y+100;
			    cameraNewPos.z = middle.z-400;				     
			}
		  	else if(follow == Follow.emit) {
		  		cameraspeed = 0.02f;
		  		 middle = co.getEmitMiddle();
		  		 if(co.currentExposes.size() > 0) {
		  			 if(co.currentExposes.get(0).lifetime <= 0) follow = Follow.cloth;
		  		 }
		  		 cameraNewPos.x = middle.x+100;
				 cameraNewPos.y = middle.y+100;
				 cameraNewPos.z = middle.z-100;
		  	}
		  	else if(follow == Follow.comment) {
		  		 middle = co.getCommentMiddle();
		  		 
		  		 //auto step to new state after comments over
		  		 /*if(co.currentComments.size() <= 0) {
		  			 follow = Follow.cloth;
		  			 cameraspeed = 0.02f;
		  		 }*/
		  		 cameraNewPos.x = middle.x+400;
				 cameraNewPos.y = middle.y+400;
				 cameraNewPos.z = middle.z-100;
		  	}
		  	
		  	else if(follow == Follow.video) {
		  		 middle = co.getVideoMiddle();
		  		 float dirx = 1.f, diry = 1.f, dirz = 1.f;
		  		 
		  		 if(middle.x < 0) dirx = -1.f;
		  		 if(middle.y < 0) diry = -1.f;
		  		 if(middle.z < 0) dirz = -1.f;
		  		 
		  		 cameraNewPos.x = middle.x+220*dirx;
				 cameraNewPos.y = middle.y+220*diry;
				 cameraNewPos.z = middle.z+220*dirz;
		  		 
		  		 
		  	}
		  	
		  	else if(follow == Follow.butler) {
		  		middle = bo.butlerMean;
		  		if(random(1.f)>0.95f) {
  				  camPosnew = new PVector((random(1.f) *1600)-800.f,(random(1.f) * 200)-70,(random(1.f) *1200)-200.f);
  				  cameraspeed = random(0.01f,0.05f);
  				  bo.camoffset.x = random(-400,400);
  				  bo.camoffset.y = random(-40,40);
  				  bo.camoffset.z = random(-290,-140);
		  		}
			
		  		
		  		cameraNewPos.x = middle.x+bo.camoffset.x;
				cameraNewPos.y = middle.y+bo.camoffset.y;
				cameraNewPos.z = middle.z+bo.camoffset.z;
	
		  	}
			
			 mc.beginDraw();
			 mc.pushMatrix();
			 mc.clear();
			 mc.background(0);
			 
		 	 cameraSlidePos.x = lerp(cameraSlidePos.x,cameraNewPos.x,(float) cameraspeed);
		     cameraSlidePos.y = lerp(cameraSlidePos.y,cameraNewPos.y,(float) cameraspeed);
		     cameraSlidePos.z = lerp(cameraSlidePos.z,cameraNewPos.z,(float) cameraspeed);
		    
		     middleSlidePos.x = lerp(middleSlidePos.x,middle.x,(float) 0.02);
		     middleSlidePos.y = lerp(middleSlidePos.y,middle.y,(float) 0.02);
		     middleSlidePos.z = lerp(middleSlidePos.z,middle.z,(float) 0.02);

		     lightRig.doLight();
		    
		     mc.camera(cameraSlidePos.x, cameraSlidePos.y,cameraSlidePos.z,middleSlidePos.x,middleSlidePos.y,middleSlidePos.z, 0, 1, 0);
		     mc.perspective(PI/2.0f, (float)mc.width/(float)mc.height, 0.1f, 50000.f);
		     
		     
		     //co.drawOutline(mainContent);
		     
		     mc.strokeWeight(2);
		     mc.fill(23);
		     mc.noStroke();
		     mc.sphere(5000);

		     bo.drawButler(mc);
		     
		     co.drawDataPacks(mc);
		     co.drawCloth(mc);

		     
		     
		     mc.popMatrix();
		    
		    
		    mc.endDraw();
  
		    ///////////////////////////////////////////////////
		    //////////////MAIN CONTENT
		    ///////////////////////////////////////////////////
		    
		    
		    layer.beginDraw();
		    layer.clear();
			glow.set("iGlobalTime", frameRate * 0.2f); 
			layer.shader(glow);
		    layer.image(mc,0,0);
		    layer.resetShader();
			dt.drawDTexture();
			dt.drawShader(layer); 
		    layer.endDraw();
		    
		    clear();
			//glow.set("iGlobalTime", frameRate * 0.2f); 
			//shader(glow);
			  if(cal.planeObjects != null && cal.planeObjects.size() > 0) {
				  for(PShape p : cal.planeObjects) {
					  shape(p);
				  }
			  }

				
			//resetShader();
			
		    ///////////////////////////////////////////////////
		    ///////////// CONTROL CONTENT
		    ///////////////////////////////////////////////////
			
			
		    image(layer,0,0,960*Planes,540);
		    image(vidC.getCameraImage(),1920-640,540,640,360);
		  
		    text("LM: " + fm.landmarks.size(),1920-640,540+370);

		    
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
				
		stroke(255,255);
		if(viewFPS == true) {
	    	text("FPS: " + frameRate + " / " + follow ,50,50);
	    }

	}
	

	
	void oscEvent(OscMessage theOscMessage) {
		if(runStatus == status.RUN) {
		  if(theOscMessage.addrPattern().equals("/butler")) {
			  int take = theOscMessage.get(0).intValue(); 
			  bo.butlerData.openTake(take);
		  }
		  if(theOscMessage.addrPattern().equals("/band")) {
			  for(int i = 0; i < 8; i++) {
				  bo.butlerData.deformMatrix[i] = theOscMessage.get(i).floatValue();
			  }
		  
		  }
		 }
	}
	
	public void keyPressed() {
		  switch(key) {
		  case 's' : viewFPS = !viewFPS;
		  			 return;
		  case 'r' : co.reState();
		  			 return;  			 
		  case 'w' : co.shake();
		  			 return;
		  case 'q' : co.reshake();
		  			 return;
		  case 'm' : co.mark();
			 	     return;			  	
		  case 'n' : co.newComment(1);
		  			 follow = Follow.comment;
		  			 cameraspeed = 0.3f;
		  			 return;
		  case 'v' : follow = Follow.video;
		  			 if(co.vb == null ||  co.vb.impact == false ) {
		  				 co.newVideoBlob(1);
		  			 }
		  			 else if(co.vb.impact == true) {
		  				 co.closeVideoBlob();
		  		     }	
		  			 return;
		  case 'b' : follow = Follow.butler;
		  			 return;
		  case 'h' : bo.butlerData.openTake(bo.testvars[bo.nextvid]);
			  		 bo.nextvid++;
			 		 if(bo.nextvid>=bo.testvars.length) bo.nextvid = 0;
					 return;		 
		  			 
		  
		  }  
	  }
	
	public void mousePressed(){
		if(runStatus == status.RUN) {
			if (mouseButton == LEFT) {
				   follow = Follow.randomFollow();
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

/*
 * public void keyPressed() {
		
		if(runStatus == status.RUN) {
			 switch(key) {
			     case 'd': fm.setDetection(!fm.detection);
			     		   return;
				 case 'e' : butler.butlerData.openTake(butler.testvars[butler.nextvid]);
					 		butler.nextvid++;
					 		if(butler.nextvid>=butler.testvars.length) butler.nextvid = 0;
				 			return;
				 case 'b' : showMode = mode.butler;
				 			butler.butlerData.initButler();
							return;
				 case 'v' : showMode = mode.cam;
							 Executors.newSingleThreadExecutor().execute(new Runnable() {
								    @Override
								    public void run() {
								    	try {
											Thread.sleep(1000);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}	
								    	butler.butlerData.stopButler();
								    }
								});
				 			return;
				 
			 }
		}
	}
	*/
