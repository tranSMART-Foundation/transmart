package uk.ac.ebi.mydas.exceptions;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:17:54
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This exception should be thrown to indicate that there is
 * something wrong with the data source (i.e. IOException, SQLException
 * or similar).
 */
public class DataSourceException extends Exception{

    public DataSourceException(String message){
        super (message);
    }

    public DataSourceException (String message, Throwable cause){
        super (message, cause);
    }
}
