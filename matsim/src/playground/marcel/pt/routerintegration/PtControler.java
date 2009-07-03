/* *********************************************************************** *
 * project: org.matsim.*
 * PtControler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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

package playground.marcel.pt.routerintegration;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.population.algorithms.PlanAlgorithm;
import org.xml.sax.SAXException;

import playground.marcel.pt.transitSchedule.TransitScheduleBuilderImpl;
import playground.marcel.pt.transitSchedule.TransitScheduleReaderV1;
import playground.marcel.pt.transitSchedule.api.TransitSchedule;
import playground.marcel.pt.transitSchedule.api.TransitScheduleBuilder;

public class PtControler extends Controler {

	private final TransitSchedule schedule;
	
	public PtControler(final String configFileName) {
		super(configFileName);
		
		TransitScheduleBuilder builder = new TransitScheduleBuilderImpl();
		this.schedule = builder.createTransitSchedule();
		try {
			new TransitScheduleReaderV1(this.schedule, this.network).readFile(this.config.getParam("transit", "inputSchedule"));
		} catch (SAXException e) {
			throw new RuntimeException("could not read transit schedule.", e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("could not read transit schedule.", e);
		} catch (IOException e) {
			throw new RuntimeException("could not read transit schedule.", e);
		}
	}

	@Override
	public PlanAlgorithm getRoutingAlgorithm(final TravelCost travelCosts, final TravelTime travelTimes) {
		return new PlansCalcPtRoute(this.config.plansCalcRoute(), this.network, travelCosts, travelTimes, this.getLeastCostPathCalculatorFactory(), this.schedule);
	}
	
}
