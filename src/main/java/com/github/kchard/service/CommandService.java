package com.github.kchard.service;

import java.util.Collections;
import java.util.List;

/**
 * Executes a sequential series of {@link Command}s during each phase of the service's lifecyle.
 * 
 * @author chardk
 *
 * @see Service
 * @see AbstractService
 */
public class CommandService extends AbstractService {

	private final List<Runnable> initializationCommands;
	private final List<Runnable> startupCommands;
	private final List<Runnable> shutdownCommands;
	
	public CommandService(final List<Runnable> initializationCommands, final List<Runnable> startupCommands, final List<Runnable> shutdownCommands) {
		this.initializationCommands = Collections.unmodifiableList(initializationCommands);
		this.startupCommands = Collections.unmodifiableList(startupCommands);
		this.shutdownCommands = Collections.unmodifiableList(shutdownCommands);
	}

	/**
	 * Executes the initialization commands sequentially.
	 */
	@Override
	protected final void doInitialize() {
		executeCommands(initializationCommands);
	}

	/**
	 * Executes the startup commands sequentially.
	 */
	@Override
	protected final void doStart() {
		executeCommands(startupCommands);
	}

	/**
	 * Executes the shutdown commands sequentially.
	 */
	@Override
	protected final void doShutdown() {
		executeCommands(shutdownCommands);
	}
	
	private void executeCommands(final List<Runnable> commands) {
		for(Runnable command : commands) {
			logger.trace("Running: " + command.getClass().getName());
			command.run();
		}
	}
}
