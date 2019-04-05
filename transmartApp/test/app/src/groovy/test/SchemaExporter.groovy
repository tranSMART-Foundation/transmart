package test

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.springframework.context.ApplicationContext

import java.util.regex.Matcher

/**
 * Uses the same Hibernate API (in SchemaExport) as the schema-export script to
 * programmatically export the DDL on-demand to validate domain class mapping
 * and constraints blocks.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class SchemaExporter {

	ExportedSchema export(ApplicationContext ctx) {

		File file = File.createTempFile('testddl', '.sql')
		file.deleteOnExit()
		schemaExport file, ctx
		String ddl = file.text

		ExportedSchema es = new ExportedSchema(ddl)

		String[] lines = ddl.split(';')
		for (line in lines) {
			line = line.trim()
			if (!line) continue

			if (line.startsWith('create sequence ')) {
				es << buildSequence(line)
			}
			else if (line.startsWith('create table ')) {
				es << buildTable(line)
			}
			else if (line.startsWith('alter table ')) {
				if (line.contains('add constraint')) {
					es << buildForeignKey(line)
				}
				else if (line.contains('drop constraint')) {
					// ignored
				}
			}
			else if (line.startsWith('create index ')) {
				es << buildIndex(line)
			}
			else if (line.startsWith('drop table') || line.startsWith('drop sequence')) {
				// ignored
			}
			else {
				// TODO
			}
		}

		es
	}

	private String join(String s) {
		s.readLines()*.trim().join ' '
	}

	private Sequence buildSequence(String line) {
		new Sequence(line - 'create sequence ', line)
	}

	private ForeignKey buildForeignKey(String line) {
		Matcher group = join(line) =~ /alter table (\w+) add constraint (\w+) foreign key \((\w+)\) references (\w+)/
		new ForeignKey(match(group, 1), match(group, 3), match(group, 4), match(group, 2), line)
	}

	private Table buildTable(String line) {

		List<String> lines = line.readLines()*.trim()

		assert lines[0].contains('create table ')
		String name = lines.remove(0) - 'create table ' - ' ('
		String schema
		if (name.contains('.')) {
			String[] parts = name.split('\\.')
			schema = parts[0]
			name = parts[1]
		}

		assert lines[-1] == ')'
		lines.remove lines.size() - 1

		List<String> primaryKeyColumns
		if (lines[-1].contains('primary key ')) {
			String pkColumns = lines.remove(lines.size() - 1) - 'primary key (' - ')'
			primaryKeyColumns = pkColumns.split(',').collect { String s -> s.trim().toUpperCase() }
		}
		else {
			// create table BIO_ASY_ANALYSIS_DATASET (
			// 	BIO_ASSAY_ANALYSIS_ID bigint not null,
			// 	bio_assay_dataset_id bigint
			// );
			primaryKeyColumns = Collections.emptyList()
		}

		Table table = new Table(schema, name, primaryKeyColumns, line)

		for (columnLine in lines) {
			table << buildColumn(columnLine)
		}

		table
	}

	private Column buildColumn(String line) {
		String create = line

		boolean unique = false
		boolean nullable = true
		line = line[0..-2] // remove trailing comma
		if (line.endsWith(' unique')) {
			unique = true
			line -= ' unique'
		}
		if (line.endsWith(' not null')) {
			nullable = false
			line -= ' not null'
		}
		if (line.endsWith(' null')) {
			line -= ' null'
		}

		Matcher group = line =~ /(.+?) (.+)/
		new Column(match(group, 1), match(group, 2), nullable, unique, create)
	}

	private Index buildIndex(String line) {
		Matcher group = join(line) =~ /create index (\w+) on (\w+) \((\w+)\)/
		new Index(match(group, 1), match(group, 2), match(group, 3), line)
	}

	private String match(Matcher group, int index) {
		((List<String>)group[0])[index]
	}

	@CompileDynamic
	private void schemaExport(File file, ApplicationContext ctx) {
		// not technically the SessionFactory, but the ConfigurableLocalSessionFactoryBean that
		// builds the SessionFactory and has access to the Hibernate Configuration to ensure
		// that the settings are the same as they'll be in the running app (except for environment
		// differences between dev/test/prod/etc.)
		def sessionFactory = ctx.getBean('&sessionFactory') // + datasourceSuffix)
		SchemaExport schemaExport = new SchemaExport(sessionFactory.configuration, sessionFactory.dataSource.connection)
				.setHaltOnError(true)
				.setOutputFile(file.path)
				.setDelimiter(';')
		schemaExport.execute false, false, false, false

		if (schemaExport.exceptions) {
			throw schemaExport.exceptions[0]
		}
	}
}
