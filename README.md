Non-Standard Use of ForkJoinTask

In each article of this series, I take a small, interesting piece of code from open repositories, simplify and generalize it. The result is an intriguing building block that might be useful somewhere—or maybe not. Either way, exploring "how it works" (and why) is always worthwhile.

Today, we'll take a look into the popular Caffeine caching library and explore how its cache cleanup procedure is implemented.

The data cleanup process is triggered by specific events, such as the addition of new data or when the data's time-to-live expires. Thus, this process is dependent on the state of the cache. To initiate such state-dependent processes in Java, it's recommended to use mechanisms like FutureTask, Semaphore, or BlockingQueue.

Check out the full article - https://www.linkedin.com/pulse/non-standard-use-forkjointask-alexandr-manunin-dn8ef/?trackingId=UHrWHqoTRJqaiAaJR3dRag%3D%3D
