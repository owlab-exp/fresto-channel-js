/**************************************************************************************
 * Copyright 2013 TheSystemIdeas, Inc and Contributors. All rights reserved.          *
 *                                                                                    *
 *     https://github.com/owlab/fresto                                                *
 *                                                                                    *
 *                                                                                    *
 * ---------------------------------------------------------------------------------- *
 * This file is licensed under the Apache License, Version 2.0 (the "License");       *
 * you may not use this file except in compliance with the License.                   *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 * 
 **************************************************************************************/
package controllers;

import play.*;
import play.data.*;
import play.mvc.*;
import play.mvc.Http.*;

import views.html.*;

import java.util.*;

import org.apache.thrift.TSerializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TBinaryProtocol.Factory;

import fresto.data.ClientID;
import fresto.data.ResourceID;
import fresto.data.RequestEdge;
import fresto.data.ResponseEdge;
import fresto.data.DataUnit;
import fresto.data.Pedigree;
import fresto.data.FrestoData;

import fresto.Global;

public class Application extends Controller {

    public static Result index() {
        //return ok(index.render("Your new application is ready."));
	StringBuffer sb = new StringBuffer();
        return ok(sb.toString());
    }
  
    public static Result whatIsMyIPAddress() {
	    String remote = request().remoteAddress();
	    String result = "getip({ \"ip\":\""+ remote +"\" });";
	    Logger.info("result: " + result);
	    return ok(result);

    }

    public static Result feedUIEventByGet() {
	    response().setHeader("Access-Control-Allow-Origin", "*");

	    DynamicForm data = Form.form().bindFromRequest();

	    feedUIEvent(data);

	    return ok("RECV_OK");
    }

    public static Result feedUIEventByPost() {
	    response().setHeader("Access-Control-Allow-Origin", "*");

	    DynamicForm data = Form.form().bindFromRequest();

	    feedUIEvent(data);

	    return ok("RECV_OK");
    }

    public static void feedUIEvent(DynamicForm data) {
	    //response().setHeader("Access-Control-Allow-Origin", "*");

	    //DynamicForm data = Form.form().bindFromRequest();
	    String stage = data.get("stage");
	    String uuid = data.get("uuid");
	    String clientIp = data.get("clientId");
	    String referrer = data.get("referrer");
	    String targetUrl = data.get("targetUrl");
	    String method = data.get("method");
	    String timestamp = data.get("timestamp");
	    String elapsedTime = data.get("elapsedTime");
	    String httpStatus = data.get("httpStatus");

	    Logger.info(stage + "," + clientIp + "," + referrer + "," + uuid + "," + targetUrl + "," + method + ", " + timestamp + "," + elapsedTime + "," + httpStatus); 
	    long receivedTime = System.currentTimeMillis(); 


	    TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
	    
	    Pedigree pedigree = new Pedigree();
	    pedigree.setReceivedTime(receivedTime);
	    
	    if("HTTP_REQUEST".equals(stage)) {
	            /**
	             * clientId
	             * resourceId
	             * referrer
	             * method
	             * timestamp
	             * uuid
	             */
	            ClientID clientId = ClientID.clientIp(clientIp);
	            String urlStr = targetUrl.split("\\?")[0];
	            ResourceID resourceId = ResourceID.url(urlStr);
	            RequestEdge requestEdge = new RequestEdge();
	            requestEdge.setClientId(clientId);
	            requestEdge.setResourceId(resourceId);
	            requestEdge.setReferrer(referrer);
	            requestEdge.setMethod(method);
	            requestEdge.setTimestamp(Long.parseLong(timestamp));
	            requestEdge.setUuid(uuid);

	            DataUnit dataUnit = DataUnit.requestEdge(requestEdge);

	            FrestoData frestoData = new FrestoData();
	            frestoData.setPedigree(pedigree);
	            frestoData.setDataUnit(dataUnit);

	    	    try {
	    	    	byte[] serializedEvent = serializer.serialize(frestoData);
	    	        Global.publishToMonitor("CB", serializedEvent);
	    	    } catch (TException te) {
	    	            te.printStackTrace();
	    	    } finally {
	    	    }

	    } else if("HTTP_RESPONSE".equals(stage)) {
	           ClientID clientId = ClientID.clientIp(clientIp);
	           String urlStr = targetUrl.split("\\?")[0];
	           ResourceID resourceId = ResourceID.url(urlStr);
	           ResponseEdge responseEdge = new ResponseEdge();
	           responseEdge.setClientId(clientId);
	           responseEdge.setResourceId(resourceId);
	           responseEdge.setHttpStatus(Integer.parseInt(httpStatus));
	           responseEdge.setElapsedTime(Integer.parseInt(elapsedTime));
	           responseEdge.setTimestamp(Long.parseLong(timestamp));
	           responseEdge.setUuid(uuid);

	            DataUnit dataUnit = DataUnit.responseEdge(responseEdge);

	            FrestoData frestoData = new FrestoData();
	            frestoData.setPedigree(pedigree);
	            frestoData.setDataUnit(dataUnit);

	    	    try {
	    	    	byte[] serializedEvent = serializer.serialize(frestoData);
	    	        Global.publishToMonitor("CF", serializedEvent);
	    	    } catch (TException te) {
	    	            te.printStackTrace();
	    	    } finally {
	    	    }

	    } else {
	           Logger.info("The client event's stage does not match with required: " + stage);
	    } 

	    //return ok("RECV_OK");
    }
}
