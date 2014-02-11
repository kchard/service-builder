package com.github.kchard.service

import groovy.transform.Canonical

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class Example {

    static void main(args) {
        //doNothingService()
        //helloService()
        //scheduledHelloService()
        //workerHelloService()
        //scheduledProducerWorkerHelloService()
        //scheduledConsumerWorkerHelloService()
        //scheduledProducerConsumerWorkerHelloService()
    }

    @Canonical
    static class Hello implements Runnable {

        String message

        @Override
        void run() {
            println "Hello ${message} at ${new Date()}"
        }
    }

    //Do nothing Service
    static void doNothingService() {
        Service service = new ServiceBuilder().build()
        service.initialize()
        service.start()
        service.shutdown()
    }

    //Hello service
    static void helloService() {
        Service service = new ServiceBuilder().onInit(new Hello("initialize"))
                                              .onStartup(new Hello("startup"))
                                              .onShutdown(new Hello("shutdown"))
                                              .build()
        service.initialize()
        service.start()
        service.shutdown()
    }

    //Scheduled Hello service
    static void scheduledHelloService() {
        Service service = new ServiceBuilder().schedule().withInitialDelay(1000L)
                                                         .command(new Hello("scheduled once"))
                                                         .once()
                                              .schedule().withInitialDelay(1000L)
                                                         .command(new Hello("scheduled with rate"))
                                                         .withRate(2000L)
                                              .schedule().withInitialDelay(1000L)
                                                         .command(new Hello("scheduled with delay"))
                                                         .withDelay(3000L)
                                              .runFor(10L)
                                              .build()
        service.initialize()
        service.start()
    }

    static class HelloProducer implements Producer<String> {

        @Override
        String produce() {
            return "Hello, I produced this at ${new Date()}"
        }
    }

    static class HelloConsumer implements Consumer<String> {

        @Override
        void consume(String item) {
            println "At ${new Date()} I consumed: ${item}"
        }
    }

    //Worker Service
    static void workerHelloService() {

        BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100)

        Service service = new ServiceBuilder().producers(new HelloProducer()).forQueue(workQueue)
                                              .consumers(new HelloConsumer()).forQueue(workQueue)
                                              .runFor(10L)
                                              .build()

        service.initialize()
        service.start()
    }

    //Worker Service
    static void scheduledProducerWorkerHelloService() {

        BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100)

        Service service = new ServiceBuilder().schedule().producer(new HelloProducer())
                                                         .forQueue(workQueue)
                                                         .withRate(1000L)
                                              .consumers(new HelloConsumer()).forQueue(workQueue)
                                              .runFor(10L)
                                              .build()

        service.initialize()
        service.start()
    }

    //Worker Service
    static void scheduledConsumerWorkerHelloService() {

        BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100)

        Service service = new ServiceBuilder().producers(new HelloProducer()).forQueue(workQueue)
                                              .schedule().consumer(new HelloConsumer())
                                                         .forQueue(workQueue)
                                                         .withRate(1000L)
                                              .runFor(10L)
                                              .build()

        service.initialize()
        service.start()
    }

    //Worker Service
    static void scheduledProducerConsumerWorkerHelloService() {

        BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100)

        Service service = new ServiceBuilder().schedule().producer(new HelloProducer())
                                                         .forQueue(workQueue)
                                                         .withRate(1000L)
                                              .schedule().consumer(new HelloConsumer())
                                                         .forQueue(workQueue)
                                                         .withRate(2000L)
                                            .runFor(10L)
                                            .build()

        service.initialize()
        service.start()
    }
}
