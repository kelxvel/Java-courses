import java.util.Stack;
import java.util.regex.*;

public class ValidatorHTML {
    private static final Pattern tagPattern = Pattern.compile("<(.*?)>");

    /**
     * Checks the correctness of HTML code in terms of order of opening / closing tag.
     * Also prints tag name and position number in <code>htmlCode</code> where error found.
     * @param htmlCode HTML code to be validate
     * @return true if the <code>htmlCode</code> correct HTML in terms of order of opening / closing tag
     */
    public boolean validate(String htmlCode) {
        Matcher m = tagPattern.matcher(htmlCode);
        Stack<String> tags = new Stack<>();
        while (m.find()) {
            String tag = m.group();
            if (tag.startsWith("</")) {
                String element = getElementNameByTag(tag);
                if (!tags.empty()) {
                    String e = getElementNameByTag(tags.pop());
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
                tags.push(tag);
            }
        }
        if (!tags.empty()) {
            String tag = tags.pop();
            System.out.println("Error in column " + htmlCode.indexOf(tag) + ": missing closing tag for: " + tag);
            return false;
        }
        return true;
    }

    private static String getElementNameByTag(String tag) {
        if (tag.startsWith("</"))
            tag = tag.replaceFirst("</", "");
        else if (tag.startsWith("<"))
            tag = tag.replaceFirst("<", "");
        else
            return null;

        if (tag.endsWith("/>"))
            tag = tag.replaceAll("/>$", "");
        else if (tag.endsWith(">"))
            tag = tag.replaceAll(">$", "");
        else
            return null;

        return (tag.trim().split(" ", 2))[0];
    }

    public static void main(String[] args) {
        String[] testCode = {
                "<html><body><p align=“right”><strong>Hello world!</strong></p></body></html>",
                "<html><body><p align=“right”><strong>Hello world!</p></body></html>",
                "<html><body><p align=“right”><strong>Hello world!</strong>"
        };
        ValidatorHTML validator = new ValidatorHTML();
        for (String c : testCode)
            System.out.println(validator.validate(c));
    }
}
