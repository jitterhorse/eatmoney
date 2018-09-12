package videoCapture;

import eatmoney.eatMoneyMain;
import processing.core.PImage;
import processing.video.Capture;

public class VideoCaptureTool {

	eatMoneyMain emm;
	public Capture cam;
	
	public VideoCaptureTool(eatMoneyMain _emm) {
		
		emm = _emm;
		cam = new Capture(emm, 1280, 720,50);
		cam.start();
	}
	
	public PImage getCameraImage() {

		if (cam.available() == true) {
				cam.read();
			  }
		return cam; 
		
	}
	
	public void captureEvent(Capture c) {
		  c.read();
		}
	
}
