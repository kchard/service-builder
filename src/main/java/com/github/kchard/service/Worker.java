package com.github.kchard.service;

public interface Worker extends Runnable {

	void cancel();
}
