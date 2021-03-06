/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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
package org.matsim.contrib.parking.run;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.contrib.parking.parkingchoice.run.RunParkingChoiceExample;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.misc.CRCChecksum;
import org.matsim.testcases.MatsimTestUtils;
import org.matsim.utils.eventsfilecomparison.EventsFileComparator;

/**
 * @author nagel
 *
 */
public class RunParkingChoiceExampleTest {
	@Rule public MatsimTestUtils utils = new MatsimTestUtils() ;
	
	/**
	 * Test method for {@link org.matsim.contrib.parking.parkingchoice.run.RunParkingChoiceExample#run(org.matsim.core.config.Config)}.
	 */
	@Test
	public final void testRun() {
		Config config = ConfigUtils.loadConfig("./src/main/resources/parkingchoice/config.xml");
		config.controler().setOutputDirectory( utils.getOutputDirectory() );
		config.controler().setLastIteration(0);
		RunParkingChoiceExample.run(config);
		
	}

}
