package com.github.kchard.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;



public class ServiceBuilder {
	
	private List<Runnable> initializationCommands = new ArrayList<Runnable>();
	private List<Runnable> startupCommands = new ArrayList<Runnable>();
	private List<Runnable> shutdownCommands = new ArrayList<Runnable>();
	private Long runFor;
	private Predicate predicate;
	
	private List<AbstractService> services = new ArrayList<AbstractService>();
	
	public ServiceBuilder onInit(Runnable command) {
		initializationCommands.add(command);
		return this;
	}
	
	public ServiceBuilder onStartup(Runnable command) {
		startupCommands.add(command);
		return this;
	}

	public ServiceBuilder onShutdown(Runnable command) {
		shutdownCommands.add(command);
		return this;
	}
	
	public ServiceBuilder withChild(AbstractService service) {
		services.add(service);
		return this;
	}
	
	public ServiceBuilder runFor(long seconds) {
		runFor = seconds;
		return this;
	}
	
	public ServiceBuilder runUntil(Predicate predicate) {
		this.predicate = predicate;
		return this;
	}
	
	public ScheduledServiceBuilder schedule() {
		return new ScheduledServiceBuilder(this);
	}
	
	public <T> ConsumerWorkerServiceBuilder<T> consumers(Consumer<T> consumer) {
		return new ConsumerWorkerServiceBuilder<T>(this, consumer);
	}
	
	public <T> ProducerWorkerServiceBuilder<T> producers(Producer<T> producer) {
		return new ProducerWorkerServiceBuilder<T>(this, producer);
	}
	
	public AbstractService build() { 
		
		if(initializationCommands.size() > 0 || startupCommands.size() > 0 || shutdownCommands.size() > 0) {
			services.add(0, new CommandService(initializationCommands, startupCommands, shutdownCommands));
		}
	
		final AbstractService compositeService = new CompositeService(services);
		
		if(runFor != null && runFor > 0) {
			new ScheduledShutdownMonitor(compositeService, runFor).start();
		}
		
		if(predicate != null) {
			new PredicateShutdownMonitor(compositeService, predicate).start();
		}
		
		return compositeService;
	}
	
	public static class ScheduledServiceBuilder {
		
		private final ServiceBuilder serviceBuilder;
		
		private List<Runnable> commands = new ArrayList<Runnable>();
		private long initialDelay = 0L;
		private TimeUnit unit = TimeUnit.MILLISECONDS;
		
		private ScheduledServiceBuilder(ServiceBuilder serviceBuilder) {
			this.serviceBuilder = serviceBuilder;
		}
		
		public ScheduledServiceBuilder withInitialDelay(long delay) {
			this.initialDelay = delay;
			return this;
		}
		
		public ScheduledServiceBuilder withTimeUnit(TimeUnit unit) {
			this.unit = unit;
			return this;
		}
		
		public ScheduledServiceBuilder command(Runnable command) {
			this.commands.add(command);
			return this;
		}
		
		public <T> ScheduledProducerBuilder<T> producer(Producer<T> producer) {
			return new ScheduledProducerBuilder<T>(this, producer);
		}
		
		public <T> ScheduledConsumerBuilder<T> consumer(Consumer<T> consumer) {
			return new ScheduledConsumerBuilder<T>(this, consumer);
		}
		
		public ServiceBuilder once() {
			serviceBuilder.services.add(new ScheduledService(commands, initialDelay, unit));
			return serviceBuilder;
		}
		
		public ServiceBuilder withDelay(long delay) {
			serviceBuilder.services.add(new FixedDelayScheduledService(commands, initialDelay, delay, unit));
			return serviceBuilder;
		}
		
		public ServiceBuilder withRate(long delay) {
			serviceBuilder.services.add(new FixedRateScheduledService(commands, initialDelay, delay, unit));
			return serviceBuilder;
		}
	}
	
	public static class ScheduledProducerBuilder<T> {
		
		private final ScheduledServiceBuilder builder;
		private final Producer<T> producer;
		
		public ScheduledProducerBuilder(ScheduledServiceBuilder builder, Producer<T> producer) {
			this.builder = builder;
			this.producer = producer;
		}
		
		public ScheduledServiceBuilder forQueue(final Queue<T> queue) {
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					queue.offer(producer.produce());
				}
			};
			
			builder.commands.add(runnable);
			return builder;
		}
	}
	
	public static class ScheduledConsumerBuilder<T> {
		
		private final ScheduledServiceBuilder builder;
		private final Consumer<T> consumer;
		
		public ScheduledConsumerBuilder(ScheduledServiceBuilder builder, Consumer<T> consumer) {
			this.builder = builder;
			this.consumer = consumer;
		}
		
		public ScheduledServiceBuilder forQueue(final Queue<T> queue) {
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					T item = queue.poll();
					if(item != null) {
						consumer.consume(item);
					}
				}
			};
			
			builder.commands.add(runnable);
			return builder;
		}
	}
	
	public static class ConsumerWorkerServiceBuilder<T> {
		
		private final ServiceBuilder serviceBuilder;
		
		private Consumer<T> consumer;
		private int maxWorkers;
		
		private ConsumerWorkerServiceBuilder(ServiceBuilder serviceBuilder, Consumer<T> consumer) {
			this.serviceBuilder = serviceBuilder;
			this.consumer = consumer;
		}
		
		public ConsumerWorkerServiceBuilder<T> maxConsumers(int maxWorkers) {
			this.maxWorkers = maxWorkers;
			return this;
		}
		
		public ServiceBuilder forQueue(BlockingQueue<T> workQueue) {
			if(maxWorkers > 0) {
				serviceBuilder.services.add(new ConsumerWorkerService<T>(consumer, workQueue, maxWorkers));
			} else {
				serviceBuilder.services.add(new ConsumerWorkerService<T>(consumer, workQueue));
			}
			
			return serviceBuilder;
		}
	}
	
	public static class ProducerWorkerServiceBuilder<T> {
		
		private final ServiceBuilder serviceBuilder;
		
		private Producer<T> producer;
		private int maxWorkers;
		
		private ProducerWorkerServiceBuilder(ServiceBuilder serviceBuilder, Producer<T> producer) {
			this.serviceBuilder = serviceBuilder;
			this.producer = producer;
		}
		
		public ProducerWorkerServiceBuilder<T> maxProducers(int maxWorkers) {
			this.maxWorkers = maxWorkers;
			return this;
		}
		
		public ServiceBuilder forQueue(BlockingQueue<T> workQueue) {
			if(maxWorkers > 0) {
				serviceBuilder.services.add(new ProducerWorkerService<T>(producer, workQueue, maxWorkers));
			} else {
				serviceBuilder.services.add(new ProducerWorkerService<T>(producer, workQueue));
			}
			
			return serviceBuilder;
		}
	}
}
