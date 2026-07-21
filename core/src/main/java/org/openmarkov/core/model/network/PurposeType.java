/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import java.util.ArrayList;


/**
 * Purpose types
 * @author cmyago
 * @version 1.0 - 11/03/2020 - previously thu where stored in org.openmarkov.gui.util.Purpose. Now the types or purpose are in the core and its representation in the gui. Adapted from NodeType. This version intends to make minor changes in the code
 * TODO set Purpose in the node as an instance or PurposeType
 */
public enum PurposeType {

	TERMINAL_EVENT("terminalEvent"),
	INITIAL_EVENT("initialEvent"),
	COST("cost"),
	EFFECTIVENESS("effectiveness"),
	TREATMENT("treatment"),
	RISK_FACTOR("riskfactor"),
	SYMPTOM("symptom"),
	SIGN("sign"),
	TEST("test"),
	DISEASE_ANOMALY("diseaseanomaly"),
	AUXILIARY("auxiliary"),
	OTHER("other") ;


	private String name;

	/**
	 *
	 */
	PurposeType( String name) {

		this.name = name;
	}

	public String getName(){
		return name;
	}

	public String toString() {
		return name;
	}

	public static ArrayList<String> purposeList(){
		ArrayList<String> purposes= new ArrayList<String>();
		PurposeType[] purposeArray = PurposeType.values();
		for (int i = 0; i <purposeArray.length ; i++) {
			purposes.add(purposeArray[i].name);
		}
		return purposes;
	}




}
