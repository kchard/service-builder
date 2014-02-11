package com.github.kchard.service

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.Before
import org.junit.Test

import com.github.kchard.service.ScheduledService;
import com.github.kchard.service.Service;
import com.github.kchard.util.itestUtils.MultiThreadedUtil

class ScheduledServiceTest {

	static final int DELAY = 1
	static final int MAX_WAIT = 250
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
		service = new ScheduledService(commands, DELAY, UNIT)
		
		service.initialize()
		service.start()
		
		MultiThreadedUtil.waitUntil({ executionOrderList.size() == 1 }, MAX_WAIT)
		service.shutdown()
		
		assertEquals([1], executionOrderList)
	}
	
	@Test
	void testMultipleCommands() {
		commands.add(createCommand(1))
		commands.add(createCommand(2))
		service = new ScheduledService(commands, DELAY, UNIT)
		
		service.initialize()
		service.start()
		
		MultiThreadedUtil.waitUntil({ executionOrderList.size() == 2 }, MAX_WAIT)
	
		service.shutdown()
		
		assertEquals([1, 2], executionOrderList)
	}
	
	private Runnable createCommand(int order) {
		[run: {executionOrderList.add(order)}] as Runnable
	}
}
