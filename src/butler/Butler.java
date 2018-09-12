package butler;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.HashMap;

import butler.readData.State;
import eatmoney.eatMoneyMain;

public class Butler {
		
		eatMoneyMain parent;
	
	    //Bulter Vars
		int [] rawData;
		int take = 0;
		boolean playbutler = false;
		public readData butlerData;
		
		PVector butlerMin = new PVector(0,0,0);
		PVector butlerMax = new PVector(0,0,0);
		public PVector butlerMean = new PVector(0,0,0);
		
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
	
		//testvars
		public int[] testvars = {20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,189};
		int[] delays = {2389,1147,2334,1412,1472,1489,1163,2476,1453,1989,4750,2422,2500,1740,2242,2332,2307,1688,1892,2045,2619,4456,1946,1927,0};
		
		HashMap<Integer, Integer> butlerDelays = new HashMap<Integer, Integer>();
		
		public int nextvid = 0;
		
		public Butler(eatMoneyMain _emm) {
			this.parent = _emm;
			setupHash();
			butlerData = new readData(parent,this);
		}
		
		private void setupHash(){
			for(int i = 0; i < testvars.length; i++) {
				butlerDelays.put(testvars[i],delays[i]);
			}
		}
		

		
		public void drawButler(PGraphics mc) { 
			if(butlerData.state == State.mix1 || butlerData.state == State.mix2 || butlerData.state == State.inmix){ 
				 rawData = butlerData.readFrame();	 
				 mc.pushMatrix();
				 //mc.rotateX(rotX);
				 //mc.rotateY(rotY);
		
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
		
				      mc.beginShape(PConstants.TRIANGLES);  
				      mc.fill(255);
				      if ((p00.z > 0) && (p01.z > 0) && (p10.z > 0) && // check for non valid values
				          (parent.abs(p00.z-p01.z) < max_edge_len) && (parent.abs(p10.z-p01.z) < max_edge_len) &&// check for edge length
				          (p00.z < maxRange) && p00.z > minRange) {  // depth cut
				    	  mc.vertex(p00.x,p00.y,p00.z); 
				    	  mc.vertex(p01.x,p01.y,p01.z);
				    	  mc.vertex(p10.x,p10.y,p10.z);
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
				          (parent.abs(p11.z-p01.z) < max_edge_len) && (parent.abs(p10.z-p01.z) < max_edge_len) &&
				          (p11.z < maxRange) && p00.z > minRange){
				    	  mc.vertex(p01.x,p01.y,p01.z);
				    	  mc.vertex(p11.x,p11.y,p11.z);
				    	  mc.vertex(p10.x,p10.y,p10.z);
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
				      mc.endShape();
		
				   }
				  }
				 
		
				 mc.popMatrix();
			 }

			  butlerMean.x = parent.lerp(butlerMin.x,butlerMax.x, 0.5f);
			  butlerMean.y = parent.lerp(butlerMin.y,butlerMax.y, 0.33f);
			  butlerMean.z = parent.lerp(butlerMin.z,butlerMax.z, 0.5f);
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

}
