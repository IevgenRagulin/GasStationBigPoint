package net.bigpoint.assessment.gasstation.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.bigpoint.assessment.gasstation.GasPump;
import net.bigpoint.assessment.gasstation.GasStation;
import net.bigpoint.assessment.gasstation.GasType;
import net.bigpoint.assessment.gasstation.exceptions.AmountInLetersCannotBeLesThanZero;
import net.bigpoint.assessment.gasstation.exceptions.GasTooExpensiveException;
import net.bigpoint.assessment.gasstation.exceptions.NotEnoughGasException;
import net.bigpoint.assessment.gasstation.exceptions.PriceCannotBeLessThanZero;
import net.bigpoint.assessment.gasstation.exceptions.PriceNotSetException;

public class GasStationImpl implements GasStation {
	Collection<GasPump> gasPumpsOnThisStation = Collections
			.synchronizedList(new ArrayList<GasPump>());
	private AtomicInteger numberOfSuccessfulSales = new AtomicInteger(0);
	private AtomicInteger numberOfCancellationsNoGas = new AtomicInteger(0);
	private AtomicInteger numberOfCancellationsTooExpensive = new AtomicInteger(
			0);
	private double totalRevenue = 0.0;
	private Map<GasType, Double> gasPrices = Collections
			.synchronizedMap(new HashMap<GasType, Double>());

	public void addGasPump(GasPump pump) {
		synchronized (gasPumpsOnThisStation) {
			gasPumpsOnThisStation.add(pump);
		}
	}

	/*
	 * Makes a deep copy of Collection<GasPump>
	 * 
	 * @param gasPumpsToCopy a collection of gas pumps which we would like to
	 * copy
	 */
	private Collection<GasPump> getCopyOfGasPumps(
			Collection<GasPump> gasPumpsToCopy) {
		Iterator<GasPump> gasPumpsIterator = gasPumpsToCopy.iterator();
		Collection<GasPump> copyGasPumps = new ArrayList<GasPump>();

		while (gasPumpsIterator.hasNext()) {
			GasPump currentGasPump = gasPumpsIterator.next();
			GasPump copiedGasPump = new GasPump(currentGasPump.getGasType(),
					currentGasPump.getRemainingAmount());
			copyGasPumps.add(copiedGasPump);
		}
		return copyGasPumps;
	}

	public Collection<GasPump> getGasPumps() {
		synchronized (gasPumpsOnThisStation) {
			return getCopyOfGasPumps(gasPumpsOnThisStation);
		}
	}

	/*
	 * Returns a pump on this gas station which has enough gas of the specified
	 * type. Returns null if such pump doesn't exist.
	 * 
	 * @param type type of gas which the buyer wants to buy
	 * 
	 * @param amountInLiters amount of gas which the buyer wants to buy
	 * 
	 * @return GasPump on this GasStation which is able to provide this type of
	 * gas. Returns null if there is no such pump
	 */
	private synchronized GasPump getGasPumpWhichIsAbleToProvideThisTypeOfGas(
			GasType type, double amountInLiters) {
		GasPump gasPump = null;
		Iterator<GasPump> iterator = this.gasPumpsOnThisStation.iterator();
		while (iterator.hasNext()) {
			GasPump currentGasPump = iterator.next();
			if ((currentGasPump.getGasType() == type)
					&& (currentGasPump.getRemainingAmount() >= amountInLiters)) {
				gasPump = currentGasPump;
			}
		}
		return gasPump;
	}

	private void incrementNumberOfCancelationsTooExpensive() {
		numberOfCancellationsTooExpensive.incrementAndGet();
	}

	private void incrementNumberOfCancelationsNoGas() {
		numberOfCancellationsNoGas.incrementAndGet();
	}

	private void incrementNumberOfSuccessfulSales() {
		numberOfSuccessfulSales.incrementAndGet();
	}

	private synchronized void incrementTotalRevenueBy(double value) {
		totalRevenue += value;
	}

	public double buyGas(GasType type, double amountInLiters,
			double maxPricePerLiter) throws NotEnoughGasException,
			GasTooExpensiveException {
		if (maxPricePerLiter < 0)
			throw new PriceCannotBeLessThanZero();

		if (amountInLiters < 0)
			throw new AmountInLetersCannotBeLesThanZero();

		double maxExpectedPrice = maxPricePerLiter * amountInLiters;
		double actualPrice = getPrice(type) * amountInLiters;
		if (actualPrice > maxExpectedPrice) {
			incrementNumberOfCancelationsTooExpensive();
			throw new GasTooExpensiveException();
		}
		synchronized (gasPumpsOnThisStation) {
			GasPump gasPumpWithEnoughGas = getGasPumpWhichIsAbleToProvideThisTypeOfGas(
					type, amountInLiters);
			if (gasPumpWithEnoughGas == null) {
				incrementNumberOfCancelationsNoGas();
				throw new NotEnoughGasException();
			}
			gasPumpWithEnoughGas.pumpGas(amountInLiters);
			incrementNumberOfSuccessfulSales();
			incrementTotalRevenueBy(actualPrice);
		}

		return actualPrice;
	}

	public synchronized double getRevenue() {
		return totalRevenue;
	}

	public synchronized int getNumberOfSales() {
		return numberOfSuccessfulSales.intValue();
	}

	public synchronized int getNumberOfCancellationsNoGas() {
		return numberOfCancellationsNoGas.intValue();
	}

	public int getNumberOfCancellationsTooExpensive() {
		return numberOfCancellationsTooExpensive.intValue();
	}

	public synchronized double getPrice(GasType type)
			throws PriceNotSetException {
		Double price = gasPrices.get(type);
		if (price == null)
			throw new PriceNotSetException();
		else
			return price;

	}

	public synchronized void setPrice(GasType type, double price)
			throws PriceCannotBeLessThanZero, PriceNotSetException {
		if (price <= 0)
			throw new PriceCannotBeLessThanZero();
		gasPrices.put(type, price);
	}

}
