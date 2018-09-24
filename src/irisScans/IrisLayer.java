package irisScans;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;

class IrisLayer{
	 
     IrisScan is;
	  int[] col = new int[3];
	  float rndm;
	  int stroke;
	  int count;
	  int alpha;
	  private PGraphics img;
	  boolean active = false;
	  int inner = 50;
	  int outer = 150;
	  
	   public IrisLayer(IrisScan _is,int _col1,int _col2,int _col3, int _count, int _stroke,int _alpha,float _rndm){
		is = _is;
	    col[0] = _col1;
	    col[1] = _col2;
	    col[2] = _col3;
	    rndm = _rndm;
	    stroke = _stroke;
	    count = _count;
	    alpha = _alpha;
	    this.drawLayer();
	  }
	  
	  public void drawLayer(){
		  
	    this.img = is.ip.emm.createGraphics(is.wid,is.wid,PConstants.P3D);

	    float delta = (rndm/100.f);
	    float mod = 1.f-delta;
	    this.img.beginDraw();
	    this.img.clear();
	    this.img.translate(is.wid/2,is.wid/2);
	    this.img.strokeWeight(stroke);
	    this.img.noFill();
	    for(int i = 0; i < count; i++){
	      float pos1 = (float) Math.random();
	      float pos1h = pos1 * ((float)(Math.random()*delta*2.f)+mod);
	      float delth1 = (float) Math.random() * 100.f ;
	      float pos2 =  pos1 + ((float)(Math.random()*0.03f));
	      float pos2h = pos1 + ((float)(Math.random()*delta*2)+mod);
	      float delt = (float)(Math.random()*20.f)-10.f;
	      float delth2 = (float)(Math.random()*100.f);
	     
	     float innerx = (float) (Math.sin(pos1*PConstants.TWO_PI)*(inner+delt));
	     float innery = (float) (Math.cos(pos1*PConstants.TWO_PI)*(inner+delt));
	     float innerxh = (float) (Math.sin(pos1h*PConstants.TWO_PI)*(inner+delt+delth1));
	     float inneryh = (float) (Math.cos(pos1h*PConstants.TWO_PI)*(inner+delt+delth1));   
	     float outerxh = (float) (Math.sin(pos2h*PConstants.TWO_PI)*(outer+delt-delth2));
	     float outeryh = (float) (Math.cos(pos2h*PConstants.TWO_PI)*(outer+delt-delth2)); 
	     
	     float outerx = (float) (Math.sin(pos2*PConstants.TWO_PI)*(outer+delt));
	     float outery = (float) (Math.cos(pos2*PConstants.TWO_PI)*(outer+delt));
	   
	     //line(innerx,innery,outerx,outery);
	     this.img.stroke((float)(col[0]-Math.random()*30.f),(float)(col[1]+Math.random()*20.f-10.f),(float)(col[2]+Math.random()*190.f-9.f),(float)(Math.random()*alpha));
	     this.img.bezier(innerx,innery,innerxh,inneryh,outerxh,outeryh,outerx,outery);
	    }
	   this.img.endDraw();
	  }

	public PImage getImage() {
		return this.img.get();
	}
	}