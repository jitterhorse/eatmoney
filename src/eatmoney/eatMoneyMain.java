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
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
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
	VideoObject vo;
	LightRig lightRig;
	displaceTexture dt;
	
	  
	public status runStatus = status.PRE;
	public mode showMode = mode.cloth;

	PShader glow;
	
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
		vo = new VideoObject(this);
		GP = new Gamepad(this);

	}
	
	public void setupCalibration(String filename, boolean newCalibration) {
		cal = new Calibrate(this,Planes,filename,newCalibration,layer);	
	}
	
	
	public void setupMain() {
		if(firstinit == false) {

			
			vidC = new VideoCaptureTool(this);
			fm = new FaceMark(this,vidC.cam);
			

			firstinit = true;
		}
		
		Planes = cal.totalcount;

		mc = createGraphics(1920*Planes,height,P3D);
		layer = createGraphics(1920*Planes,height,P2D);
		
		co = new ClothObject(mc,this,this);
		mc.smooth(8);
		
		lightRig = new LightRig(mc,this);
		dt = new displaceTexture(this,1920*Planes,height);
		
		glow = loadShader("shader\\glow.glsl"); 
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
			  		cameraspeed = 0.02f;
			  		middle = co.getClothMiddle();
			  		cameraNewPos.x = 0;
					cameraNewPos.y = 0;
					cameraNewPos.z = -400;
		  		}
		  		else if(co.cc == clothCenter.pin) {
			  		cameraspeed = 0.02f;
			  		middle = co.getPinMiddle();
				    cameraNewPos.x = middle.x+100;
				    cameraNewPos.y = middle.y+100;
				    cameraNewPos.z = middle.z-400;				     
		  		}
		  		else if(co.cc == clothCenter.emit) {
		  		cameraspeed = 0.02f;
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
		  		 
		  		 //auto step to new state after comments over
		  		 /*if(co.currentComments.size() <= 0) {
		  			 follow = Follow.cloth;
		  			 cameraspeed = 0.02f;
		  		 }*/
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
		  		middle = bo.butlerMean;
		  		middle.add(bo.ButlerOffset);
		  		if(random(1.f)>0.95f) {
  				  camPosnew = new PVector((random(1.f) *1600)-800.f,(random(1.f) * 200)-70,(random(1.f) *1200)-200.f);
  				  camPosnew.add(bo.ButlerOffset);
  				  cameraspeed = random(0.01f,0.05f);
  				  bo.camoffset.x = random(-400,400);
  				  bo.camoffset.y = random(-40,40);
  				  bo.camoffset.z = random(-290,-40);
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
		    
		     middleSlidePos.x = lerp(middleSlidePos.x,middle.x,(float) cameraspeed);
		     middleSlidePos.y = lerp(middleSlidePos.y,middle.y,(float) cameraspeed);
		     middleSlidePos.z = lerp(middleSlidePos.z,middle.z,(float) cameraspeed);

		     lightRig.doLight();
		    
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
		    	 if(bo.easing < 1.f && bo.displaymode == ObjectMode.in) bo.easing += 0.01f;
		    	 else if(bo.easing >= 1.f && bo.displaymode == ObjectMode.in) bo.displaymode = ObjectMode.run;
		    	 else if(bo.easing > 0. && bo.displaymode == ObjectMode.out) {
		    		 bo.easing -= 0.01f;
		    		 if(bo.easing <= 0.f) {
		    			 bo.displaymode = ObjectMode.off;
		    			 showMode = mode.cloth;
		    			 break butlerloop;
		    		 }
		    	 }
		    	 bo.drawButler(mc);
		     }
		     
		     ///////////////////////////////////////VIDEO  
		     videoloop:
		     if(vo.displaymode != ObjectMode.off) {
		    	 if(vo.easing < 1.f && vo.displaymode == ObjectMode.in) vo.easing += 0.01f;
		    	 else if(vo.easing >= 1.f && vo.displaymode == ObjectMode.in) vo.displaymode = ObjectMode.run;
		    	 else if (vo.displaymode == ObjectMode.run) {
	    				PVector currentCamPos = middleSlidePos;
	    				float d = middle.dist(currentCamPos);
						if(d < 200. && vo.vb.lifetime < 1. && vo.vb.impact == false ) {
							vo.vb.lifetime += 0.01;
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
			    			 showMode = mode.cloth;
			    			 break videoloop;
			    		 }
		    		 }
		    	 }
		    	 
		    	 vo.drawVideo(mc);
 
		     }
		     
		     
		     mc.popMatrix();
		     
		    
			//draw comments in middle of screen
				if(showMode == mode.comment) {
					int cwidth = co.userC.cWidth;
					mc.camera(mc.width/2.0f, mc.height/2.0f, (mc.height/2.0f) / tan(PI*30.0f / 180.0f), mc.width/2.0f, mc.height/2.0f,0, 0, 1, 0);
					mc.image(co.drawComments(mc),(mc.width/2.f - ((float)cwidth/2.f) ),0);
				}
		     
		     
		   
		     
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
			
			textSize(10);
			  
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
		int f = 0;
		if(co != null && co.currentComments != null) {
			f = co.currentComments.size();
		}
	    text("FPS: " + frameRate + " / " + showMode  +" / " + f,50,50);
	    
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
	
	
	//////////////////////////////////////////////////////START AND STOP STATES
	
	public void startButler() {
		//shut down vid if running
		if(showMode == mode.cam) {
			vo.closeVideoBlob();
			vo.displaymode = ObjectMode.out;
			showMode = mode.cloth;
		}
		//shutdown comment if running
		if(showMode == mode.comment) {
			co.stopComment();
			showMode = mode.cloth;
		}
		//
		if(bo.displaymode == ObjectMode.off) {
			 showMode = mode.butler;
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
		 cameraspeed = 0.3f;

	}
	
	public void showVideo() {
		if(showMode == mode.comment) {
			co.stopComment();
		}
		
		if(showMode == mode.butler) {
			stopButler();
			showMode = mode.cloth;
		}

		if(vo.vb == null ||  vo.vb.impact == false ) {
			 showMode = mode.cam;
			 vo.displaymode = ObjectMode.in;
			 vo.newVideoBlob(1,co.nodecount*co.nodecount);
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
		}
  		  	
		showMode = mode.cloth;
  		co.cc = clothCenter.cloth;
  		co.displaymode = ObjectMode.in;
  		if(bo.displaymode != ObjectMode.off && bo.displaymode != ObjectMode.out) bo.displaymode = ObjectMode.out;
 
	}
	
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
	
	public void keyPressed() {
		  switch(key) {
		  case '0' : showEmpty();
			 	     return;

		  case '1' : showCloth();
		  		     return;
		  		     
		  case '2' : showVideo();
		  			 return;
		  		     
		  case '3' : startComments(1);
		  			 return;
		  			 	 
		  case '4' : startButler();
		  			 return;	
		  			 
		  case 'b' : if(bo.displaymode == ObjectMode.run) {
				  		 bo.butlerData.openTake(bo.nextvid);
				  		 bo.nextvid++;
				 		 if(bo.nextvid>=73) bo.nextvid = 20;
		  			 }
					 return;			 		 

		  			 
		  case 'q' : co.reState();
			 		 return;  			 
		  case 'w' : co.shake();
					 return;
		  case 'e' : co.reshake();
					 return;
		  case 'r' : co.mark();
			 	     return;	
	 	     
		  }  
	  }
	
	public void mousePressed(){
		if(runStatus == status.RUN) {
			if (mouseButton == LEFT && showMode == mode.cloth) {
				   co.cc = clothCenter.random();
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
