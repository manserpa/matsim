/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
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

package playground.michalm.telaviv;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.*;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

public class RunFlowCapEasingScenario {
	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("d:/temp/golan/configtest.xml");
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		controler.run();
	}
}
