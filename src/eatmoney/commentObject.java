package eatmoney;


import java.io.*;
import java.io.FileReader.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import enums.ObjectMode;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.opengl.PShader;

class commentObject{

  ArrayList<commentar> commentare;
  DataInputStream in;
  String loadcomm;
  PApplet parent;
  eatMoneyMain emm;
  PGraphics areaC;
  int current = 0;
  int textSize = 50;
  
  float scollPosY = 0;
  PFont f;
  
  float totallinesFade = 0.f;
  float totalcommentsFade = 0.f;
  
  int cWidth = 1200;
  int[] planeWidth = {cWidth - 10,cWidth - 30,cWidth - 50,cWidth - 70,cWidth - 90}; 
  int[] textWidth = {cWidth - 20,cWidth - 40,cWidth - 60,cWidth - 80,cWidth - 100}; 
  int[] shiftx = {0,20,40,60,80};
  float distanceComments = 0.2f;
  
  float easing = 0.f;
  ObjectMode displaymode = ObjectMode.off;
  
  public commentObject(PApplet _parent, eatMoneyMain _emm){
   this.parent = _parent;
   this.emm = _emm;
   areaC = parent.createGraphics(cWidth,800,PConstants.P2D);
   f = parent.createFont("Iosevka", 50);
   areaC.textFont(f);
   //this.load();
   scollPosY = areaC.height;
  }

  public int load(int file){
    commentare = new ArrayList<commentar>();
    current = 0;
    BufferedReader br;
    try {
        
        InputStream inputStream       = new FileInputStream(parent.sketchPath() +"\\comments\\comments_"+ file +".txt");
        Reader inputStreamReader = new InputStreamReader(inputStream, "Cp1252");
        br = new BufferedReader(inputStreamReader);
        String line = br.readLine();

        while (line != null) {
        	String id = line.substring(0, 1);
            String content = line.substring(2);
            
            int layer = Integer.parseInt(id);
            int len = content.length();
            commentar uc = new commentar(layer,content,len,parent);
            
            commentare.add(uc);
            line = br.readLine();
        }
        
        br.close();
    }
    catch (IOException e) {
          e.printStackTrace();
        } 
    return commentare.size();
  }
  
  public PGraphics draw(PGraphics target){
   target.endDraw();

   areaC.beginDraw();
   areaC.clear();
   areaC.translate(0,scollPosY);
 
   
   int totallines = 0;
   int totalcomments = 0;

   for(commentar c : commentare){
     if(c.visible == true){
         areaC.pushMatrix();
         areaC.textFont(f);
         areaC.textSize(textSize);
         areaC.textLeading(textSize*1.0f);
         if(c.alpha < 1.) c.addAlpha();
         float alpha = c.alpha;
         areaC.fill(255,190*alpha*easing);
         areaC.noStroke();
         //shifty = (countComments * heightSpacebetweenComments) + (inbetweenlines * textLeading) + lines*lineheight
         float shifty = (totalcomments * (textSize*distanceComments)) + totallines*((areaC.textAscent() + areaC.textDescent()));
         areaC.rect(5+shiftx[c.layer],shifty,planeWidth[c.layer],c.lines * (areaC.textAscent() + areaC.textDescent()),5);  
         areaC.fill(0,255*alpha*easing);
         areaC.stroke(0,255*alpha*easing);
         areaC.text(c.content,10+shiftx[c.layer],shifty,textWidth[c.layer],c.lines * ((areaC.textAscent() + areaC.textDescent())));   
         areaC.popMatrix();
         totallines += c.lines;
         totalcomments++;
         
     }
   }
   totalcommentsFade = parent.lerp(totalcommentsFade,(float)totalcomments,0.7f);
   totallinesFade = parent.lerp(totallinesFade,(float)totallines,0.7f);
   scollPosY = areaC.height - ((totalcommentsFade *  (textSize*distanceComments))+ totallinesFade*(areaC.textAscent() + areaC.textDescent()));
   areaC.endDraw();
   
   target.beginDraw();
   return areaC;
  }
  
  public void next(){
      current++;
      if(current > commentare.size()){
        for(commentar c : commentare){
         c.setVisible(false); 
        }
        current = 1;
      }
      else{
        commentare.get(current-1).setVisible(true);
        commentare.get(current-1).alpha = 0.f;
        emm.sendOsc(5);
      }
  }


  class commentar{
	    boolean visible = false;
	    String content;
	    PApplet parent;
	    int lines;
	    float alpha = 0.f;
	    int layer = 0;
	    int len;
	    
	    public commentar(int _layer, String _content,int _len, PApplet _parent){
	      content = _content;
	      parent = _parent;
	      layer = _layer;
	      len = _len;
	      parent.textFont(f);
	      parent.textSize(textSize);
	      float tw = parent.textWidth(content);
	      lines = parent.ceil(tw/(float)textWidth[layer]);
	    }
	    
	    public void setVisible(boolean v){
	     visible = v; 
	    }
	    
	    public void addAlpha(){
	     alpha+= 0.05; 
	    }
  }

}