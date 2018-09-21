package eatmoney;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class displaceTexture {

	PApplet parent;
	PGraphics disp;
	float [][][] states;
	float offX, offY = 0.f;
	int pwidth,pheight;
	PShader displace;
	PFont f;
	float lifetime = 0.f;
	public int contentID  = 0; // number of content to use
	public boolean display = false;
	
	String[] contents = {"LET-THEM-EAT-MONEY.COM","UPDATE", "NOVA.OCEANCITIES.COM/4.0","RECEIVING DATA"};
	
	public displaceTexture(PApplet _parent,int width, int height) {
		this.parent = _parent;
		pwidth = width;
		pheight = height;
		disp = parent.createGraphics(width,height,PConstants.P2D);
		f = _parent.createFont("Beauty Normal-Italic", 250);
		displace = parent.loadShader("shader\\displaceF.glsl");
		disp = parent.createGraphics(width,height,PConstants.P2D);	    
	}
	
	public void setupTex() {
		offX = 1.f/(float)pwidth * parent.floor((float) (parent.random(8.f) - 4.));
		offY = 1.f/(float)pheight * parent.floor((float) (parent.random(8.f) - 4.));
	}
	
	
	public void drawDTexture() {
		  disp.beginDraw();
		  disp.clear();
		  disp.textFont(f);
		  disp.textAlign(PConstants.CENTER,PConstants.CENTER);
		  disp.textSize(100);
		  disp.noStroke();
		  float lt = lifetime;
		  if(lifetime > 1.) lt = 1.f - (lifetime-1.f);
		  float alp = easeInOut(lifetime,0.f,255.f,2.f);
		  disp.fill(255,alp+0.00001f);
		  float pos = easeInOut(lifetime,-50.f,100.f,2.f);
		  disp.text(contents[contentID], (float)disp.width/2.f, (float)disp.height/2.f + pos);
		  disp.endDraw();
	}

	public static float easeInOut (float t,float b , float c, float d) {
		if ((t/=d/2) < 1) return c/2*t*t*t + b;
		return c/2*((t-=2)*t*t + 2) + b;
	}

   void drawShader(PGraphics t) {
	    displace.set("offset",(float)offX,(float)offY);
	    displace.set("dispT",disp);
	    t.filter(displace);
   }
}
