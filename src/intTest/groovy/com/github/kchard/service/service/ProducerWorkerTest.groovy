package com.github.kchard.service

import static org.junit.Assert.*

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue

import org.junit.After
import org.junit.Before
import org.junit.Test

import com.github.kchard.service.Producer;
import com.github.kchard.service.ProducerWorker;
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class ProducerWorkerTest {
	
	ProducerWorker worker
	
	AProducer producer
	BlockingQueue<String> workQueue
	ExecutorService executor
	
	@Before
	void setUp() {
		producer = new AProducer<String>()
		workQueue = new LinkedBlockingQueue<String>()
		worker = new ProducerWorker(producer, workQueue)
		executor = Executors.newSingleThreadExecutor()
	}
	
	@After
	void tearDown() {
		executor.shutdown()
	}
	
	@Test
	void testProduce() {
		
		executor.execute(worker)
		
		MultiThreadedUtil.wait(10)
		
		worker.cancel()
		
		assertTrue(producer.getCount() - workQueue.size() <= 1) // Could be off by one if count++ has executed but has not been added to queue yet
	}
	
	@Test
	void testCancel() {
		
		worker.cancel()
		executor.execute(worker)
		
		MultiThreadedUtil.wait(10)
		
		assertTrue(workQueue.size() == 0)
	}
	
	private static class AProducer implements Producer<String> {

		volatile int count = 0
		
		@Override
		public String produce() {
			count++
			return "ITEM ${count}"
		}
	}
}
