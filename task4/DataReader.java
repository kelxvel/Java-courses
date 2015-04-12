import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public interface DataReader {
    double[] read() throws Exception;
}

class FileReader implements DataReader {
    protected final File file;

    public FileReader(File file) {
        this.file = file;
    }

    public double[] read() throws Exception {
        try (Scanner in = new Scanner(file)) {
            List<Double> list = new ArrayList<>();

            while (in.hasNextDouble())
                list.add(in.nextDouble());

            double[] array = new double[list.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = list.get(i);

            return array;
        }
    }
}

class StdinReader implements DataReader {
    public double[] read() throws Exception {
        try (Scanner in = new Scanner(System.in)) {
            List<Double> list = new ArrayList<>();

            while (in.hasNextDouble())
                list.add(in.nextDouble());

            double[] array = new double[list.size()];
            for (int i = 0; i < array.length; i++)
                array[i] = list.get(i);

            return array;
        }
    }
}

class CSVFileReader extends FileReader implements DataReader {
    public CSVFileReader(File file) {
        super(file);
    }

    @Override
    public double[] read() throws Exception {
        try (Scanner in = new Scanner(file)) {
            String[] sa = in.nextLine().split(",");

            double[] array = new double[sa.length];
            for (int i = 0; i <sa.length; i++)
                array[i] = Double.parseDouble(sa[i].trim());

            return array;
        }
    }
}

class XMLFileReader extends FileReader implements DataReader {
    public XMLFileReader(File file) {
        super(file);
    }

    @Override
    public double[] read() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);

        Element root = doc.getDocumentElement();

        NodeList valuesList = root.getElementsByTagName("values");
        List<Double> list = new ArrayList<>();
        for (int n = 0; n < valuesList.getLength(); n++) {
            Element e = (Element) valuesList.item(n);
            NodeList valueList = e.getElementsByTagName("value");

            for (int m = 0; m < valueList.getLength(); m++) {
                Element valueElement = (Element) valueList.item(m);
                Text textNode = (Text) valueElement.getFirstChild();
                String value = textNode.getData().trim();
                list.add(Double.parseDouble(value));
            }
        }
        double[] array = new double[list.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = list.get(i);
        return array;
    }
}

class DataReaderFactory {
    public static DataReader newInstance(String type, String fileName) throws IllegalArgumentException {
        File file = fileName.equals("-") ? null : new File(fileName);
        DataReader reader = null;
        switch (type) {
            case "plain":
                reader = new FileReader(file);
                break;
            case "stdin":
                reader = new StdinReader();
                break;
            case "csv":
                reader = new CSVFileReader(file);
                break;
            case "xml":
                reader = new XMLFileReader(file);
                break;
            default:
                throw new IllegalArgumentException("Unresolved reader type: " + type);
        }
        return reader;
    }
}