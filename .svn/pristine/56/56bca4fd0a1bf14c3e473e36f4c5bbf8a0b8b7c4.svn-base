package com.pfizer.mrbt.genomics.TransmartClient;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class GetSNPs {
private static String GET_SNP_SOURCE="http://amre1al336.pcld.pfizer.com:8080/transmartPfizer/webservice/getSnpSources";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// http://amre1al336.pcld.pfizer.com:8080/transmartPfizer/webservice/getSnpSources
		try {
			WebResource webRes = Client.create().resource(GET_SNP_SOURCE);
			Builder clientReqBuilder = webRes.accept(MediaType.APPLICATION_XML);
			String result = clientReqBuilder.get(String.class);
			System.out.println(result);
		} catch (UniformInterfaceException ex) { // resource return code is unexpected.
			int statusCode = ex.getResponse().getClientResponseStatus().getStatusCode();
			System.out.println("status code: " + statusCode);
			ex.printStackTrace();
		} catch (ClientHandlerException ex) { // error in client handler while processing
			ex.printStackTrace();
		}
	}
}

