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
package fresto;

import play.*;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
//import com.orientechnologies.orient.core.db.graph.OGraphDatabase;

public class Global extends GlobalSettings {

	private String EVENT_HUB_HOSTNAME;
	private String EVENT_HUB_PORT;
	private static ZMQ.Context context;
	private static ZMQ.Socket publisher;
	//private static OGraphDatabase oGraph;

	@Override
		public void onStart(Application app) {
			Logger.info("Initializing");
			EVENT_HUB_HOSTNAME = Play.application().configuration().getString("event.hub.hostname");
			EVENT_HUB_PORT = Play.application().configuration().getString("event.hub.port");

			Logger.info("Connecting JeroMQ socket");
			
			context = ZMQ.context(1);
			publisher = context.socket(ZMQ.PUB);
			publisher.connect("tcp://" + EVENT_HUB_HOSTNAME + ":" + EVENT_HUB_PORT);

			Logger.info("JeroMQ publisher uses " + EVENT_HUB_HOSTNAME + ":" + EVENT_HUB_PORT);

			//oGraph = new OGraphDatabase("remote:fresto3.owlab.com/frestodb");
			//oGraph.setProperty("minPool", 3);
			//oGraph.setProperty("maxPool", 10);

			Logger.info("Application has started");
		}

	@Override
		public void onStop(Application app) {
			Logger.info("Application shutdown...");
			Logger.info("Closing JeroMQ sockets");
			publisher.close();
			context.term();

			//oGraph.close();
		}

	public static ZMQ.Socket getPublisher(){
		// If sending is atomic action, then this method can be used
		return publisher;
	}

	public static synchronized void publishToMonitor(String topic, byte[] message){

		publisher.send(topic.getBytes(), ZMQ.SNDMORE);
		publisher.send(message, 0);

	}

	//public static OGraphDatabase openDatabase() {
	//	return oGraph.open("admin", "admin");
	//}
}
