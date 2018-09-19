package eatmoney;

import enums.ObjectMode;
import enums.mode;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class VideoObject {

	eatMoneyMain parent;
	PShader vidTex,vidTex2;	
	VideoBlob vb;
	PShape videoPlane;
	ObjectMode displaymode = ObjectMode.off;
	float easing = 1.f;
	  
	  class VideoBlob{
		  
		  int mode = 0; // viewsize
		  int id;
		  PVector position;
		  boolean visible = true;
		  float lifetime = 0.f;
		  boolean impact = false;
		  boolean close = false;
		  int shader = 0;
		  
		  public VideoBlob(int nodecount,int _mode,int _shader) {
			  id = (int) Math.floor(Math.random() * (nodecount));
			  position = new PVector((float)(Math.random()*5000.)-2500,(float)(Math.random()*5000.)-2500,(float)(Math.random()*5000.)-2500);
			  mode = _mode;
			  shader = _shader;
		  }
		  
	  }
	  
	public VideoObject(eatMoneyMain _emm) {
		  parent = _emm;
		  createVidPlane();
		  vidTex = parent.loadShader("shader\\bloodyF2.glsl","shader\\bloodyV.glsl");
		  vidTex2 = parent.loadShader("shader\\algorithmF.glsl","shader\\bloodyV.glsl");
	}
	
	
	
	
	public void drawVideo(PGraphics target) {
		//draw Video Point
	    if(vb != null && vb.visible == true) {
	    	target.pushMatrix();
		    PVector pos = new PVector(parent.co.particles[vb.id].cx,parent.co.particles[vb.id].cy,parent.co.particles[vb.id].cz);
		    target.strokeWeight(1);
		    target.stroke(128,255);
		    target.line(pos.x,pos.y,pos.z,vb.position.x,vb.position.y,vb.position.z);  

		   
		    
		    //vidTex.set("time", (parentc.frameCount * 0.1f));
		    //vidTex.set("vidtextureold", parentc.vidC.getLastImage());
			
			if(parent.vidC.dotracking == true) {
				target.endDraw();
				parent.vidC.drawTrackings();
				target.beginDraw();
			}

			PMatrix3D mat = ((PGraphics3D)target).cameraInv;
		    mat.m03 = vb.position.x;
		    mat.m13 = vb.position.y;
		    mat.m23 = vb.position.z;

			float scale = 800.f;
			float rotate = 0.f;
			if(vb.mode == 1) {
				scale = 600.f;
				rotate = 0.1f*PConstants.PI;
			}
			
			mat.m00 += rotate *vb.lifetime;
			target.applyMatrix(mat);
			
			
			target.scale(vb.lifetime*scale, vb.lifetime * scale,1.f);
			
			if(vb.shader == 1) {
		    vidTex2.set("vidtexture", parent.vidC.getCameraImage());
		    vidTex2.set("tracktexture", parent.vidC.getTrackingImage());
		    target.shader(vidTex2);
			}
			else if (vb.shader == 2) {
			vidTex.set("vidtexture", parent.vidC.getCameraImage());
			vidTex.set("tracktexture", parent.vidC.getTrackingImage());		  
		    vidTex.set("blurmix", 0.f);
		    target.shader(vidTex);
			}
		   
		    target.shape(videoPlane);
		    
		    if(vb.shader != 0) {
		    	target.resetShader();
		    }
		    target.popMatrix();
	    }
	}
	
	
	public PVector getVideoMiddle() {
		PVector middle = new PVector(0,0,0);
		if(vb != null) {
			middle = vb.position;
		}
		return middle;
	}
	
	
	public void newVideoBlob(int channel,int particlecount,int mode,int shader) {
		//todo: swicth video channel automatically
		vb = new VideoBlob(particlecount,mode,shader);	
	}
	
	public void closeVideoBlob() {
		this.vb.close = true;
	}
	
	  void createVidPlane() {
		  	videoPlane = parent.createShape();
		  	videoPlane.beginShape(PConstants.QUADS);
		  	videoPlane.noStroke();
		  	videoPlane.fill(255);
		  	videoPlane.normal(0,0, -1);
		  	videoPlane.vertex(-0.64f,-0.36f,0,0);
		  	videoPlane.normal(0, 0, -1);
		  	videoPlane.vertex(-0.64f,0.36f,0,0,1);
		  	videoPlane.normal(0, 0, -1);
		  	videoPlane.vertex(0.64f,0.36f,0,1,1); 
		  	videoPlane.normal(0, 0, -1);
		  	videoPlane.vertex(0.64f,-0.36f,0,1,0);

		  	videoPlane.endShape();
		 }
	
}
