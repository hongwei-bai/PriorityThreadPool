# PriorityThreadPool
The project is a java project.

A priority-based thread pool, with two priorities - high and normal, high priority tasks are managed by a task stack which comply with LIFO rule, while normal priority tasks are managed by a non-block queue. all high priority would be executed before normal ones.

the threadpool has two kinds of priorities:
1. normal
2. high

to start a normal priority task, invoke execute(TraceableTask r),
while to start a high priority task, invoke executeImmediately(TraceableTask r).

high priority tasks have obvious priority to be executed than normal ones.

it is sutable for load images in background threads for display, the images right on screen are expected to be loaded immediately, and other images can be buffered for later use.

the project gives a small example, and the print output can make you quite clear about the priority rule.

try it :)
good luck!

author: bhw1899
email: bhw8412@hotmail.com

