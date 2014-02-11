package com.github.kchard.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Executes a sequential series of {@link Runnable}s after an initial delay and then again after each delay period. 
 * The timer begins when the {@link #start()} method is called.
 * 
 * <p>If any of the commands throw an exception, the service is shutdown. This can be used to let the service know there is no more work to do.</p>
 * 
 * @author chardk
 *
 * @see Service
 * @see AbstractService
 */
public class FixedRateScheduledService extends AbstractService {

	private long initialDelay;
	private final long delay;
	private final TimeUnit unit;
	private final List<Runnable> commands;
	
	private ScheduledExecutorService executor;
	
	/**
	 * 
	 * @param commands The commands to execute
	 * @param initialDelay The delay after {@link #start()} is called before the commands will execute the first time
	 * @param delay The delay after the initial execution before subsequent execution
	 * @param unit The unit of time the delay is specified in
	 */
	public FixedRateScheduledService(final List<Runnable> commands, final long initialDelay, final long delay, final TimeUnit unit) {
		this.commands = Collections.unmodifiableList(commands);
		this.initialDelay = initialDelay;
		this.delay = delay;
		this.unit = unit;
	}

    @Override
    protected final boolean requiresPendingStatus() {
        return true;
    }
	
	@Override
	protected final void doInitialize() {
		executor = Executors.newScheduledThreadPool(2);
	}

	@Override
	protected final void doStart() {
		executor.scheduleAtFixedRate(new ExecuteCommands(), initialDelay, delay, unit);
	}

	@Override
	protected final void doShutdown() {
		executor.shutdown();
		
	}
	
	private class ExecuteCommands implements Runnable {

        private volatile boolean started = false;

		@Override
		public void run() {

            if(!started) {
                pendingServiceStarted();
                started = true;
            }

			for(Runnable command : commands) {
				logger.trace("Running: " + command.getClass().getName());
				try {
					command.run();
				} catch(RuntimeException e) {
					logger.error("Shutting down executor: " + e);
                    reportException(e);
				}
			}
		}
	}
}
