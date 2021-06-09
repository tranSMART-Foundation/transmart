import groovyx.gpars.dataflow.DataflowVariable
import groovyx.gpars.remote.LocalHost
import groovyx.gpars.remote.LocalNode
import static java.lang.System.getenv
import java.sql.ResultSet
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import groovy.sql.Sql
import groovy.util.logging.Log


// find any unfilled biomart_user.cms_file encoded files
// where the bytea column is null
// find the file, load into PostgreSQL

def cli = new CliBuilder()
cli.h 'Loads missing file contents to biomart_user.cms_file BLOB', longOpt: 'help'
cli.usage = ''

def options = cli.parse args
if (!options) {
    Log.err 'Invalid options'
    System.exit 1
}

String url = "jdbc:postgresql://${getenv('PGHOST')}:${getenv('PGPORT')}/${getenv('PGDATABASE')}"
Connection conn = DriverManager.getConnection(url, getenv('PGUSER'), getenv('PGPASSWORD'))

List<String> fileName = new ArrayList<>()
List<String> fileContent = new ArrayList<>()
List<String> fileInstance = new ArrayList<>()

ResultSet res

// First we select all rows with null BYTES column

res = conn.createStatement().executeQuery("""SELECT NAME, CONTENT_TYPE, INSTANCE_TYPE FROM biomart_user.cms_file
					     WHERE BYTES is null""")
while(res.next()) {
    fileName << res.getObject(1)
    fileContent << res.getObject(2)
    fileInstance << res.getObject(3)
}
res.close()

// Step through arrays of filenames and load
// FILENAME and INSTANCE_TYPE is a UNIQUE key so they only return one row

fileName.eachWithIndex { it, i ->

    System.out.println "Processing NAME '${it}' INSTANCE_TYPE '${fileInstance[i]}'"
    // input file can be in current directory (data/oracle/biomart_user) for ORACLE only
    // or in /data/common/biomart_user shared with PostgreSQL

    def imgFile = new File('../../common/biomart_user/'+it)
    if(!imgFile.exists()){
	imgFile = new File(it)
    }
    if(!imgFile.exists()){
	System.out.println "ERROR: File biomart_user/${it} not found"
	System.exit 1
    }
    System.out.println "Loading cms_file ${imgFile.getPath()}"

    // Read the content of imgFile into a byte array

    InputStream inStream = imgFile.newInputStream()

    ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

    byte[] buffer = new byte[2000]
    int byteread = 0
    while((byteread = inStream.read(buffer)) != -1) {
	outBytes.write(buffer)
    }
    byte[] fileBytes = outBytes.toByteArray()
    inStream.close()
    outBytes.close()

    PreparedStatement stmt = conn.prepareStatement("UPDATE BIOMART_USER.CMS_FILE SET BYTES = ? WHERE NAME = ? AND INSTANCE_TYPE = ?")
    stmt.setBytes(1, fileBytes)
    stmt.setString(2, it)
    stmt.setString(3, fileInstance[i])
    try {
	stmt.executeUpdate()
    } catch (e) {
	System.out.println "ERROR: SQL failed: " + e.message
	System.exit 1
    }
}

