package eatmoney;

import enums.ObjectMode;
import enums.mode;
import irisScans.Iris;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class VideoObject {

	eatMoneyMain parent;
	PShader vidTex2;	
	public VideoBlob vb;
	PShape videoPlane;
	ObjectMode displaymode = ObjectMode.off;
	float easing = 0.f;
	public Iris iris;
	public int person = 0;

	  public class VideoBlob{

		  int id;
		  PVector position;
		  boolean visible = true;
		  float lifetime = 0.f;
		  boolean impact = false;
		  float trackingtime = 0.f;
		  boolean close = false;
		  int shader = 0;
		  boolean iris = false;
		  public boolean shootIris = false;
		  
		  public VideoBlob(int nodecount,int _shader) {
			  id = (int) Math.floor(Math.random() * (nodecount));
			  position = new PVector((float)(Math.random()*1000.)-500,(float)(Math.random()*1000.)-500,(float)(Math.random()*1000.)-500);
			  if(_shader == 2) {
				  shader = 0;
				  iris = true;
			  }
			  else if(_shader == 0) {
				  trackingtime = 1.f;
				  shader = _shader;
			  }
			  else if (_shader == 1){
				  shader = _shader;
			  }
		  }
		  
	  }
	  
	public VideoObject(eatMoneyMain _emm) {
		  parent = _emm;
		  iris = new Iris(parent,400);
		  createVidPlane();
		  vidTex2 = parent.loadShader("shader\\algorithmF.glsl","shader\\bloodyV.glsl");
	}
	
	
	
	
	public void drawVideo(PGraphics target) {
		//draw Video Point
	    if(vb != null && vb.visible == true) {
	    	int iri = 0;
	    	target.pushMatrix();
		    PVector pos = new PVector(parent.co.particles[vb.id].cx,parent.co.particles[vb.id].cy,parent.co.particles[vb.id].cz);
		    target.strokeWeight(3);
		    target.stroke(128,255);
		    target.line(pos.x,pos.y,pos.z,parent.lerp(pos.x, vb.position.x, easing),parent.lerp(pos.y, vb.position.y, easing),parent.lerp(pos.y, vb.position.y, easing));  
			if(parent.vidC.dotracking == true) {
				target.endDraw();
				parent.vidC.drawTrackings();
				target.beginDraw();
			}
			
			if(vb.iris == true && vb.shootIris == true) {
				target.endDraw();
				iris.draw(person);
				iri = 1;
				target.beginDraw();
			}
			
			if(iris.getStatus(person)==true) {
				vb.trackingtime = 1.f;
			}
			
			//System.out.println("status: " + iri +  "/" + vb.iris + "/" + shootIris);
			
			PMatrix3D mat = ((PGraphics3D)target).cameraInv;
		    mat.m03 = parent.lerp(pos.x, vb.position.x, easing);
		    mat.m13 = parent.lerp(pos.y, vb.position.y, easing);
		    mat.m23 = parent.lerp(pos.z, vb.position.z, easing);

			float scale = 800.f;
			float rotate = 0.f;	
			mat.m00 += rotate *vb.lifetime;
			target.applyMatrix(mat);			
			target.scale(vb.lifetime*scale, vb.lifetime * scale,1.f);
			
		    vidTex2.set("status",vb.shader);		
		    vidTex2.set("vidtexture", parent.vidC.getCameraImage());
		    vidTex2.set("tracktexture", parent.vidC.getTrackingImage());
		    vidTex2.set("trackingTimer", vb.trackingtime);
		    vidTex2.set("irisTex", iris.getIrisImage());
		    vidTex2.set("showIris", iri);
		    
		    target.shader(vidTex2);
		    target.shape(videoPlane);	    
		    target.resetShader();
	
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
	
	
	public void newVideoBlob(int channel,int particlecount,int shader) {
		//todo: swicth video channel automatically
		vb = new VideoBlob(particlecount,shader);	
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
