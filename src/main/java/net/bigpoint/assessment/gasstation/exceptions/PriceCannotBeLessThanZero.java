package net.bigpoint.assessment.gasstation.exceptions;

/*
 * This exception is thrown whenever the user tries to get a price for the gas type for which the price has not yet been set
 */
public class PriceCannotBeLessThanZero extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2935218479478380515L;

}
