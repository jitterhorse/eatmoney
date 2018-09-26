package eatmoney;

import java.util.ArrayList;

import processing.data.JSONArray;
import processing.data.JSONObject;

public class ClothStates {

	eatMoneyMain emm;
	ArrayList<CState> allCStates;
	
	public ClothStates(eatMoneyMain _emm) {
		emm = _emm;
		loadPresets();
	}
	
	
	
	public void loadPresets() {
		allCStates = new ArrayList<CState>();
		JSONArray savedData = emm.loadJSONArray("presets//clothStates.json");
		for (int i = 0; i < savedData.size(); i++) {
			JSONObject state = savedData.getJSONObject(i);
			CState s = new CState(state.getInt("stateId"),state.getFloat("damping"),state.getFloat("generalState"),state.getFloat("shakeH"));
			allCStates.add(s);
		}
	}
	
	public void loadState(int id) {
		emm.generalState = allCStates.get(id).genState;
		emm.cont.state.setValue(allCStates.get(id).genState * 100.f);
		emm.cont.state.update();
		emm.co.param_cloth_particle.DAMP_VELOCITY = allCStates.get(id).damping;
		emm.cont.damping.setValue(allCStates.get(id).damping*10000.f);
		emm.cont.damping.update();
		emm.co.shakeH = allCStates.get(id).shakeH;
		emm.co.TB.swopTime = allCStates.get(id).genState;
	}
	
	class CState{
		
		int id;
		float damping;
		float genState;
		float shakeH;
		
		public CState(int _id,float _damping, float _genState, float _shakeH) {
			id = _id;
			damping = _damping;
			genState = _genState;
			shakeH = _shakeH;
		}
		
	}
	
	
	
}
