package uk.ac.ebi.mydas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:17:54
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This exception should be thrown to indicate that the format
 * of the request is not recognised.  Should result in a 402 error.
 */
public class BadCommandArgumentsException extends Exception{

    public BadCommandArgumentsException(String message){
        super (message);
    }

    public BadCommandArgumentsException(String message, Throwable cause){
        super (message, cause);
    }
}
