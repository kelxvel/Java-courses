import java.util.Stack;
import java.util.regex.*;

public class ValidatorHTML {
    private static final Pattern p = Pattern.compile("\\<(.*?)\\>");

    public boolean validate(String htmlCode) {
        Matcher m = p.matcher(htmlCode);

        Stack<String> elements = new Stack<>();
        while (m.find()) {
            String tag = m.group();
            if (tag.startsWith("</")) {
                String element = tag.replaceFirst("</", "").replaceAll(">$", "");
                if (!elements.empty()) {
                    String e = elements.pop();
                    if (!e.equalsIgnoreCase(element)) {
                        System.out.println("Error in column " + htmlCode.indexOf(tag) + ": " + tag);
                        return false;
                    }
                } else {
                    System.out.println("Error in column " + htmlCode.indexOf(tag) + ": " + tag);
                    return false;
                }
            } else if (tag.endsWith("/>")) {
                continue;
            } else {
                tag = tag.replaceFirst("<", "").replaceAll(">$", "");
                String element = (tag.split(" ", 2))[0]; // get only element-name without params
                elements.push(element);
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String html = "<html><body><p align=“right”><strong>Hello world!</strong></p></body></html>";
        String html2 = "<html><body><p align=“right”><strong>Hello world!</p></body></html>";
        ValidatorHTML validator = new ValidatorHTML();
        System.out.println(validator.validate(html));
        System.out.println(validator.validate(html2));
    }
}
