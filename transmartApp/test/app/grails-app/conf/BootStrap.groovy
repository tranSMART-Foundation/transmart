import groovy.sql.Sql

import javax.sql.DataSource

class BootStrap {

	DataSource dataSource

	def init = {
		dropSpuriousForeignKeys()
	}

	/**
	 * Gets around the problem where there are foreign keys for multiple tables
	 * on a single column, which makes it impossible to create test data.
	 */
	private void dropSpuriousForeignKeys() {
		Sql sql = new Sql(dataSource)

		dropForeignKeys sql, 'bio_content_reference',
				'fk7f15af2e59c9a854', 'fk7f15af2ebcb2af00', 'fk7f15af2eec14b3d0'

		dropForeignKeys sql, 'bio_data_compound',
				'fkeb521f8959c9a854', 'fkeb521f89bcb2af00', 'fkeb521f89e8f5561e'

		dropForeignKeys sql, 'bio_data_correlation',
				'fk1dd38ec4669389a0', 'fk1dd38ec4d45ffa51'

		dropForeignKeys sql, 'bio_data_disease',
				'fkbef2e3be59c9a854', 'fkbef2e3bebcb2af00', 'fkbef2e3bee8f5561e', 'fkbef2e3beec14b3d0'

		dropForeignKeys sql, 'bio_data_omic_marker',
				'fk4475804317b5241a', 'fk4475804359c9a854', 'fk44758043e8f5561e', 'fk44758043fd041368'

		dropForeignKeys sql, 'bio_data_uid',
				'fk726f2f927bc70de4', 'fk726f2f92bcb2af00', 'fk726f2f92eb7c5d41', 'fk726f2f92ec14b3d0'
	}

	private void dropForeignKeys(Sql sql, String table, String... fkNames) {
		for (fkName in fkNames) {
			sql.executeUpdate 'alter table ' + table + ' drop constraint ' + fkName
		}
	}
}
