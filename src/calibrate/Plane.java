package calibrate;

import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

class Plane{
	
	  int count;
	  int totalcount;
	  ArrayList<handle> allHandles;
	  float [][][] renderSource;// = new float[2][20][2];
	  float [][][] renderResult;
	  float offset = 0;
	  Calibrate c;
	  
	  public Plane(int _count,int _totalcount,Calibrate _c){
		  this.c = _c;
	      this.count = _count;
	      this.totalcount = _totalcount;
	      this.allHandles = new ArrayList<handle>();
	      float widthplane = ((float)this.c.cwidth/(float)this.totalcount);
	      float heightplane = widthplane * 0.5625f;
	      offset = widthplane*this.count;
	        for(int i = 0; i < 2; i++){
	         for(int j = 0; j < 4; j++){
	          handle h = new handle(offset+((c.cwidth/(this.totalcount * 3)) * j),(heightplane)*i,(this.count*8)+(i*4)+j);
	          this.allHandles.add(h);
	         }
	       }
	       
	       //top and bottom
	       renderSource = new float[2][c.stepsx+1][2];
	       //inpoints
	       renderResult = new float[c.stepsy+1][c.stepsx+1][2];
	  }
	  
  
	  
	  public void draw(PGraphics target){   
	      int handleshift = this.count * 8;
	      
	      if(c.mouseP == true && (c.currenthandle > (255 - 8 - handleshift) && c.currenthandle <= (255-handleshift))){
	         this.allHandles.get((int)(255-c.currenthandle - handleshift)).pos.x = c.em.mouseX-c.border-210;
	         this.allHandles.get((int)(255-c.currenthandle - handleshift)).pos.y = c.em.mouseY-c.border;
	        }
	    
	      target.pushMatrix();
	      target.noFill();
	      target.strokeWeight(2);
	      target.stroke(0);
	      target.bezier( this.allHandles.get(0).pos.x,allHandles.get(0).pos.y,
	              allHandles.get(1).pos.x,allHandles.get(1).pos.y,
	              allHandles.get(2).pos.x,allHandles.get(2).pos.y,
	              allHandles.get(3).pos.x,allHandles.get(3).pos.y);
	      target.stroke(122,20,29,100);
	      target.line(allHandles.get(0).pos.x,allHandles.get(0).pos.y,allHandles.get(1).pos.x,allHandles.get(1).pos.y); 
	      target.line(allHandles.get(2).pos.x,allHandles.get(2).pos.y,allHandles.get(3).pos.x,allHandles.get(3).pos.y);  
	      
	      
	      target.stroke(0);
	      target.bezier( allHandles.get(4).pos.x,allHandles.get(4).pos.y,
	              allHandles.get(5).pos.x,allHandles.get(5).pos.y,
	              allHandles.get(6).pos.x,allHandles.get(6).pos.y,
	              allHandles.get(7).pos.x,allHandles.get(7).pos.y);
	      target.stroke(122,20,29,100);
	      target.line(allHandles.get(4).pos.x,allHandles.get(4).pos.y,allHandles.get(5).pos.x,allHandles.get(5).pos.y); 
	      target.line(allHandles.get(6).pos.x,allHandles.get(6).pos.y,allHandles.get(7).pos.x,allHandles.get(7).pos.y); 
	    
	      target.stroke(0);
	      target.line(allHandles.get(0).pos.x,allHandles.get(0).pos.y,allHandles.get(4).pos.x,allHandles.get(4).pos.y);
	      target.line(allHandles.get(3).pos.x,allHandles.get(3).pos.y,allHandles.get(7).pos.x,allHandles.get(7).pos.y);
	      target.popMatrix();
	      
	      
	      // top line
	      for (int i = 0; i <= c.stepsx; i++) {
	        float t = i / (float)c.stepsx;
	        float x = target.bezierPoint(allHandles.get(0).pos.x,
	                              allHandles.get(1).pos.x,
	                              allHandles.get(2).pos.x,
	                              allHandles.get(3).pos.x,
	                              t);
	        float y = target.bezierPoint(allHandles.get(0).pos.y,
	                              allHandles.get(1).pos.y,
	                              allHandles.get(2).pos.y,
	                              allHandles.get(3).pos.y,
	                              t);
	        
	        //c.stroke(0,0,255,190);
	        //c.strokeWeight(2);
	        //c.noFill();
	        //c.ellipse(x,y,17,17);
	        renderSource[0][i][0] = x;
	        renderSource[0][i][1] = y;
	    }
	    //bottom Line
	      for (int i = 0; i <= c.stepsx; i++) {
	        float t = i / (float)c.stepsx;
	        float x = target.bezierPoint(allHandles.get(4).pos.x,
	                              allHandles.get(5).pos.x,
	                              allHandles.get(6).pos.x,
	                              allHandles.get(7).pos.x,
	                              t);
	        float y = target.bezierPoint(allHandles.get(4).pos.y,
	                              allHandles.get(5).pos.y,
	                              allHandles.get(6).pos.y,
	                              allHandles.get(7).pos.y,
	                              t);
	        
	        //c.stroke(0,0,255,100);
	        //c.strokeWeight(2);
	        //c.noFill();
	        //c.ellipse(x,y,17,17);
	        renderSource[1][i][0] = x;
	        renderSource[1][i][1] = y;
	    }  
	    
	      for(int i = 0; i <= c.stepsy;i++){
	        for(int j = 0; j <= c.stepsx;j++){
	          
	        float x = c.em.lerp(renderSource[0][j][0],renderSource[1][j][0],i/(float)c.stepsy);
	        float y = c.em.lerp(renderSource[0][j][1],renderSource[1][j][1],i/(float)c.stepsy);
	        target.fill(0,255,0);
	        target.noStroke();
	        target.ellipse(x,y,5,5);
	        renderResult[i][j][0] = x;
	        renderResult[i][j][1] = y;
	        }
	      }
	      
	      for(handle h : allHandles){
		       target.fill(h.col,255);
		       target.ellipse(h.pos.x,h.pos.y,20,20);
		      }
		      
	  }
	  
	  public PShape renderPlane(){
	      float deltau = (1.f/(float)totalcount);
	      float startu = deltau*count;
	      
	      PShape object;
	      object = c.cali.createShape();
	      object.beginShape(PConstants.QUADS);
	      object.textureMode(PConstants.NORMAL);
	      object.texture(c.content);
	      object.noStroke(); 
	      int xoffset = c.em.mainDisplayWidth;
	      
	      for(int i = 0; i < c.stepsy;i++){
	        for(int j = 0; j < c.stepsx;j++){  
	        float x = xoffset+renderResult[i][j][0]*totalcount*1.5f;
	        float y = renderResult[i][j][1]*totalcount*1.5f;
	        object.vertex(x,y,0,startu+((float)j/(float)c.stepsx)*deltau,(float)i/(float)c.stepsy);
	        x = xoffset+renderResult[i][j+1][0]*totalcount*1.5f;
	        y = renderResult[i][j+1][1]*totalcount*1.5f;
	        object.vertex(x,y,0,startu+(((float)j+1)/(float)c.stepsx)*deltau,(float)i/(float)c.stepsy);   
	        x = xoffset+renderResult[i+1][j+1][0]*totalcount*1.5f;
	        y = renderResult[i+1][j+1][1]*totalcount*1.5f;
	        object.vertex(x,y,0,startu+(((float)j+1)/(float)c.stepsx)*deltau,((float)i+1)/(float)c.stepsy); 
	        x = xoffset+renderResult[i+1][j][0]*totalcount*1.5f;
	        y = renderResult[i+1][j][1]*totalcount*1.5f;
	        object.vertex(x,y,0,startu+((float)j/(float)c.stepsx)*deltau,((float)i+1)/(float)c.stepsy);  
	        }
	      }
	      
	      object.endShape();
	      return object;
	  }
	  	  
	  class lines{
	   public float[] x = {0,0,0,0};
	   public float[] y = {0,0,0,0};
	  }
  	  
	  class handle{
	   public PVector pos; 
	   public int col;
	   public handle(float x, float y,int _cnt){
	     col = 255 - _cnt;
	     pos = new PVector();
	     this.pos.x = x;
	     this.pos.y = y;
	   }
	  }
	}