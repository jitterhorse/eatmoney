package videoCapture;

import org.bytedeco.javacpp.opencv_core.Point2f;
import org.bytedeco.javacpp.opencv_core.Point2fVector;
import org.bytedeco.javacpp.opencv_core.Rect;

import eatmoney.eatMoneyMain;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.video.Capture;

public class VideoCaptureTool {

	eatMoneyMain emm;
	public Capture cam;
	boolean cameraInit = false;
	
	PImage oldImage;
	
	PGraphics trackingTex;
	public boolean dotracking = true;
	public boolean trackingFace = true;
    public boolean trackingEyes = true;
    public boolean trackingFacemark = true;
	
	public VideoCaptureTool(eatMoneyMain _emm) {
		
		emm = _emm;
		cam = new Capture(emm, 1280, 720,50);
		cam.start();
		cameraInit = true;
		
		trackingTex = emm.createGraphics(1280,720,PConstants.P2D);
	}
	
	public PImage getCameraImage() {

		if (cam.available() == true) {
				cam.read();
			  }
		
		oldImage = cam.copy();
		return cam; 
		
	}
	
	public void captureEvent(Capture c) {
		  c.read();
		}

	public PImage getLastImage() {
		return oldImage;
	}
	
	public void drawTrackings() {
		trackingTex.beginDraw();
		trackingTex.clear();
	    if(trackingFace == true) {	
	    	for (long i = 0; i < emm.fm.faceVector.size(); i++) {
	    	trackingTex.pushMatrix();
    		Rect r = emm.fm.getFaceV(i);
    		trackingTex.fill(255,50);
    		trackingTex.stroke(0,255,0,255);
    		trackingTex.strokeWeight(2);
    		trackingTex.rect(r.x()*4.f, r.y()*4.f, r.width()*4f, r.height()*4f);
    		trackingTex.popMatrix();
	    	}
	    }
	    
	    if(trackingEyes == true) {   
	       	for (long i = 0; i < emm.fm.eyeVector.size(); i++) {
		    	trackingTex.pushMatrix();
	    		Rect r = emm.fm.getEyeV(i);
	    		trackingTex.fill(255,50);
	    		trackingTex.stroke(0,255,0,255);
	    		trackingTex.strokeWeight(2);
	    		trackingTex.rect(r.x()*4.f, r.y()*4.f, r.width()*4f, r.height()*4f);
	    		trackingTex.popMatrix();
		    	}
	    	
	    }	
	    
	    if(trackingFacemark == true) {
	    	
	    	trackingTex.pushMatrix();
	    	
			  for (long i = 0; i < emm.fm.landmarks.size(); i++) {
                Point2fVector v = emm.fm.landmarks.get(i);
                for(long j = 0; j < v.size();j++) {
                    Point2f xop = v.get(j);
                    trackingTex.fill(255);
                    trackingTex.stroke(255);
                    trackingTex.strokeWeight(6);
                    trackingTex.point(xop.x()*4.f,xop.y()*4.f);
                }
               
			  }
			  trackingTex.popMatrix();
			 
	    }
		
	
		trackingTex.endDraw();
		
	}

	public PGraphics getTrackingImage() {
		return trackingTex;
	}
	
	
}
