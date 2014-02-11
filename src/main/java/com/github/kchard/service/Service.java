package com.github.kchard.service;

/**
 * A Service defines a process which proceeds through several distinct {@link State}s 
 *
 * 
 * @author chardk
 * 
 * @see AbstractService
 */
public interface Service {

	/**
	 * @return The id of the Service
	 */
	String id();
	
	/**
	 * @return The name of the Service
	 */
	String name();
	
	/**
	 * @return The current {@link State} of this Service
	 */
	State currentState();
	
	/**
	 * 
	 * @return The Exception that caused the Service to SHUTDOWN or null
	 */
	Exception exception();
	
	/**
	 * 
	 * @return The current (@link ActionTimes} of this Service
	 */
	ActionTimes actionTimes();
	
	/**
	 * Execute logic used to initialize the service
	 */
	void initialize();
	
	/**
	 * Execute logic used to start the service
	 */
	void start();
	
	/**
	 * Execute logic used to shutdown the service
	 */
	void shutdown();
}
