package transmart.mydas

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataQueryResource
import org.transmartproject.core.dataquery.acgh.ACGHValues
import org.transmartproject.core.dataquery.acgh.Region
import org.transmartproject.core.dataquery.acgh.RegionResult
import org.transmartproject.core.dataquery.acgh.RegionRow
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.querytool.QueriesResource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DasService)
class DasServiceTests {

    @Before
    void before() {
        service.queriesResourceService = {Object[] args -> null} as QueriesResource
        service.dataQueryResourceNoGormService = {Object[] args ->
            new RegionResult() {

                List<Assay> assays = [[getId: {1L}, toString: { 'Assay 1' }] as Assay, [getId: {2L}, toString: { 'Assay 2' }] as Assay]
                List<Region> regions =
                    [
                            [getId: { 1L }, getChromosome: { '1' }, getStart: { 100L }, getEnd: { 400L }, getNumberOfProbes: { 10 }, toString: { 'Region 1' }] as Region,
                            [getId: { 2L }, getChromosome: { '2' }, getStart: { 200L }, getEnd: { 500L }, getNumberOfProbes: { 12 }, toString: { 'Region 2' }] as Region
                    ]

                Map<?, ACGHValues> acghValuesPerAssay =
                    [
                            ([assays[0], regions[0]]): ([getProbabilityOfLoss: { 0.1 }, getProbabilityOfGain: { 0.4 }, toString: { 'ACGHValues (1 1)' }] as ACGHValues),
                            ([assays[0], regions[1]]): ([getProbabilityOfLoss: { 0.2 }, getProbabilityOfGain: { 0.3 }, toString: { 'ACGHValues (1 2)' }] as ACGHValues),
                            ([assays[1], regions[0]]): ([getProbabilityOfLoss: { 0.3 }, getProbabilityOfGain: { 0.2 }, toString: { 'ACGHValues (2 1)' }] as ACGHValues),
                            ([assays[1], regions[1]]): ([getProbabilityOfLoss: { 0.4 }, getProbabilityOfGain: { 0.1 }, toString: { 'ACGHValues (2 2)' }] as ACGHValues)
                    ]

                List<Assay> getIndicesList() {
                    assays
                }

                Iterator<RegionRow> getRows() {
                    [
                            [
                                    getRegion: { regions[0] },
                                    getRegionDataForAssay: {Assay assay -> acghValuesPerAssay[[assay, regions[0]]]},
                                    toString: { 'RegionRow 1' }
                            ] as RegionRow,
                            [
                                    getRegion: { regions[1] },
                                    getRegionDataForAssay: {Assay assay -> acghValuesPerAssay[[assay, regions[1]]]},
                                    toString: { 'RegionRow 2' }
                            ] as RegionRow,
                    ].iterator()
                }

                void close() {}
            }
        } as DataQueryResource
    }

    @Test
    void testGetAcghFeatures() {
        def segment = service.getAcghFeatures('STUDY_ID', 10, ['2'])
        assertNotNull segment
        assertNotNull '2', segment.segmentId
        assertNotNull segment.features
        assertEquals 1, segment.features.size()
    }
}
