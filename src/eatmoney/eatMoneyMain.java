package eatmoney;

import eatmoney.readData.State;
import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;
import processing.core.PVector;

public class eatMoneyMain extends PApplet {
	
	//global Vars
	String ppath = "";

	OscP5 oscP5;
	NetAddress myRemoteLocation;
	
	//mouse vars
	boolean mousePress = false;
	float rotx, roty = 0;
	PVector mouseclick = new PVector(0,0,0);
	float dx,dy,sx,sz = 0;
	
	boolean shiftSpace = false;
	float shiftx,shiftz = 0;


	//Bulter Vars
	int [] rawData;
	int take = 0;
	boolean playbutler = false;
	readData butler;
	
	PVector butlerMin = new PVector(0,0,0);
	PVector butlerMax = new PVector(0,0,0);
	PVector butlerMean = new PVector(0,0,0);
	
	//MeshVars
	int       i00, i01, i10, i11; // indices
	PVector   p00, p10, p01, p11; // points
	int       kdh = 424;
	int       kdw = 512;
	int       max_edge_len = 25;
	int steps = 2;
	
	float depthscale = 0.7f;
	float minRange = 500;
	float maxRange = 1800 * depthscale;
	
	float rotX = PApplet.radians(0);
	float rotY = PApplet.radians(180);	
	
	//camera vars
	PVector camTarget ;
	PVector camTargetnew ;
	PVector camPos;
	PVector camPosnew;
	float changespeed = 0.05f;
	float fov = 60; 
	
	//testvars
	
	int[] testvars = {10,14,15,19,20,24,26,30,32,35,36,42,46,49,53,56,59,62,64,66,70,71,78,81};
	int nextvid = 0;
	
	public static void main(String[] args) {
		PApplet.main("eatmoney.eatMoneyMain");

	}

	public void settings() {
		//size(1400,768,P3D);
		fullScreen(P3D,2);  
		ppath = sketchPath();
	}
	
	public void setup() {
		
		camTarget = new PVector(width/2.0f, height/2.0f,0);
		camTargetnew = new PVector(width/2.0f, height/2.0f,0);
		camPos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		camPosnew = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*30.0f / 180.0f));
		
		frameRate(30);
		butler = new readData(this);
		oscP5 = new OscP5(this,7000);
		smooth();
		stroke(0);
		perspective(radians(fov),(float)width/(float)height,10,15000);
	}
	
	public void draw() {
		 
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
		  
		  
		  camTarget.x = lerp(camTarget.x,butlerMean.x,changespeed);
		  camTarget.y = lerp(camTarget.y,butlerMean.y,changespeed);
		  camTarget.z = lerp(camTarget.z,butlerMean.z,changespeed);

		  camPos.x = lerp (camPos.x,camPosnew.x,changespeed*0.1f);
		  camPos.y = lerp (camPos.y,camPosnew.y,changespeed*0.1f); 
		  camPos.z = lerp (camPos.z,camPosnew.z,changespeed*0.1f);
		
		
		 clear();
		 background(16);
		 
		 perspective(radians(fov),(float)width/(float)height,0.1f,4500);
		 camera(camPos.x, camPos.y, camPos.z, camTarget.x, camTarget.y, camTarget.z, 0, 1, 0);		
		 //camera(map(mouseX,0,1920,800,800), 2, map(mouseY,0,1080,0,1400), camTarget.x, camTarget.y, camTarget.z, 0, 1, 0);
		 //lights();
		 directionalLight(200, 200, 200, 0, 1, 0);
		 lightFalloff(1.2f, 0, 0);		 
		 pointLight(180, 140, 40, camPos.x*1.2f, camPos.y*1.2f, camPos.z );
		 //translate(width/2, height/2, 0);
		 noStroke();
		 
		
		  
		  //rotateX(rotx + dy);
		  //rotateY(roty + dx);
		  //translate(shiftx + sx,0,shiftz + sz);
		    
		 
		  if(butler.state == State.mix1 || butler.state == State.mix2 || butler.state == State.inmix){ 

		  rawData = butler.readFrame();
		 
		  pushMatrix();
		  //rotateX(rotX);
		  //rotateY(rotY);

		 butlerMin = new PVector(4500,4500,4500);
		 butlerMax = new PVector(0,0,0);
		  
		 for(int y=0; y < kdh-steps;y+=steps)
		  {
			int y_kdw = y * kdw; 
		    int y_steps_kdw = (y+steps)*kdw;
		  
		    
		    for(int x=0;x < kdw-steps;x+=steps)
		    {

	    	  i00 = x + y_kdw;
	    	  i01 = x + y_steps_kdw;
	    	  i10 = (x + steps) + y_kdw;
	          i11 = (x + steps) + y_steps_kdw;

		      p00 = depthToWorld(x,y,rawData[i00]);
		      p01 = depthToWorld(x,y+steps,rawData[i01]);
		      p10 = depthToWorld(x+steps,y,rawData[i10]);
		      p11 = depthToWorld(x+steps,y+steps,rawData[i11]);

		      beginShape(TRIANGLES);  
		      fill(255);
		      if ((p00.z > 0) && (p01.z > 0) && (p10.z > 0) && // check for non valid values
		          (abs(p00.z-p01.z) < max_edge_len) && (abs(p10.z-p01.z) < max_edge_len) &&// check for edge length
		          (p00.z < maxRange) && p00.z > minRange) {  // depth cut
		    	  vertex(p00.x,p00.y,p00.z); 
		    	  vertex(p01.x,p01.y,p01.z);
		    	  vertex(p10.x,p10.y,p10.z);
		    	  butlerMin.x = (p00.x < butlerMin.x) ? p00.x : butlerMin.x;
		    	  butlerMin.y = (p00.y < butlerMin.y) ? p00.y : butlerMin.y;
		    	  butlerMin.z = (p00.z < butlerMin.z) ? p00.z : butlerMin.z;
		    	  butlerMin.x = (p01.x < butlerMin.x) ? p01.x : butlerMin.x;
		    	  butlerMin.y = (p01.y < butlerMin.y) ? p01.y : butlerMin.y;
		    	  butlerMin.z = (p01.z < butlerMin.z) ? p01.z : butlerMin.z;
		    	  butlerMin.x = (p10.x < butlerMin.x) ? p10.x : butlerMin.x;
		    	  butlerMin.y = (p10.y < butlerMin.y) ? p10.y : butlerMin.y;
		    	  butlerMin.z = (p10.z < butlerMin.z) ? p10.z : butlerMin.z;
		    	  
		    	  butlerMax.x = (p00.x > butlerMax.x) ? p00.x : butlerMax.x;
		    	  butlerMax.y = (p00.y > butlerMax.y) ? p00.y : butlerMax.y;
		    	  butlerMax.z = (p00.z > butlerMax.z) ? p00.z : butlerMax.z;
		    	  butlerMax.x = (p01.x > butlerMax.x) ? p01.x : butlerMax.x;
		    	  butlerMax.y = (p01.y > butlerMax.y) ? p01.y : butlerMax.y;
		    	  butlerMax.z = (p01.z > butlerMax.z) ? p01.z : butlerMax.z;
		    	  butlerMax.x = (p10.x > butlerMax.x) ? p10.x : butlerMax.x;
		    	  butlerMax.y = (p10.y > butlerMax.y) ? p10.y : butlerMax.y;
		    	  butlerMax.z = (p10.z > butlerMax.z) ? p10.z : butlerMax.z;
		    	  
		          }
		      if ((p11.z > 0) && (p01.z > 0) && (p10.z > 0) &&
		          (abs(p11.z-p01.z) < max_edge_len) && (abs(p10.z-p01.z) < max_edge_len) &&
		          (p11.z < maxRange) && p00.z > minRange){
		    	  vertex(p01.x,p01.y,p01.z);
		    	  vertex(p11.x,p11.y,p11.z);
		    	  vertex(p10.x,p10.y,p10.z);
		    	  butlerMin.x = (p11.x < butlerMin.x) ? p11.x : butlerMin.x;
		    	  butlerMin.y = (p11.y < butlerMin.y) ? p11.y : butlerMin.y;
		    	  butlerMin.z = (p11.z < butlerMin.z) ? p11.z : butlerMin.z;
		    	  butlerMin.x = (p01.x < butlerMin.x) ? p01.x : butlerMin.x;
		    	  butlerMin.y = (p01.y < butlerMin.y) ? p01.y : butlerMin.y;
		    	  butlerMin.z = (p01.z < butlerMin.z) ? p01.z : butlerMin.z;
		    	  butlerMin.x = (p10.x < butlerMin.x) ? p10.x : butlerMin.x;
		    	  butlerMin.y = (p10.y < butlerMin.y) ? p10.y : butlerMin.y;
		    	  butlerMin.z = (p10.z < butlerMin.z) ? p10.z : butlerMin.z;
		    	  
		    	  butlerMax.x = (p11.x > butlerMax.x) ? p11.x : butlerMax.x;
		    	  butlerMax.y = (p11.y > butlerMax.y) ? p11.y : butlerMax.y;
		    	  butlerMax.z = (p11.z > butlerMax.z) ? p11.z : butlerMax.z;
		    	  butlerMax.x = (p01.x > butlerMax.x) ? p01.x : butlerMax.x;
		    	  butlerMax.y = (p01.y > butlerMax.y) ? p01.y : butlerMax.y;
		    	  butlerMax.z = (p01.z > butlerMax.z) ? p01.z : butlerMax.z;
		    	  butlerMax.x = (p10.x > butlerMax.x) ? p10.x : butlerMax.x;
		    	  butlerMax.y = (p10.y > butlerMax.y) ? p10.y : butlerMax.y;
		    	  butlerMax.z = (p10.z > butlerMax.z) ? p10.z : butlerMax.z;
		          }
		      endShape();

		   }
		  }
		 

		 popMatrix();
		 }

		  butlerMean.x = lerp(butlerMin.x,butlerMax.x, 0.5f);
		  butlerMean.y = lerp(butlerMin.y,butlerMax.y, 0.33f);
		  butlerMean.z = lerp(butlerMin.z,butlerMax.z, 0.5f);

		  /*
		  pushMatrix();
		  fill(255,100,100,120);
	      translate(butlerMean.x,butlerMean.y,butlerMean.z);
		  sphere(50);		  
		  popMatrix();
		  */
		  
		  stroke(255);
		  fill(255);

		  surface.setTitle("fps: " + frameRate);

	}
	
	PVector depthToWorld(float x, float y, float depth) {

	    final double fx_d = 1.0 / 5.9421434211923247e+02;
	    final double fy_d = 1.0 / 5.9104053696870778e+02;
	    final double cx_d = 3.3930780975300314e+02;
	    final double cy_d = 2.4273913761751615e+02;
	    
	    PVector result = new PVector();
	    //double depthValue = depthLookUp[depth];//rawDepthToMeters(depthValue);
	    result.x = (float)((x - cx_d) * depth * fx_d);
	    result.y = (float)((y - cy_d) * depth * fy_d);
	    result.z = (float)(depth)*depthscale;
	    return result;
	}
	
	void oscEvent(OscMessage theOscMessage) {
		
		
		  if(theOscMessage.addrPattern().equals("/butler")) {
			  int take = theOscMessage.get(0).intValue(); 
			  butler.openTake(take);
		  }
		  if(theOscMessage.addrPattern().equals("/band")) {
			  for(int i = 0; i < 8; i++) {
				  butler.deformMatrix[i] = theOscMessage.get(i).floatValue();
			  }
		  
		  }
		}
	
	public void keyPressed() {
		 switch(key) {
		 case 'e' : butler.openTake(testvars[nextvid]);
			 		nextvid++;
			 		if(nextvid>=testvars.length) nextvid = 0;
		 			return;
		 case 'r' : butler.openTake(189);
					return;
		 case 'n' : butler.toggleNoise();
		 			return;
		 }
	}
	
	public void mousePressed(){
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
	public void mouseReleased(){
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
}
