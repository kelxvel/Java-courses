import java.io.IOException;

public class Task4 {
    private static String sortType;

    private static String inputType;
    private static String inputFile;

    private static String outputType;
    private static String outputFile;

    public static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                String key = args[i].substring(1);
                switch (key) {
                    case "s":
                        sortType = args[++i];
                        break;
                    case "i":
                        String[] inputs = args[++i].split(",");
                        inputType = inputs[0];
                        if (inputs.length > 1)
                            inputFile = inputs[1];
                        else
                            inputFile = args[++i];
                        break;
                    case "o":
                        String[] outputs = args[++i].split(",");
                        outputType = outputs[0];
                        if (outputs.length > 1)
                            outputFile = outputs[1];
                        else
                            outputFile = args[++i];
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length < 6)
                throw new IOException("Usage: -s <sort type> -i <input type>,<input file name> "
                        + "-o <output type>,<output file type>");
            parseArgs(args);

            DataReader reader = DataReaderFactory.newInstance(inputType, inputFile);
            double[] array = reader.read();

            Sorter sorter = SorterFactory.getInstance(sortType);
            sorter.sort(array);

            DataWriter writer = DataWriterFactory.newInstance(outputType, outputFile);
            writer.write(array);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}