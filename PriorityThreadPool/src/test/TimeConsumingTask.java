package test;

import java.util.ArrayList;

public class TimeConsumingTask {
    public static ArrayList<Double> generateRandomSequence(final int length) {
        ArrayList<Double> list = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Double number = Math.random();
            list.add(number);
        }
        return list;
    }

    public static ArrayList<Double> bubbleSort(final ArrayList<Double> list) {
        ArrayList<Double> resultList = new ArrayList<>(list);
        for (int i = 0; i < resultList.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (resultList.get(i) < resultList.get(j)) {
                    Double swp = resultList.get(i);
                    resultList.set(i, resultList.get(j));
                    resultList.set(j, swp);
                }
            }
        }
        return list;
    }

    public static void printSequence(ArrayList<Double> list) {
        String string = "";
        for (Double number : list) {
            string += number + ",";
        }
        System.out.println(string);
    }
}
