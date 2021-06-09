import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.remote.LocalHost
import groovyx.gpars.remote.LocalNode
import oracle.sql.BLOB
import oracle.jdbc.OracleResultSet
import java.sql.ResultSet
import java.sql.Connection
import java.sql.PreparedStatement
import groovy.sql.Sql
import inc.oracle.SqlProducer
import inc.oracle.Log

// find any unfilled biomart_user.cms_file encoded files
// where the BLOB is null
// update to EMPTY_BLOB so object is writable
// find the file, load into Oracle

def cli = new CliBuilder()
cli.h 'Loads missing file contents to biomart_user.cms_file BLOB', longOpt: 'help'
cli.usage = ''

def options = cli.parse args
if (!options) {
    Log.err 'Invalid options'
    System.exit 1
}

// This is set with autocommit = false
Sql sql = SqlProducer.createFromEnv()

List<String> fileName = new ArrayList<>()
List<String> fileContent = new ArrayList<>()
List<String> fileInstance = new ArrayList<>()

// New rows have null values for BYTES column
// Needs to be EMPTY_BLOB() for updating

sql.executeQuery """UPDATE biomart_user.cms_file
               SET BYTES = EMPTY_BLOB()
               WHERE BYTES IS null"""
sql.commit()

// Now we can select all rows with EMPTY_BLOB

sql.eachRow """SELECT NAME, CONTENT_TYPE, INSTANCE_TYPE FROM biomart_user.cms_file
               WHERE LENGTH(BYTES) = 0""",
{
    fileName << it['NAME']
    fileContent << it['CONTENT_TYPE']
    fileInstance << it['INSTANCE_TYPE']
}

// Step through arrays of filenames and load
// FILENAME and INSTANCE_TYPE is a UNIQUE key

ResultSet res

fileName.eachWithIndex { it, i ->

    Connection conn = sql.getConnection()
    PreparedStatement stmt = conn.prepareStatement("SELECT BYTES FROM BIOMART_USER.CMS_FILE WHERE NAME = ? AND INSTANCE_TYPE = ? FOR UPDATE",
						   ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)
    stmt.setString(1, it)
    stmt.setString(2, fileInstance[i])
    try {
	res = stmt.executeQuery()
    } catch (e) { Log.err "SQL failed: " + e.message }

    // res has selected row

    if(res.next()){

	// input file can be in current directory (data/oracle/biomart_user) for ORACLE only
	// or in /data/common/biomart_user shared with PostgreSQL

	def blobFile = new File('../../common/biomart_user/'+it)
	if(!blobFile.exists()){
	    blobFile = new File(it)
	}
	if(!blobFile.exists()){
	    Log.err "File biomart_user/${it} not found"
	    System.exit 1
	}
	Log.out "Loading cms_file ${blobFile.getPath()}"
	InputStream inStream = blobFile.newInputStream()

	// Output BLOB column opened with output stream

	BLOB blob = (BLOB) res.getBLOB("BYTES")
	OutputStream outStream = new BufferedOutputStream(blob.setBinaryStream(1L))

	// write buffers from input file to BLOB until done
	// needs autocommit off to avoid premature commit after first (partial) write
	
	byte[] buffer = new byte[blob.getBufferSize()]
	int byteread = 0
	while((byteread = inStream.read(buffer)) != -1) {
	    outStream.write(buffer, 0, byteread)
	}

	// close input and output streams for this row

	outStream.close()
	inStream.close()
    }

    // All done. Close results
    // Commit all updates - autocommit is off by default for connections

    res.close()
    sql.commit()
}

