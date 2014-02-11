package com.github.kchard.service

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.github.kchard.service.AbstractService
import com.github.kchard.service.CompositeService
import com.github.kchard.service.Service
import com.github.kchard.service.State
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class CompositeServiceTest {
	
	def services
	def executionOrderList
	
	Service service
	
	@Before
	void setUp() {
		services = []
		executionOrderList = []
	}
	
	@Test
	void testSingleService() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child.currentState())
		
		service.start()
        MultiThreadedUtil.wait(250)

        assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child.currentState())
		
		assertEquals(['init1', 'start1', 'shut1'], executionOrderList)
	}
	
	@Test
	void testMultipleServices() {
		
		def child1 = new AService(1)
		def child2 = new AService(2)
		services.add(child1)
		services.add(child2)
		
		service = new CompositeService(services)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child1.currentState())
		assertEquals(State.READY, child2.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child1.currentState())
		assertEquals(State.INITIALIZED, child2.currentState())
		
		service.start()

        MultiThreadedUtil.wait(250)

		assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child1.currentState())
		assertEquals(State.STARTED, child2.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child1.currentState())
		assertEquals(State.SHUTDOWN, child2.currentState())
		
		assertEquals(['init1', 'init2', 'start1', 'start2', 'shut2', 'shut1'], executionOrderList)
	}
	
	@Test
	void testMultipleServicesWithNonReverseShutdown() {
		
		def child1 = new AService(1)
		def child2 = new AService(2)
		services.add(child1)
		services.add(child2)
		
		service = new CompositeService(services, false)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child1.currentState())
		assertEquals(State.READY, child2.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child1.currentState())
		assertEquals(State.INITIALIZED, child2.currentState())
		
		service.start()
        MultiThreadedUtil.wait(250)

		assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child1.currentState())
		assertEquals(State.STARTED, child2.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child1.currentState())
		assertEquals(State.SHUTDOWN, child2.currentState())
		
		assertEquals(['init1', 'init2', 'start1', 'start2', 'shut1', 'shut2'], executionOrderList)
	}
	
	@Test
	void testInitializeTwice() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child.currentState())
		
		service.initialize()
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child.currentState())

		service.start()
        MultiThreadedUtil.wait(250)

		assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child.currentState())
		
		assertEquals(['init1', 'start1', 'shut1'], executionOrderList)
	}
	
	@Test
	void testStartTwice() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child.currentState())
		
		service.start()
		service.start()

        MultiThreadedUtil.wait(250)

		assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child.currentState())
		
		assertEquals(['init1', 'start1', 'shut1'], executionOrderList)
	}
	
	@Test
	void testShutdownTwice() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		assertEquals(State.INITIALIZED, child.currentState())
		
		service.start()
        MultiThreadedUtil.wait(250)

		assertEquals(State.STARTED, service.currentState())
		assertEquals(State.STARTED, child.currentState())
		
		service.shutdown()
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child.currentState())
		
		assertEquals(['init1', 'start1', 'shut1'], executionOrderList)
	}
	
	@Test
	void testStartBeforeInitialize() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		service.start()
		
		assertEquals(State.READY, service.currentState())
		assertEquals(State.READY, child.currentState())
		assertEquals([], executionOrderList)
	}
	
	@Test
	void testShutdownBeforeStart() {
		
		def child = new AService(1)
		services.add(child)
		
		service = new CompositeService(services)
		service.shutdown()
		
		assertEquals(State.SHUTDOWN, service.currentState())
		assertEquals(State.SHUTDOWN, child.currentState())
		assertEquals(['shut1'], executionOrderList)
	}
	
	
	
	private class AService extends AbstractService {

		private int order;
		
		public AService(int order) {
			this.order = order;
		}
		
		@Override
		protected void doInitialize() {
			executionOrderList.add("init${order}".toString())
		}

		@Override
		protected void doStart() {
			executionOrderList.add("start${order}".toString())
            pendingServiceStarted()
		}

		@Override
		protected void doShutdown() {
			executionOrderList.add("shut${order}".toString())
		}
	}
}
