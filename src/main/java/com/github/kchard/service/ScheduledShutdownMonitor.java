package com.github.kchard.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A monitor used to schedule the shutdown of a {@link Service} after a given time has elapsed.
 * 
 * @author chardk
 *
 * @see AbstractService
 */
public class ScheduledShutdownMonitor {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private static final Long DEFAULT_INTERVAL = 1000L;
	
	private final Service service;
	private final long monitorInterval;
	private final long shutdownAfter;
	private final TimeUnit timeUnit;
	private final ScheduledExecutorService executor;
	
	private boolean startedMonitorThread = false;
	
	public ScheduledShutdownMonitor(final AbstractService service, final long shutdownAfter) {
		this(service, DEFAULT_INTERVAL, shutdownAfter, TimeUnit.SECONDS);
	}
	
	public ScheduledShutdownMonitor(final AbstractService service, final long monitorInterval, final long shutdownAfter) {
		this(service, monitorInterval, shutdownAfter, TimeUnit.SECONDS);
	}
	
	public ScheduledShutdownMonitor(final AbstractService service, final long monitorInterval, final long shutdownAfter, final TimeUnit timeUnit) {
		this.service = service;
		this.monitorInterval = monitorInterval;
		this.shutdownAfter = shutdownAfter;
		this.timeUnit = timeUnit;
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

		private boolean startedShutdownThread = false;
		
		@Override
		public void run() {
			
			//Once the service has started schedule shutdown of the service.
			if (State.STARTED.equals(service.currentState()) && !startedShutdownThread) {
				executor.schedule(new ShutdownRunnable(), shutdownAfter, timeUnit);
				startedShutdownThread = true;
			}
			
			//If the service is shutdown or has an exception, shutdown the ExecutorService
			if(service.currentState().compareTo(State.STARTED) > 0 ) {
				executor.shutdown();
			}
		}
	}
	
	private class ShutdownRunnable implements Runnable {

		@Override
		public void run() {
			try {
				logger.debug("Shutting down monitored service: " + service.name());
				service.shutdown();
			} finally {
				executor.shutdown();
			}
		}
	}
}
