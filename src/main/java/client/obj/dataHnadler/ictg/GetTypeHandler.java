package main.java.client.obj.dataHnadler.ictg;

import java.util.HashSet;
import java.util.Set;

import main.java.client.obj.dataHnadler.DataHandler;
import main.java.client.obj.model.ctg.IntentSummaryModel;

public class GetTypeHandler extends DataHandler {

	@Override
	public void handleData(IntentSummaryModel intentSummary, String key, Set<String> dataSet) {
		Set<String> newDataSet = new HashSet<String>();
		for (String data : dataSet)
			newDataSet.add(data.replace("\"", ""));

		intentSummary.getGetTypeCandidateList().addAll(newDataSet);
		return;
	}

}
