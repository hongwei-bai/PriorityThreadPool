package test;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JFrame;

import threadpool.PriorityThreadPool;
import threadpool.PriorityThreadPool.ImageLoadCompleteListener;
import threadpool.TraceableRunnable;

public class Test extends JFrame {
    private static final long serialVersionUID = -6064141947733622858L;
    private PriorityThreadPool mPriorityThreadPool;

    public Test() {
        mPriorityThreadPool = new PriorityThreadPool();
        mPriorityThreadPool.setHighPriorityStackCapacity(
                PriorityThreadPool.HIGH_PRIORITY_STACK_CAPACITY_NOLIMIT);

        ShowEmptyWindowToKeepTestAlive();
    }

    private void ShowEmptyWindowToKeepTestAlive() {
        setLocationRelativeTo(null);
        setPreferredSize(new Dimension(100, 100));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
    }

    public void runTest() {
        mPriorityThreadPool.addImageLoadCompleteListener(new ImageLoadCompleteListener() {

            @Override
            public void onImageLoadComplete(Object... paras) {
                if (null == paras) {
                    return;
                }

                if (null == paras[0]) {
                    return;
                }

                int taskid = (int) paras[0];
                String msg = "task [" + taskid + "] complete";

                long minimumElement = -1;
                long maximumElement = -1;
                if (paras.length == 3) {
                    minimumElement = (long) paras[1];
                    maximumElement = (long) paras[2];
                    msg += "the minimum element is " + minimumElement
                            + " and the maximum element is " + maximumElement;
                }
                System.out.println(msg);
            }
        });
        for (int i = 0; i < 100; i++) {
            mPriorityThreadPool.execute(new TestTask(i));
            mPriorityThreadPool.executeImmediately(new TestTaskWithCallback(1000 + i));
        }
    }

    private class TestTask extends TraceableRunnable {

        private int mTaskId;

        public TestTask(int id) {
            mTaskId = id;
        }

        @Override
        public void run() {
            ArrayList<Double> sequence = TimeConsumingTask.generateRandomSequence(5000);
            TimeConsumingTask.bubbleSort(sequence);
            setParas(mTaskId);
        }

        @Override
        public int getParaCount() {
            return 1;
        }
    }

    /**
     * @author bhw1899
     * @example This task is callback with two parameter of maximum and minimum
     *          elements.
     */
    private class TestTaskWithCallback extends TraceableRunnable {
        private int mTaskId;

        public TestTaskWithCallback(int id) {
            mTaskId = id;
        }

        @Override
        public void run() {
            ArrayList<Double> sequence = TimeConsumingTask.generateRandomSequence(5000);
            ArrayList<Double> result = TimeConsumingTask.bubbleSort(sequence);
            setParas(mTaskId, result.get(0), result.get(4999));
        }

        @Override
        public int getParaCount() {
            return 2;
        }
    }

    public static void main(String[] args) {
        new Test().runTest();
    }
}
