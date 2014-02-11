package com.github.kchard.util.itestUtils

class MultiThreadedUtil {

	static void wait(timeout) {
		waitUntil({false}, timeout, false)
	}
	
	static void waitUntil(Closure expression, long timeout, boolean throwExceptionOnTimeout=true) {
		
		long start = System.currentTimeMillis()
		while(!expression.call()) {
			if(System.currentTimeMillis() - start > timeout) {
				if(throwExceptionOnTimeout) {
					throw new RuntimeException("Waited longer than ${timeout}")
				} else {
					break
				}
			}
		}
	}
}
