package com.taobao.top.push;

import static org.junit.Assert.*;

import org.junit.Test;

public class DeliveryRaterTest {
	@Test
	public void rate_test() throws InterruptedException {
		DeliveryRater rater = new DeliveryRater();
		rater.setDeliveryRatePeriodMillis(10);
		assertEquals(1F, rater.getDeliveryRate(), 0.01);

		Thread.sleep(100);
		rater.increaseDeliveryNubmer(1);
		assertEquals(1F, rater.getDeliveryRate(), 0.01);

		Thread.sleep(100);
		rater.increaseSendCount(2);
		rater.increaseDeliveryNubmer(1);
		assertEquals(0.5, rater.getDeliveryRate(), 0.01);

		Thread.sleep(100);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(8);
		assertEquals(0.8, rater.getDeliveryRate(), 0.01);

		Thread.sleep(100);
		rater.increaseSendCount(10);
		rater.increaseDeliveryNubmer(10);
		assertEquals(1.0, rater.getDeliveryRate(), 0.01);
	}
}
