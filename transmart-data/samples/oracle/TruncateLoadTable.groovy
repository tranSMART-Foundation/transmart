/*
 * Copyright 2021 Axiomedix Inc.
 *
 * This file is part of transmart-data.
 *
 * Transmart-data is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-data.  If not, see <http://www.gnu.org/licenses/>.
 */

import groovy.sql.Sql
import DatabaseConnection

def parseOptions() {
  def cli = new CliBuilder(usage: "TruncateLoadTable.groovy -t table")
  cli.t('which table', required: true, longOpt: "table", args: 1)
  def options = cli.parse(args)
  options
}


def truncateTable(options) {
    String stmt = "TRUNCATE TABLE ${options.table}"
   println "Executing ${stmt}"
  sql = DatabaseConnection.setupDatabaseConnection()
    sql.execute(stmt)
  sql.close()
}

options = parseOptions()
if (!options) {
	System.exit 1
}

truncateTable(options)

