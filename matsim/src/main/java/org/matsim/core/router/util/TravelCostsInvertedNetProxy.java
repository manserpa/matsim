/* *********************************************************************** *
 * project: org.matsim.*
 * TravelCostsInvertedNetProxy
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
package org.matsim.core.router.util;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

/**
 * Proxy for a TravelCost instance to make it work with the 
 * LeastCostPathCalculator working on an inverted network.
 * 
 * @author dgrether
 *
 */
public class TravelCostsInvertedNetProxy implements TravelCost {

	private Network originalNetwork;
	private TravelCost travelCosts;

	public TravelCostsInvertedNetProxy(Network originalNetwork,
			TravelCost travelCosts) {
		this.originalNetwork = originalNetwork;
		this.travelCosts = travelCosts;
	}

	public double getLinkGeneralizedTravelCost(Link link, double time) {
		//as we have no turning move travel costs defined
		//the fromLink is sufficient to calculate travelCosts
		Link fromLink = this.originalNetwork.getLinks().get(link.getFromNode().getId());
		return this.travelCosts.getLinkGeneralizedTravelCost(fromLink, time);
	}

}
