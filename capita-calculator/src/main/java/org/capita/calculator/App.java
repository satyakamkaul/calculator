package org.capita.calculator;

import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		int numberOfTestCases = Integer.parseInt(sc.nextLine());// scan number of test cases
		if(numberOfTestCases>100 || numberOfTestCases<1) {
			System.out.println("Invalid Input");
			System.exit(1);
		}
		String[] testCaseString = new String[numberOfTestCases];
		int counter = 0;
		// scan all expressions for the given number of test cases
		while (numberOfTestCases!=0) {
			testCaseString[counter] = sc.nextLine();
			counter++;
			numberOfTestCases--;
		}
		
		sc.close();
		counter=1;
		String response="";
		CalculatorSerice serice = new CalculatorSerice();
		//for each expression invoke the evaluation.
		for (String expr : testCaseString) {
			
			//validate the start and end of expression
			if (!expr.matches(CaclulatorConstants.RegexBeginAndEnd)) {
				response = "Invalid Expression";
			}
			else {
				//actual processing
				response = serice.processExpression(expr);
			}
			
			System.out.println("Case #"+counter+ ": "+response);
			counter++;

		}
	}
}
