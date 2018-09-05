package uk.ac.ebi.mydas.datasource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.BadCommandException;

/**
 * Date: 19-Apr-2010
 *
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * This interface should be implemented to allow the use of commands 
 * different from those in the DAS specification
 *
 */
public interface CommandExtender {
	
	/**
	 * @param request Servlet request which originated the call to this method
	 * @param response Servlet response where this method should write the adequate answer
	 * @param dataSourceConfig configuration of the data source that implemented this interface 
	 * @param command the command that haven't been recognized as any of the DAS commands
	 * @param queryString segment of the URL that contains any extra parameter that this method can use
	 * @throws BadCommandException to throw if this command is not recognized
	 * @throws DataSourceException for any error related the data source.
	 */
	public void executeOtherCommand(	HttpServletRequest request, 
									HttpServletResponse response,
									DataSourceConfiguration dataSourceConfig, 
									String command,
									String queryString) 
		throws BadCommandException, DataSourceException;
}
