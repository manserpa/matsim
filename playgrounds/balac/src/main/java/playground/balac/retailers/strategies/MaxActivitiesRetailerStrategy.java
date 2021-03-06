/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package playground.balac.retailers.strategies;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.MatsimServices;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;

import playground.balac.retailers.RetailerGA.RunRetailerGA;
import playground.balac.retailers.data.LinkRetailersImpl;
import playground.balac.retailers.models.MaxActivityModel;
import playground.balac.retailers.utils.Utils;


public class MaxActivitiesRetailerStrategy extends RetailerStrategyImpl
{
	private final static Logger log = Logger.getLogger(MaxActivitiesRetailerStrategy.class);

  public static final String CONFIG_GROUP = "Retailers";
  public static final String NAME = "maxActivitiesRetailerStrategy";
  public static final String GENERATIONS = "numberOfGenerations";
  public static final String POPULATION = "PopulationSize";
  private Map<Id<ActivityFacility>, ActivityFacilityImpl> shops;
  private Map<Id<ActivityFacility>, ActivityFacilityImpl> retailerFacilities;
  private Map<Id<ActivityFacility>, ActivityFacilityImpl> movedFacilities = new TreeMap<>();

  public MaxActivitiesRetailerStrategy(MatsimServices controler)
  {
    super(controler);
  }

  @Override
	public Map<Id<ActivityFacility>, ActivityFacilityImpl> moveFacilities(Map<Id<ActivityFacility>, ActivityFacilityImpl> retailerFacilities, TreeMap<Id<Link>, LinkRetailersImpl> freeLinks)
  {
    this.retailerFacilities = retailerFacilities;
    MaxActivityModel mam = new MaxActivityModel(this.controler, retailerFacilities);
    TreeMap<Integer, String> first = createInitialLocationsForGA(mergeLinks(freeLinks, retailerFacilities));
    log.info("first = " + first);
    mam.init(first);
    this.shops = mam.getScenarioShops();
    Integer populationSize = Integer.valueOf(Integer.parseInt(this.controler.getConfig().findParam("Retailers", "PopulationSize")));
    if (populationSize == null) log.warn("In config file, param = PopulationSize in module = Retailers not defined, the value '10' will be used as default for this parameter");
    Integer numberGenerations = Integer.valueOf(Integer.parseInt(this.controler.getConfig().findParam("Retailers", "numberOfGenerations")));
    if (numberGenerations == null) log.warn("In config file, param = numberOfGenerations in module = Retailers not defined, the value '100' will be used as default for this parameter");
    RunRetailerGA rrGA = new RunRetailerGA(populationSize, numberGenerations);
    ArrayList<Integer> solution = rrGA.runGA(mam);
    int count = 0;
    for (ActivityFacilityImpl af : this.retailerFacilities.values())
    {
      if (first.get(solution.get(count)) != af.getLinkId().toString()) {
          Utils.moveFacility(af, this.controler.getScenario().getNetwork().getLinks().get(Id.create(first.get(solution.get(count)), Link.class)));
        log.info("The facility " + af.getId() + " has been moved");
        this.movedFacilities.put(af.getId(), af);
        log.info("Link Id after = " + af.getLinkId());

        log.info("Count= " + count);
      }
      ++count;
    }
    return this.movedFacilities;
  }
}
