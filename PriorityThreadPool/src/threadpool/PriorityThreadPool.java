package threadpool;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;

public class PriorityThreadPool {
    private Queue<TraceableRunnable> mNormalPriorityTaskQueue;
    private Stack<TraceableRunnable> mHighPriorityTaskStack;
    private ArrayList<ImageLoadCompleteListener> mImageLoadCompleteListener;
    private ArrayList<Thread> mThreadList;
    private final Object LOCK_QUEUE;
    private final Object LOCK_STACK;

    private int HIGH_PRIORITY_STACK_CAPACITY = 50;
    public static final int HIGH_PRIORITY_STACK_CAPACITY_NOLIMIT = -1;
    private int SHARED_THREAD_NUMBER = 4;
    private int DEDICATED_THREAD_NUMBER = 16;
    private int THREAD_NUMBER = SHARED_THREAD_NUMBER + DEDICATED_THREAD_NUMBER;

    private final int NEXT_ACTION_INTERRUPT = 0;
    private final int NEXT_ACTION_RUN_HIGH_PRIORITY_STACK = 1;
    private final int NEXT_ACTION_RUN_NORMAL_PRIORITY_QUEUE = 2;

    public PriorityThreadPool() {
        LOCK_QUEUE = new Object();
        LOCK_STACK = new Object();
        mNormalPriorityTaskQueue = new LinkedBlockingQueue<>();
        mHighPriorityTaskStack = new Stack<>();
        mThreadList = new ArrayList<>();
        mImageLoadCompleteListener = new ArrayList<>();
        for (int i = 0; i < DEDICATED_THREAD_NUMBER; i++) {
            Thread dadicatedThread = new DadicatedThread();
            dadicatedThread.setDaemon(true);
            mThreadList.add(dadicatedThread);
            dadicatedThread.start();
        }
        for (int i = DEDICATED_THREAD_NUMBER; i < THREAD_NUMBER; i++) {
            Thread sharedThread = new SharedThread();
            sharedThread.setDaemon(true);
            mThreadList.add(sharedThread);
            sharedThread.start();
        }
    }

    public void execute(TraceableRunnable r) {
        synchronized (LOCK_QUEUE) {
            mNormalPriorityTaskQueue.offer(r);
        }
        notifyAllSharedThread();
    }

    public interface ImageLoadCompleteListener {
        public void onImageLoadComplete(Object... paras);
    }

    public void addImageLoadCompleteListener(ImageLoadCompleteListener l) {
        mImageLoadCompleteListener.add(l);
    }

    public void executeImmediately(TraceableRunnable r) {
        if (HIGH_PRIORITY_STACK_CAPACITY != HIGH_PRIORITY_STACK_CAPACITY_NOLIMIT
                && mHighPriorityTaskStack.size() > HIGH_PRIORITY_STACK_CAPACITY) {
            return;
        }

        synchronized (LOCK_STACK) {
            mHighPriorityTaskStack.push(r);
        }

        notifyAllThread();
    }

    /**
     * @param capacity, there are two priorities, high and normal.
     * normal priority task is managed by a non-blocked queue, comply with FIFO rules.
     * high priority task is managed by a stack, comply with last-in£¬first-out - LIFO rules.
     * 
     * both normal and high priority tasks can use shared threads.
     * only high priority tasks can use dedicated threads.
     * 
     * if this capacity is set to a positive number, the stack can only maintain such amount of task,
     * extra tasks arrived when stack is full will be dropped off.
     * if capacity is set to -1, the stack will be unlimited - no task would be dropped.
     */
    public void setHighPriorityStackCapacity(int capacity) {
        HIGH_PRIORITY_STACK_CAPACITY = capacity;
    }

    private void notifyAllThread() {
        for (Thread t : mThreadList) {
            synchronized (t) {
                t.notify();
            }
        }

        notifyAllSharedThread();
    }

    private void notifyAllSharedThread() {
        for (int i = DEDICATED_THREAD_NUMBER; i < THREAD_NUMBER; i++) {
            Thread sharedThread = mThreadList.get(i);
            synchronized (sharedThread) {
                sharedThread.notify();
            }
        }
    }

    public class DadicatedThread extends Thread {

        public DadicatedThread() {
        }

        @Override
        public void run() {
            while (true) {
                if (mHighPriorityTaskStack.isEmpty()) {
                    synchronized (this) {
                        try {
                            wait();
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                TraceableRunnable r = null;
                synchronized (LOCK_STACK) {
                    if (mHighPriorityTaskStack.size() > 0) {
                        r = mHighPriorityTaskStack.pop();
                    }
                }
                if (r != null) {
                    r.run();
                    if (mImageLoadCompleteListener != null) {
                        for (ImageLoadCompleteListener l : mImageLoadCompleteListener) {
                            l.onImageLoadComplete(r.getParas());
                        }
                    }
                }
            }
        }
    }

    public class SharedThread extends Thread {

        public SharedThread() {
        }

        @Override
        public void run() {
            while (true) {
                int nextAction = NEXT_ACTION_INTERRUPT;
                if (!mHighPriorityTaskStack.isEmpty()) {
                    nextAction = NEXT_ACTION_RUN_HIGH_PRIORITY_STACK;
                } else if (!mNormalPriorityTaskQueue.isEmpty()) {
                    nextAction = NEXT_ACTION_RUN_NORMAL_PRIORITY_QUEUE;
                } else {
                    synchronized (this) {
                        try {
                            wait();
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                TraceableRunnable r = null;
                if (NEXT_ACTION_RUN_HIGH_PRIORITY_STACK == nextAction) {
                    synchronized (LOCK_STACK) {
                        if (mHighPriorityTaskStack.size() > 0) {
                            r = mHighPriorityTaskStack.pop();
                        }
                    }
                } else {
                    synchronized (LOCK_QUEUE) {
                        r = mNormalPriorityTaskQueue.poll();
                    }
                }

                if (r != null) {
                    r.run();
                    if (mImageLoadCompleteListener != null) {
                        for (ImageLoadCompleteListener l : mImageLoadCompleteListener) {
                            l.onImageLoadComplete(r.getParas());
                        }
                    }
                }
            }

        }
    }
}
