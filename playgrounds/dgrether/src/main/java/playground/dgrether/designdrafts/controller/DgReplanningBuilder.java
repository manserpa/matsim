/* *********************************************************************** *
 * project: org.matsim.*
 * DgReplanningBuilder
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
package playground.dgrether.designdrafts.controller;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.scoring.ScoringFunction;


/**
 * @author dgrether
 *
 */
public interface DgReplanningBuilder {
	
	/**
	 * even more useful would be createReplanner(Agent a, ScoringFunction sf)
	 */
	public DgReplanner createReplanner(Person p, ScoringFunction sf);

}
