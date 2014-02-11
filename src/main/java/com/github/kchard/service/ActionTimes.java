package com.github.kchard.service;

public class ActionTimes {

	private final long createdTime;
	private final long intitializedTime;
	private final long startedTime;
	private final long shutdownTime;
	private final long exceptionTime;
	
	public ActionTimes() {
		this(System.currentTimeMillis(), -1, -1, -1, -1);
	}
	
	public ActionTimes(long createdTime, long inititalizedTime, long startedTime, long shutdownTime, long exceptionTime) {
		this.createdTime = createdTime;
		this.intitializedTime = inititalizedTime;
		this.startedTime = startedTime;
		this.shutdownTime = shutdownTime;
		this.exceptionTime = exceptionTime;
	}
	
	public long created() {
		return createdTime;
	}
	
	public long initialized() {
		return intitializedTime;
	}
	
	public long started() {
		return startedTime;
	}
	
	public long shutdown() {
		return shutdownTime;
	}
	
	public long exception() {
		return exceptionTime;
	}
	
	static ActionTimes initialize(ActionTimes actionTimes) {
		return new ActionTimes(actionTimes.created(), System.currentTimeMillis(), actionTimes.started(), actionTimes.shutdown(), actionTimes.exception());
	}
	
	static ActionTimes start(ActionTimes actionTimes) {
		return new ActionTimes(actionTimes.created(), actionTimes.initialized(), System.currentTimeMillis(), actionTimes.shutdown(), actionTimes.exception());
	}
	
	static ActionTimes shutdown(ActionTimes actionTimes) {
		return new ActionTimes(actionTimes.created(), actionTimes.initialized(), actionTimes.started(), System.currentTimeMillis(), actionTimes.exception());
	}
	
	static ActionTimes exception(ActionTimes actionTimes) {
		return new ActionTimes(actionTimes.created(), actionTimes.initialized(), actionTimes.started(), actionTimes.shutdown(), System.currentTimeMillis());
	}
}
