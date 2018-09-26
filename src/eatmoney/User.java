package eatmoney;

import processing.core.PConstants;
import processing.core.PGraphics;

class User{
	  
	  int total = 12333000;
	  int totallive = 348900;
	  int[] div = {0,0};
	  int[] changeAMT = {0,0};
	  boolean[] change = {false,false};
	  int[] direction = {0,0};
	  VideoObject parent;
	  PGraphics userG;
	  
	  public User(VideoObject _parent){
		  parent = _parent;
		  userG = parent.parent.createGraphics(350, 197, PConstants.P2D);
	  }

	void calcUser(){
	  if(Math.random() > 0.99 && change[0]== false){
	    setnumber();
	    change[0] = true;
	  }
	  if(Math.random() > 0.99 && change[1]== false){
	    setnumber2();
	    change[1] = true;
	  }
	  
	}

	void drawUser(){
	  this.calcUser();
	  userG.beginDraw();
	  userG.clear();
	  userG.textAlign(PConstants.RIGHT,PConstants.TOP);
	  userG.stroke(255);
	  userG.fill(255);
	  userG.textSize(16);
	  userG.text("FOLLOWER:  " + total,userG.width - 60,10);
	  userG.text("LIVE:  " + totallive,userG.width - 60,50);
	  
	    
	    if(change[0] == true){
	      total += direction[0];
	      div[0] -= direction[0];
	      int col = (direction[0] == 1) ? parent.parent.color(0,255,0) : parent.parent.color(255,0,0);
	      String sign = (direction[0] == 1) ? "+ " : "";
	      userG.fill(col);
	      userG.textAlign(PConstants.LEFT,PConstants.TOP);
	      userG.text(sign + changeAMT[0],userG.width - 60,10);
	      if(div[0] == 0) change[0] = false;
	    }
	   
	     
	    if(change[1] == true){
	      totallive += direction[1];
	      div[1] -= direction[1];
	      int col = (direction[1] == 1) ? parent.parent.color(0,255,0) : parent.parent.color(255,0,0);
	      String sign = (direction[1] == 1) ? "+ " : "";
	      userG.fill(col);
	      userG.textAlign(PConstants.LEFT,PConstants.TOP);
	      userG.text(sign + changeAMT[1],userG.width - 60,50);
	      if(div[1] == 0) change[1] = false;
	    }
	   userG.endDraw();
	}


	  void setnumber(){
	   div[0] = (int)(Math.random() * 10 - 5); 
	   if(div[0] == 0) div[0] = 1;
	   changeAMT[0] = div[0];
	   direction[0] = (div[0] >= 0) ? 1 : -1;
	  }

	  void setnumber2(){
	   div[1] = (int)(Math.random() * 100 - 50); 
	   if(div[1] == 0) div[1] = 1;
	   changeAMT[1] = div[1];
	   direction[1] = (div[1] >= 0) ? 1 : -1;
	  } 
	    
	  public void setUserCount(int count) {
		  this.total = count;
		  this.totallive = (int) (count * (0.3 * Math.random() + 0.15));
	  }
	  
	  public PGraphics getUserImage() {
		  return userG;
	  }
	  
	  
}