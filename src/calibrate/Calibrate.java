package calibrate;

import java.util.ArrayList;

import eatmoney.eatMoneyMain;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class Calibrate {

	  eatMoneyMain em;
	
	  int count;
	  public int totalcount;
	  ArrayList<handle> allHandles;
	  float [][][] renderSource;// = new float[2][20][2];
	  float [][][] renderResult;
	  float offset = 0;
	  
	  public PGraphics cali;

	public PGraphics preview;
	  int cheight;
	  int cwidth;	 
	  
	  PGraphics content;
	  int targetwidth;
	  int targetheight = 1080;

	  private ArrayList<Plane> allPlanes;
	  public ArrayList<PShape> planeObjects;
	  
	  public boolean mouseP = false;
	  int edit = 0;
	  int editpoint=0;
	  int stepsx = 16;
	  int stepsy = 9;
	
	  int border = 150;

	  public float currenthandle = 0.f;
	  String filename;
	  boolean newCalibration;
	  
	public Calibrate(eatMoneyMain _em,int _totalcount,String _filename, boolean _newCalibration, PGraphics _content) {
		em = _em;
		totalcount = _totalcount;
		this.content = _content;
		newCalibration = _newCalibration;
		targetwidth = 1920*totalcount;
		cwidth = 1280;
		cheight = 720/totalcount;
		
		filename = _filename;
		
		
		
		if(newCalibration == false) {
			JSONArray savedDataCheck = em.loadJSONArray("json//"+filename);
			totalcount = savedDataCheck.size();
			cheight = 720/totalcount;
		}
		
		cali = em.createGraphics(cwidth+ border*2,cheight+ border*2,PConstants.P3D);
		preview = em.createGraphics(cwidth,cheight,PConstants.P3D); 
		this.allPlanes = new ArrayList<Plane>(); 
		
		 //create ContentPlanes
		for(int i = 0; i < this.totalcount; i++){
		  Plane pl = new Plane(i,this.totalcount,this);
		  this.allPlanes.add(pl);
		 }
		
		if(newCalibration == false) {
			this.readData();
		}
		
		this.drawPlanes();
		this.renderPlanes();
		
		if(newCalibration == true) {
			this.saveData();
		}	
		
	}
	
	public void drawPlanes() {
		cali.beginDraw();
		cali.hint(PConstants.DISABLE_OPTIMIZED_STROKE);
		cali.clear();
		cali.strokeWeight(1);
		cali.background(70);
		cali.translate(border,border);
		//render background
		for(int i = 0; i < allPlanes.size(); i++){
			cali.fill(255,100);
			cali.stroke(70);
			cali.rect((cwidth/totalcount)*i,0,(cwidth/totalcount),(720/totalcount));
		}
	    for(int i = 0; i < allPlanes.size(); i++){
	      allPlanes.get(i).draw(this.cali);
	    }
		cali.endDraw();
		
		preview.beginDraw();
		preview.hint(PConstants.DISABLE_OPTIMIZED_STROKE);
		preview.clear();		
		for(int i = 0; i < allPlanes.size(); i++){
		      allPlanes.get(i).draw(this.preview);
		    }
		
		preview.endDraw();
	}
	
	
	
	public void renderPlanes() {
		planeObjects = new ArrayList<PShape>();
		for(int i = 0; i < allPlanes.size(); i++){
			PShape plane = allPlanes.get(i).renderPlane();
			planeObjects.add(plane);
		}
	}
	
	public void saveData() {
		JSONArray saveddata = new JSONArray();
		for(Plane p : allPlanes) {
			JSONArray planedata = new JSONArray();
			for(int i = 0; i < p.allHandles.size();i++) {
				JSONObject handle = new JSONObject();
				handle.setFloat("posx", p.allHandles.get(i).pos.x);
				handle.setFloat("posy", p.allHandles.get(i).pos.y);
				handle.setInt("col", p.allHandles.get(i).col);
				planedata.setJSONObject(i, handle);
			}
			saveddata.setJSONArray(allPlanes.indexOf(p), planedata);
		}
		
		//System.out.println(filename);
		em.saveJSONArray(saveddata,"json//"+filename);
	}

	public void readData() {
		JSONArray savedData = em.loadJSONArray("json//"+filename);
		//System.out.println(savedData.size());
		for (int i = 0; i < savedData.size(); i++) {
			JSONArray plane = savedData.getJSONArray(i);
			//System.out.println(plane.size());
				for (int j = 0; j < plane.size(); j++) {
					JSONObject handle = plane.getJSONObject(j);
					allPlanes.get(i).allHandles.get(j).pos.x = handle.getFloat("posx");
					allPlanes.get(i).allHandles.get(j).pos.y = handle.getFloat("posy");	
					allPlanes.get(i).allHandles.get(j).col = handle.getInt("col");	
					}
			}
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
