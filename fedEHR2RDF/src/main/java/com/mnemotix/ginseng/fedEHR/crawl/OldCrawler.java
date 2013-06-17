package com.mnemotix.ginseng.fedEHR.crawl;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.globus.gsi.CredentialException;

import com.mnemotix.ginseng.fedEHR.ApiManager;

import fr.maatg.pandora.clients.commons.exception.ClientError;
import fr.maatg.pandora.clients.fedehr.utils.FedEHRConnection;
import fr.maatg.pandora.ns.idal.ServerError;

public class OldCrawler {

	public static void main(String[] args) {
		try {

			Properties properties = new Properties();
			FileReader fileReader = new FileReader(args[0]);
			properties.load(fileReader);
			String pwd = properties.getProperty("pwd");
			String fedEHRServiceURL = properties.getProperty("fedehr.service.url");
			String certFile = properties.getProperty("usercert");
			String keyFile = properties.getProperty("userkey");
			String caPathPattern = "file:"+properties.getProperty("capath.pattern.path");
			long receiveTimeout = Long.parseLong(properties.getProperty("receivetimeout")); //10 minutes
			FedEHRConnection fedConnection = new FedEHRConnection(fedEHRServiceURL, certFile, keyFile, caPathPattern, pwd, receiveTimeout);
			System.out.println(fedConnection.fedEHRPortType.getLocalHospitalNodeName(""));
			Writer writer = new FileWriter(args[1]);
			ApiManager.crawlAllPatients(fedConnection, writer, 1000);
			writer.close();
		} catch (ClientError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CredentialException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
