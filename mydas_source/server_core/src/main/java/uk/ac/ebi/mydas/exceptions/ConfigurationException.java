package uk.ac.ebi.mydas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 08-May-2007
 * Time: 12:44:46
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * If this Exception is thrown, indicates that the problem is with the initial reading
 * of the configuration of the server and so it fatal.  Should be reported back ASAP to
 * the deployer of the servlet.
 */
public class ConfigurationException extends Exception{

    public ConfigurationException(String message){
        super(message);
    }

    public ConfigurationException(String message, Throwable cause){
        super (message, cause);
    }
}
