/* *********************************************************************** *
 * project: org.matsim.*
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

package org.matsim.contrib.minibus.fare;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.matsim.contrib.minibus.PConfigGroup;
import org.matsim.contrib.minibus.PConfigGroup.PVehicleSettings;

/**
 * Calculates the fare for a given {@link StageContainer}.
 * 
 * @author aneumann
 *
 */
public final class TicketMachineDefaultImpl implements TicketMachineI {
	
	//private final double earningsPerBoardingPassenger;
	//private final double earningsPerMeterAndPassenger;
	private final double subsidiesPerBoardingPassenger;
	private final Collection<PVehicleSettings> pVehicleSettings;
	private String subsidizedStopsFile;
	
	@Inject public TicketMachineDefaultImpl(PConfigGroup pConfig ) {
		this.pVehicleSettings = pConfig.getPVehicleSettings();
		this.subsidizedStopsFile = pConfig.getSubsidizedStops();
		this.subsidiesPerBoardingPassenger = pConfig.getSubsidiesPerBoardingPassenger();
		//this.earningsPerBoardingPassenger = pConfig.getEarningsPerBoardingPassenger() ;
		//this.earningsPerMeterAndPassenger = pConfig.getEarningsPerKilometerAndPassenger()/1000. ;
	}
	
	@Override
	public double getFare(StageContainer stageContainer) {
		
		double earningsPerBoardingPassenger = 0.0;
		double earningsPerMeterAndPassenger = 0.0;
	
		for (PVehicleSettings pVS : this.pVehicleSettings) {
            if (stageContainer.getVehicleId().toString().contains(pVS.getPVehicleName())) {
            	earningsPerBoardingPassenger = pVS.getEarningsPerBoardingPassenger();
            	earningsPerMeterAndPassenger = pVS.getEarningsPerKilometerAndPassenger() / 1000.;
            }
        }
		
		List<String> subsidizedStops = new ArrayList<>();
        String line = "";
        
        if(this.subsidizedStopsFile != null)	{
	        try (BufferedReader br = new BufferedReader(new FileReader(this.subsidizedStopsFile))) {
	
	            while ((line = br.readLine()) != null) {
	                subsidizedStops.add(line);
	            }
	
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
		
        
		if (subsidizedStops.contains(stageContainer.getStopEntered().toString()))	{
			return earningsPerBoardingPassenger + earningsPerMeterAndPassenger * stageContainer.getDistanceTravelledInMeter() + 
					this.subsidiesPerBoardingPassenger;
		}
		else {
			return earningsPerBoardingPassenger + earningsPerMeterAndPassenger * stageContainer.getDistanceTravelledInMeter();
		}
	}
	
	@Override
	public double getPassengerDistanceKilometer(StageContainer stageContainer) {
		return stageContainer.getDistanceTravelledInMeter() / 1000;
	}
}
