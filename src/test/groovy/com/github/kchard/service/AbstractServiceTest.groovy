package com.github.kchard.service

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class AbstractServiceTest {

	boolean throwOnInit
	boolean throwOnStart
	boolean throwOnShutdown

    boolean reportException
	
	AbstractService service
	
	@Before
	void setUp() {
		throwOnInit = false
		throwOnStart = false
		throwOnShutdown = false
        reportException = false
		
		service = new AService()
	}
	
	@Test
	void testNoExceptions() {
	
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		
		assertEquals(State.SHUTDOWN, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() != -1)
        assertTrue(service.actionTimes().started() != -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() == -1)
	}
	
	@Test
	void testExceptionOnInitWithShutdownEnabled() {
	
		service.enableShutdownOnException()
		throwOnInit = true
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.start()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.shutdown()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() == -1)
        assertTrue(service.actionTimes().started() == -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
	}
	
	@Test
	void testExceptionOnInitWithShutdownDisabled() {
	
		service.disableShutdownOnException()
		throwOnInit = true
		
		assertEquals(State.READY, service.currentState())
		
		try {
			service.initialize()
		} catch(Exception e) {}
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.start()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.shutdown()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() == -1)
        assertTrue(service.actionTimes().started() == -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
	}
	
	@Test
	void testExceptionOnStartWithShutdownEnabled() {
	
		service.enableShutdownOnException()
		throwOnStart = true
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.shutdown()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() != -1)
        assertTrue(service.actionTimes().started() == -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
	}
	
	@Test
	void testExceptionOnStartWithShutdownDisabled() {
	
		service.disableShutdownOnException()
		throwOnStart = true
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		
		assertEquals(State.INITIALIZED, service.currentState())
		
		try {
			service.start()
		} catch(Exception e) {}
		
		assertEquals(State.EXCEPTIONAL, service.currentState())
		
		service.shutdown()
		
		assertEquals(State.EXCEPTIONAL, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() != -1)
        assertTrue(service.actionTimes().started() == -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
	}
	
	@Test
	void testExceptionOnShutdown() {
	
		service.enableShutdownOnException()
		throwOnShutdown = true
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		
		assertEquals(State.STARTED, service.currentState())
		
		boolean hadException = false
		try {
			service.shutdown()
		} catch(Exception e) {
			hadException = true
		}
		
		assertTrue(hadException)
		assertEquals(State.EXCEPTIONAL, service.currentState())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() != -1)
        assertTrue(service.actionTimes().started() != -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
	}

    @Test
    void testReportException() {

        reportException = true

        assertEquals(State.READY, service.currentState())

        service.initialize()

        assertEquals(State.INITIALIZED, service.currentState())

        service.start()

        assertEquals(State.EXCEPTIONAL, service.currentState())

        assertEquals("KABOOM!!!", service.exception().getMessage())

        assertTrue(service.actionTimes().created() != -1)
        assertTrue(service.actionTimes().initialized() != -1)
        assertTrue(service.actionTimes().started() == -1)
        assertTrue(service.actionTimes().shutdown() != -1)
        assertTrue(service.actionTimes().exception() != -1)
    }


	private class AService extends AbstractService {

		@Override
		protected void doInitialize() {
			if(throwOnInit) throw new RuntimeException("BOOM!!!");
		}

		@Override
		protected void doStart() {
			if(throwOnStart) throw new RuntimeException("BOOM!!!");
            if(reportException) reportException(new RuntimeException("KABOOM!!!"))
		}

		@Override
		protected void doShutdown() {
			if(throwOnShutdown) throw new RuntimeException("BOOM!!!");
		}
	}
}
