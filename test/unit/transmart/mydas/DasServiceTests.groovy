package transmart.mydas

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataQueryResource
import org.transmartproject.core.dataquery.acgh.ACGHValues
import org.transmartproject.core.dataquery.acgh.ChromosomalSegment
import org.transmartproject.core.dataquery.acgh.CopyNumberState
import org.transmartproject.core.dataquery.acgh.Region
import org.transmartproject.core.dataquery.acgh.RegionResult
import org.transmartproject.core.dataquery.acgh.RegionRow
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.constraints.ACGHRegionQuery
import org.transmartproject.core.querytool.QueriesResource


/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(DasService)
class DasServiceTests {

    @Before
    void before() {
        service.queriesResourceService = {Object[] args -> null} as QueriesResource
        service.dataQueryResourceNoGormService = new DataQueryResource() {
            RegionResult runACGHRegionQuery(ACGHRegionQuery acghRegionQuery, o) {
                new RegionResult() {

                    List<Assay> assays = [[getId: {1L}, toString: { 'Assay 1' }] as Assay, [getId: {2L}, toString: { 'Assay 2' }] as Assay]
                    List<Region> regions =
                        [
                                [getId: { 1L }, getChromosome: { '1' }, getStart: { 100L }, getEnd: { 400L }, getNumberOfProbes: { 10 }, toString: { 'Region 1' }] as Region,
                                [getId: { 2L }, getChromosome: { '2' }, getStart: { 200L }, getEnd: { 500L }, getNumberOfProbes: { 12 }, toString: { 'Region 2' }] as Region
                        ]

                    Map<?, ACGHValues> acghValuesPerAssay =
                        [
                                ([assays[0], regions[0]]): ([getCopyNumberState: { CopyNumberState.GAIN }, toString: { 'ACGHValues (1 1)' }] as ACGHValues),
                                ([assays[0], regions[1]]): ([getCopyNumberState: { CopyNumberState.LOSS }, toString: { 'ACGHValues (1 2)' }] as ACGHValues),
                                ([assays[1], regions[0]]): ([getCopyNumberState: { CopyNumberState.NORMAL }, toString: { 'ACGHValues (2 1)' }] as ACGHValues),
                                ([assays[1], regions[1]]): ([getCopyNumberState: { CopyNumberState.AMPLIFICATION }, toString: { 'ACGHValues (2 2)' }] as ACGHValues)
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

            }

            List<ChromosomalSegment> getChromosomalSegments(ACGHRegionQuery acghRegionQuery) {
                [
                        new ChromosomalSegment(chromosome: '1', start: 100, end: 400),
                        new ChromosomalSegment(chromosome: '2', start: 200, end: 500)
                ]
            }
        }
    }

    @Test
    void testGetAcghFeatures() {
        def segments = service.getAcghFeatures(10, ['2'], null, null)
        assertNotNull segments
        assertEquals 1, segments.size()
        assertEquals '2', segments[0].getSegmentId()
        assertNotNull segments[0].getFeatures()
        assertEquals service.getAcghDasTypes().size(), segments[0].getFeatures().size()
    }

    @Test
    void testGetAcghFeaturesForLoss() {
        def segments = service.getAcghFeatures(10, ['2'], null, null, [service.copyNumberStateToDasTypeMapping[CopyNumberState.LOSS]])
        assertNotNull segments
        assertEquals 1, segments.size()
        assertEquals '2', segments[0].getSegmentId()
        assertNotNull segments[0].getFeatures()
        assertEquals 1, segments[0].getFeatures().size()
        assertEquals new Double(0.5), segments[0].getFeatures()[0].getScore()
    }

    @Test
    void testGetAcghEntryPoints() {
        def entryPoints = service.getAcghEntryPoints(10)
        assertNotNull entryPoints
        assertEquals 2, entryPoints.size()
    }

}
