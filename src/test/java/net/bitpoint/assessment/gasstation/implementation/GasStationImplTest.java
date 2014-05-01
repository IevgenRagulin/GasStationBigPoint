package net.bitpoint.assessment.gasstation.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.exceptions.PriceNotSetException;
import net.bigpoint.assessment.gasstation.implementation.GasStationImpl;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class GasStationImplTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void testSetup() {

	}

	/*
	 * After adding gas pumps to gas station, number of gas pumps returned
	 * corresponds to number of gas pumps added
	 */
	@Test
	public void returnGasPumpsNumberCorrespondsToAdded() {
		GasStationImpl gasStation = new GasStationImpl();
		GasPump gasPump = new GasPump(GasType.DIESEL, 100);
		gasStation.addGasPump(gasPump);
		gasPump = new GasPump(GasType.DIESEL, 100);
		gasStation.addGasPump(gasPump);
		assertEquals(2, gasStation.getGasPumps().size());
	}

	/*
	 * Checks if modifying the resulting collection after using getGasPumps does
	 * not affect this gas station.
	 */
	@Test
	public void returnGasPumpsMustNotModifyGasStation() {
		GasStationImpl gasStation = new GasStationImpl();
		GasPump gasPump = new GasPump(GasType.DIESEL, 100);
		gasStation.addGasPump(gasPump);
		Collection<GasPump> gasPumps = gasStation.getGasPumps();
		// removes all gas pumps from this collection
		gasPumps.removeAll(gasPumps);
		Collection<GasPump> gasPumpsTwo = gasStation.getGasPumps();
		assertTrue(gasPumpsTwo.size() != 0);
	}

	/*
	 * Tests the correctness of calculation number of successfull sales
	 */
	@Test
	public void testNumberOfSuccessfullSales() throws NotEnoughGasException,
			GasTooExpensiveException {
		GasStationImpl gasStation = new GasStationImpl();
		/* After creation of the gas station number of sales should be 0 */
		assertEquals(0, gasStation.getNumberOfSales());

		GasPump dieselPump = new GasPump(GasType.DIESEL, 100);
		gasStation.addGasPump(dieselPump);
		gasStation.setPrice(GasType.DIESEL, 1);
		gasStation.buyGas(GasType.DIESEL, 1, 1);
		/* After successful buying of gas number of successful sales should be 1 */
		assertEquals(1, gasStation.getNumberOfSales());

		try {
			gasStation.buyGas(GasType.DIESEL, 1000.0, 2);
		} catch (NotEnoughGasException e) {
		}
		/*
		 * After unsuccessful buying of gas number of successful sales should
		 * still be 1
		 */
		assertEquals(1, gasStation.getNumberOfSales());
	}

	/*
	 * Test the correctness of calculation of cancelations
	 */
	@Test
	public void testNumberOfCancelations() throws NotEnoughGasException,
			GasTooExpensiveException {
		GasStationImpl gasStation = new GasStationImpl();
		/* After creation of the gas station number of cancelations should be 0 */
		assertEquals(0, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());

		GasPump regularPump = new GasPump(GasType.REGULAR, 100);
		gasStation.addGasPump(regularPump);
		gasStation.setPrice(GasType.REGULAR, 10);
		try {
			gasStation.buyGas(GasType.REGULAR, 1, 1);
		} catch (GasTooExpensiveException e) {
		}
		/*
		 * After trying to buy to expensive gas
		 * numberOfcancelationsTooExpensive==1
		 */
		assertEquals(1, gasStation.getNumberOfCancellationsTooExpensive());

		/*
		 * After trying to buy gas which is not available number of
		 * cancellations no gus==1
		 */
		gasStation.setPrice(GasType.SUPER, 10);
		try {
			gasStation.buyGas(GasType.SUPER, 1, 10);
		} catch (NotEnoughGasException e) {
		}
		assertEquals(1, gasStation.getNumberOfCancellationsNoGas());
	}

	/*
	 * Tests if gas is pumped as expected (amount of remaining gas decreases)
	 * after somebody buys it, and if NotEnoughGasException is thrown when we
	 * don't have enough gas left
	 */
	@Test
	public void testIfGasIsPumped() throws NotEnoughGasException,
			GasTooExpensiveException {
		GasStationImpl gasStation = new GasStationImpl();
		/* After creation of the gas station number of cancelations should be 0 */
		assertEquals(0, gasStation.getNumberOfCancellationsNoGas());
		assertEquals(0, gasStation.getNumberOfCancellationsTooExpensive());

		GasPump superPump = new GasPump(GasType.SUPER, 100);
		gasStation.addGasPump(superPump);
		gasStation.setPrice(GasType.SUPER, 10);

		gasStation.buyGas(GasType.SUPER, 50, 20);
		gasStation.buyGas(GasType.SUPER, 50, 20);
		exception.expect(NotEnoughGasException.class);
		gasStation.buyGas(GasType.SUPER, 1, 20);
	}

	/*
	 * Tests if revenue is calculated correctly
	 */
	@Test
	public void testRevenueCalculation() throws NotEnoughGasException,
			GasTooExpensiveException {
		GasStationImpl gasStation = new GasStationImpl();
		/* After creation of the gas station revenue should be 0.0 */
		assertEquals(0.0, gasStation.getRevenue(), 0);

		/* Add pumps, set prices for the gas */
		final double DIESEL_PRICE = 11.25;
		final double SUPER_PRICE = 12.25;
		final double DIESEL_AMOUNT_BOUGHT = 20;
		final double SUPER_AMOUNT_BOUGHT = 30;
		GasPump superPump = new GasPump(GasType.SUPER, 100);
		GasPump dieselPump = new GasPump(GasType.DIESEL, 100);
		gasStation.addGasPump(superPump);
		gasStation.addGasPump(dieselPump);
		gasStation.setPrice(GasType.DIESEL, DIESEL_PRICE);
		gasStation.setPrice(GasType.SUPER, SUPER_PRICE);

		/* Calculate expected revenue */
		final double DIESEL_MAX_PRICE = 15;
		final double SUPER_MAX_PRICE = 15;
		final double DIESEL_REVENUE = DIESEL_PRICE * DIESEL_AMOUNT_BOUGHT;
		final double SUPER_REVENUE = SUPER_PRICE * SUPER_AMOUNT_BOUGHT;
		final double TOTAL_REVENUE = DIESEL_REVENUE + SUPER_REVENUE;
		gasStation.buyGas(GasType.DIESEL, DIESEL_AMOUNT_BOUGHT,
				DIESEL_MAX_PRICE);
		assertEquals(DIESEL_REVENUE, gasStation.getRevenue(), 0);
		gasStation.buyGas(GasType.SUPER, SUPER_AMOUNT_BOUGHT, SUPER_MAX_PRICE);
		assertEquals(TOTAL_REVENUE, gasStation.getRevenue(), 0);
	}

	@Test
	public void testPriceNotSetExceptionIsThrown() {
		GasStationImpl gasStation = new GasStationImpl();
		exception.expect(PriceNotSetException.class);
		gasStation.getPrice(GasType.DIESEL);
	}

}
