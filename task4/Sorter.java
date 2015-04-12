public interface Sorter {
    void sort(double[] array);
}

class QuickSorter implements Sorter {
    private QuickSorter() {}

    private static final QuickSorter INSTANCE = new QuickSorter();

    public static QuickSorter getInstance() {
        return INSTANCE;
    }

    public void sort(double[] array) {
        quickSort(array, 0, array.length-1);
    }

    private void quickSort(double[] array, int left, int right) {
        if (left >= right)
            return;

        swap(array, left, (left + right) / 2);

        int last = left;
        for (int i = left+1; i <= right; i++)
            if (array[i] < array[left])
                swap(array, ++last, i);

        swap(array, left, last);

        quickSort(array, left, last-1);
        quickSort(array, last+1, right);
    }

    public static void swap(double[] array, int i, int j) {
        double temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

class BubbleSorter implements Sorter {
    private BubbleSorter() {}

    private static final BubbleSorter INSTANCE = new BubbleSorter();

    public static BubbleSorter getInstance() {
        return INSTANCE;
    }

    public void sort(double[] array) {
        for (int k = array.length-1; k >=0; k--) {
            for (int i = 0; i < k; i++) {
                if (array[i] > array[i+1])
                    swap(array, i, i+1);
            }
        }
    }

    public static void swap(double[] array, int i, int j) {
        double temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}

class ShiftSorter implements Sorter {
    private ShiftSorter() {}

    private static final ShiftSorter INSTANCE = new ShiftSorter();

    public static ShiftSorter getInstance() {
        return INSTANCE;
    }

    public void sort(double[] array) {
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

class SorterFactory {
    public static Sorter getInstance(String type) throws IllegalArgumentException {
        Sorter sorter = null;
        switch (type.toLowerCase()) {
            case "quick":
                sorter = QuickSorter.getInstance();
                break;
            case "bubble":
                sorter = BubbleSorter.getInstance();
                break;
            case "shift":
                sorter = ShiftSorter.getInstance();
                break;
            default:
                throw new IllegalArgumentException("Unresolved sort type: " + type);
        }
        return sorter;
    }
}