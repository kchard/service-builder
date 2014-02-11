package com.github.kchard.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author chardk
 *
 * @param <T>
 * @see Worker
 * @see WorkerFactory
 */
public class ProducerWorkerService<T> extends AbstractService {

private static final int DEFAULT_MAX_WORKERS = 512;
	
	private final Producer<T> producer;
	private final BlockingQueue<T> workQueue;
	private final int maxWorkers;
	
	private ExecutorService workerExecutor;
	
	public ProducerWorkerService(final Producer<T> consumer, final BlockingQueue<T> workQueue) {
		this(consumer, workQueue, DEFAULT_MAX_WORKERS);
	}
	
	public ProducerWorkerService(final Producer<T> producer, final BlockingQueue<T> workQueue, final int maxWorkers) {
		this.producer = producer;
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
			workerExecutor.execute(new ProducerWorker<T>(producer, workQueue));
		}
	}

	@Override
	protected final void doShutdown() {
		workerExecutor.shutdownNow();
	}
}
