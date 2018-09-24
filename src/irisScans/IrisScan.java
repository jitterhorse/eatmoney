package irisScans;

import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

class IrisScan{
	  private ArrayList<IrisLayer> irisLayers;
	  private PShader irisShader;
	  private String name;
	  private int [][] vals;
	  private float[] hei;
	  int wid = 400;
	  private int c = 0;
	  private boolean text = false;
	  Iris ip;
	  
	  public IrisScan(Iris _ip,int[][] _vals,float[] _hei,String _name){
		ip = _ip;
	    vals = _vals;
	    hei = _hei;
	    name = _name;
	    this.irisShader = ip.emm.loadShader("shader\\iris.glsl");
	    this.irisShader.set("resolution", new PVector((float)wid,(float)wid,0));
	    createIrisLayer();
	  }
	  
	  public void resetIris(){
	    for(int i = 0; i < irisLayers.size(); i++){
	     hei[i] = 0;
	     irisLayers.get(i).active = false;
	     text = false;
	    }
	    c = 0;
	  }
	  
	  public void drawIris(PGraphics targ){
		 	
		 for(int i = 0; i < hei.length;i++) {
		    this.irisShader.set("h"+i, hei[i]);
			this.irisShader.set("it"+i, irisLayers.get(i).getImage());
		 }	
		 
		 targ.shader(this.irisShader);
		 targ.noStroke();
		 targ.rect(0, 0, wid, wid);
		 targ.resetShader();
		
	    if(text == false) {
		    if(hei[c] < 1. && irisLayers.get(c).active == false ) hei[c] += 0.03;
	        else if(hei[c] >= 1. && irisLayers.get(c).active == false){
	        	irisLayers.get(c).active = true;
	        	c++;
	        	if(c == vals.length) text = true;
	        } 
	    }
	    
	  }
	  
	  public boolean getStatus() {
		  return text;
	  }
	  
	  
	  void createIrisLayer(){
	    irisLayers = new ArrayList<IrisLayer>();
	      for(int i = 0; i<vals.length; i++){
	       IrisLayer il = new IrisLayer(this,vals[i][0],vals[i][1],vals[i][2],vals[i][3],vals[i][4],vals[i][5],vals[i][6] / 10.f);
	       irisLayers.add(il);
	      }
	  }
	  
}