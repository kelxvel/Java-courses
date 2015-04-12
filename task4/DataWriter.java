import java.io.*;
import java.util.Arrays;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public interface DataWriter {
    void write(double[] array) throws Exception;
}

class FileWriter implements DataWriter {
    protected final File file;

    public FileWriter(File file) {
        this.file = file;
    }

    public void write(double[] array) throws Exception {
        try (PrintWriter out = new PrintWriter(file)) {
            for (int i = 0; i < array.length; i++)
                out.println(array[i]);
        }
    }
}

class StdoutWriter implements DataWriter {
    public void write(double[] array) {
        System.out.println(Arrays.toString(array));
    }
}

class CSVFileWriter extends FileWriter implements DataWriter {
    public CSVFileWriter(File file) {
        super(file);
    }

    @Override
    public void write(double[] array) throws Exception {
        try (PrintWriter out = new PrintWriter(file)) {
            StringBuilder sb = new StringBuilder();
            int iMax = array.length-1;
            for (int i = 0; i < iMax; i++)
                sb.append(array[i] + ", ");
            sb.append(array[iMax]);
            out.print(sb.toString());
        }
    }
}

class XMLFileWriter extends FileWriter implements DataWriter {
    public XMLFileWriter(File file) {
        super(file);
    }

    @Override
    public void write(double[] array) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement("root");

        Element values = doc.createElement("values");
        for (int i = 0; i < array.length; i++) {
            Element value = doc.createElement("value");
            value.appendChild(doc.createTextNode(Double.toString(array[i])));

            values.appendChild(value);
        }
        root.appendChild(values);
        doc.appendChild(root);

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult stream = new StreamResult(new FileOutputStream(file));
        transformer.transform(source, stream);
    }
}

class DataWriterFactory {
    public static DataWriter newInstance(String type, String fileName) throws IllegalArgumentException {
        File file = fileName.equals("-") ? null : new File(fileName);
        DataWriter writer = null;
        switch (type) {
            case "plain":
                writer = new FileWriter(file);
                break;
            case "stdout":
                writer = new StdoutWriter();
                break;
            case "csv":
                writer = new CSVFileWriter(file);
                break;
            case "xml":
                writer = new XMLFileWriter(file);
                break;
            default:
                throw new IllegalArgumentException("Unresolved writer type: " + type);
        }
        return writer;
    }
}