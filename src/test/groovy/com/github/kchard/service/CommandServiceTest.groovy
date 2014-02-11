package com.github.kchard.service

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class CommandServiceTest {
	
	def initializationCommands
	def startupCommands
	def shutdownCommands
	
	def executionOrderList
	
	Service service
	
	@Before
	void setUp() {
		
		initializationCommands = []
		startupCommands = []
		shutdownCommands = []
		
		executionOrderList = [] 
	}

	@Test
	void testSingleCommand() {
		
		initializationCommands.add(createCommand(1))
		startupCommands.add(createCommand(2))
		shutdownCommands.add(createCommand(3))
		
		service = new CommandService(initializationCommands, startupCommands, shutdownCommands)
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		
		assertEquals([1, 2, 3], executionOrderList)
	}
	
	@Test
	void testMultipleCommands() {
		
		initializationCommands.add(createCommand(1))
		initializationCommands.add(createCommand(2))
		startupCommands.add(createCommand(3))
		startupCommands.add(createCommand(4))
		shutdownCommands.add(createCommand(5))
		shutdownCommands.add(createCommand(6))
		
		service = new CommandService(initializationCommands, startupCommands, shutdownCommands)
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		
		assertEquals([1, 2, 3, 4, 5, 6], executionOrderList)
	}
	
	@Test
	void testInitializeTwice() {
		
		initializationCommands.add(createCommand(1))
		startupCommands.add(createCommand(2))
		shutdownCommands.add(createCommand(3))
		
		service = new CommandService(initializationCommands, startupCommands, shutdownCommands)
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		
		assertEquals([1, 2, 3], executionOrderList)
	}
	
	@Test
	void testStartTwice() {
		
		initializationCommands.add(createCommand(1))
		startupCommands.add(createCommand(2))
		shutdownCommands.add(createCommand(3))
		
		service = new CommandService(initializationCommands, startupCommands, shutdownCommands)
		
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		service.start()
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		
		assertEquals([1, 2, 3], executionOrderList)
	}
	
	@Test
	void testShutdownTwice() {
		
		initializationCommands.add(createCommand(1))
		startupCommands.add(createCommand(2))
		shutdownCommands.add(createCommand(3))
		
		service = new CommandService(initializationCommands, startupCommands, shutdownCommands)
		
		assertEquals(State.READY, service.currentState())
		
		service.initialize()
		assertEquals(State.INITIALIZED, service.currentState())
		
		service.start()
		assertEquals(State.STARTED, service.currentState())
		
		service.shutdown()
		service.shutdown()
		assertEquals(State.SHUTDOWN, service.currentState())
		
		assertEquals([1, 2, 3], executionOrderList)
	}
	
	private Runnable createCommand(int order) {
		[run: {executionOrderList.add(order)}] as Runnable
	}
}
