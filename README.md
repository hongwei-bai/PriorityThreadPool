# PriorityThreadPool
A priority-based thread pool, with two priorities - high and normal, high priority tasks are managed by a task stack which comply with LIFO rule, while normal priority tasks are managed by a non-block queue. all high priority would be executed before normal ones.
