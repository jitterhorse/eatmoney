package eatmoney;

import java.util.ArrayList;
import java.util.Random;

import com.thomasdiewald.pixelflow.java.DwPixelFlow;
import com.thomasdiewald.pixelflow.java.softbodydynamics.DwPhysics;
import com.thomasdiewald.pixelflow.java.softbodydynamics.constraint.DwSpringConstraint;
import com.thomasdiewald.pixelflow.java.softbodydynamics.particle.DwParticle;
import com.thomasdiewald.pixelflow.java.softbodydynamics.particle.DwParticle3D;
import com.thomasdiewald.pixelflow.java.softbodydynamics.softbody.DwSoftBody3D;
import com.thomasdiewald.pixelflow.java.softbodydynamics.softbody.DwSoftGrid3D;
import com.thomasdiewald.pixelflow.java.utils.DwCoordinateTransform;
import com.thomasdiewald.pixelflow.java.utils.DwStrokeStyle;

import eatmoney.ClothObject.clothCenter;
import eatmoney.textBadges.textB;
import enums.Follow;
import enums.ObjectMode;
import enums.mode;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PMatrix;
import processing.core.PMatrix3D;
import processing.core.PShape;
import processing.core.PVector;
import processing.opengl.PGraphics2D;
import processing.opengl.PGraphics3D;
import processing.opengl.PShader;

public class ClothObject {

	  final DwCoordinateTransform transform = new DwCoordinateTransform();
	
	  DwPixelFlow context;
	  
	  DwPhysics.Param param_physics = new DwPhysics.Param();
	  public DwParticle.Param param_cloth_particle = new DwParticle.Param();
	  
	  DwSpringConstraint.Param param_cloth_spring = new DwSpringConstraint.Param();
	  DwPhysics<DwParticle3D> physics = new DwPhysics<DwParticle3D>(param_physics);
	  
	  DwSoftGrid3D cloth = new DwSoftGrid3D();
	  
	  ArrayList<DwSoftBody3D> softbodies = new ArrayList<DwSoftBody3D>();
	  
	  
	  // entities to display
	  boolean DISPLAY_MESH           = true;
	  boolean DISPLAY_GRID 		     = false;
	  
	  boolean UPDATE_PHYSICS         = true;
	  
	  // first thing to do, inside draw()
	  boolean NEED_REBUILD = false;
	  
	  boolean MOVE_CAM       = true;
	  
	  PApplet parent;
	  eatMoneyMain parentc;
	  PGraphics  texture;
	  PShader tex,tex2,data;
	  PShader dataLines;

	  PShape object,crosses;
	  PShape shp_aabb;
	  int packets = 400;
	  float sizeObjects = 10.f;
	  PGraphics targetGraph;
	  
	  PVector camCenter = new PVector(0,0,0);
	  
	  float easing = 1.f;
	  public float textureAlpha = 0.f;
	  
	  public enum clothCenter{
		  cloth,pin,emit,comment;

		public static clothCenter random(){
			clothCenter[] vias = {clothCenter.cloth,clothCenter.pin,clothCenter.emit};
		    Random generator = new Random();
		    return vias[generator.nextInt(vias.length)];
		    }
		  
	  }
	  
	  clothCenter cc = clothCenter.cloth;
	  ObjectMode displaymode = ObjectMode.run;
	  
	  boolean shake = false;
	  int shakeCount = 0;
	  
	  boolean reshake = false;
	  
	  boolean mark = false;
	  int markCount = 0;
  
	  
	  boolean createExpose = false;
	  int exposeCount = 0;

	  boolean createBadges = false;
	  int badgeCount = 0;
	  
	  boolean cross = true;
	  
	  textBadges TB;
	  
	  ArrayList<DataPacket> currentHandles = new ArrayList<DataPacket>();
	  ArrayList<GridExpose> currentExposes = new ArrayList<GridExpose>();
	  ArrayList<lineAttach> currentBadges = new ArrayList<lineAttach>();
	  
	  DwParticle3D[] particles;
	  int nodedistance = 80; //distance between nodes
	  int nodecount = 30; // per axis = 30*30 = 900
	  
	  float[][] norms;
	  
	  commentObject userC;
	  
	  ArrayList<CommentBlob> currentComments = new ArrayList<CommentBlob>();
	  
	  float[] clothMoverSpeeds = {0.01f,0.03f};
	  
	  
	  class CommentBlob{
		  
		  float lifetime = 0.5f;
		  float impact = 0.f;
		  boolean active = true; //is active
		  boolean comments = false; //triggered comment?
		  PVector center = new PVector(0,0,0);
		  PVector activePosition = new PVector(0,0,0);
		  PVector size = new PVector(0,0,0);
		  PGraphics target = targetGraph;
		  PVector speeds = new PVector(0,0,0);
		  int col = 0;
		  int detail = 4;
		  int len;
		  
		  public CommentBlob(PVector _origin,int _len) {
			  center = _origin;
			  len = _len;
			  size.x = parent.random(700)+100;
			  size.y = parent.random(700)+100;
			  size.z = parent.random(700)+100;
			  speeds.x = parent.random(0.1f);
			  speeds.y = parent.random(0.1f);
			  speeds.z = parent.random(0.1f);
			  lifetime += (float)len * 0.025;		  
			  col = parentc.color((float)(Math.random()*100.+155.),(float)(Math.random()*50+30),0.f,(float)(Math.random()*100.+155));
			  detail += Math.floor(Math.random()*10);
			  
		  }
		  
		  public void move() {
			  if(active == true) {
				  activePosition.x = (float) (center.x + (size.x*lifetime)  * Math.sin(parent.frameCount*speeds.x));
				  activePosition.y = (float) (center.y + (size.y*lifetime) * Math.cos(parent.frameCount*speeds.y));
				  activePosition.z = (float) (center.z + size.z * (1.-Math.cos(parent.frameCount*speeds.z)));
				  
				  lifetime -= 0.02;
				  if(lifetime <= 0)   impact += 0.03;
				  if(impact >= 1.) active = false;
			  }
		  }
		  
		  
	  }
	    
	  
	  public ClothObject(PGraphics _target, PApplet _parent, eatMoneyMain _parentc) {
		  parent = _parent;
		  targetGraph = _target;
		  parentc = _parentc;
		  
		  TB = new textBadges(parentc,this);	
		  
		  userC = new commentObject(parent,parentc);
		  
		  texture = parent.createGraphics(2000,2000,PConstants.P2D);
		  tex = parent.loadShader("shader\\datatexture.glsl");
		  tex2 = parent.loadShader("shader\\oil.glsl");
		  data = parent.loadShader("shader\\packetsF2.glsl","shader\\packetsV2.glsl");
		  dataLines = parent.loadShader("shader\\dataLinesF.glsl");

		  createObj();
		  createCrosses();
		  setupPhysics();
	  }
	  
	  public void reState() {
		  parentc.sendOsc(PApplet.floor((float) (Math.random()*4.)));
		  if(Math.random() > 0.5 && parentc.generalState > 0.2) {
			  shake();
		  }
		  
		  else if(Math.random() > 0.8 && parentc.generalState > 0.3) {	  
			  reshake();
		  }
		  
		  if(Math.random() > 0.5 && parentc.generalState > 0.5) {
			  
			  mark();
		  }
		  		  
		  if(Math.random() > 0.5 && parentc.generalState > 0.1) {
			  emit();
		  }
		  if(Math.random() > 0.5 && parentc.generalState > 0.7) {  
			  badges();
		  }
		  
		  if(Math.random() > 0.5 && parentc.generalState > 0.25) {	  
			  DISPLAY_MESH = !DISPLAY_MESH;
		  }
		  if(Math.random() > 0.5 && parentc.generalState > 0.35) {
			  DISPLAY_GRID = !DISPLAY_GRID;
		  }	
		  
		  if(Math.random() > 0.8 && parentc.generalState > 0.45) {
			  cc = clothCenter.random();
		  }
		  if(Math.random() > 0.9 && parentc.generalState > 0.65) {
			  textureAlpha = parentc.abs(textureAlpha - 1.f);
		  }	
		  if(Math.random() > 0.2 && parentc.generalState > 0.45) {
			  clothMoverSpeeds[0] = (float) (Math.random()*0.03f);
			  clothMoverSpeeds[1] = (float) (Math.random()*0.05f);
		  }
		 
	  }
	  
	  
	  public void resetCloth() {
		  currentBadges = new ArrayList<lineAttach>();
		  currentHandles = new ArrayList<DataPacket>();
		  currentExposes = new ArrayList<GridExpose>();
		  TB.currentBadges = new ArrayList<textB>();
		  DISPLAY_MESH           = true;
		  DISPLAY_GRID 		     = false;
		  textureAlpha = 0.f;
	  }
	  
	  
	  public void calcCloth() {
		  
		  if(parentc.showMode == mode.cloth && Math.random() > 0.995) {
			  reState();
		  }
		  
		   createTexture(); //texture for CLOTH

		   float[] new1 = {100,parent.sin(parent.frameCount*clothMoverSpeeds[0])*500,1000,1};
		   float[] new2 = {1500,parent.sin(parent.frameCount*clothMoverSpeeds[1])*500,1000,1};

		   particles = physics.getParticles();	
		   particles[0].moveTo(new1, 0.8f);
		   particles[29].moveTo(new2, 0.8f);
		   
	   
		    if(NEED_REBUILD){
		      createBodies();
		      NEED_REBUILD = false;
		    }


		    // update physics simulation
		    if(UPDATE_PHYSICS){
		      physics.update(1);
		    }
		    
		    // update softbody surface normals
		    for(DwSoftBody3D body : softbodies){
		      body.computeNormals();
		    }

		    
		    /////////////////////////////////////////////////////////////////////
		    //////////////////           CALCULATE EXTRAS 
		    //////////////////////////////////////////////////////////////////

		    
			   if(shake == true) {
				   currentHandles = new ArrayList<DataPacket>();
				   float newphys = (float) (Math.random()*0.1f - 0.05f) ;
				   float newphys1 = (float) (Math.random()*0.02f - 0.01f) ;
				   param_physics.GRAVITY = new float[]{ 0,newphys1,newphys};
				   for(int i = 0; i < shakeCount; i++) {
					   int te = particles.length;
					   int t = parent.floor((float) (Math.random()*te));
					   DataPacket d = new DataPacket(t);
					   currentHandles.add(d);
					   
					   float[] new3 = {	(float) (particles[t].cx * (1.f + (Math.random() * 10.f - 5f))),
							   			(float) (particles[t].cy * (1.f + (Math.random() * 10.f - 5f))),
							   			(float) (particles[t].cz * (1.f + (Math.random()*10.f - 5f))),
							   			1};
					   
					   particles[t].moveTo(new3, 0.1f);	   
					   /*
					   float[] grav = {20.1f,2.f,0.5f};
					   particles[t].addGravity(grav);
					   */
				   }
				   shake = false;
				   shakeCount = 0;
			   }
			   
			   if(reshake == true) {
				   float newphys = (float) (Math.random()*0.1f - 0.05f) ;
				   param_physics.GRAVITY = new float[]{ 0,0,newphys};
				   for(DataPacket p : currentHandles) {
					   int t = p.id;
					   
					   float[] new3 = {	(float) (particles[t].cx * (1.f + (Math.random() * 20.f - 10f))),
					   			(float) (particles[t].cy * (1.f + (Math.random() * 20.f - 10f))),
					   			(float) (particles[t].cz * (1.f + (Math.random()*20.f - 10f))),
					   			1};
			   
					   particles[t].moveTo(new3, 0.1f);	
				   }
				   
				   reshake = false;
			   }
			   
			   if(createExpose == true) {
				   currentExposes = new ArrayList<GridExpose>();
				   for(int i = 0; i < exposeCount; i++) {
					   int te = particles.length;
					   GridExpose g = new GridExpose(parent.floor((float) (Math.random()*te)));
					   currentExposes.add(g);
				   }
				   createExpose = false;
				   
			   }
			   
			   if(createBadges == true) {
				   currentBadges = new ArrayList<lineAttach>();
				   for(int i = 0; i < badgeCount; i++) {
					   int te = particles.length;
					   lineAttach g = new lineAttach(parent.floor((float) (Math.random()*te)));
					   currentBadges.add(g);
					   
				   }
				   createBadges = false;
			   }
			   
			   if(mark == true) {
				   int count = (int) (5 + parentc.random(50));
				   TB.createBadge(count);
				   mark = false;
			   }
			   
			
	  }

	  public void drawDataPacks(PGraphics target) {
			
			norms = cloth.normals[1];
			data.set("time", parent.frameCount*0.01f);
			for(DataPacket dp : currentHandles) {
	            PMatrix pm3 = particles[dp.id].getShapeTransform();
	            
				float[] matr = new float[16];
				pm3.get(matr);
	            
	            float [] rot = norms[dp.id];
				float nlen = 500;
	            		
				target.pushMatrix();
				target.translate(matr[3],matr[7],matr[11]);
				target.noStroke();
				target.fill(255);
				target.sphere(5);
				target.stroke(255);
				target.strokeWeight(2);
				target.fill(255);

				
				target.shader(dataLines,PConstants.LINES);
				target.line(0,0,0,rot[0]*nlen,rot[1]*nlen,rot[2]*nlen);

				target.translate(rot[0]*nlen,rot[1]*nlen,rot[2]*nlen);
				target.noStroke();
				target.fill(255);
				target.sphere(1);
		
				PVector norm = new PVector(rot[0],rot[1],rot[2]);
				PVector up = new PVector(0,1,0);
				PVector ori = up.cross(norm);
				ori.normalize();


	    	    float angleOfRotation = PVector.angleBetween(norm, up);
	    		PVector axisOfRotation = norm.cross(up); 
	    		target.rotate( (-angleOfRotation), axisOfRotation.x, axisOfRotation.y, axisOfRotation.z ); 
				
				data.set("maxCount",(float)dp.maxcount);
				data.set("sizeDis", dp.size);
				data.set("speed", dp.speed);
				
				target.shader(data);
				
				target.lightFalloff(1.0f, 0.001f, 0.0f);
				target.lightSpecular(255, 0, 0);
				target.specular(12, 135, 0);
				target.shininess(2);
			    
				target.shape(object);
				
				target.resetShader();
				
				target.popMatrix();
			}
	  }
	
	  public void drawCloth(PGraphics target) {
		  
		  //DISPLAY_MESH = true;
		  if(cc == clothCenter.comment) {
			DISPLAY_MESH = false;
		  }
		  
		  if(DISPLAY_MESH){
			  target.pushMatrix();
		      for(DwSoftBody3D body : softbodies){
		    	body.createShapeMesh(target);		    	
		        body.displayMesh(target);
		   
		      }
		      target.popMatrix();
		    }
		  
		  
		  
		    if(DISPLAY_GRID) {
		    	target.pushMatrix();
		    	for(DwSoftBody3D body : softbodies){
				    DwStrokeStyle styleS = new DwStrokeStyle();
				    styleS.stroke_weight = 1;
				    styleS.stroke_color = parent.color(255,255,255,20);
				    body.createShapeWireframe(target, styleS);
				    body.computeNormals();
				    target.shader(dataLines,PConstants.LINES);
				    target.translate(0,0,-400);
			        body.displayWireframe(target);
			        target.translate(0,0,800);
			        body.displayWireframe(target);
			      }
		    	target.popMatrix();
		    }
		   
		   
		    target.pushMatrix();
		    for(GridExpose g : currentExposes) {
		    	if(g.offset < 0 && g.lifetime > 0.) {
		    		PVector pos = new PVector(particles[g.id].cx,particles[g.id].cy,particles[g.id].cz);
		    		PVector dir = new PVector(norms[g.id][0],norms[g.id][1],norms[g.id][2]);
		    		dir.normalize();
		    		target.pushMatrix();
		    		target.translate(pos.x, pos.y,pos.z + (g.grid*400));
		    		
		    		PVector upVector = new PVector (0,0,-1);
		    	    float angleOfRotation = PVector.angleBetween(dir, upVector);
		    		PVector axisOfRotation = dir.cross(upVector); 
		    		target.rotate( (-angleOfRotation), axisOfRotation.x, axisOfRotation.y, axisOfRotation.z );  
		    		
		    		
		    		//mainContent.rotate(dir.x,dir.y,dir.z,1.f);
		    		target.translate(0, 0, (1200.0f*g.grid) * (1.f-g.lifetime));
		    		target.rectMode(PConstants.CENTER);
		    		target.noFill();
		    		target.shader(dataLines,PConstants.LINES);
		    		target.stroke(255,255 * (g.lifetime));
		    		target.rect(0, 0, g.size, g.size);
		    		if(g.count > 1) {
		    			for(int i = 1; i < g.count; i ++) {
			    			target.pushMatrix();
			    			target.translate(0, 0, g.dist*i);
			    			target.stroke(255-(i*50),255 * (g.lifetime));
			    			target.rect(0, 0, g.size * (1f-i*0.1f), g.size* (1f-i*0.1f));
			    			target.popMatrix();
		    			}
		    		}
		    		target.popMatrix();
		    		g.lifetime -= 0.01;
		    	}
		    	
		    	else {
		    		g.offset--;
		    	}
		    }
		    target.popMatrix();
		    
		    target.pushMatrix();		    
		    for(lineAttach tb : currentBadges) {
		    		
		    		PVector pos = new PVector(particles[tb.id].cx,particles[tb.id].cy,particles[tb.id].cz);
		    	    PVector position = new PVector(particles[450].cx,particles[450].cy,particles[450].cz);

		    	    float x = (float) (position.x + pos.x);
		    	    float y = (float) (position.y + pos.y);
		    	    float z = (float) (position.z + pos.z);
		    	    
		    	    float x0 = (float) (position.x + (pos.x*200));
		    	    float y0 = (float) (position.y + (pos.y*200));
		    	    float z0 = (float) (position.z + (pos.z*200));
		    	    target.stroke(255);
		    	    target.strokeWeight(1);
		    	    target.shader(dataLines,PConstants.LINES);
		    	    target.line(x0,y0,z0,x,y,z);
		    }
		    
		    target.popMatrix();
		    

		
			 TB.drawBadges(target);
			
		    
		    
		  
		  if(currentComments.size() > 0 && userC.easing >= 1.) {
			  //CommentBlob c = currentComments.get(currentComments.size()-1);
			  CommentBlob c = currentComments.get(0);
			  c.move();
			  target.pushMatrix();
			  target.translate(c.activePosition.x, c.activePosition.y,c.activePosition.z);
			  target.noFill();
			  target.stroke(255);
			  target.strokeWeight(1);
			  target.sphereDetail(c.detail);
			  target.sphere(3 + (5000 * c.impact));
			  target.noLights();
			  target.strokeWeight(40 + 5000 * c.impact);
			  target.stroke(c.col);
			  target.point(0, 0);
			  target.popMatrix();
			  if(c.lifetime < 0 && c.comments == false) {
				  userC.next();
				  c.comments = true;
			  }
			  if(c.active == false) currentComments.remove(c);  
		  	}
		  
		  
		  
		  if(cross == true) {
				 for(int i = 0; i < nodecount*nodecount; i++) {
					 		PVector pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    PVector v1  = new PVector(-10,0,0);
						    pos.add(v1); 
						    crosses.setVertex((i*6),pos);
						    
					 		pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    v1  = new PVector(10,0,0);
						    pos.add(v1); 
						    crosses.setVertex((i*6)+1,pos);	

					 		pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    v1  = new PVector(0,-10,0);
						    pos.add(v1); 
						    crosses.setVertex((i*6)+2,pos);	

					 		pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    v1  = new PVector(0,10,0);
						    pos.add(v1); 
						    crosses.setVertex((i*6)+3,pos);

					 		pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    v1  = new PVector(0,0,-10);
						    pos.add(v1); 
						    crosses.setVertex((i*6)+4,pos);
						    
					 		pos = new PVector(particles[i].cx,particles[i].cy,particles[i].cz);
						    v1  = new PVector(0,0,10);
						    pos.add(v1); 
						    crosses.setVertex((i*6)+5,pos);
						    
					       
				 
				 }
				 target.pushMatrix();
				 target.shader(dataLines,PConstants.LINES);
				 target.shape(crosses);
				 target.popMatrix();
		  }

	  }
	  
	 
	  boolean in_frustum(PVector pos, PGraphics target) {
	        PMatrix3D MVP = ((PGraphics3D)target).projmodelview;
	        float[] where = {pos.x,pos.y,pos.z-100,1.f};
	        float[] Pclip = new float[4];
	        MVP.mult(where, Pclip);
	        return PApplet.abs(Pclip[0]) < Pclip[3] && 
	               PApplet.abs(Pclip[1]) < Pclip[3] && 
	               0 < Pclip[2] && 
	               Pclip[2] < Pclip[3];
	    }
	  
	  public void createTexture() {
		  texture.beginDraw();
		  texture.clear();
		  /*
		  tex.set("iTime",parent.frameCount*0.01f);
		  tex.set("thresh", easing);
		  tex.set("alpha", textureAlpha);
		  texture.shader(tex);
		  */
		  tex2.set("iTime",parent.frameCount*0.01f);
		  tex2.set("thresh", easing);
		  tex2.set("alpha", textureAlpha);
		  texture.shader(tex2);		  
		  texture.rect(0,0,parent.width,parent.height);
		  texture.resetShader();
		  texture.endDraw();			  
	  }
	  
	  public void setupPhysics() {
		// main library context
		    context = new DwPixelFlow(parent);
		    context.print();
		    context.printGL();
		    

		    ////////////////////////////////////////////////////////////////////////////
		    // PARAMETER settings
		    // ... to control behavior of particles, springs, etc...
		    ////////////////////////////////////////////////////////////////////////////
		    
		    // physics world parameters
		    param_physics.GRAVITY = new float[]{ 0,0,-0.1f};
		    param_physics.bounds  = new float[]{ -4200, -4200, -2200, +4200, +4200, +2200 };
		    param_physics.iterations_collisions = 2;
		    param_physics.iterations_springs    = 8;
		    
		    
		    
		    // particle parameters (for simulation)
		    param_cloth_particle.DAMP_BOUNDS    = 0.49999f;
		    param_cloth_particle.DAMP_COLLISION = 0.99999f;
		    param_cloth_particle.DAMP_VELOCITY  = 0.93999f; 
		    
		    
		    // spring parameters (for simulation)
		    param_cloth_spring.damp_dec = 0.999999f;
		    param_cloth_spring.damp_inc = 0.009999f;
		    

		    // soft-body parameters (for building)
		    cloth.CREATE_STRUCT_SPRINGS = true;
		    cloth.CREATE_SHEAR_SPRINGS  = false;
		    cloth.CREATE_BEND_SPRINGS   = true;
		    cloth.bend_spring_mode      = 0;
		    cloth.bend_spring_dist      = 2;
  
		    // softbodies
		    createBodies();
	  }
	  
	  
	  public void createBodies(){
		    
		    // first thing to do!
		    physics.reset();
		    
		    int nodex_x, nodes_y, nodes_z, nodes_r;
		    int nodes_start_x, nodes_start_y, nodes_start_z;
		    float r,g,b,s;
		    

		    // add to global list
		    softbodies.clear();
		    softbodies.add(cloth);

		    
		    // set some common things, like collision behavior
		    for(DwSoftBody3D body : softbodies){
		      body.self_collisions = true;
		      body.collision_radius_scale = 1f;
		    }
		    
		    
		    ///////////////////// CLOTH ////////////////////////////////////////////////
		    nodex_x = nodecount;
		    nodes_y = nodecount;
		    nodes_z = 1;
		    nodes_r = nodedistance;
		    nodes_start_x = 0;
		    nodes_start_y = 0;
		    nodes_start_z = 400;
		    r = 255;
		    g = 240;
		    b = 220;
		    s = 1f;
		    cloth.setMaterialColor(parent.color(r  ,g  ,b  ));
		    cloth.setParticleColor(parent.color(r*s,g*s,b*s));
		    cloth.setParam(param_cloth_particle);
		    cloth.setParam(param_cloth_spring);
		    cloth.create(physics, nodex_x, nodes_y, nodes_z, nodes_r, nodes_start_x, nodes_start_y, nodes_start_z);
		    cloth.createShapeParticles(parent);
		    DwStrokeStyle styleS = new DwStrokeStyle();
		    styleS.stroke_weight = 1;
		    styleS.stroke_color = 255;
		    cloth.createShapeWireframe(parent.g, styleS);
		    cloth.texture_XYp = (PGraphics2D) texture;
		    cloth.texture_XYn = (PGraphics2D) texture;


		    
		    // fix all 4 corners
		    /*
		    cloth.getNode(        10,         0, 0).enable(false, false, false);
		    cloth.getNode(nodex_x-1,         0, 0).enable(false, false, false);
		    cloth.getNode(nodex_x-1, nodes_y-1, 0).enable(false, false, false);
		    cloth.getNode(        0, nodes_y-1, 0).enable(false, false, false);
		    */

		  } 
	  
	  
	  //create DataPackets Particles 
	  void createObj(){
		     object = parent.createShape();
		     object.beginShape(PConstants.QUADS);
		     object.noStroke();
		     //object.colorMode(RGB, (float) 1.0) ;
		     for(int i = 0; i < packets;i++){
		      object.fill(255);
		      object.normal(0, 0, -1);
		      object.vertex(0,0,0,0,0);
		      object.normal(0, 0, -1);
		      object.vertex(0,sizeObjects,0,0,1);
		      object.normal(0, 0, -1);
		      object.vertex(sizeObjects,sizeObjects,0,1,1); 
		      object.normal(0, 0, -1);
		      object.vertex(sizeObjects,0,0,1,0);
		     }
		     object.endShape();
		 }
	  
	  void createCrosses() {
		  	crosses = parent.createShape();
		  	for(int i = 0; i < nodecount*nodecount; i++) {
			  	crosses.beginShape(PConstants.LINES);
		        crosses.stroke(255);
		        crosses.vertex(-10,0,0);
		        crosses.vertex(10,0,0);
		        crosses.vertex(0,-10,0);
		        crosses.vertex(0,10,0);
		        crosses.vertex(0,0,-10);
		        crosses.vertex(0,0,10);
		        crosses.endShape();
		  	}
	  }

	  
	  // sticks with particles sitting on cloth
	  class DataPacket{
		  
		  int id;
		  int maxcount;
		  float speed;
		  PVector size;
		  
		  public DataPacket(int _id) {
			  this.id = _id;
			  maxcount = (int)(20 + (Math.random() * (packets-20)));
			  speed = (float) (Math.random() * 40.f) -20.f;
			  size = new PVector((float)(Math.random()*400f),(float)(Math.random()*400f),(float)(Math.random()*400f));
		  }

	  }
	  
	  
	  // rectangles flying away from camera
	  class GridExpose {
		  
		  int id;
		  float speed;
		  float lifetime = 1.f;
		  int offset; // time offset
		  int grid; // from wich grid to start from?
		  float size;  // size of rect
		  int count; // child size
		  float dist; // how far are childs away
		  
		  public GridExpose(int _id) {
			  this.id = _id;
			  speed = (float) Math.random();
			  offset = (int) (Math.random()*300.f);
			  grid = (Math.random()>0.5f) ? -1 : 1;
			  size = (float)(Math.random() * 200.) + 10;
			  count = 1 + (int)(Math.random() * 4);
			  dist = (float)(Math.random() * 200.) + 10;
		  }
	  }
	  
	  
	  //textBadges
	  class lineAttach{
		  int id;
		  float start = 0.f;
		  float end = 1.f;
 
		  public lineAttach(int _id) {
			  this.id = _id;
			  start = (float) (Math.random() * 0.5 + 0.1);
			  end = (float) (Math.random() * (1.f-start));
			  
		  }
		  
	  }

	
	  
	  public PVector getClothMiddle() {
		  PVector middle = new PVector();
		   for(int i = 0;i <particles.length;i++) {
			   middle.x += particles[i].cx;
			   middle.y += particles[i].cy;
			   middle.z += particles[i].cz;
		   }
		   
		   middle.x /=  (float)particles.length;
		   middle.y /=  (float)particles.length;
		   middle.z /=  (float)particles.length;
		   camCenter = middle;
		return middle;
	}

	public PVector getPinMiddle() {
		PVector middle = new PVector();
		if(currentHandles.size() > 0) {
			 int tar = currentHandles.get(0).id;
		     middle.x = particles[tar].cx;
		     middle.y = particles[tar].cy;
		     middle.z = particles[tar].cz;
		   
		     float[][] norms = cloth.normals[1];
		     float [] rot = norms[0];
		     float nlen = 500;
		     middle.x += rot[0]*nlen;
		     middle.y += rot[1]*nlen;
		     middle.z += rot[2]*nlen;
		}
		else {
			middle = getClothMiddle();
		}
		camCenter = middle;
		return middle;
	}

	public PVector getEmitMiddle() {
		PVector middle = new PVector(0,0,0);
		if(currentExposes.size() > 0) {
			int id = currentExposes.get(0).id;
			middle.x = particles[id].cx;
			middle.y = particles[id].cy;
			middle.z = particles[id].cz;
		}
		camCenter = middle;
		return middle;
	}

	public PVector getCommentMiddle() {
		PVector middle = new PVector(0,0,0);
		if(currentComments.size() > 0) {
			int id = currentComments.size()-1;
			CommentBlob c = currentComments.get(id);
		    middle = c.center;
		}
		camCenter = middle;
		return middle;
	}

	
	
	public void shake() {
		shake = true;
		shakeCount = parent.floor(parent.random(30));
	}

	public void reshake() {
		if(currentHandles != null && currentHandles.size() > 0) {
		reshake = true;
		}
		
	}
	
	public void mark() {
		mark = true;
		markCount = parent.floor(parent.random(30));
	}
	
	public void emit() {
		createExpose = true;
		exposeCount = parent.floor(parent.random(40));
		
	}


	public void badges() {
		createBadges = true;
		badgeCount = parent.floor(parent.random(40));
	}

	public void newComment(int num) {
		int count = userC.load(num);
		for(int i = 0;i < count;i++) {
			PVector rand = new PVector();
			rand.x = (float) (Math.random() * 500.);
			rand.y = (float) (Math.random() * 500.);
			rand.z = (float) (Math.random() * 500.);
			int len = 1;
			if(i != 0) len = userC.commentare.get(i-1).len;
			CommentBlob c = new CommentBlob(rand,len);
			currentComments.add(c);
		}

	}
	
	public void stopComment() {
		currentComments = new ArrayList<CommentBlob>();

		
	}
	
	public PGraphics drawComments(PGraphics target) {
		return userC.draw(target);	
	}

	public void drawOutline(PGraphics target) {
		displayAABB(physics.param.bounds,target);
		
	}
	
	public void displayAABB(float[] aabb,PGraphics target){
	    if(shp_aabb == null){
	      float xmin = aabb[0], xmax = aabb[3];
	      float ymin = aabb[1], ymax = aabb[4];
	      float zmin = aabb[2], zmax = aabb[5];
	      
	      shp_aabb = parentc.createShape(PConstants.GROUP);
	      
	      PShape plane_zmin =  parentc.createShape();
	      plane_zmin.beginShape(PConstants.QUAD);
	      plane_zmin.stroke(0);
	      plane_zmin.strokeWeight(1);
	      plane_zmin.fill(64);
	      plane_zmin.normal(0, 0, 1); plane_zmin.vertex(xmin, ymin, zmin);
	      plane_zmin.normal(0, 0, 1); plane_zmin.vertex(xmax, ymin, zmin);
	      plane_zmin.normal(0, 0, 1); plane_zmin.vertex(xmax, ymax, zmin);
	      plane_zmin.normal(0, 0, 1); plane_zmin.vertex(xmin, ymax, zmin);
	      plane_zmin.endShape(PConstants.CLOSE);
	      shp_aabb.addChild(plane_zmin);
	      
	      PShape plane_zmax = parentc.createShape();
	      plane_zmax.beginShape(PConstants.QUAD);
	      plane_zmax.noFill();
	      plane_zmax.stroke(0);
	      plane_zmax.strokeWeight(1);
	      plane_zmax.vertex(xmin, ymin, zmax);
	      plane_zmax.vertex(xmax, ymin, zmax);
	      plane_zmax.vertex(xmax, ymax, zmax);
	      plane_zmax.vertex(xmin, ymax, zmax);
	      plane_zmax.endShape(PConstants.CLOSE);
	      shp_aabb.addChild(plane_zmax);
	      
	      PShape vert_lines = parentc.createShape();
	      vert_lines.beginShape(PConstants.LINES);
	      vert_lines.stroke(0);
	      vert_lines.strokeWeight(1);
	      vert_lines.vertex(xmin, ymin, zmin);  vert_lines.vertex(xmin, ymin, zmax);
	      vert_lines.vertex(xmax, ymin, zmin);  vert_lines.vertex(xmax, ymin, zmax);
	      vert_lines.vertex(xmax, ymax, zmin);  vert_lines.vertex(xmax, ymax, zmax);
	      vert_lines.vertex(xmin, ymax, zmin);  vert_lines.vertex(xmin, ymax, zmax);
	      vert_lines.endShape();
	      shp_aabb.addChild(vert_lines);
	      
	      PShape corners = parentc.createShape();
	      corners.beginShape(PConstants.POINTS);
	      corners.stroke(0);
	      corners.strokeWeight(7);
	      corners.vertex(xmin, ymin, zmin);  corners.vertex(xmin, ymin, zmax);
	      corners.vertex(xmax, ymin, zmin);  corners.vertex(xmax, ymin, zmax);
	      corners.vertex(xmax, ymax, zmin);  corners.vertex(xmax, ymax, zmax);
	      corners.vertex(xmin, ymax, zmin);  corners.vertex(xmin, ymax, zmax);
	      corners.endShape();
	      shp_aabb.addChild(corners);
	    }
	    target.shape(shp_aabb);

	  }



  
}


//clot handles
/*
pushMatrix();
translate(new1[0],new1[1],new1[2]);
noStroke();
fill(255,0,0);
sphere(20);
popMatrix();
 
pushMatrix();
translate(new2[0],new2[1],new2[2]);
noStroke();
fill(255,0,0);
sphere(20);
popMatrix();
/*
float[] mouse_world  = new float[4];
float[] mouse_screen = new float[4];


mouse_screen[0] = 300;
mouse_screen[1] = 100;
mouse_screen[2] = 0;
transform.useCurrentTransformationMatrix((PGraphics3D) this.g);
transform.screenToWorld(mouse_screen, mouse_world);

line(mouse_world[0],mouse_world[1],mouse_world[2],particles[57].cx,particles[57].cy,particles[57].cz);

mouse_screen[0] = 300;
mouse_screen[1] = 200;
mouse_screen[2] = 0;
transform.useCurrentTransformationMatrix((PGraphics3D) this.g);
transform.screenToWorld(mouse_screen, mouse_world);

line(mouse_world[0],mouse_world[1],mouse_world[2],particles[157].cx,particles[157].cy,particles[157].cz);	   
*/
