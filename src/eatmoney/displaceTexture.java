package eatmoney;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.opengl.PShader;

public class displaceTexture {

	PApplet parent;
	PGraphics disp;
	float [][][] states;
	float offX, offY = 0.1f;
	int pwidth,pheight;
	PShader displace;
	
	public displaceTexture(PApplet _parent,int width, int height) {
		this.parent = _parent;
		pwidth = width;
		pheight = height;
		disp = parent.createGraphics(width,height,PConstants.P2D);
		
		displace = parent.loadShader("shader\\displaceF.glsl");
		disp = parent.createGraphics(width,height,PConstants.P2D);
	    offX = 1.f/(float)width * parent.floor((float) (parent.random(80.f) - 40.));
		offY = 1.f/(float)height * parent.floor((float) (parent.random(80.f) - 40.));
		startImg();
	}
	
	
	public void drawDTexture() {
		if(parent.random(1.f) > 0.98f) initNew();
		float thresh = 0.68f + (parent.random(0.3f)*parent.sin(parent.frameCount*0.001f));
		  disp.beginDraw();
		  disp.clear();
		  //disp.background(0);
		    for(int i = 0; i < 20; i ++){
		      for(int j = 0; j < 20; j ++){
		        for(int k = 0; k < 2; k ++){
		        float state = states[i][j][k];
		        if(state > 0){
		          disp.rectMode(PConstants.CENTER);
		          disp.noStroke();
		          disp.fill(state*255,100);
		          disp.rect(parent.map(i,0,20,0,pwidth),parent.map(j,0,20,0,pheight),192*state,108*state);
		          states[i][j][k] -= 0.0018;
		        }
		        else if(state <= 0){
		          for(int u = -1; u <= 1; u++){
		            for(int v = -1; v <= 1; v++){
		              int posx = i+u;
		              int posy = j+v;
		              if(posx>=20) posx = 19;
		              else if (posx <= 0) posx = 0;
		              if(posy>=20) posy = 19;
		              else if (posy <= 0) posy = 0;
		              float stateN = states[posx][posy][k];
		              if(stateN > thresh){
		                states[i][j][k] = parent.random(1.f);
		                if(parent.random(1.f) > 0.99) states[i][j][k] = 1.f;
		              }
		          
		            }
		          }
		        }
		        }
		      } 
		    }
		    disp.endDraw();
	}
	
	void startImg(){
		 states = new float[20][20][2];
		 int x = parent.floor(parent.random(20));
		 int y = parent.floor(parent.random(20));
		 states[x][y][0] = 1.f;
		 states[x][y][1] = 1.f;
		}


   void initNew(){
		  startImg();
		  offX = 1.f/1920.f * parent.floor((float) (parent.random(40.f) - 20.));
		  offY = 1.f/1080.f * parent.floor((float) (parent.random(40.f) - 20.));
		}


   void drawShader(PGraphics t) {
	    displace.set("offset",(float)offX,(float)offY);
	    displace.set("dispT",disp);
	    t.shader(displace);
   }
}
