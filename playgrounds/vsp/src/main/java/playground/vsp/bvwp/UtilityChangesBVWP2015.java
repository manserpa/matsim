/* *********************************************************************** *
 * project: org.matsim.*
 * UtilityChangesBVWP2003.java
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
package playground.vsp.bvwp;

import playground.vsp.bvwp.MultiDimensionalArray.Attribute;



/**
 * @author Ihab
 *
 */
class UtilityChangesBVWP2015 extends UtilityChanges {


	@Override
	UtlChangesData utlChangePerEntry(Attribute attribute,
			double deltaAmount, double quantityNullfall, double quantityPlanfall, double margUtl) {

		UtlChangesData utlChanges = new UtlChangesData() ;

		if ( attribute.equals(Attribute.Nutzerkosten_Eu) ) {
			// (Nutzerpreis hat keine Auswirkungen auf Resourcenverzehr!)
			utlChanges.utl = 0. ;
		} else {
			if ( deltaAmount > 0 ) {
				// wir sind aufnehmend; es zaehlt der Planfall:
				utlChanges.utl = quantityPlanfall * margUtl ;
			} else {
				utlChanges.utl = -quantityNullfall * margUtl ;
			}
		}

		return utlChanges;
	}

	@Override
	double computeImplicitUtilityPerItem(Attributes econValues, Attributes quantitiesNullfall, Attributes quantitiesPlanfall) {
		double sum = 0. ;
		for ( Attribute attribute : Attribute.values() ) {
			if ( attribute != Attribute.XX && attribute != Attribute.Produktionskosten_Eu ) {
				final double quantityPlanfall = quantitiesPlanfall.getByEntry(attribute);
				final double quantityNullfall = quantitiesNullfall.getByEntry(attribute);
				final double margUtl = econValues.getByEntry(attribute) ;

				sum += - margUtl * (quantityPlanfall+quantityNullfall)/2. ;
			}
		}
		return sum ;
	}

}
