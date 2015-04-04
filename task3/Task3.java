import java.io.*;
import java.util.*;

public class Task3 {
    public static double[] readDoubleArray(File file) throws IOException {
        try (Scanner fin = new Scanner(file)) {
            List<Double> list = new ArrayList<>();

            while (fin.hasNextDouble())
                list.add(fin.nextDouble());

            double[] array = new double[list.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = list.get(i);
            list.clear();

            return array;
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 2)
                throw new IOException("Usage: [sort type] [input file name]");

            double[] array = readDoubleArray(new File(args[1]));

            String sortType = args[0].toLowerCase();
            Sortable sortable = null;
            switch (sortType) {
                case "quick":
                    sortable = new QuickSortableArray(array);
                    break;
                case "bubble":
                    sortable = new BubbleSortableArray(array);
                    break;
                case "shift":
                    sortable = new ShiftSortableArray(array);
                    break;
                default:
                    throw new IOException("Unresolved sort type \"" + sortType + "\"");
            }
            sortable.sort();

            System.out.println(sortable);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}

interface Sortable {
    void sort();
}

class SortableArray implements Sortable {
    protected double[] array;

    public SortableArray(double[] array) {
        this.array = array.clone();
    }

    public void sort() {
        Arrays.sort(array);
    }

    public void swap(int i, int j) {
        double temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    public String toString() {
        return Arrays.toString(array);
    }
}

class QuickSortableArray extends SortableArray {

    public QuickSortableArray(double[] array) {
        super(array);
    }

    public void sort() {
        quickSort(array, 0, array.length-1);
    }

    private void quickSort(double[] array, int left, int right) {
        if (left >= right)
            return;

        swap(left, (left + right) / 2);

        int last = left;
        for (int i = left+1; i <= right; i++)
            if (array[i] < array[left])
                swap(++last, i);

        swap(left, last);

        quickSort(array, left, last-1);
        quickSort(array, last+1, right);
    }
}

class BubbleSortableArray extends SortableArray {

    public BubbleSortableArray(double[] array) {
        super(array);
    }

    public void sort() {
	for (int k = array.length-1; k >=0; k--) {
	    for (int i = 0; i < k; i++) {
		if (array[i] > array[i+1])
		   swap(i, i+1);
	    }
	}
    }
}

class ShiftSortableArray extends SortableArray {

    public ShiftSortableArray(double[] array) {
        super(array);
    }

    public void sort() {
        for (int i = 1; i < array.length; i++) {
            double a = array[i];
            int j = 0;
            while (a > array[j])
                j++;

            for (int k = i; k > j; k--)
                array[k] = array[k-1];
            array[j] = a;
        }
    }
}
