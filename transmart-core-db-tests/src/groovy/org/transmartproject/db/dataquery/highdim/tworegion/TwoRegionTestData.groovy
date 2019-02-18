/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
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
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery.highdim.tworegion

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.i2b2data.PatientDimension

/**
 * @author j.hudecek
 */
@Slf4j('logger')
class TwoRegionTestData extends AbstractTestData {

	public static final String TRIAL_NAME = '2R_SAMP_TRIAL'

	private static final char MINUS = '-'
	private static final char PLUS = '+'

	DeGplInfo platform
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeTwoRegionEvent> events
	List<DeTwoRegionJunctionEvent> junctionEvents
	List<DeTwoRegionJunction> junctions
	List<DeTwoRegionEventGene> eventGenes

	TwoRegionTestData(String conceptCode = 'bogus') {
		// Create VCF platform and assays
		platform = new DeGplInfo(
				title: 'Test two region',
				organism: 'Homo Sapiens',
				markerType: 'two_region')
		platform.id = 'BOGUSGPL2R'
		patients = HighDimTestData.createTestPatients(3, -800, TRIAL_NAME)
		assays = HighDimTestData.createTestAssays(patients, -1400, platform, TRIAL_NAME, conceptCode)

		events = []
		junctions = []
		junctionEvents = []
		eventGenes = []
		//1st event: deletion assay0, chr1 2-10 - chr3 12-18 + assay0, chr10 2-10 - chr13 12-18 + assay0, chrX 2-10 - chr3 12-18
		//2nd event: deletion assay1, chrX 2-10 - chr3 12-18
		//junction without event assay1, chrY 2-10 - chr3 12-18
		DeTwoRegionEvent event = new DeTwoRegionEvent(cgaType: 'deletion')
		DeTwoRegionJunction junction = new DeTwoRegionJunction(
				downChromosome: '1',
				downPos: 2,
				downEnd: 10,
				downStrand: PLUS,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: MINUS,
				isInFrame: true,
				assay: assays[0])
		junctions << junction
		DeTwoRegionJunctionEvent junctionEvent = new DeTwoRegionJunctionEvent(
				event: event,
				junction: junction,
				pairsSpan: 10)
		junctionEvents << junctionEvent
		junction.junctionEvents = [junctionEvent] as Set

		junction = new DeTwoRegionJunction(
				downChromosome: '10',
				downPos: 2,
				downEnd: 10,
				downStrand: PLUS,
				upChromosome: '13',
				upPos: 12,
				upEnd: 18,
				upStrand: MINUS,
				isInFrame: true,
				assay: assays[0])
		junctions << junction
		junctionEvent = new DeTwoRegionJunctionEvent(
				event: event,
				junction: junction,
				pairsSpan: 10)
		junctionEvents << junctionEvent
		junction.junctionEvents = [junctionEvent] as Set
		DeTwoRegionEvent event1 = event
		events << event

		DeTwoRegionEventGene gene = new DeTwoRegionEventGene(geneId: 'TP53', effect: 'fusion')

		event = new DeTwoRegionEvent(soapClass: 'translocation', eventGenes: [gene] as Set)
		gene.event = event
		eventGenes << gene

		DeTwoRegionJunction junction2 = new DeTwoRegionJunction(
				downChromosome: 'X',
				downPos: 2,
				downEnd: 10,
				downStrand: PLUS,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: MINUS,
				isInFrame: true,
				assay: assays[1])
		junctions << junction2
		junctionEvent = new DeTwoRegionJunctionEvent(event: event, junction: junction2, pairsSpan: 10)
		junction2.junctionEvents = [junctionEvent] as Set
		junctionEvents << junctionEvent
		junctionEvent = new DeTwoRegionJunctionEvent(event: event1, junction: junction2, pairsSpan: 11)
		junctionEvents << junctionEvent
		events << event

		junctions << new DeTwoRegionJunction(
				downChromosome: 'Y',
				downPos: 2,
				downEnd: 10,
				downStrand: PLUS,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: MINUS,
				isInFrame: true,
				assay: assays[1])

		junctions << new DeTwoRegionJunction(
				downChromosome: 'Y',
				downPos: 2,
				downEnd: 10,
				downStrand: PLUS,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: MINUS,
				isInFrame: true,
				assay: assays[2])
	}

	void saveAll() {
		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll events, logger
		saveAll eventGenes, logger
		saveAll junctions, logger
		saveAll junctionEvents, logger
	}
}
