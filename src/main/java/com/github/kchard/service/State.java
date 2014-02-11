package com.github.kchard.service;

/**
 * <p>Defines the possible states of a {@link Service}</p>
 * 
 * <ul>
 * 	<li>READY</li>
 *  <li>INITIALIZED</li>
 *  <li>PENDING</li>
 *  <li>STARTED</li>
 *  <li>SHUTDOWN</li>
 *  <li>EXCEPTIONAL</li>
 * </ul>
 *
 * @author chardk
 *
 */
public enum State {
	READY, INITIALIZED, PENDING, STARTED, SHUTDOWN, EXCEPTIONAL;

	public boolean isReady() {
		return State.READY.equals(this);
	}

	public boolean isInitialized() {
		return State.INITIALIZED.equals(this);
	}

    public boolean isPending() {
        return State.PENDING.equals(this);
    }

	public boolean isStarted() {
		return State.STARTED.equals(this);
	}

	public boolean isShutdown() {
		return State.SHUTDOWN.equals(this);
	}

	public boolean isExceptional() {
		return State.EXCEPTIONAL.equals(this);
	}

	public State getNext() {
		if(isReady()) {
			return State.INITIALIZED;
		} else if(isInitialized()) {
			return State.STARTED;
		} else if(isPending()) {
            return State.SHUTDOWN;
        } else if(isStarted()) {
			return State.SHUTDOWN;
		}  else {
			return this;
		}
	}
}
