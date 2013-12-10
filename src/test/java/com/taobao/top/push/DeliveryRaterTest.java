package com.taobao.top.push;

import static org.junit.Assert.*;

import org.junit.Test;

public class DeliveryRaterTest {
	@Test
	public void latest_rate_test() throws InterruptedException {
		DeliveryRater rater = new DeliveryRater();
		rater.setDeliveryRatePeriodMillis(10);
		rater.setDelta(0.11F);

		assertEquals(1F, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseDeliveryNubmer(1);
		assertEquals(1F, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseSendCount(1);
		assertEquals(0, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		assertEquals(1F, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseSendCount(2);
		rater.increaseDeliveryNubmer(1);
		assertEquals(0.5, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(8);
		assertEquals(0.8, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(9);// in delta
		assertEquals(1.0, rater.getLatestDeliveryRate(), 0.01);
		System.out.println(rater.getLatestDeliveryRate());

		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(10);
		assertEquals(1.0, rater.getLatestDeliveryRate(), 0.01);
	}

	@Test
	public void recovery_test() throws InterruptedException {
		DeliveryRater rater = new DeliveryRater();
		rater.setDeliveryRatePeriodMillis(10);
		rater.setDelta(0.11F);
		rater.setRecoveryStep(0.1F);

		assertEquals(1F, rater.calculateSmartRate(), 0.01);

		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(8);
		assertEquals(0.8F, rater.calculateSmartRate(), 0.01);
		assertEquals(0.8F, rater.calculateSmartRate(), 0.01);

		// reduce
		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(8);
		assertEquals(0.8F * 0.8F, rater.calculateSmartRate(), 0.01);
		Thread.sleep(20);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(8);
		assertEquals(0.8F * 0.8F * 0.8F, rater.calculateSmartRate(), 0.01);

		Thread.sleep(20);
		rater.increaseSendCount(10);
		assertEquals(0, rater.calculateSmartRate(), 0.01);

		// recovery by step
		for (int i = 1; i < 10; i++) {
			Thread.sleep(20);
			assertEquals(0.1F * i, rater.calculateSmartRate(), 0.01);
		}
		Thread.sleep(20);
		assertEquals(1F, rater.calculateSmartRate(), 0.01);
		Thread.sleep(20);
		assertEquals(1F, rater.calculateSmartRate(), 0.01);
	}
}
