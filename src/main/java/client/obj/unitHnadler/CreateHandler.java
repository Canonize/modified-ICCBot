package main.java.client.obj.unitHnadler;

import main.java.analyze.model.analyzeModel.ObjectSummaryModel;

public class CreateHandler extends UnitHandler {

	@Override
	public void handleSingleObject(ObjectSummaryModel singleObject) {

		singleObject.getCreateList().add(unit);
	}

}
