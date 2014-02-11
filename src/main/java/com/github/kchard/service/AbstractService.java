package com.github.kchard.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>An abstract implementation of the {@link Service} interface that implements {@link State} transition logic.</p>
 * 
 * A service extending this class will have the following state transitions in the non exceptional case:
 * <ol>
 * 	<li>READY</li>
 *  <li>INITIALIZED</li>
 *  <li>STARTED</li>
 *  <li>SHUTDOWN</li>
 * </ol>
 * 
 * <p>The following constraints are placed on when a service can make each state transition:</p>
 * <ol>
 * 	<li>A service may only transition to INITIALIZED if it is READY</li>
 *  <li>A service may only transition to STARTED if it is INITIALIZED or PENDING</li>
 *  <li>A service may only transition to SHUTDOWN if it is not shutdown already</li>
 * </ol>
 * 
 * <p>If a RuntimeException occurs during any of the state transitions and shutdownOnException is enabled, 
 * the service will immediately call shutdown and terminate in the SHUTDOWN state.</p> 
 * 
 * <p>If a RuntimeException occurs during any of the state transitions and shutdownOnException is disabled, 
 * the service will enter the EXCEPTIONAL state and then rethrow the Exception. 
 * The only allowable action once a service has entered the EXCEPTIONAL state is shutdown.</p>
 * 
 * <p>This class is thread safe.</p>
 *  
 * @author chardk
 *
 * @see CommandService
 * @see ScheduledService
 * @see FixedDelayScheduledService
 * @see FixedRateScheduledService
 * @see CompositeService
 */
public abstract class AbstractService implements Service {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private final String id;
	private final String name; 
	
	private boolean shutdownOnException;
	private State state;
	private ActionTimes actionTimes;
	private Exception e;
	
	public AbstractService() {
		this(null);
	}
	
	public AbstractService(final String name) {
		id = UUID.randomUUID().toString();
		if(name == null) {
			this.name = getClass().getSimpleName() + "-" + id;
		} else {
			this.name = name;
		}
		
		shutdownOnException = true;
		state = State.READY;
		actionTimes = new ActionTimes();
	}
	
	public String id() {
		return id;
	}
	
	public String name() {
		return name;
	}
	
	/**
	 * @return The current state of the Service
	 */
	@Override
	public final synchronized State currentState() {
		return state;
	}
	
	/**
	 * @return The current state of the Service
	 */
	@Override
	public final synchronized Exception exception() {
		return e;
	}
	
	@Override
	public final synchronized ActionTimes actionTimes() {
		return actionTimes;
	}

    /**
     * Flag to instruct this service to call shutdown on itself in the event of an exception
     */
    protected final synchronized void enableShutdownOnException() {
        shutdownOnException = true;
    }

    /**
     * Flag to instruct this service to propagate and exception
     */
    protected final synchronized void disableShutdownOnException() {
        shutdownOnException = false;
    }

    /**
     * This method will set the service's state to EXCEPTIONAL and shutdown the service.
     * The purpose of this method is to allow subclasses to handle exceptions on their own and then shutdown the service with an EXCEPTIONAL state
     * @param e
     */
    protected final synchronized void reportException(Exception e) {
        this.state = State.EXCEPTIONAL;
        this.e = e;
        actionTimes = ActionTimes.exception(actionTimes);
        shutdown();
    }

    /**
     * This method is used to determine if the service should transition from INITIALIZED to STARTED directly
     * or if it should transition from INITIALIZED to PENDING. If the service indicates that it should transition
     * from INITIALIZED to PENDING, it is the services responsibility to update its state to STARTED by calling
     * pendingServiceStarted(). This functionality allows a service to indicate that it is delaying actions using
     * additional threads.
     *
     * @return
     */
    protected synchronized boolean requiresPendingStatus() {
        return false;
    }

    /**
     * This method should be called by a service that is PENDING to indicate it has STARTED
     */
    protected synchronized final void pendingServiceStarted() {
        if(State.PENDING.equals(state)) {
            state = State.STARTED;
            actionTimes = ActionTimes.start(actionTimes);
        } else {
            logger.warn("Attempting to update a service to STARTED that is not PENDING. This will have no affect.");
        }
    }

	/**
	 * Handles the transition of the service from READY to INITIALIZED and invokes the implementing classes
	 * {@link #doInitialize()} method.
	 */
	@Override
	public final synchronized void initialize() {
		logger.debug("Initializing service: " + name);
		if(State.READY.equals(state)) {
			try {
				doInitialize();
				//This check is necessary in case doInitialize calls start or shutdown
				if(State.READY.equals(state)) {
					state = State.INITIALIZED;
                    actionTimes = ActionTimes.initialize(actionTimes);
				}
			} catch(RuntimeException e) {
				actionTimes = ActionTimes.exception(actionTimes);
				state = State.EXCEPTIONAL;
				this.e = e;
				if(shutdownOnException) {
					this.shutdown();
				} else {
					throw e;
				}
			}

		} else {
			logger.warn("Attempting to initialize a service that has already been initialized. It will not be initialized again.");
		}
	}

	/**
	 * This method is invoked during the transition from READY to INITIALIZED from {@link #initialize()}
	 */
	protected abstract void doInitialize();

	/**
	 * Handles the transition of the service from INITIALIZED to STARTED and invokes the implementing classes
	 * {@link #doStart()} method.
	 */
	@Override
	public final synchronized void start() {
		logger.debug("Starting up service: " + name);
		if(State.INITIALIZED.equals(state)) {
			try {
				doStart();
				//This check is necessary in case doStart calls shutdown
				if(State.INITIALIZED.equals(state)) {
					if(requiresPendingStatus()) {
                        state = State.PENDING;
                    } else {
                        state = State.STARTED;
                        actionTimes = ActionTimes.start(actionTimes);
                    }
				}
			} catch(RuntimeException e) {
				actionTimes = ActionTimes.exception(actionTimes);
				state = State.EXCEPTIONAL;
				this.e = e;
				if(shutdownOnException) {
					this.shutdown();
				} else {
					throw e;
				}
			}
		} else {
			logger.warn("Attempting to start a service that has already been started or was never initialized. It will not be started again.");
		}
	}
	
	/**
	 * This method is invoked during the transition from INITIALIZED to STARTED from {@link #start()}
	 */
	protected abstract void doStart();

	/**
	 * Handles the transition of the service to SHUTDOWN and invokes the implementing classes 
	 * {@link #doShutdown()} method.
	 */
	@Override
	public final synchronized void shutdown() {
		logger.debug("Shutting down service: " + name);
		if (!State.SHUTDOWN.equals(state)) {
			try {
				doShutdown();
			} catch(RuntimeException e) {
                actionTimes = ActionTimes.exception(actionTimes);
                state = State.EXCEPTIONAL;
				this.e = e;
				throw e;
			} finally {
				if(!State.EXCEPTIONAL.equals(state)) {
					state = State.SHUTDOWN;
				}

                actionTimes = ActionTimes.shutdown(actionTimes);
			}
			
		} else {
			logger.warn("Attempting to stop a service that has already been stopped or was never started. It will not be stopped again.");
		}
	}
	
	/**
	 * This method is invoked during the transition from SHUTDOWN from {@link #shutdown()}
	 */
	protected abstract void doShutdown();
}
