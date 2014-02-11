package com.github.kchard.service;

public interface Consumer<T> {

	void consume(T item);
}
