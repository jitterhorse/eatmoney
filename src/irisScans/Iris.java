package irisScans;

import java.util.ArrayList;

import eatmoney.eatMoneyMain;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class Iris {

	ArrayList<IrisScan> iScans;

	int[][] vals1 = {{244,245,255,1000,3,120,186},{210,103,104,40,4,140,186},{20,190,80,1000,1,280,182},{20,210,190,400,4,50,153},{100,210,10,200,4,90,106}};
	int[][] vals2 = {{144,65,65,600,2,180,186},{214,193,104,70,5,140,86},{120,190,180,100,2,340,82},{120,110,190,400,4,50,193},{90,120,160,300,3,90,186}};
	float[] hei1 = {0.f,0.f,0.f,0.f,0.f};
	float[] hei2 = {0.f,0.f,0.f,0.f,0.f};
	String name1 = "FRERICH KONNST";
	String name2 = "STEFAN TARP";

	int wid = 400;
	PGraphics main;
	eatMoneyMain emm;

	public Iris(eatMoneyMain _emm , int size){
	 emm =_emm;
	 wid = size;
	 main = emm.createGraphics(wid,wid,PConstants.P3D);
	 iScans = new ArrayList<IrisScan>();
	 IrisScan i = new IrisScan(this,vals1,hei1,name1);
	 iScans.add(i);
	 IrisScan i2 = new IrisScan(this,vals2,hei2,name2);
	 iScans.add(i2);  
	 
	}

	public void draw(int ir){
	 main.beginDraw();
	 main.clear();
	 main.fill(0);
	 main.noStroke();
	 main.ellipse(wid/2,wid/2,100,100);
	 iScans.get(ir).drawIris(main);
	 main.endDraw();
	}

	public PGraphics getIrisImage() {
		return main;
	}

	public void reset() {
		iScans.get(0).resetIris();
		iScans.get(1).resetIris();
		emm.vo.vb.trackingtime = 0.f;
	}

	public boolean getStatus(int ir) {
		return iScans.get(ir).getStatus();
	}


	
	
}
