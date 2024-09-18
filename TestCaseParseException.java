/*
 * CSC 345 PROJECT
 * Class:           TestCaseParseException.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A subclass of Exception that indicates that an issue
 *                  occurred while processing the parameters of a test
 *                  case in TestCaseReader.
 */

public class TestCaseParseException extends Exception {
    /**
     * Default constructor with neither a custom message nor Throwable.
     */
    public TestCaseParseException() {
        super();
    }

    /**
     * Initializes an Exception with a custom message but with no specific
     * Throwable.
     * 
     * @param message the String message of the exception
     */
    public TestCaseParseException(String message) {
        super(message);
    }

    /**
     * Initializes an exception with both a specific Throwable and custom message.
     * 
     * @param message the String message of the exception
     * @param cause   the Throwable of the exception
     */
    public TestCaseParseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes an Exception with a specific Throwable but with no custom
     * message.
     * 
     * @param cause the Throwable of the exception
     */
    public TestCaseParseException(Throwable cause) {
        super(cause);
    }
}