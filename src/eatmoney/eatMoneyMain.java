package eatmoney;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.Point2fVector;

import butler.Butler;
import calibrate.Calibrate;
import calibrate.eatMoneyController;
import controlP5.ControlP5;
import enums.mode;
import enums.status;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.video.Capture;
import videoCapture.FaceMark;
import videoCapture.VideoCaptureTool;

public class eatMoneyMain extends PApplet {
	
	//global Vars
	public String ppath = "";
	PGraphics mc;
	OscP5 oscP5;
	NetAddress myRemoteLocation;
	ControlP5 mainControl;

	FaceMark fm;
    VideoCaptureTool vidC;
	
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
	float changespeed = 0.05f;
	float fov = 60; 
	
	Butler butler;
	
	public status runStatus = status.PRE;
	public mode showMode = mode.cam;
	
	int videoWidth = 1280;
	int videoHeight = 720;
	

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
		smooth();

		cont = new eatMoneyController(this,this);	
		oscP5 = new OscP5(this,7000);	
		butler = new Butler(this);
	
		

	}
	
	public void setupCalibration(String filename, boolean newCalibration) {
		cal = new Calibrate(this,Planes,filename,newCalibration,mc);	
	}
	
	public void setupMain() {
		Planes = cal.totalcount;
		mc = createGraphics(1920*Planes,height,P3D);
		
		vidC = new VideoCaptureTool(this);
		fm = new FaceMark(this,vidC.cam);
		
		
		mc.stroke(0);
		mc.perspective(radians(fov),(float)1920*Planes/(float)height,10,15000);
		 
		for(PShape p : cal.planeObjects) {
			 p.setTexture(mc);
		}

		camTarget = new PVector(width/2.0f, height/2.0f,0);
		camTargetnew = new PVector(width/2.0f, height/2.0f,0);
		camPos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		camPosnew = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
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
		else if(runStatus == status.RUN) {
			if(mousePress == true){
				  dx = (mouseclick.x - mouseX) * 0.01f;
				  dy = (mouseclick.y - mouseY) * 0.01f;
			  }
			  if(shiftSpace == true){
				  sx = (mouseclick.x - mouseX);
				  sz = (mouseclick.y - mouseY);
			    }
			  
			  if(random(1.f)>0.95f) {
				  camPosnew = new PVector((random(1.f) *1600)-800.f,(random(1.f) * 200)-70,(random(1.f) *1200)-200.f);
				  changespeed = random(0.01f,0.5f);
			  }
			  
			  if(showMode == mode.butler) {
				  camTarget.x = lerp(camTarget.x,butler.butlerMean.x,changespeed);
				  camTarget.y = lerp(camTarget.y,butler.butlerMean.y,changespeed);
				  camTarget.z = lerp(camTarget.z,butler.butlerMean.z,changespeed);
			  }
			  
			  else if(showMode == mode.cam) {
				  
				  
			  }
			  
			  camPos.x = lerp (camPos.x,camPosnew.x,changespeed*0.1f);
			  camPos.y = lerp (camPos.y,camPosnew.y,changespeed*0.1f); 
			  camPos.z = lerp (camPos.z,camPosnew.z,changespeed*0.1f);
			
			 mc.beginDraw();
			 mc.clear();
			 mc.background(16);
			 
			 mc.perspective(radians(fov),(float)1920*Planes/(float)height,0.1f,4500);
			 mc.camera(camPos.x, camPos.y, camPos.z, camTarget.x, camTarget.y, camTarget.z, 0, 1, 0);		
			 //camera(map(mouseX,0,1920,800,800), 2, map(mouseY,0,1080,0,1400), camTarget.x, camTarget.y, camTarget.z, 0, 1, 0);
			 //lights();
			 mc.directionalLight(200, 200, 200, 0, 1, 0);
			 mc.lightFalloff(1.2f, 0, 0);		 
			 mc.pointLight(180, 140, 40, camPos.x*1.2f, camPos.y*1.2f, camPos.z );
			 //translate(width/2, height/2, 0);
			 mc.noStroke();
   
			 if(showMode == mode.butler) {
			   butler.drawButler(mc);
			 }
			 
			 
			 
			 mc.stroke(255);
			 mc.fill(255);
			 mc.endDraw();
	  
			  //show Main content
			  if(cal.planeObjects != null && cal.planeObjects.size() > 0) {
				  for(PShape p : cal.planeObjects) {
					  shape(p);
				  }
			  }
			  
			  //show Control Content
			  image(mc,0,0,960*Planes,540);
			  image(vidC.getCameraImage(),640,340);
			  //text("detection:" + fm.detections,1290,340);
			  
			  pushMatrix();
			  translate(640,340);
			  for (long i = 0; i < fm.landmarks.size(); i++) {
                  Point2fVector v = fm.landmarks.get(i);
                  for(long j = 0; j < v.size();j++) {
                      Point2f xop = v.get(j);
                      fill(255);
                      strokeWeight(4);
                      point(xop.x(),xop.y());
                  }
                 
			  }
			  popMatrix();
			  
		}
				
		stroke(255,255);
		text("FPS: " + frameRate,10,height-20);

	}
	

	
	void oscEvent(OscMessage theOscMessage) {
		if(runStatus == status.RUN) {
		  if(theOscMessage.addrPattern().equals("/butler")) {
			  int take = theOscMessage.get(0).intValue(); 
			  butler.butlerData.openTake(take);
		  }
		  if(theOscMessage.addrPattern().equals("/band")) {
			  for(int i = 0; i < 8; i++) {
				  butler.butlerData.deformMatrix[i] = theOscMessage.get(i).floatValue();
			  }
		  
		  }
		 }
	}
	
	public void keyPressed() {
		
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
	
	public void mousePressed(){
		if(runStatus == status.RUN) {
			  if (mouseButton == LEFT){
				  mousePress = true;
				  mouseclick.x = mouseX;
				  mouseclick.y = mouseY;
			  }
			  else if (mouseButton == RIGHT){
				  shiftSpace = true;
				  mouseclick.x = mouseX;
				  mouseclick.y = mouseY;
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
