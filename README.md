service-builder
===============

The service-builder library provides a simple builder dsl for defining services. In this context, a service is
defined as a process that progresses through several lifecycle phases. A service is defined, initialized, started,
and finally stopped. The library handles all of the state transition logic. The user needs only to specify the code
to execute during each phase. Additionally, the library provides a mechanism to schedule tasks and setup producer/consumer
work queues.

## Examples

A service that does nothing:

```
Service service = new ServiceBuilder().build();

service.initialize();
service.start();
service.shutdown();
```

Assume you have a Runnable defined as follows:

```
public class Hello implements Runnable {

        private final String message;

        public Hello(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            System.out.println("Hello " + message + " at " + new Date());
        }
}
```

A service that executes Hello at each state transition:

```
Service service = new ServiceBuilder().onInit(new Hello("initialize"))
                                      .onStartup(new Hello("startup"))
                                      .onShutdown(new Hello("shutdown"))
                                      .build();
service.initialize();
service.start();
service.shutdown();
```

Output:
```
Hello initialize at Tue Feb 11 13:39:12 PST 2014
Hello startup at Tue Feb 11 13:39:12 PST 2014
Hello shutdown at Tue Feb 11 13:39:12 PST 2014
```

A service that schedules a Hello once, at a fixed rate, and at a fixed delay:

```
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
                                      .build();

service.initialize();
service.start();
```

Output:

```
Hello scheduled with delay at Tue Feb 11 13:42:53 PST 2014
Hello scheduled with rate at Tue Feb 11 13:42:53 PST 2014
Hello scheduled once at Tue Feb 11 13:42:53 PST 2014
Hello scheduled with rate at Tue Feb 11 13:42:55 PST 2014
Hello scheduled with delay at Tue Feb 11 13:42:56 PST 2014
Hello scheduled with rate at Tue Feb 11 13:42:57 PST 2014
Hello scheduled with rate at Tue Feb 11 13:42:59 PST 2014
Hello scheduled with delay at Tue Feb 11 13:42:59 PST 2014
Hello scheduled with rate at Tue Feb 11 13:43:01 PST 2014
Hello scheduled with delay at Tue Feb 11 13:43:02 PST 2014
Hello scheduled with rate at Tue Feb 11 13:43:03 PST 2014
```

Assume you have the following Producer/Consumer defined:
```
public class HelloProducer implements Producer<String> {

        @Override
        public String produce() {
            return "Hello, I produced this at " + new Date();
        }
}

public class HelloConsumer implements Consumer<String> {

        @Override
        public void consume(String item) {
            System.out.println("At " + new Date() + "I consumed: " + item);
        }
}
```

A bounded work queue:

```
BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100);

Service service = new ServiceBuilder().producers(new HelloProducer()).forQueue(workQueue)
                                      .consumers(new HelloConsumer()).forQueue(workQueue)
                                      .runFor(10L)
                                      .build();

service.initialize();
service.start();
```

Output:

```
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
At Tue Feb 11 13:47:30 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:47:30 PST 2014
.
.
.
```

A scheduled producer:

```
BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100);

Service service = new ServiceBuilder().schedule().producer(new HelloProducer())
                                                 .forQueue(workQueue)
                                                 .withRate(1000L)
                                      .consumers(new HelloConsumer()).forQueue(workQueue)
                                      .runFor(10L)
                                      .build();

service.initialize();
service.start();
```

Output:

```
At Tue Feb 11 13:53:00 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:00 PST 2014
At Tue Feb 11 13:53:01 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:01 PST 2014
At Tue Feb 11 13:53:02 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:02 PST 2014
At Tue Feb 11 13:53:03 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:03 PST 2014
At Tue Feb 11 13:53:04 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:04 PST 2014
At Tue Feb 11 13:53:05 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:05 PST 2014
At Tue Feb 11 13:53:06 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:06 PST 2014
At Tue Feb 11 13:53:07 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:07 PST 2014
At Tue Feb 11 13:53:08 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:08 PST 2014
At Tue Feb 11 13:53:09 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:09 PST 2014
At Tue Feb 11 13:53:10 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:53:10 PST 2014

```

A scheduled consumer:

```
BlockingQueue<String> workQueue = new LinkedBlockingQueue<String>(100);

Service service = new ServiceBuilder().producers(new HelloProducer()).forQueue(workQueue)
                                      .schedule().consumer(new HelloConsumer())
                                                 .forQueue(workQueue)
                                                 .withRate(1000L)
                                      .runFor(10L)
                                      .build();

service.initialize();
service.start();
```

Output:

```
At Tue Feb 11 13:55:11 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:12 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:13 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:14 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:15 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:16 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:17 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:18 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:19 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:20 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
At Tue Feb 11 13:55:21 PST 2014 I consumed: Hello, I produced this at Tue Feb 11 13:55:11 PST 2014
```
