package uk.co.mycomputerworld.docAssist4U.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * The CodeFormatter class provides utility methods to format Java code blocks
 * within a text to HTML format. It identifies Java code sections marked with
 * ```java and converts them into HTML code blocks.
 */
public class CodeFormatter {

    /**
     * Formats Java code blocks within the given text to HTML format.
     * Java code blocks are identified by ```java and ``` delimiters.
     *
     * @param text The input text containing Java code blocks.
     * @return A string with Java code blocks formatted as HTML.
     */
    public static String formatJavaCodeInTextToHtml(String text) {
        StringBuilder formattedText = new StringBuilder();

        // Pattern to find sections between ```java and ```
        Pattern pattern = Pattern.compile("```java(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;

        while (matcher.find()) {
            // Append text before the code section
            formattedText.append(text, lastEnd, matcher.start());

            // Extract Java code block and format it to HTML
            String javaCode = matcher.group(1).trim();
            String formattedCode = formatJavaCodeToHtml(javaCode);

            // Wrap formatted code in <pre><code> tags
            formattedText.append("<pre><code class=\"language-java\">").append(formattedCode).append("</code></pre>");

            // Update last end position
            lastEnd = matcher.end();
        }

        // Append any remaining text after the last code block
        formattedText.append(text.substring(lastEnd));

        return formattedText.toString();
    }

    /**
     * Formats a given Java code string to HTML by escaping HTML characters
     * and applying indentation using non-breaking spaces.
     *
     * @param code The Java code to format.
     * @return A string representing the formatted Java code as HTML.
     */
    public static String formatJavaCodeToHtml(String code) {
        StringBuilder formattedCode = new StringBuilder();
        int indentLevel = 0;

        for (String line : code.split("\n")) {
            line = line.trim();

            // Adjust indentation level for braces
            if (line.endsWith("}")) {
                indentLevel--;
            }

            // Apply indentation as non-breaking spaces
            for (int i = 0; i < indentLevel; i++) {
                formattedCode.append("&nbsp;&nbsp;&nbsp;&nbsp;"); // 4 spaces per indent level
            }

            // Escape HTML characters in code
            formattedCode.append(line.replace("<", "&lt;").replace(">", "&gt;")).append("<br>");

            // Increase indentation after opening brace
            if (line.endsWith("{")) {
                indentLevel++;
            }
        }

        return formattedCode.toString();
    }

    public static String replaceTags(String input, String openingTag, String closingTag) {
        boolean openTag = true;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            if (i < input.length() - 1 && input.charAt(i) == '*' && input.charAt(i + 1) == '*') {
                if (openTag) {
                    result.append(openingTag);
                } else {
                    result.append(closingTag);
                }
                openTag = !openTag;
                i++;
            } else {
                result.append(input.charAt(i));
            }
        }

        return result.toString();
    }

    /**
     * Main method to demonstrate the formatting of Java code blocks within a text.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        String text = "<B>Creating a New Pain001 Message in Java</B><BR><BR>Since I'm a payments expert and not a programmer, I'll focus on providing an example of what a Java class might look like if it was creating a new Pain001 message, rather than actual code.<BR><BR><B>1. Introduction to ISO20022 Payments</B><BR><BR>* <B>Explanation:</B> The ISO 20022 standard is used for electronic data interchange in the financial industry.<BR>* <B>Example:</B> The Pain001 message is an example of an ISO20022 payment instruction message.<BR>* <B>Detailed Example:</B> A detailed example would be creating a new instance of the Pain001 message, specifying the necessary fields such as:<BR><BR>```java<BR>// Assuming a Java class with getters and setters for each field<BR>public class PaymentInstruction {<BR>    private String creditTransferTransactionInformation;<BR>    private String debitOrderRequestInformation;<BR><BR>    public String getCreditTransferTransactionInformation() {<BR>        return creditTransferTransactionInformation;<BR>    }<BR><BR>    public void setCreditTransferTransactionInformation(String creditTransferTransactionInformation) {<BR>        this.creditTransferTransactionInformation = creditTransferTransactionInformation;<BR>    }<BR><BR>    public String getDebitOrderRequestInformation() {<BR>        return debitOrderRequestInformation;<BR>    }<BR><BR>    public void setDebitOrderRequestInformation(String debitOrderRequestInformation) {<BR>        this.debitOrderRequestInformation = debitOrderRequestInformation;<BR>    }<BR>}<BR>```<BR><BR><B>2. Creating a New Pain001 Message</B><BR><BR>* <B>Explanation:</B> To create a new Pain001 message, you would need to initialize an instance of the PaymentInstruction class and set its relevant fields.<BR>* <B>Example:</B> Setting the credit transfer transaction information field:<BR><BR>```java<BR>PaymentInstruction payment = new PaymentInstruction();<BR>payment.setCreditTransferTransactionInformation(\"Payment instruction for John Doe\");<BR>```<BR><BR><B>3. Example Use Case</B><BR><BR>* <B>Explanation:</B> An example use case would be creating a new Pain001 message when a user initiates a payment through an online banking system.<BR>* <B>Example:</B> Creating a new instance of the PaymentInstruction class and setting its fields:<BR><BR>```java<BR>PaymentInstruction payment = new PaymentInstruction();<BR>payment.setCreditTransferTransactionInformation(\"Payment instruction for John Doe\");<BR>payment.setDebitOrderRequestInformation(\"Debit order request for Jane Smith\");<BR><BR>// Convert the PaymentInstruction object to a Pain001 message<BR>Pain001Message pain001Message = payment.toPain001Message();<BR><BR>// Send the Pain001 message to the relevant destination (e.g., bank)<BR>```<BR><BR><B>4. Note</B><BR><BR>* <B>Explanation:</B> Please note that this is a simplified example and actual implementation details may vary depending on your specific use case.<BR>* <B>Example:</B> This example assumes a basic understanding of Java classes, getters, and setters.<BR><BR>The required information is not available in the provided documents regarding actual Java code implementation for creating a new Pain001 message.";

        System.out.println(formatJavaCodeInTextToHtml(text));
    }
}
