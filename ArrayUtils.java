import java.util.Arrays;
import java.util.Scanner;

public class ArrayUtils {
    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            System.out.println("Enter a comma-separated array: ");
            String stringArray = in.nextLine();

            int[] array = parseIntArray(stringArray, ",");
            quickSort(array, 0, array.length - 1);

            System.out.println("Sorted array: " + Arrays.toString(array));
        }
        catch (NumberFormatException e) {
            System.err.println(e);
        }
    }

    public static int[] parseIntArray(String source, String separator) throws NumberFormatException {
        String[] sa = source.split(separator);
        int[] array = new int[sa.length];

        long value;
        for (int i = 0; i < sa.length; i++) {
            value = Long.parseLong(sa[i].trim());
            if (value > Integer.MAX_VALUE)
                throw new NumberFormatException("The maximum allowed value = " + Integer.MAX_VALUE);
            if (value < Integer.MIN_VALUE)
                throw new NumberFormatException("The minimum allowed value = " + Integer.MIN_VALUE);

            array[i] = (int) value;
        }
        return array;
    }

    public static void quickSort(int[] array, int left, int right) {
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

    public static void swap(int[] array, int i, int j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
