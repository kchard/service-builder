package com.github.kchard.service
import static org.junit.Assert.*

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.github.kchard.service.Consumer;
import com.github.kchard.service.ConsumerWorker;
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class ConsumerWorkerTest {
	
	static final int MAX_WAIT = 5000
	
	ConsumerWorker worker
	
	AConsumer consumer
	BlockingQueue<String> workQueue
	ExecutorService executor
	
	@Before
	void setUp() {
		consumer = new AConsumer<String>()
		workQueue = new LinkedBlockingQueue<String>()
		worker = new ConsumerWorker(consumer, workQueue)
		executor = Executors.newSingleThreadExecutor()
	}
	
	@After
	void tearDown() {
		executor.shutdown()
	}
	
	@Test
	void testOneItem() {
		
		workQueue.offer("ITEM")
		
		executor.execute(worker)
		
		MultiThreadedUtil.waitUntil( { workQueue.isEmpty() }, MAX_WAIT)
		
		assertTrue(consumer.getCount() == 1)
	}
	
	@Test
	void testTwoItems() {
		
		workQueue.offer("ITEM1")
		workQueue.offer("ITEM2")
		
		executor.execute(worker)
		
		MultiThreadedUtil.waitUntil( { workQueue.isEmpty() }, MAX_WAIT)
		
		assertTrue(consumer.getCount() == 2)
	}
	
	@Test
	void testCancel() {
		
		workQueue.offer("ITEM")
		
		worker.cancel()
		executor.execute(worker)
		
		MultiThreadedUtil.wait(100)
		
		assertTrue(consumer.getCount() == 0)
	}
	
	private static class AConsumer implements Consumer<String> {

		volatile int count = 0
		
		@Override
		public void consume(String item) {
			count++
		}
	}
}
