package org.capita.calculator;

import java.text.DecimalFormat;
import java.util.Stack;

public class CalculatorSerice {
	private static Stack<Double> operandStack;
	private static Stack<String> operatorStack; 
	private static boolean isOperator; // check if no two repetitive operators are found except brackets.
	private static boolean isNumber; 
	private static String operand; 
	private static int br;


	public String processExpression(String expr) {
		flushAll();//flush all the variable used for previous expression.

		char[] arr = expr.toCharArray();

		for (int i = 0, j = 0; i < arr.length; i++) {

			String nextChar = String.valueOf(arr[i]);
			if (Character.isDigit(arr[i])) {// if digit push to the operan stack
				operand = operand + nextChar;
				isOperator = false;
				isNumber = true;
			} else { // found operator
				if (!isValid(arr[i])) { 
					return CaclulatorConstants.INVALID_EXPRESSION;
				}
				isNumber = false; // no bracket after number				
				isOperator = true; // found operator so next time it should not be operator again except brackets.
				if (operand.length() > 0) { 
					operandStack.push(Double.valueOf(operand));// push the number including having more than one digits
					operand = "";
				}

				if (arr[i] == '(') {
					br++;// keep track of opening and closing brackets
					operatorStack.push(nextChar);
				} else if (arr[i] == ')') {
					if (--br < 0) {// keep track of opening and closing brackets
						return CaclulatorConstants.INVALID_EXPRESSION;
					}
					// keep on popping and calculating till the corresponding ( is found.
					while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
						calculate(operatorStack.pop());
					}
					operatorStack.pop();// pop the (
					isOperator = false; // there can be multiple )
				} else {
					// if no brackets and the stack contains higher priority operator, keep on calculating till otherwise. push the new operator on stack
					while (!operatorStack.isEmpty() && CaclulatorConstants.OperatorPriority.containsKey(nextChar)
							&& CaclulatorConstants.OperatorPriority.containsKey(operatorStack.peek())
							&& CaclulatorConstants.OperatorPriority.get(nextChar)
									.intValue() <= CaclulatorConstants.OperatorPriority.get(operatorStack.peek())
											.intValue()) {
						calculate(operatorStack.pop());
					}
					operatorStack.push(nextChar);
				}

			}
			//push last digit in the expression to stack
			if (i == arr.length - 1 && operand.length() > 0) {
				operandStack.push(Double.valueOf(operand));
			}

		}
		//complete the remaining expression
		while (!operatorStack.empty()) {
			calculate(operatorStack.pop());
		}
		
		if (!(br == 0)) {
			return CaclulatorConstants.INVALID_EXPRESSION;
		}
		
		//pop the last operand as the final result from the stack.
		DecimalFormat df2 = new DecimalFormat(".##");
		return  df2.format(operandStack.pop()) + "";
	}

	private void flushAll() {
		operandStack = new Stack<Double>();
		operatorStack = new Stack<String>();
		isOperator = false;
		isNumber = false;
		operand = "";
		br = 0;
	}

	static boolean isValid(char c) {
		if (isOperator && c != '(' && !operatorStack.empty() && !operatorStack.peek().equals(")")) {
			return false;
		}
		if (isNumber && c == '(') {
			return false;
		}
		return true;
	}

	public void calculate(String valueOf) {
		Double calculation = Double.valueOf(0);
		switch (valueOf) {
		case "+":
			calculation = operandStack.pop() + operandStack.pop();
			operandStack.push(calculation);
			break;
		case "-":
			Double after = operandStack.pop();
			Double before = operandStack.pop();
			calculation = before - after;
			operandStack.push(calculation);
			break;
		case "*":
			calculation = operandStack.pop() * operandStack.pop();
			operandStack.push(calculation);
			break;

		case "/":
			Double denominator = operandStack.pop();
			Double numerator = operandStack.pop();
			calculation = numerator / denominator;
			operandStack.push(calculation);
			break;
		case "^":
			Double power = operandStack.pop();
			Double base = operandStack.pop();
			calculation = Math.pow(base, power);
			operandStack.push(calculation);
			break;
		}
	}
}
