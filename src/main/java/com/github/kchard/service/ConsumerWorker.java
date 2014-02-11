package com.github.kchard.service;

import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author chardk
 *
 * @param <T>
 * 
 * @see WorkerFactory
 * @see WorkerService
 */
public class ConsumerWorker<T> implements Worker {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final Consumer<T> consumer;
	private final BlockingQueue<T> workQueue;
	
	private volatile boolean run = true;
	
	public ConsumerWorker(final Consumer<T> consumer, final BlockingQueue<T> workQueue) {
		this.consumer = consumer;
		this.workQueue = workQueue;
	}
	
	@Override
	public final void run() {
		while(run) {
			try {
				consumer.consume(workQueue.take());
			} catch (InterruptedException e) {
				//This handles the case when BlockingQueue.take is interrupted
				
				//Stop the worker 
				cancel();
				
				//Preserve the interrupted status of the Thread
				Thread.currentThread().interrupt();
				
			} catch (RuntimeException e) {
				//In the case of any other exception, stop the Worker.
				//If more work needs to be done, the code that started this Worker can start additional Workers at its discretion
				cancel();
				
				logger.info("Shutting down " + getClass() + " due to an Exception: " + e.getMessage());
			}
		}
	}
	
	public final void cancel() {
		run = false;
	}
}
