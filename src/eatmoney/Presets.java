package eatmoney;

import java.util.ArrayList;

import enums.Target;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class Presets {

	
	eatMoneyMain emm;
	public ArrayList<Preset> allPresets;
	
	///////////////////////////PRESET CLASS
	class Preset{
		
		ArrayList<Change> changes;
		Target target;
		
		public Preset() {
			changes = new ArrayList<Change>();
		}

		public void addChange(Change _c) {
			changes.add(_c);
		}
		public void setTarget(Target _t) {
			target = _t;
		}
	}
	////////////////////////////CHANGE CLASS
	class Change{
		String target;
		Integer value;
	
		public Change(String _target,Integer _value) {
			target = _target;
			value = _value;
		}
	}
	
	
	///////////////////////////////INIT PRESETS
	public Presets(eatMoneyMain _emm) {
		emm = _emm;
		readData();	
	}
	//visible values:
	//0=cloth
	//1=cam
	//2=comment
	//3=butler
	
	//shader values
	//0=mit FX
	//1=ohne FX
	
	//cam values
	//0=cam1 - ücam1
	//1=cam2 - ücam2
	//2=ptz
	//3=handy
	
	//preset values
	//0 - pr 1
	//1 - pr 2 (split screen)

	//comments starting with 1 refering to filenames

	//ptz positions
	//0 - yld + onz vorn mitte
	//1 - connst oben steg
	//2 - links rampe konnst
	//3 - podest hinten tarp
	
	//////////////////////////////READ DATA
	
	public void readData() {
		allPresets = new ArrayList<Preset>();
		JSONArray savedData = emm.loadJSONArray("presets//presets.json");
		for (int i = 0; i < savedData.size(); i++) {
			JSONObject pre = savedData.getJSONObject(i);
			Object[] keys = pre.keys().toArray();
			Preset p = new Preset();
				for (int j = 0; j < pre.size(); j++) {
					if(keys[j].toString().equals("visible")){
						int t = pre.getInt("visible");
						if(t == 0) p.setTarget(Target.cloth);
						else if(t==1) p.setTarget(Target.cam);
						else if(t==2) p.setTarget(Target.comment);
						else if(t==3) p.setTarget(Target.butler);
					}
					else {
						Change c = new Change(keys[j].toString(),pre.getInt(keys[j].toString()));
						p.addChange(c);
					}
				}
				allPresets.add(p);
			}
	}
	
	public void loadPreset(int num) {
		Preset p = allPresets.get(num);
		int command = 0;
		for(Change c : p.changes) {
				if(c.target.equals("cloth")) {
					emm.changeCloth(c.value);
				}
				else if(c.target.equals("cam")) {
					emm.mm.switchCh(c.value);
				}
				else if(c.target.equals("shader")) {
					command = c.value;
				}
				else if(c.target.equals("preset")) {
					emm.mm.switchPreset(c.value);
				}
				else if(c.target.equals("comment")) {
					command = c.value;
				}
				else if(c.target.equals("osd")) {
					emm.showInsert(c.value);
				}
				else if(c.target.equals("ptz")) {
					emm.GP.udp.recallPreset(c.value);
				}				
			}
		
		if(p.target == Target.cam) {
			emm.showVideo(command);
		}
		else if(p.target == Target.cloth) {
			emm.showCloth();
		}
		else if(p.target == Target.butler) {
			emm.startButler();
		}
		else if(p.target == Target.comment) {
			emm.startComments(command);
		}
		else if(p.target == Target.no) {
			
		}
	}
}
