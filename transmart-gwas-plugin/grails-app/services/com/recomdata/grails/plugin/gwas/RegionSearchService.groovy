/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

package com.recomdata.grails.plugin.gwas

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import javax.sql.DataSource

@Slf4j('logger')
class RegionSearchService {

    static transactional = false

    @Value('${com.recomdata.gwas.usehg19table:false}')
    private boolean usehg19table

    @Autowired private DataSource dataSource

    Map getGeneLimits(Long searchId, String ver, Long flankingRegion) {

	List params = [searchId]
	String sql
	if (ver == '19') {
	    sql = geneLimitsHg19SqlQuery
        }
        else {
	    sql = geneLimitsSqlQuery
	    params << ver
	}

	def row = new Sql(dataSource).firstRow(sql, params)
	if (row) {
	    long high = row.high
	    long low = row.low
            if (flankingRegion) {
                high += flankingRegion
                low -= flankingRegion
            }
	    [low: low, high: high, chrom: row.chrom]
        }
    }

    List<String> getGenesForSnp(String snp) {
	List<String> results = []
	new Sql(dataSource).eachRow(genesForSnpQuery, [snp]) { row ->
	    results << row.BIO_MARKER_NAME
        }
	results
    }

    Map getSnpLimits(Long searchId, String ver, Long flankingRegion) {
	def row = new Sql(dataSource).firstRow(snpLimitsSqlQuery, [searchId, ver])
	if (row) {
	    long high = row.high
	    long low = row.low
            if (flankingRegion) {
                high += flankingRegion
                low -= flankingRegion
            }
	    [low: low, high: high, chrom: row.chrom]
        }
    }

    Map getAnalysisData(List<Long> analysisIds, List<Map> ranges, Long limit, Long offset,
	                Double cutoff, String sortField, String order, String search, String type,
	                List<String> geneNames, List<String> transcriptGeneNames, boolean doCount) {

	StringBuilder analysisQCriteria = new StringBuilder()
        StringBuilder queryCriteria = new StringBuilder()
        StringBuilder regionList = new StringBuilder()
	List sqlParams = []

	String countQuery
	String analysisNameQuery = analysisNameSqlQuery
	boolean hg19only = false

	Map<Long, String> analysisNameMap = [:]

	if (usehg19table) {
            if(!ranges){
                hg19only = true
            }
            else {
		hg19only = true // default to true
                for(range in ranges){
                    if(range.ver!='19'){
                        hg19only = false
                        break
                    }
                }
            }
        }

	String analysisQuery
	if (type == 'gwas') {
            analysisQuery = gwasSqlQuery
            countQuery = gwasSqlCountQuery
            if(hg19only){ // for hg19, special query
                analysisQuery = gwasHg19SqlQuery
                countQuery = gwasHg19SqlCountQuery
            }
        }
	else if (type == 'eqtl') {
            analysisQuery = eqtlSqlQuery
            countQuery = eqtlSqlCountQuery
            if(hg19only){
                analysisQuery = eqtlHg19SqlQuery
                countQuery = eqtlHg19SqlCountQuery
            }
        }
        else {
            throw new Exception('Unrecognized data type')
        }

	int rangesDone = 0

        if(ranges!=null){
            for (range in ranges) {
                if (rangesDone != 0) {
		    regionList << ' OR '
                }
                //Chromosome
                if (range.chromosome != null) {
                    if (range.low == 0 && range.high == 0) {
			regionList << "(info.chrom = '${range.chromosome}' "
                    }
                    else {
			regionList << "(info.pos >= ${range.low} AND info.pos <= ${range.high} AND info.chrom = '${range.chromosome}' "
                    }

		    if (!hg19only) {
			regionList << "  AND info.hg_version = '${range.ver}' "
                    }
		    regionList << ')'
                }
                //Gene
                else {
		    regionList << "(info.pos >= ${range.low} AND info.pos <= ${range.high} "
		    if (!hg19only) {
			regionList << "  AND info.hg_version = '${range.ver}' "
                    }
		    regionList << ')'
                }
                rangesDone++
            }
        }

        //Add analysis IDs
        if (analysisIds) {
	    analysisQCriteria << ' AND data.BIO_ASSAY_ANALYSIS_ID IN (' << analysisIds[0]
            for (int i = 1; i < analysisIds.size(); i++) {
		analysisQCriteria << ', ' << analysisIds[i]
            }
	    analysisQCriteria << ') '
	    queryCriteria << analysisQCriteria

            //Originally we only selected the analysis name if there was a need to (more than one analysis) - but this query is much faster
            analysisQuery = analysisQuery.replace('_analysisSelect_', 'DATA.bio_assay_analysis_id AS analysis_id, ')
            analysisQuery = analysisQuery.replace('_analysisJoin_', '')
        }

        //Add gene names
        if (geneNames) {
            // quick fix for hg19 only
            if(hg19only){
		queryCriteria << ' AND info.rsgene IN ('
            }
            else{
		queryCriteria << ' AND info.gene_name IN ('
            }
	    queryCriteria << "'" << geneNames[0] << "'"
            for (int i = 1; i < geneNames.size(); i++) {
		queryCriteria << ", '" << geneNames[i] << "'"
            }
	    queryCriteria << ') '
        }
	else if (type == 'eqtl' && transcriptGeneNames) {
	    queryCriteria << ' AND data.gene IN ('
	    queryCriteria << "'" << transcriptGeneNames[0] << "'"
            for (int i = 1; i < transcriptGeneNames.size(); i++) {
		queryCriteria << ", '" << transcriptGeneNames[i] << "'"
            }
	    queryCriteria << ') '
        }

        if (cutoff) {
	    queryCriteria << ' AND p_value <= ?'
        }

        if (search) {
	    queryCriteria << " AND (data.rs_id LIKE '%${search}%'"
	    queryCriteria << " OR data.ext_data LIKE '%${search}%'"
            if(hg19only){
		queryCriteria << " OR info.rsgene LIKE '%${search}%'"
            }
            else{
		queryCriteria << " OR info.gene_name LIKE '%${search}%'"
            }
	    queryCriteria << " OR info.pos LIKE '%${search}%'"
	    queryCriteria << " OR info.chrom LIKE '%${search}%'"
	    if (type == 'eqtl') {
		queryCriteria << " OR data.gene LIKE '%${search}%'"
            }
	    queryCriteria << ') '
        }

        // handle null regionlist issue
        // If no regions, default to hg19. If hg19only, we don't need to check this.
	if (!regionList) {
            if (hg19only) {
		regionList << '1=1'
            }
            else {
		regionList << "info.hg_version = '19'"
            }
        }

        analysisQuery = analysisQuery.replace('_regionlist_', regionList.toString())

        // this is really a hack
	String sortOrder = sortField?.trim()
        if(hg19only){
            sortOrder = sortOrder.replaceAll('info.gene_name', 'info.rsgene')
        }
        analysisQuery = analysisQuery.replace('_orderclause_', sortOrder + ' ' + order)
        countQuery = countQuery.replace('_regionlist_', regionList.toString())

        // analysis name query

	new Sql(dataSource).eachRow(analysisNameQuery + analysisQCriteria) { row ->
	    analysisNameMap[row.id] = row.name
        }

        // data query
	sqlParams.clear()
	String finalQuery = analysisQuery + queryCriteria + '\n) a'
        if (limit > 0) {
	    finalQuery += " where a.row_nbr between ${offset + 1} and ${offset + limit}"
        }
        if (cutoff) {
	    sqlParams << cutoff
        }

	List<List> results = []
	new Sql(dataSource).eachRow(finalQuery, sqlParams) { row ->
	    List result = [row.rsid, row.pvalue, row.logpvalue, row.extdata, analysisNameMap[row.analysis_id],
			   row.rsgene, row.chrom, row.pos, row.intronexon, row.recombinationrate, row.regulome]
	    if (type != 'gwas') {
		result << [row.gene]
            }
	    results << result
        }

        //Count - skip if we're not to do this (loading results from cache)
	long total = 0
        if (doCount) {
	    sqlParams.clear()
            if (cutoff) {
		sqlParams << cutoff
            }
	    def row = new Sql(dataSource).firstRow(countQuery + queryCriteria, sqlParams)
	    if (row) {
		total = row.TOTAL
            }
        }

	[results: results, total: total]
    }

    Map getQuickAnalysisDataByName(String analysisName, String type) {

	String quickQuery = type == 'eqtl' ? quickQueryEqtl : quickQueryGwas
	List<List> results = []
	new Sql(dataSource).eachRow(quickQuery, [analysisName]) { row ->
	    List result = [row.rsid, row.pvalue, row.logpvalue, row.extdata, row.analysis, row.rsgene,
			   row.chrom, row.pos, row.intronexon, row.recombinationrate, row.regulome]
	    if (type == 'eqtl') {
		result << [row.gene]
            }
	    results << result
        }

	[results: results]
    }

    private static final String geneLimitsSqlQuery = '''
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom
	FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN BIOMART.bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
	WHERE SEARCH_KEYWORD_ID=?
	  AND snpinfo.hg_version = ?
	'''
	
    private static final String geneLimitsHg19SqlQuery = '''
	SELECT ginfo.chrom_stop as high, ginfo.chrom_start as low, ginfo.chrom
	FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN BIOMART.bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_gene_info ginfo ON ginfo.entrez_id = bm.PRIMARY_EXTERNAL_ID
	WHERE SEARCH_KEYWORD_ID=?
	'''
	
    private static final String genesForSnpQuery = '''
	SELECT DISTINCT(GENE_NAME) as BIO_MARKER_NAME
	FROM DEAPP.DE_SNP_GENE_MAP
	WHERE SNP_NAME = ?
	'''
	
    private static final String snpLimitsSqlQuery = '''
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom
	FROM SEARCHAPP.SEARCH_KEYWORD sk
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON sk.keyword = snpinfo.rs_id
	WHERE SEARCH_KEYWORD_ID=?
	  AND snpinfo.hg_version = ?
	'''
	
    private static final String analysisNameSqlQuery = '''
	SELECT DATA.bio_assay_analysis_id as id, DATA.analysis_name as name
	FROM BIOMART.bio_assay_analysis DATA
	WHERE 1=1 
	'''

    //Query with mad Oracle pagination
    private static final String gwasSqlQuery = '''
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
	                 info.pos AS pos, info.gene_name AS rsgene,
	                 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
	                 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata,
	                 info.exon_intron as intronexon, info.recombination_rate as recombinationrate, info.regulome_score as regulome,
	                 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
	                 FROM biomart.bio_assay_analysis_gwas DATA
	                 _analysisJoin_
	                 JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
	                 WHERE 1=1
	'''

    //changed query
    private static final String gwasHg19SqlQuery = '''
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
		 info.pos AS pos, info.rsgene AS rsgene,
		 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
		 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata,
		 info.exon_intron as intronexon, info.recombination_rate as recombinationrate, info.regulome_score as regulome,
		 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
		 FROM biomart.bio_assay_analysis_gwas DATA
		 _analysisJoin_
		 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and ( _regionlist_ )
		 WHERE 1=1
	'''

    private static final String eqtlSqlQuery = '''
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
                 info.pos AS pos, info.gene_name AS rsgene,
                 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
                 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata, DATA.gene as gene,
                 info.exon_intron as intronexon, info.recombination_rate as recombinationrate, info.regulome_score as regulome,
                 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
                 FROM biomart.bio_assay_analysis_eqtl DATA
                 _analysisJoin_
                 JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
                 WHERE 1=1
	'''

    private static final String eqtlHg19SqlQuery = '''
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
		 info.pos AS pos, info.rsgene AS rsgene,
		 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
		 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata, DATA.gene as gene,
		 info.exon_intron as intronexon, info.recombination_rate as recombinationrate, info.regulome_score as regulome,
		 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
		 FROM biomart.bio_assay_analysis_eqtl DATA
		 _analysisJoin_
		 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
		 WHERE 1=1
	'''

    private static final String gwasSqlCountQuery = '''
	SELECT COUNT(*) AS TOTAL
		FROM biomart.Bio_Assay_Analysis_Gwas data 
		JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
		WHERE 1=1
	'''

    private static final String gwasHg19SqlCountQuery = '''
	SELECT COUNT(*) AS TOTAL
		FROM biomart.Bio_Assay_Analysis_Gwas data
		JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
		WHERE 1=1
	'''

    private static final String eqtlSqlCountQuery = '''
	SELECT COUNT(*) AS TOTAL
		FROM biomart.Bio_Assay_Analysis_Eqtl data
		JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
		WHERE 1=1
	'''

    private static final String eqtlHg19SqlCountQuery = '''
	SELECT COUNT(*) AS TOTAL
		FROM biomart.Bio_Assay_Analysis_Eqtl data
		JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
		WHERE 1=1
	'''

    private static final String quickQueryGwas = '''
	SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata, intronexon, recombinationrate, regulome
		FROM biomart.BIO_ASY_ANALYSIS_GWAS_TOP50
		WHERE analysis = ?
		ORDER BY logpvalue desc
	'''

    // changed ORDER BY rnum by pvalue
    private static final String quickQueryEqtl = '''
	SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata, intronexon, recombinationrate, regulome, gene
		FROM biomart.BIO_ASY_ANALYSIS_EQTL_TOP50
		WHERE analysis = ?
		ORDER BY logpvalue desc
	'''
}
