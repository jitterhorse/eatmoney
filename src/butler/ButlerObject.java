package butler;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.util.HashMap;

import butler.readData.State;
import eatmoney.eatMoneyMain;
import enums.ObjectMode;

public class ButlerObject {
		
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
		public int steps = 2;
		
		float depthscale = 0.7f;
		float minRange = 500;
		float maxRange = 1800 * depthscale;
		
		public PVector ButlerOffset = new PVector(1500,0,-1500);
	
		public PVector camoffset = new PVector(200,-10,-220);
		
		HashMap<Integer, Offset> butlerDelays = new HashMap<Integer, Offset>();
		
		public int nextvid = 20;
		
		public float easing = 0.f;
		public ObjectMode displaymode = ObjectMode.off;
		
		public class Offset{		
			int file;
			int offset;
			
			public Offset(int _file, int _offset) {
				file = _file;
				offset = _offset;
			}
			
			public int getDelay() {
				return offset;
			}
			
			public int getFile() {
				return file;
			}
			
		}
		
		
		public ButlerObject(eatMoneyMain _emm) {
			this.parent = _emm;
			setupHash();
			butlerData = new readData(parent,this);
		}
		
		private void setupHash(){
			JSONArray offsets = parent.loadJSONArray("data\\offset.json");

			for(int i = 0; i < offsets.size(); i++) {
				JSONObject jo = offsets.getJSONObject(i);
				Offset off = new Offset(jo.getInt("num"),jo.getInt("delay"));
				butlerDelays.put(jo.getInt("id"),off);
			}
			Offset off = new Offset(189,0);
			butlerDelays.put(189,off);
			
		}
		

		
		public void drawButler(PGraphics mc) { 
			if(butlerData.state == State.mix1 || butlerData.state == State.mix2 || butlerData.state == State.inmix){ 
				 rawData = butlerData.readFrame();	 
				 mc.pushMatrix();
				 mc.translate(ButlerOffset.x, ButlerOffset.y,ButlerOffset.z);
				 mc.lightSpecular(24, 24, 24);
				 mc.specular(12, 12, 0);
				 mc.shininess(0);
	
				 butlerMin = new PVector(4500,4500,4500);
				 butlerMax = new PVector(0,0,0);
	 
				 float stepadd = parent.map(easing,0.f,1.f,28.f,0.f);
				 if(stepadd < 0.) stepadd = 0.f;
				 else if(stepadd > 28.) stepadd = 28.f;
				 
				 steps = 2;
				 steps += parent.floor(stepadd);
				 
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
				      mc.strokeWeight(1);
				      float ease = (Math.random() < easing) ? 1.f : 0.f;
				      mc.stroke(90);
				      mc.fill(255*ease,255*ease);
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
			  butlerMean.y = parent.lerp(butlerMin.y,butlerMax.y, 0.25f);
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
