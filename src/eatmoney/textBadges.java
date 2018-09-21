package eatmoney;


import java.util.ArrayList;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics3D;
import processing.opengl.PGraphicsOpenGL;
import processing.opengl.PShader;

public class textBadges {

	eatMoneyMain main;
	ClothObject co;
	PGraphics font; 
	PShape object;
	PFont f;
	PShader textShader;
	
	ArrayList<textB> currentBadges;
	
	float textheight, textwidth, aspect;
	int cx = 10;
	int cy = 10;
	
	public textBadges(eatMoneyMain _main, ClothObject _co) {
		  main = _main;
		  co = _co;
		  
		  f = main.createFont("DejaVu Sans Mono", 24);
		  createMap();
		  createObject();
		  textShader = main.loadShader("shader\\textF.glsl","shader\\textV.glsl");
		  textShader.set("glyphMap",font);
		  PVector tmD = new PVector((float)cx,(float)cy,0.f);
		  textShader.set("textMapDim",tmD);//size of TextMap
		  textShader.set("initial",33.f);
		  textShader.set("scale",10.0f);
		  textShader.set("aspectT",aspect);
	}
	
	public void createBadge(int count) {
		currentBadges = new ArrayList<textB>();
		int sizeCloth = co.nodecount * co.nodecount;
		for(int i = 0; i < count; i++) {
			int id = main.floor(main.random(sizeCloth));
			textB tb = new textB(main,id);
			currentBadges.add(tb);
		}
	}
	
	
	public void drawBadges(PGraphics target) {
		if(currentBadges != null) {
			for(textB t : currentBadges) {
				target.pushMatrix();
				//target.translate(co.particles[t.id].cx,co.particles[t.id].cy,co.particles[t.id].cz);
				target.stroke(199,255);
				target.strokeWeight(1);
				target.line(co.particles[t.id].cx,co.particles[t.id].cy+(t.offset*0.1f),co.particles[t.id].cz,co.particles[t.id].cx,co.particles[t.id].cy+(t.offset),co.particles[t.id].cz);
				textShader.set("inputText",t.textImg);
				PVector siz = new PVector((float)t.maxleng,(float)t.lines,0.f);
				textShader.set("count",siz); 
				target.translate(co.particles[t.id].cx,co.particles[t.id].cy+(t.offset),co.particles[t.id].cz);
				target.rotateY(PConstants.PI);
			    /*
				PMatrix3D mat = ((PGraphics3D)target).cameraInv;
			    mat.m03 = co.particles[t.id].cx;
			    mat.m13 = co.particles[t.id].cy+(t.offset);
			    mat.m23 = co.particles[t.id].cz;

			    target.applyMatrix(mat);
				*/
				target.shader(textShader);
				target.shape(object);
				target.resetShader();
				target.popMatrix();
			}
		}
	}
	
	void createMap(){
		  font = main.createGraphics(100,100,PConstants.P2D);
		  font.textFont(f);
		  font.textSize(30);
		  textheight = font.textAscent() + font.textDescent();
		  textwidth = font.textWidth("M");
		  aspect = textwidth/textheight;
		  font = main.createGraphics(main.ceil(cx*textwidth),main.ceil(cy*textheight),PConstants.P2D);
		  font.beginDraw();
		  font.textSize(30);
		  font.textFont(f);

		  int ch = 33;
		  for(int i = 0; i < cy; i++){
		    for(int j = 0; j < cx; j++){      
		      font.textAlign(PConstants.LEFT,PConstants.TOP);
		      font.text((char)ch,(float)j*textwidth,(float)i*textheight);
		      ch++;
		    }
		  }
		  font.endDraw();
	}
	
	void createObject(){
		 object = main.createShape();
		 object.beginShape(PConstants.QUADS);
		 object.noStroke();
		 object.fill(255);
		 object.textureMode(PConstants.NORMAL);
		 object.vertex(0,0,0,0,0);
		 object.vertex(1,0,0,1,0);
		 object.vertex(1,1,0,1,1);
		 object.vertex(0,1,0,0,1); 
		 object.endShape();
		  
		}
	
	
	class textB{
	
		eatMoneyMain main;
		ArrayList<String> interfaceT = new ArrayList<String>();
		int maxleng = 0;
		int lines = 0;
		PGraphics textImg; 
		int id = 0;
		float offset;
		String[] tagwords = {"nord-EU","EU","OC","oceancities","LTEM","LET_THEM_EAT_MONEY","TARP","NOVA","YLD","ONZ","SINA","VERSION","oceanics","EU","HPP","CONNST","ROSSER","ROLOEG"};
		
		
		
		public textB(eatMoneyMain _main,int _id) {
			main = _main;
			id = _id;
			offset = (main.random(1.f) > 0.5f) ? -1.f : 1.f;
			float wide = main.random(1300);
			offset *= wide;
			createRandomText();
			textToTexture();
		}
	
		void createRandomText(){
			   String s;
			   Random rndm = new Random();
			   int leng = main.ceil((float) (Math.random()*10.f));
			   for (int i = 0; i < leng; i++){
			     int len = main.ceil(main.random(20));
			     s = generateString(rndm,"abcdefghei127831985623508-.,",len);
			     if(main.random(1.f) > 0.70f) {
			    	 String insert = tagwords[main.floor(main.random(tagwords.length))];
			    	 int insertpoint = main.floor(main.random(s.length()));
			    	 s = s.substring(0, insertpoint) + "-" + insert  + "_" + s.substring(insertpoint, s.length());
			     }
			    	 
			     
			     lines++;
			     maxleng = main.max(s.length(),maxleng);
			     interfaceT.add(s);
			   }
			}
		
		public String generateString(Random rng, String characters, int length){
		    char[] text = new char[length];
		    for (int i = 0; i < length; i++)
		    {
		        text[i] = characters.charAt(rng.nextInt(characters.length()));
		    }
		    return new String(text);
		}

		
		void textToTexture(){
			  int x = maxleng;
			  int y = lines;
			  textImg = main.createGraphics(x,y,PConstants.P2D);
			  textImg.textFont(f);
		      textImg.textSize(30);
			  ((PGraphicsOpenGL)textImg).textureSampling(2);
			  textImg.beginDraw();
			  textImg.loadPixels();
			  for(int i = 0; i < x*y; i++){
			    textImg.pixels[i] = 0;
			  }
			  for(String s : interfaceT){
			    int li = interfaceT.indexOf(s);
			    
			    for(int i = 0; i < s.length(); i++){
			      char c = s.charAt(i);
			      textImg.pixels[(li*x)+i] = main.color((int)c,255);
			    }
			  }
			  textImg.updatePixels();
			  textImg.endDraw();

			  
			}
		
	}
	
	
	
}
	
	
