import java.text.*;

public class Calculator {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: [value] [value] [add || sub || mul || div || mod]");
            System.exit(1);
        }

        double first = Double.parseDouble(args[0].replace(',', '.'));
        double second = Double.parseDouble(args[1].replace(',', '.'));
        String operator = args[2].toLowerCase();

        double result = 0.0;
        try {
            result = calculate(first, second, operator);
        }
        catch (OverflowException | ArithmeticException e) {
            System.err.println(e);
            System.exit(2);
        }

        DecimalFormat df = new DecimalFormat("#.###");
        System.out.println(df.format(result));
    }

    public static double calculate(double first, double second, String operator)
            throws OverflowException, ArithmeticException {
        double result = 0.0;
        switch (operator) {
            case "add":
                if (isAddOverflow(first, second)) throw new OverflowException();
                result = first + second;
                break;
            case "sub":
                if (isSubOverflow(first, second)) throw new OverflowException();
                result = first - second;
                break;
            case "mul":
                if (isMulOverflow(first, second)) throw new OverflowException();
                result = first * second;
                break;
            case "div":
                if (second == 0.0) throw new ArithmeticException("Divide by zero");
                if (isDivOverflow(first, second)) throw new OverflowException();
                result = first / second;
                break;
            case "mod":
                result = first % second;
                break;
            default:
                System.err.println("Unresolved operation");
                System.exit(3);
                break;
        }
        return result;
    }

    public static boolean isAddOverflow(double l, double r) {
        if ((l == Double.MAX_VALUE && r > 0.0) || (r == Double.MAX_VALUE && l > 0.0) ||
                (l == Double.MIN_VALUE && r < 0.0) || (r == Double.MIN_VALUE && l < 0.0)) {
            return true;
        }
        return false;
    }

    public static boolean isSubOverflow(double l, double r) {
        if ((l == Double.MAX_VALUE && r < 0.0) || (r == Double.MAX_VALUE && l < 0.0) ||
                (l == Double.MIN_VALUE && r > 0.0) || (r == Double.MIN_VALUE && l > 0.0)) {
            return true;
        }
        return false;
    }

    public static boolean isMulOverflow(double l, double r) {
        if (((Math.abs(r) > 0.0) && (Math.abs(l) == Double.MAX_VALUE)) ||
                ((Math.abs(l) > 0.0) && (Math.abs(r) == Double.MAX_VALUE))) {
            return true;
        }
        return false;
    }

    public static boolean isDivOverflow(double l, double r) {
        if (((Math.abs(r) < 0.0) && (Math.abs(l) == Double.MAX_VALUE)) ||
                ((Math.abs(l) < 0.0) && (Math.abs(r) == Double.MAX_VALUE))) {
            return true;
        }
        return false;
    }

    private static class OverflowException extends Exception {
        public String toString() {
            return getClass().getName() + ": Overflow";
        }
    }
}
