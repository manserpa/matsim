/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2011 by the members listed in the COPYING,        *
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

package org.matsim.contrib.minibus.replanning;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.minibus.fare.StageContainer;
import org.matsim.contrib.minibus.fare.StageContainerHandler;
import org.matsim.contrib.minibus.fare.TicketMachineI;
import org.matsim.contrib.minibus.genericUtils.RecursiveStatsContainer;
import org.matsim.contrib.minibus.operator.Operator;
import org.matsim.contrib.minibus.operator.PPlan;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * 
 * Removes all stops belonging to a demand relation with trips below a certain threshold.
 * Threshold is standard deviation of number of trips or collected fare of all relations twice a scaling factor.
 * 
 * @author aneumann
 *
 */
public final class ReduceStopsToBeServedRFareBF extends AbstractPStrategyModule implements StageContainerHandler{
	
	private final static Logger log = Logger.getLogger(ReduceStopsToBeServedRFareBF.class);
	
	public static final String STRATEGY_NAME = "ReduceStopsToBeServedRFareBF";
	
	private final double sigmaScale;
	private final boolean useFareAsWeight;

	private TicketMachineI ticketMachine;
	private LinkedHashMap<Id<TransitRoute>, LinkedHashMap<Id<TransitStopFacility>, LinkedHashMap<Id<TransitStopFacility>, Double>>> route2StartStop2EndStop2WeightMap = new LinkedHashMap<>();
	
	public ReduceStopsToBeServedRFareBF(ArrayList<String> parameter) {
		super();
		if(parameter.size() != 2){
			log.error("Too many parameter. Will ignore: " + parameter);
			log.error("Parameter 1: Scaling factor for sigma");
			log.error("Parameter 2: true=use the fare as weight, false=use number of trips as weight");
		}
		this.sigmaScale = Double.parseDouble(parameter.get(0));
		this.useFareAsWeight = Boolean.parseBoolean(parameter.get(1));
		log.info("enabled");
	}
	
	@Override
	public PPlan run(Operator operator) {
		// get best plans route id
		TransitRoute routeToOptimize = null;
		if (operator.getBestPlan().getLine().getRoutes().size() != 2) {
			log.error("Each plan should have 2 routes (BaF) -> there is something wrong in plan: " + operator.getBestPlan().getId().toString());
		}
		for (TransitRoute route : operator.getBestPlan().getLine().getRoutes().values()) {
			String[] routeIdSplit = route.getId().toString().split("-");
			String routeTyp = routeIdSplit[routeIdSplit.length - 1];
			if(routeTyp.equals("Back"))
				routeToOptimize = route;
		}
		
		ArrayList<TransitStopFacility> stopsToBeServed = getStopsToBeServed(this.route2StartStop2EndStop2WeightMap.get(routeToOptimize.getId()), routeToOptimize);
		
		if (stopsToBeServed.size() < 2) {
			// too few stops left - cannot return a new plan
			return null;
		}
		
		// profitable route, change startTime
		PPlan newPlan = new PPlan(operator.getNewPlanId(), this.getStrategyName(), operator.getBestPlan().getId());
		newPlan.setNVehicles(1);
		newPlan.setStopsToBeServed(stopsToBeServed);
		newPlan.setStartTime(operator.getBestPlan().getStartTime());
		newPlan.setEndTime(operator.getBestPlan().getEndTime());
		newPlan.setPVehicleType(operator.getBestPlan().getPVehicleType());
		
		newPlan.setLine(operator.getRouteProvider().createTransitLineFromOperatorPlan(operator.getId(), newPlan));
		
		return newPlan;
	}

	private ArrayList<TransitStopFacility> getStopsToBeServed(LinkedHashMap<Id<TransitStopFacility>, LinkedHashMap<Id<TransitStopFacility>, Double>> startStop2EndStop2WeightMap, TransitRoute routeToOptimize) {
		ArrayList<TransitStopFacility> tempStopsToBeServed = new ArrayList<>();
		RecursiveStatsContainer stats = new RecursiveStatsContainer();
		
		if (startStop2EndStop2WeightMap == null) {
			// There is no entry for that particular line - possibly no demand - returning empty line
			return tempStopsToBeServed;
		}
		
		// calculate standard deviation
		for (LinkedHashMap<Id<TransitStopFacility>, Double> endStop2TripsMap : startStop2EndStop2WeightMap.values()) {
			for (Double trips : endStop2TripsMap.values()) {
				stats.handleNewEntry(trips);
			}
		}
		
		if (stats.getNumberOfEntries() == 1) {
			// We use circular routes. There is always a way back (with no demand). Add a second entry.
			stats.handleNewEntry(0.0);			
		}
		
		double sigmaTreshold = stats.getStdDev() * this.sigmaScale;
		Set<Id<TransitStopFacility>> stopIdsAboveTreshold = new TreeSet<>();
		
		// Get all stops serving a demand above threshold
		for (Entry<Id<TransitStopFacility>, LinkedHashMap<Id<TransitStopFacility>, Double>> endStop2TripsMapEntry : startStop2EndStop2WeightMap.entrySet()) {
			for (Entry<Id<TransitStopFacility>, Double> tripEntry : endStop2TripsMapEntry.getValue().entrySet()) {
				if (tripEntry.getValue() > sigmaTreshold) {
					// ok - add the corresponding stops to the set
					stopIdsAboveTreshold.add(endStop2TripsMapEntry.getKey());
					stopIdsAboveTreshold.add(tripEntry.getKey());
				}
			}
		}
		
		// Get new stops to be served
		for (TransitRouteStop stop : routeToOptimize.getStops()) {
			if (stopIdsAboveTreshold.contains(stop.getStopFacility().getId())) {
				tempStopsToBeServed.add(stop.getStopFacility());
			}
		}
		
		ArrayList<TransitStopFacility> stopsToBeServed = new ArrayList<>();
		
		// avoid using a stop twice in a row
		for (TransitStopFacility stop : tempStopsToBeServed) {
			if (stopsToBeServed.size() > 0) {
				if (stopsToBeServed.get(stopsToBeServed.size() - 1).getId() != stop.getId()) {
					stopsToBeServed.add(stop);
				}
			} else {
				stopsToBeServed.add(stop);
			}			
		}
		
		// delete last stop, if it is the same as the first one
		if (stopsToBeServed.size() > 1) {
			if (stopsToBeServed.get(0).getId() == stopsToBeServed.get(stopsToBeServed.size() - 1).getId()) {
				stopsToBeServed.remove(stopsToBeServed.size() - 1);
			}
		}
		
		return stopsToBeServed;
	}
	
	private Id<TransitRoute> reverseRouteId(Id<TransitRoute> routeId)	{
		String[] routeIdSplit = routeId.toString().split("-");
		String reversedRoute = "";
		for(int i = 0; i < routeIdSplit.length - 1; i++)	{
			reversedRoute += routeIdSplit[i] + "-";
		}
		reversedRoute += "Back";
		
		Id<TransitRoute> reversedRouteId = Id.create(reversedRoute, TransitRoute.class);
		
		return reversedRouteId;
	}
	
	private Id<TransitStopFacility> reverseStopId(Id<TransitStopFacility> stopId)	{
		String[] stopIdSplit = stopId.toString().split("_");
		String reversedStop = "";
		for(int i = 0; i < stopIdSplit.length - 1; i++)	{
			reversedStop += stopIdSplit[i] + "_";
		}
		if(stopIdSplit[stopIdSplit.length - 1].equals("A"))
			reversedStop += "B";
		else
			reversedStop += "A";
		
		Id<TransitStopFacility> reversedStopId = Id.create(reversedStop, TransitStopFacility.class);
		
		return reversedStopId;
	}

	@Override
	public String getStrategyName() {
		return ReduceStopsToBeServedRFareBF.STRATEGY_NAME;
	}
	
	@Override
	public void reset() {
		this.route2StartStop2EndStop2WeightMap = new LinkedHashMap<>();
	}

	@Override
	public void handleFareContainer(StageContainer stageContainer) {
		// (manserpa) modified the weight map a bit such that it accumulates the trips from the back and the forth route
		String[] routeIdSplit = stageContainer.getRouteId().toString().split("-");
		String routeTyp = routeIdSplit[routeIdSplit.length - 1];
		
		Id<TransitRoute> routeId = null;
		Id<TransitStopFacility> startStopId = null;
		Id<TransitStopFacility> endStopId = null;

		if(routeTyp.equals("Back"))	{
			routeId = stageContainer.getRouteId();
			startStopId = stageContainer.getStopEntered();
			endStopId = stageContainer.getStopLeft();
		}
		else if(routeTyp.equals("Forth")) {
			routeId = reverseRouteId(stageContainer.getRouteId());
			endStopId = reverseStopId(stageContainer.getStopEntered());
			startStopId = reverseStopId(stageContainer.getStopLeft());
		}
		
		if (this.route2StartStop2EndStop2WeightMap.get(routeId) == null) {
			this.route2StartStop2EndStop2WeightMap.put(routeId, new LinkedHashMap<Id<TransitStopFacility>, LinkedHashMap<Id<TransitStopFacility>, Double>>());
		}

		if (this.route2StartStop2EndStop2WeightMap.get(routeId).get(startStopId) == null) {
			this.route2StartStop2EndStop2WeightMap.get(routeId).put(startStopId, new LinkedHashMap<Id<TransitStopFacility>, Double>());
		}

		if (this.route2StartStop2EndStop2WeightMap.get(routeId).get(startStopId).get(endStopId) == null) {
			this.route2StartStop2EndStop2WeightMap.get(routeId).get(startStopId).put(endStopId, 0.0);
		}

		double oldWeight = this.route2StartStop2EndStop2WeightMap.get(routeId).get(startStopId).get(endStopId);
		double additionalWeight = 1.0;
		
		if (this.useFareAsWeight) {
			additionalWeight = this.ticketMachine.getFare(stageContainer);
		}
		this.route2StartStop2EndStop2WeightMap.get(routeId).get(startStopId).put(endStopId, oldWeight + additionalWeight);
	}

	public void setTicketMachine(TicketMachineI ticketMachine) {
		this.ticketMachine = ticketMachine;
	}
}