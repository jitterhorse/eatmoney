package eatmoney;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class LightRig {

	PGraphics target;
	PApplet parent;
	eatMoneyMain emm;
	
	boolean blackout = false;
	float multi = 1.f;
	float direction = -1.f;
	float speed = 0.05f;
	float changespeed = 0.01f;
	
	LightCol PL = new LightCol(255,255,255,255,-1000, -1000, -100);
	LightCol AL = new LightCol(96,96,96,255);
	LightCol DL = new LightCol(210,210,210,255,-1, -1.5f, -2);
	
	LightCol PLnew = new LightCol(255,255,255,255,-1000, -1000, -100);
	LightCol ALnew = new LightCol(96,96,96,255);
	LightCol DLnew = new LightCol(210,210,210,255,-1, -1.5f, -2);
	
	public boolean PLset = true;
	public boolean ALset = false;
	public boolean DLset = true;
	
	class LightCol {	
		float r;
		float g;
		float b;
		float a;
		
		PVector pos = new PVector(0,0,0);
		
		public LightCol(float _r, float _g, float _b, float _a ) {
			r = _r;
			g = _g;
			b = _b;
			a = _a;

		}
		
		public LightCol(float _r, float _g, float _b, float _a,float _posx, float _posy, float _posz ) {
			r = _r;
			g = _g;
			b = _b;
			a = _a;
			pos.x = _posx;
			pos.y = _posy;
			pos.z = _posz;
		}
	}
	
	LightRig(PGraphics _target, PApplet _parent, eatMoneyMain _emm){
		target = _target;
		parent = _parent;
		emm = _emm;
		
	}
	
	
	public void doLight(PVector offset) {
		DL.r = parent.lerp(DL.r,DLnew.r,changespeed);
		DL.g = parent.lerp(DL.g,DLnew.g,changespeed);
		DL.b = parent.lerp(DL.b,DLnew.b,changespeed);
		
		AL.r = parent.lerp(AL.r,ALnew.r,changespeed);
		AL.g = parent.lerp(AL.g,ALnew.g,changespeed);
		AL.b = parent.lerp(AL.b,ALnew.b,changespeed);
				
		
		
		//target.pointLight(parent.abs(parent.sin(parent.frameCount*0.001f)*PL.r), parent.abs(parent.cos(parent.frameCount*0.003f)*PL.g), parent.abs(1.f-parent.cos(parent.frameCount*0.005f)*PL.b), PL.pos.x+offset.x, PL.pos.y+offset.y, PL.pos.z+offset.z);    
		float intens = parent.abs(parent.sin(parent.frameCount*0.001f));
		//target.pointLight(intens*PL.r, intens*PL.g, intens*PL.b, PL.pos.x+offset.x, PL.pos.y+offset.y, PL.pos.z+offset.z);   
		if(PLset == true) target.pointLight(intens*PL.r, intens*PL.g, intens*PL.b,emm.cameraSlidePos.x,emm.cameraSlidePos.y,emm.cameraSlidePos.z);
		//target.ambientLight(parent.abs(parent.cos(parent.frameCount*0.001f)*AL.r*multi), AL.g, AL.g);
		if(ALset == true) target.ambientLight(AL.r, AL.g, AL.g);
		if(DLset == true) target.directionalLight(DL.r*multi, DL.g*multi, DL.b*multi, DL.pos.x+offset.x, DL.pos.y+offset.y, DL.pos.z+offset.z);
		target.lightFalloff(1.0f, 0.001f, 0.0f);
		target.lightSpecular(128, 67, 67);
		target.specular(120, 12, 12);
		target.shininess(2);
		
		
		changeLightRig();
	}
	
	public void changeLightRig() {
		if(blackout == true && direction == -1.) {
			multi-=speed;
			if(multi <= 0.f) direction = 1.f;
		}
		
		else if(blackout == true && direction == 1.) {
			multi+=speed;
			if (multi >= 1.) {
				blackout = false;
				direction = -1.f;
			}
		}
		
		if(Math.random() > 0.9995) {
			blackout = true;
			direction = -1.f;
		}
		
		if(Math.random() > 0.999) {
			DL.r = (float) (Math.random() * 255);
			DL.g = DL.r; DL.b = DL.r;
			//DL.g = (float) (Math.random() * 255);
			//DL.b = (float) (Math.random() * 255);
		}
		if(Math.random() > 0.999) {
			AL.r = (float) (Math.random() * 255);
			AL.g = AL.r * 0.9f; AL.b = AL.r * 0.9f;
			//AL.g = (float) (Math.random() * 255);
			//AL.b = (float) (Math.random() * 255);
		}
		
	}
	
	
}
