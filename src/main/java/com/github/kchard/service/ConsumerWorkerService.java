package com.github.kchard.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author chardk
 *
 * @param <T>
 * @see Consumer
 * @see ConsumerWorker
 */
public class ConsumerWorkerService<T> extends AbstractService {

	private static final int DEFAULT_MAX_WORKERS = 512;
	
	private final Consumer<T> consumer;
	private final BlockingQueue<T> workQueue;
	private final int maxWorkers;
	
	private ExecutorService workerExecutor;
	
	public ConsumerWorkerService(final Consumer<T> consumer, final BlockingQueue<T> workQueue) {
		this(consumer, workQueue, DEFAULT_MAX_WORKERS);
	}
	
	public ConsumerWorkerService(final Consumer<T> consumer, final BlockingQueue<T> workQueue, final int maxWorkers) {
		this.consumer = consumer;
		this.workQueue = workQueue;
		this.maxWorkers = maxWorkers;
	}

	@Override
	protected final void doInitialize() {
		workerExecutor = Executors.newFixedThreadPool(maxWorkers); 
	}

	@Override
	protected final void doStart() {
		for(int i = 0; i < maxWorkers; i++) {
			workerExecutor.execute(new ConsumerWorker<T>(consumer, workQueue));
		}
	}

	@Override
	protected final void doShutdown() {
		workerExecutor.shutdownNow();
	}
}
