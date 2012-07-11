/* *********************************************************************** *
 * project: org.matsim.*
 * networkChange.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

/**
 * 
 */
package playground.ikaddoura.parkAndRide.prepare;

import java.io.IOException;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.scenario.ScenarioLoaderImpl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

/**
 * @author Ihab
 *
 */

public class NetworkAddPRMain {
	
	
	// input
	static String networkFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlinTest/testScenario_network.xml";
	static String scheduleFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlinTest/testScenario_schedule.xml";
	static String vehiclesFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlinTest/testScenario_vehicles.xml";
	
	// output
	static String prFacilitiesFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlinTest/PRfacilities_berlin.txt";
	static String prNetworkFile = "../../shared-svn/studies/ihab/parkAndRide/inputBerlinTest/testScenario_network_PR.xml";
	
	static double extensionRadius = 10;
	static int maxSearchSteps = 100;
	
//	static String networkFile;
//	static String scheduleFile;
//	static String vehiclesFile;
//	
//	// output
//	static String prFacilitiesFile;
//	static String prNetworkFile;
//	
//	static double extensionRadius;
//	static int maxSearchSteps;
		     
	// parkAndRide Link:
	private double capacity = 2000;
	private double freeSpeed = 2.77778;
	private double length = 20;
	private double nrOfLanes = 40;
	
	private ScenarioImpl scenario = (ScenarioImpl) ScenarioUtils.createScenario(ConfigUtils.createConfig());

	public static void main(String[] args) throws IOException {
		
//		// input
//		networkFile = args[0];
//		scheduleFile = args[1];
//		vehiclesFile = args[2];
//		
//		// output
//		prFacilitiesFile = args[3];
//		prNetworkFile = args[4];
//		
//		extensionRadius = Double.parseDouble(args[5]);
//		maxSearchSteps = Integer.parseInt(args[6]);
		
		NetworkAddPRMain addParkAndRide = new NetworkAddPRMain();
		addParkAndRide.run();
	}

	private void run() {
		
		loadScenario();
		
		PRNodeSearch prNodeSearch = new PRNodeSearch();
		prNodeSearch.searchForCarLink(this.scenario, extensionRadius, maxSearchSteps);
		
		PRFacilityCreator prFacilityCreator = new PRFacilityCreator();
		prFacilityCreator.setCapacity(this.capacity);
		prFacilityCreator.setFreeSpeed(this.freeSpeed);
		prFacilityCreator.setLength(this.length);
		prFacilityCreator.setNrOfLanes(this.nrOfLanes);
		
		int i = 0;
		for (Id nodeId : prNodeSearch.getCarLinkToNodes().keySet()){
			Id id = new IdImpl(i);
			prFacilityCreator.createPRFacility(id, prNodeSearch.getCarLinkToNodes().get(nodeId).getNode(), this.scenario, prNodeSearch.getCarLinkToNodes().get(nodeId).getStopName());
			i++;
		}
		
		System.out.println("***");
		
		if (prNodeSearch.getStopsWithoutPRFacility().isEmpty()){
			System.out.println("For all TransitStopFacilities a car-Link was found and a Park'n'Ride Facility was created.");
		}
//		for (TransitStopFacility stop : prNodeSearch.getStopsWithoutPRFacility()){
//			System.out.println("No Park'n'Ride Facility created for: " + stop.getId() + ": " + stop.getName() + " because no car-Link was found!");
//		}
//		for (TransitStopFacility stop : prNodeSearch.getStopsNotSUBerlin()){
//			System.out.println("No Park'n'Ride Facility created for: " + stop.getId() + ": " + stop.getName() + " because of the facility name. To be checked...");
//		}
		
		NetworkWriter networkWriter = new NetworkWriter(scenario.getNetwork());
		networkWriter.write(prNetworkFile);
		
		TextFileWriter writer = new TextFileWriter();	
		writer.write(prFacilityCreator.getParkAndRideFacilities(), prFacilitiesFile);
		writer.writeInfo(prNodeSearch.getStopsWithoutPRFacility(), "../../shared-svn/studies/ihab/parkAndRide/inputBerlin/info_stops_noCarLinkFound.txt");
		writer.writeInfo(prNodeSearch.getStopsNotSUBerlin(), "../../shared-svn/studies/ihab/parkAndRide/inputBerlin/info_stops_noPRbyName.txt");
		
//		for (TransitStopFacility stop : prNodeSearch.getTransitStop2nearestCarLink().keySet()){
//			System.out.println("TranistStopFacility: " + stop.getId().toString() + " " + stop.getName() + " " + stop.getCoord().toString() + " / next car-Link: " + prNodeSearch.getTransitStop2nearestCarLink().get(stop).getId() + " / toNode:" + prNodeSearch.getTransitStop2nearestCarLink().get(stop).getToNode().getCoord().toString());
//		}	
	}

	private void loadScenario() {
		Config config = scenario.getConfig();
		config.scenario().setUseTransit(true);
		config.scenario().setUseVehicles(true);
		config.transit().setTransitScheduleFile(scheduleFile);
		config.transit().setVehiclesFile(vehiclesFile);
		config.network().setInputFile(networkFile);
		ScenarioLoaderImpl scenarioLoader = new ScenarioLoaderImpl(scenario);
		scenarioLoader.loadScenario();		
	}
}
