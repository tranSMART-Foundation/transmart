package uk.ac.ebi.simpledas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:17:54
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This exception should be thrown to indicate that the format
 * of the request is not recognised.  Should result in a 400 error.
 */
public class BadCommandException extends Exception{

    public BadCommandException(String message){
        super (message);
    }

    public BadCommandException(String message, Throwable cause){
        super (message, cause);
    }
}
