# Part IB Further Java: More Safe Message Queues
Three implementations of safe, concurrent producer-consumer queues.

## :clipboard: Task
1. Complete the implementation of `OneLockConcurrentQueue` that uses only a single, shared lock.
2. Test your implementation of `OneLockConcurrentQueue` by using `ConcurrentQueueTest` in the repository.
3. Complete the implementation of `TwoLockConcurrentQueue` which implements the `ConcurrentQueue` interface and which supports fine-grained locking by locking on the first and last `Link` items in the queue as suggested in the paper [1].
4. Complete the implementation of `NoLockConcurrentQueue` which implements the `ConcurrentQueue` interface and which uses no locks by making use of the `AtomicReference` class in `java.util.concurrent.atomic`.
5. Test your implementation of `TwoLockConcurrentQueue` and `NoLockConcurrentQueue` by using `ConcurrentQueueTest`.

## :books: Sources
[1] Maged M. Michael, Michael L. Scott (1996) *Simple, Fast, and Practical Non-Blocking and Blocking Concurrent Queue Algorithms*
