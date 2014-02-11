package com.github.kchard.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A CompositeService can be used to build complicated services by composing several simple services.
 * 
 * @author chardk
 *
 * @see Service
 * @see AbstractService
 */
public class CompositeService extends AbstractService {

	private final List<AbstractService> services;
	private final boolean shutdownInReverseOrder;

    private ScheduledExecutorService pendingExecutor;
    private ScheduledExecutorService exceptionExecutor;

	/**
	 * A convenience constructor that will default shutdownInReverseOrder to true
	 * 
	 * @param services The services to compose
	 */
	public CompositeService(final List<AbstractService> services) {
		this(services, true);
	}
	
	/**
	 * @param services The services to compose
	 * @param shutdownInReverseOrder Flag used to indicate whether the services should be shutdown in the reverse order they were started
	 */
	public CompositeService(final List<AbstractService> services, final boolean shutdownInReverseOrder) {
		this.services = Collections.unmodifiableList(services);
		this.shutdownInReverseOrder = shutdownInReverseOrder;
		
		this.enableShutdownOnException();
		for(AbstractService service : services) {
			service.disableShutdownOnException();
		}
	}

    @Override
    protected final boolean requiresPendingStatus() {
        return true;
    }
	
	/**
	 * Initialize all child services
	 */
	@Override
	protected final void doInitialize() {
        pendingExecutor = Executors.newSingleThreadScheduledExecutor();
        exceptionExecutor = Executors.newSingleThreadScheduledExecutor();

        for (Service service : services) {
			service.initialize();
		}
	}
	
	/**
	 * Start all child services
	 */
	@Override
	protected final void doStart() {

        for (Service service : services) {
			service.start();
		}

        pendingExecutor.scheduleWithFixedDelay(new PendingMonitor(), 0, 1000, TimeUnit.MILLISECONDS);
        exceptionExecutor.scheduleWithFixedDelay(new ExceptionMonitor(), 0, 1000, TimeUnit.MILLISECONDS);
	}

	/**
	 * Shutdown all child services
	 */
	@Override
	protected final void doShutdown() {

        if(currentState().compareTo(State.INITIALIZED) > 0) {
            pendingExecutor.shutdown();
            exceptionExecutor.shutdown();
        }

        List<Service> copy = new ArrayList<Service>(services);

		if (shutdownInReverseOrder) {
			Collections.reverse(copy);
		}

		for (Service service : copy) {
			try {
				service.shutdown();
			} catch(RuntimeException e) {
				//Just log the exception so that the remainder of the services will be shutdown
				logger.error("Exception occurred while shutting down child service: " + e.getMessage());
			}		
		}
	}

    private class PendingMonitor implements Runnable {

        @Override
        public void run() {

            boolean noPendingServices = true;
            for(Service service :services) {
                if(service.currentState() == State.PENDING) {
                    noPendingServices = false;
                    break;
                }
            }

            if(noPendingServices) {
                pendingServiceStarted();
                throw new RuntimeException("Stopping scheduled execution");
            }
        }
    }

    private class ExceptionMonitor implements Runnable {

        @Override
        public void run() {

            Exception e = null;
            for(Service service :services) {
                if(service.currentState() == State.EXCEPTIONAL) {
                    e = service.exception();
                    break;
                }
            }

            if(e != null) {
                reportException(e);
            }
        }
    }
}
