package com.github.kchard.service

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.Before
import org.junit.Test

import com.github.kchard.service.FixedDelayScheduledService;
import com.github.kchard.service.Service;
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class FixedDelayScheduledServiceTest {

	static final int DELAY = 1
	static final int MAX_WAIT = 5000
	static final TimeUnit UNIT = TimeUnit.MILLISECONDS
	
	def commands

	Service service
	
	def executionOrderList
	
	@Before
	void setUp() {
		commands = []
		executionOrderList = []
	}
	
	@Test
	void testSingleCommand() {
		commands.add(createCommand(1))
		service = new FixedDelayScheduledService(commands, DELAY, DELAY, UNIT)
		
		service.initialize()
		service.start()

        MultiThreadedUtil.wait(MAX_WAIT)

        service.shutdown()

        assertTrue(executionOrderList.size() > 0)
	}
	
	@Test
	void testMultipleCommands() {
		commands.add(createCommand(1))
		commands.add(createCommand(2))
		service = new FixedDelayScheduledService(commands, DELAY, DELAY, UNIT)
		
		service.initialize()
		service.start()

        MultiThreadedUtil.wait(MAX_WAIT)

        service.shutdown()

        assertTrue(executionOrderList.size() > 0)
	}
	
	private Runnable createCommand(int order) {
		[run: {executionOrderList.add(order)}] as Runnable
	}
}
