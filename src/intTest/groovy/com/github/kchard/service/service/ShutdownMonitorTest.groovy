package com.github.kchard.service

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.Before
import org.junit.Test

import com.github.kchard.service.AbstractService;
import com.github.kchard.service.ScheduledShutdownMonitor;
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class ShutdownMonitorTest {

	static final long MAX_WAIT = 250
	static final long MONITOR_INTERVAL = 10
	static final long SHUTDOWN_AFTER = 100
	
	AbstractService service
	boolean throwException
	
	ScheduledShutdownMonitor monitor
	
	@Before
	void setUp() {
		service = new AService()
		throwException = false
		
		monitor = new ScheduledShutdownMonitor(service, MONITOR_INTERVAL, SHUTDOWN_AFTER, TimeUnit.MILLISECONDS)
	}
	
	@Test
	void testShutdownWithMonitor() {
		
		monitor.start()
		
		service.initialize()
		service.start()
		
		MultiThreadedUtil.waitUntil({ service.currentState().isShutdown() }, MAX_WAIT)
		
		assertTrue(service.currentState().isShutdown())
		assertTrue(monitor.executor.isShutdown())
		
	}
	
	@Test
	void testShutdownWithService() {
		
		monitor.start()
		
		service.initialize()
		service.start()
		service.shutdown()
		
		MultiThreadedUtil.wait(MAX_WAIT)
		
		assertTrue(service.currentState().isShutdown())
		assertTrue(monitor.executor.isShutdown())
		
	}
	
	@Test
	void testException() {
		
		throwException = true
		
		monitor.start()
		
		service.initialize()
		
		MultiThreadedUtil.wait(MAX_WAIT)
		
		assertTrue(service.currentState().isExceptional())
		assertTrue(monitor.executor.isShutdown())
	}
	
	@Test
	void testUnhandledException() {
		
		throwException = true
		service.disableShutdownOnException()
		
		monitor.start()
		
		try {
			service.initialize()
		} catch(Exception e) {}
		
		
		MultiThreadedUtil.wait(MAX_WAIT)
		
		assertTrue(service.currentState().isExceptional())
		assertTrue(monitor.executor.isShutdown())
	}
	
	class AService extends AbstractService {

		@Override
		protected void doInitialize() {
			if(throwException) {
				throw new RuntimeException()
			}
		}

		@Override
		protected void doStart() {
		}

		@Override
		protected void doShutdown() {
		}
	}
}
