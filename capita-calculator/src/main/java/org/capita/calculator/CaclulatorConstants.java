package org.capita.calculator;

import java.util.HashMap;
import java.util.Map;

public class CaclulatorConstants {
	
	public static final String INVALID_EXPRESSION = "INVALID EXPRESSION";
	
	static String RegexBeginAndEnd = "^([0-9]|\\().*.([0-9]|\\))$";

	static Map<String, Integer> OperatorPriority = new HashMap<String, Integer>();
	
	static{
		OperatorPriority.put("^", 5);
		OperatorPriority.put("*", 4);
		OperatorPriority.put("/", 3);
		OperatorPriority.put("+", 2);
		OperatorPriority.put("-", 2);
	}
	
	
	
}
