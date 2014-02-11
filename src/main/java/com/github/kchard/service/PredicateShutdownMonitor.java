package com.github.kchard.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A monitor used to shutdown a {@link Service} when a given predicate is true.
 * 
 * @author chardk
 *
 * @see AbstractService
 */
public class PredicateShutdownMonitor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private static final Long DEFAULT_INTERVAL = 1000L;
	
	private final Service service;
	private final long monitorInterval;
	private final Predicate predicate;
	private final ScheduledExecutorService executor;
	
	private boolean startedMonitorThread = false;
	
	public PredicateShutdownMonitor(final AbstractService service, Predicate predicate) {
		this(service, DEFAULT_INTERVAL, predicate);
	}
	
	public PredicateShutdownMonitor(final AbstractService service, final long monitorInterval, Predicate predicate) {
		this.service = service;
		this.monitorInterval = monitorInterval;
		this.predicate = predicate;
		this.executor = Executors.newSingleThreadScheduledExecutor();
	}
	
	public void start() { 
		if(!startedMonitorThread) {
			logger.debug("Starting monitor for: " + service.name());
			executor.scheduleWithFixedDelay(new MonitorRunnable(), monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
			startedMonitorThread = true;
		}
	}
	
	private class MonitorRunnable implements Runnable {

		@Override
		public void run() {
			
			if(predicate.evaluate()) {
				service.shutdown();
			}
			
			//If the service is shutdown or has an exception, shutdown the ExecutorService
			if(service.currentState().compareTo(State.STARTED) > 0 ) {
				executor.shutdown();
			}
		}
	}
}
