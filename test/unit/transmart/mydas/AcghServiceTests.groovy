package transmart.mydas

import grails.test.mixin.TestFor
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.acgh.AcghValues
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.querytool.QueriesResource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AcghService)
class DasServiceTests {

    @Before
    void before() {
        service.queriesResourceService = {Object[] args -> null} as QueriesResource
        List<Map> regionsProperties =
            [
                    [getId: { 1L }, getChromosome: { '1' }, getStart: { 100L }, getEnd: { 400L },
                            getNumberOfProbes: { 10 }, getLabel: { 'Region 1' }, toString: { 'Region 1' }],
                    [getId: { 2L }, getChromosome: { '2' }, getStart: { 200L }, getEnd: { 500L },
                            getNumberOfProbes: { 12 }, getLabel: { 'Region 1' }, toString: { 'Region 2' }]
            ]


        List<AssayColumn> assays = [
                [getId: {1L}, toString: { 'Assay 1' }, getLabel: { 'Assay 1' } ] as AssayColumn,
                [getId: {2L}, toString: { 'Assay 2' }, getLabel: { 'Assay 2' }] as AssayColumn
        ]

        Map<?, AcghValues> acghValuesPerAssay =
            [
                    ([assays[0], regionsProperties[0]]): ([getCopyNumberState: { CopyNumberState.GAIN }, toString: { 'ACGHValues (1 1)' }] as AcghValues),
                    ([assays[0], regionsProperties[1]]): ([getCopyNumberState: { CopyNumberState.LOSS }, toString: { 'ACGHValues (1 2)' }] as AcghValues),
                    ([assays[1], regionsProperties[0]]): ([getCopyNumberState: { CopyNumberState.NORMAL }, toString: { 'ACGHValues (2 1)' }] as AcghValues),
                    ([assays[1], regionsProperties[1]]): ([getCopyNumberState: { CopyNumberState.AMPLIFICATION }, toString: { 'ACGHValues (2 2)' }] as AcghValues)
            ]

        List<RegionRow> regionRows = [
                [
                        *:regionsProperties[0],
                        getAt: {AssayColumn assay -> acghValuesPerAssay[[assay, regionsProperties[0]]]},
                        toString: { 'RegionRow 1' }
                ] as RegionRow,
                [
                        *:regionsProperties[1],
                        getAt: {AssayColumn assay -> acghValuesPerAssay[[assay, regionsProperties[1]]]},
                        toString: { 'RegionRow 2' }
                ] as RegionRow,
        ]

        service.acghResource = [
                retrieveData: { assayConstraints, dataConstraints, projection ->
                    new TabularResult() {

                        @Override
                        List getIndicesList() {
                            assays
                        }

                        @Override
                        Iterator getRows() {
                            [
                                    regionRows[0],
                                    regionRows[1],
                            ].iterator()
                        }

                        String getColumnsDimensionLabel() {}
                        String getRowsDimensionLabel() {}
                        void close() throws IOException {}

                        @Override
                        Iterator iterator() {
                            return null
                        }
                    }
                },
                createAssayConstraint: { params, name -> },
                createDataConstraint: { params, name -> },
                createProjection: { params, name -> }
        ] as HighDimensionDataTypeResource
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
