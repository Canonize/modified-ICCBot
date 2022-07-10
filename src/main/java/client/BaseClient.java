package main.java.client;

import java.io.IOException;

import main.java.client.statistic.model.StatisticResult;

import org.dom4j.DocumentException;

/**
 * BaseClient
 * 
 * @author hanada
 * @version 2.0
 */
public abstract class BaseClient {
	protected StatisticResult result;

	public void start() {
		clientAnalyze();
		try {
			clientOutput();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	protected abstract void clientAnalyze();

	public abstract void clientOutput() throws IOException, DocumentException;

	public StatisticResult getResult() {
		return result;
	}
}