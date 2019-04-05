import com.recomdata.util.ariadne.Attr
import com.recomdata.util.ariadne.Batch
import com.recomdata.util.ariadne.Control
import com.recomdata.util.ariadne.Controls
import com.recomdata.util.ariadne.Link
import com.recomdata.util.ariadne.Node
import com.recomdata.util.ariadne.Nodes
import com.recomdata.util.ariadne.Properties
import com.recomdata.util.ariadne.Resnet
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.SearchFilter
import org.transmart.SearchResult

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * ResNetService that will provide an .rnef file for Jubilant data
 *
 * @author mmcduffie
 */
@Slf4j('logger')
class JubilantResNetService {

    private static final int RLIMIT = 10000

    static transactional = false

    SearchFilter searchFilter // Passed from the JubilantController TODO _not_ threadsafe
    Set misses = [] // the proteins that could not be found TODO _not_ threadsafe

    @Autowired private LiteratureQueryService literatureQueryService

    Batch createResNet() {
	misses.clear() // Clear the misses with each run

	SearchResult sResult = new SearchResult()
        Batch batch = null

        if (literatureQueryService.litJubOncIntCount(searchFilter) > 0) {
            sResult.result = literatureQueryService.litJubOncIntData(searchFilter, null)
            batch = new Batch()
	    batch.resnet << processInteractionData(sResult, 'Onc')
        }

        if (literatureQueryService.litJubAsthmaIntCount(searchFilter) > 0) {
            sResult.result = literatureQueryService.litJubAsthmaIntData(searchFilter, null)
            if (batch == null) {
                batch = new Batch()
            }
	    batch.resnet << processInteractionData(sResult, 'Asthma')
        }

        if (literatureQueryService.litJubOncAltCount(searchFilter) > 0) {
            sResult.result = literatureQueryService.litJubOncAltData(searchFilter, null)
            if (batch == null) {
                batch = new Batch()
            }
	    batch.resnet << processAlterationData(sResult, 'Onc')
        }

        if (literatureQueryService.litJubAsthmaAltCount(searchFilter) > 0) {
            sResult.result = literatureQueryService.litJubAsthmaAltData(searchFilter, null)
            if (batch == null) {
                batch = new Batch()
            }
	    batch.resnet << processAlterationData(sResult, 'Asthma')
        }

        if (literatureQueryService.litJubOncInhCount(searchFilter) > 0) {
            sResult.result = literatureQueryService.litJubOncInhData(searchFilter, null)
            if (batch == null) {
                batch = new Batch()
            }
	    batch.resnet << processInhibitorData(sResult, 'Onc')
        }

	batch
    }

    Resnet processInteractionData(SearchResult searchResults, String domain) {
	Set<String> intMisses = [] // Interaction misses
	Map<String, Node> nodeMap = [:] // Map of nodes where we have already found the URN
	Map refCountMap = [:] // Map of ref counts for the index attribute
	Random random = new Random()

	Resnet resnet = new Resnet(
	    name: searchFilter.searchText + '-Int-' + domain + '-' + new Date(),
	    type: 'Pathway')

        Properties pResNet = new Properties()
	pResNet.attr << new Attr(name: 'Source', value: 'Jubilant')

        Nodes nodes = new Nodes()
        Controls controls = new Controls()

        for (searchResult in searchResults.result) {
            if (searchResult.sourceComponent != null && searchResult.targetComponent != null) {
                String source = searchResult.sourceComponent.trim()
                String target = searchResult.targetComponent.trim()
                if (intMisses.contains(source) || intMisses.contains(target)) {
		    logger.info 'Source: {} or target: {} is missing...', source, target
                    continue
                }

		Node sourceNode
                if (nodeMap.containsKey(source)) {
		    sourceNode = nodeMap[source]
                }
                else {
		    String srcURN = literatureQueryService.findGeneURN(source)
                    if (srcURN == null) {
			logger.info 'Missed Source: {}', source
			intMisses << source
                        continue
                    }

		    sourceNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: srcURN)
		    sourceNode.attr << new Attr(name: 'NodeType', value: 'Protein')
		    sourceNode.attr << new Attr(name: 'Name', value: source)
		    nodeMap[source] = sourceNode
		    nodes.node << sourceNode
                }

		Node targetNode
                if (nodeMap.containsKey(target)) {
		    targetNode = nodeMap[target]
                }
                else {
		    String tgtURN = literatureQueryService.findGeneURN(target)
                    if (tgtURN == null) {
			logger.info 'Missed Target: {}', target
			intMisses << target
                        continue
                    }

		    targetNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: tgtURN)
		    targetNode.attr << new Attr(name: 'NodeType', value: 'Protein')
		    targetNode.attr << new Attr(name: 'Name', value: target)
		    nodeMap[target] = targetNode
		    nodes.node << targetNode
                }

                boolean isBinding = false

		Control control = new Control(localId: 'L' + +random.nextInt(RLIMIT))

                if (searchResult.mechanism != null) {
		    if ('Complex'.equalsIgnoreCase(searchResult.mechanism) ||
			'Interaction'.equalsIgnoreCase(searchResult.mechanism) ||
			'Dissociation'.equalsIgnoreCase(searchResult.mechanism) ||
			'Binding'.equalsIgnoreCase(searchResult.mechanism) ||
			'Association'.equalsIgnoreCase(searchResult.mechanism) ||
			'Heterodimerization'.equalsIgnoreCase(searchResult.mechanism) ||
			'Recruitment'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Binding'
                        isBinding = true
                    }
		    else if ('Degradation'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Expression'
			createAtt control, 'Effect', 'negative'
			createAtt control, 'Mechanism', 'Degradation'
		    }
		    else if ('Accumulation'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Expression'
			createAtt control, 'Effect', 'positive'
			createAtt control, 'Mechanism', 'Accumulation'
		    }
		    else if ('Stabilization'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Expression'
			createAtt control, 'Effect', 'positive'
			createAtt control, 'Mechanism', 'Stabilization'
                    }
		    else if ('Downregulation'.equalsIgnoreCase(searchResult.mechanism) ||
			     'Inhibition'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Regulation'
			createAtt control, 'Effect', 'negative'
                    }
		    else if ('Activation'.equalsIgnoreCase(searchResult.mechanism) ||
			     'Assembly'.equalsIgnoreCase(searchResult.mechanism) ||
			     'Upregulation'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Regulation'
			createAtt control, 'Effect', 'positive'
                    }
		    else if ('Cleavage'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Expression'
			createAtt control, 'Effect', 'negative'
			createAtt control, 'Mechanism', 'Cleavage'
                    }
		    else if ('Synthesis'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'MolSynthesis'
                    }
		    else if ('Translocation'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'MolTransport'
                    }
		    else if ('Release'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'MolTransport'
			createAtt control, 'Mechanism', 'Release'
                    }
		    else if ('Secretion'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'MolTransport'
			createAtt control, 'Mechanism', 'Secretion'
                    }
		    else if ('Splicing'.equalsIgnoreCase(searchResult.mechanism)) {
			createAtt control, 'ControlType', 'Expression'
			createAtt control, 'Mechanism', 'Splicing'
                    }
                    else {
			logger.warn 'Unknown interaction mechanism found: {}', searchResult.mechanism
			createAtt control, 'ControlType', searchResult.mechanism
                    }
                }
                else {
		    logger.warn 'Interaction mechanism is null, mapping relationship type to unknown'
		    createAtt control, 'ControlType', 'Unknown'
                }

                if (isBinding) {
		    control.link << new Link(type: 'in-out', ref: sourceNode.localId)
		    control.link << new Link(type: 'in-out', ref: targetNode.localId)
		}
		else {
		    control.link << new Link(type: 'in', ref: sourceNode.localId)
		    control.link << new Link(type: 'out', ref: targetNode.localId)
		}

		createAtt control, 'X-Mode', searchResult.interactionMode
		createAtt control, 'X-Region', searchResult.region
		createAtt control, 'X-Regulation', searchResult.regulation
		createAtt control, 'X-Effect', searchResult.effect
		createAtt control, 'X-Technique', searchResult.techniques
		createAtt control, 'X-Localization', searchResult.localization

		int refCount
                if (!refCountMap.containsKey(searchResult.reference.referenceId)) {
                    refCount = 1
		    refCountMap[searchResult.reference.referenceId] = refCount
                }
                else {
		    refCount = refCountMap[searchResult.reference.referenceId]
                    refCount++
		    refCountMap[searchResult.reference.referenceId] = refCount
                }
		createAtt control, 'mref', searchResult.reference.referenceId, refCount
		createAtt control, 'msrc', searchResult.reference.referenceTitle, refCount
		createAtt control, 'Disease', searchResult.reference.disease
		createAtt control, 'X-Disease Site', searchResult.reference.diseaseSite
		createAtt control, 'X-Disease Types', searchResult.reference.diseaseTypes
		createAtt control, 'X-Disease Stage', searchResult.reference.diseaseStage
		createAtt control, 'X-Disease Description', searchResult.reference.diseaseDescription
		createAtt control, 'X-Physiology', searchResult.reference.physiology
		createAtt control, 'X-Clinical Statistics', searchResult.reference.statClinical
		createAtt control, 'X-Clinical Correlation', searchResult.reference.statClinicalCorrelation
		createAtt control, 'X-Statistical Tests', searchResult.reference.statTests
		createAtt control, 'X-Coefficient', searchResult.reference.statCoefficient
		createAtt control, 'X-P Value', searchResult.reference.statPValue
		createAtt control, 'X-Statistical Description', searchResult.reference.statDescription

                if (searchResult.inVivoModel != null) {
		    createAtt control, 'X-In Vivo Model Type', searchResult.inVivoModel.modelType
		    createAtt control, 'X-In Vivo Description', searchResult.inVivoModel.description
		    createAtt control, 'X-In Vivo Stimulation', searchResult.inVivoModel.stimulation
		    createAtt control, 'X-In Vivo Control Challenge', searchResult.inVivoModel.controlChallenge
		    createAtt control, 'X-In Vivo Challenge', searchResult.inVivoModel.challenge
		    createAtt control, 'X-In Vivo Sentization', searchResult.inVivoModel.sentization
		    createAtt control, 'X-In Vivo Zygosity', searchResult.inVivoModel.zygosity
		    createAtt control, 'X-In Vivo Experimental Model', searchResult.inVivoModel.experimentalModel
		    createAtt control, 'X-In Vivo Animal Wild Type', searchResult.inVivoModel.animalWildType
		    createAtt control, 'X-In Vivo Tissue', searchResult.inVivoModel.tissue
		    createAtt control, 'X-In Vivo Cell Type', searchResult.inVivoModel.cellType

                    if (searchResult.inVivoModel.cellLine != null) {
                        String fullCellLine = searchResult.inVivoModel.cellLine
                        int count = 1
			for (String cellLine in fullCellLine.split(',')) {
			    createAtt control, 'X-In Vivo Cell Line ' + count, cellLine
                            count++
                        }
                    }
		    createAtt control, 'X-In Vivo Body Substance', searchResult.inVivoModel.bodySubstance
                }

                if (searchResult.inVitroModel != null) {
		    createAtt control, 'X-In Vitro Model Type', searchResult.inVitroModel.modelType
		    createAtt control, 'X-In Vitro Description', searchResult.inVitroModel.description
		    createAtt control, 'X-In Vitro Stimulation', searchResult.inVitroModel.stimulation
		    createAtt control, 'X-In Vitro Control Challenge', searchResult.inVitroModel.controlChallenge
		    createAtt control, 'X-In Vitro Challenge', searchResult.inVitroModel.challenge
		    createAtt control, 'X-In Vitro Sentization', searchResult.inVitroModel.sentization
		    createAtt control, 'X-In Vitro Zygosity', searchResult.inVitroModel.zygosity
		    createAtt control, 'X-In Vitro Experimental Model', searchResult.inVitroModel.experimentalModel
		    createAtt control, 'X-In Vitro Animal Wild Type', searchResult.inVitroModel.animalWildType
		    createAtt control, 'X-In Vitro Tissue', searchResult.inVitroModel.tissue
		    createAtt control, 'X-In Vitro Cell Type', searchResult.inVitroModel.cellType
                    if (searchResult.inVitroModel.cellLine != null) {
                        String fullCellLine = searchResult.inVitroModel.cellLine
                        int count = 1
			for (cellLine in fullCellLine.split(',')) {
			    createAtt control, 'X-In Vitro Cell Line ' + count, cellLine
                            count++
                        }
                    }
		    createAtt control, 'X-In Vitro Body Substance', searchResult.inVitroModel.bodySubstance
                }
		controls.control << control
            }
        }

	misses.addAll intMisses

	pResNet.attr << new Attr(name: 'Notes', value: intMisses.toString())

	resnet.setProperties pResNet
	resnet.nodes = nodes
	resnet.controls = controls

	resnet
    }

    Resnet processAlterationData(SearchResult searchResults, String domain) {
	Set<String> altMisses = [] // Alteration misses
	Map<String, Node> nodeMap = [:] // Map of nodes where we have already found the URN
	Map refCountMap = [:] // Map of ref counts for the index attribute
	Random random = new Random()

        Resnet resnet = new Resnet()
        resnet.setName(searchFilter.searchText + '-Alt-' + domain + '-' + new Date())
        resnet.setType('Pathway')

        Properties pResNet = new Properties()
	pResNet.attr << new Attr(name: 'Source', value: 'Jubilant')

        Nodes nodes = new Nodes()
        Controls controls = new Controls()

        for (searchResult in searchResults.result) {
            if (searchResult.reference != null) {
                if (searchResult.reference.component != null && searchResult.reference.disease != null) {
                    String component = searchResult.reference.component.trim()
                    String disease = searchResult.reference.disease.trim()
                    if (altMisses.contains(component) || altMisses.contains(disease)) {
			logger.info 'Component: {} or disease: {} is missing...', component, disease
                        continue
                    }

		    Node componentNode
                    String cmpURN = ''

                    if (nodeMap.containsKey(component)) {
			componentNode = nodeMap[component]
                    }
                    else {
                        cmpURN = literatureQueryService.findGeneURN(component)
                        if (cmpURN == null) {
			    logger.info 'Missed Component: {}', component
			    altMisses << component
                            continue
                        }

			componentNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: cmpURN)
			componentNode.attr << new Attr(name: 'NodeType', value: 'Protein')
			componentNode.attr << new Attr(name: 'Name', value: component)
			nodeMap[component] = componentNode
			nodes.node << componentNode
                    }

		    Node diseaseNode
                    if (nodeMap.containsKey(disease)) {
			diseaseNode = nodeMap[disease]
                    }
                    else {
			String disURN = null
                        if (searchResult.reference.diseaseMesh != null) {
                            disURN = literatureQueryService.findDiseaseURN(searchResult.reference.diseaseMesh.trim())
                        }
                        else {
			    logger.info 'DiseaseMesh is null, trying disease...'
                        }
                        if (disURN == null) {
                            disURN = literatureQueryService.findDiseaseURN(disease)
                            if (disURN == null) {
				logger.info 'Missed Disease: {}', disease
				altMisses << disease
                                continue
                            }
                        }

			diseaseNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: disURN)

			diseaseNode.attr << new Attr(name: 'NodeType', value: 'Disease')
			diseaseNode.attr << new Attr(name: 'Name', value: disease)
			nodeMap[disease] = diseaseNode
			nodes.node << diseaseNode
                    }

		    Control control = new Control(localId: 'L' + +random.nextInt(RLIMIT))

                    if (searchResult.alterationType != null) {
			createAtt control, 'ControlType', 'StateChange'
			if ('Expression'.equalsIgnoreCase(searchResult.alterationType) ||
			    'Epigenetic Event'.equalsIgnoreCase(searchResult.alterationType) ||
			    'Mutation'.equalsIgnoreCase(searchResult.alterationType)) {
			    createAtt control, 'Mechanism', searchResult.alterationType
			}
			else if ('Genomic Level Change'.equalsIgnoreCase(searchResult.alterationType)) {
			    createAtt control, 'Mechanism', searchResult.alterationType
			    createAtt control, 'Effect', 'unknown'
			}
			else if ('Gene Amplification'.equalsIgnoreCase(searchResult.alterationType)) {
			    createAtt control, 'Mechanism', searchResult.alterationType
			    createAtt control, 'Effect', 'positive'
			}
			else if ('LOH'.equalsIgnoreCase(searchResult.alterationType)) {
			    createAtt control, 'Mechanism', 'Loss of heterozygosity'
			}
			else if ('PTM'.equalsIgnoreCase(searchResult.alterationType)) {
			    createAtt control, 'Mechanism', 'Posttranslational modification'
			}
			else {
			    logger.warn 'Unknown alteration type: {}', searchResult.alterationType
			    createAtt control, 'ControlType', searchResult.alterationType
			}
		    }
		    else {
			logger.warn 'Alteration type is null, mapping relationship type to unknown'
			createAtt control, 'ControlType', 'Unknown'
		    }

		    control.link << new Link(type: 'in', ref: diseaseNode.localId)

		    createAtt control, 'X-Control', searchResult.control
		    createAtt control, 'X-Effect', searchResult.effect
		    createAtt control, 'X-Description', searchResult.description
		    createAtt control, 'X-Techniques', searchResult.techniques
		    createAtt control, 'X-PatientsPercent', searchResult.patientsPercent
		    createAtt control, 'X-PatientsNumber', searchResult.patientsNumber
		    createAtt control, 'X-PopNumber', searchResult.popNumber
		    createAtt control, 'X-PopIncCriteria', searchResult.popInclusionCriteria
		    createAtt control, 'X-PopExcCriteria', searchResult.popExclusionCriteria
		    createAtt control, 'X-PopDescription', searchResult.popDescription
		    createAtt control, 'X-PopType', searchResult.popType
		    createAtt control, 'X-PopValue', searchResult.popValue
		    createAtt control, 'X-PopPhase', searchResult.popPhase
		    createAtt control, 'X-PopStatus', searchResult.popStatus
		    createAtt control, 'X-PopExpModel', searchResult.popExperimentalModel
		    createAtt control, 'X-PopTissue', searchResult.popTissue
		    createAtt control, 'X-PopBodySubstance', searchResult.popBodySubstance
		    createAtt control, 'X-PopLocalization', searchResult.popLocalization
		    createAtt control, 'X-PopCellType', searchResult.popCellType
		    createAtt control, 'X-ClinSubmucosaMarkerType', searchResult.clinSubmucosaMarkerType
		    createAtt control, 'X-ClinSubmucosaUnit', searchResult.clinSubmucosaUnit
		    createAtt control, 'X-ClinSubmucosaValue', searchResult.clinSubmucosaValue
		    createAtt control, 'X-ClinAsmMarkerType', searchResult.clinAsmMarkerType
		    createAtt control, 'X-ClinAsmUnit', searchResult.clinAsmUnit
		    createAtt control, 'X-ClinAsmValue', searchResult.clinAsmValue
		    createAtt control, 'X-ClinCellularSource', searchResult.clinCellularSource
		    createAtt control, 'X-ClinCellularType', searchResult.clinCellularType
		    createAtt control, 'X-ClinCellularCount', searchResult.clinCellularCount
		    createAtt control, 'X-ClinPriorMedPercent', searchResult.clinPriorMedPercent
		    createAtt control, 'X-ClinPriorMedDose', searchResult.clinPriorMedDose
		    createAtt control, 'X-ClinPriorMedName', searchResult.clinPriorMedName
		    createAtt control, 'X-ClinBaselineVariable', searchResult.clinBaselineVariable
		    createAtt control, 'X-ClinBaselinePercent', searchResult.clinBaselinePercent
		    createAtt control, 'X-ClinBaselineValue', searchResult.clinBaselineValue
		    createAtt control, 'X-ClinSmoker', searchResult.clinSmoker
		    createAtt control, 'X-ClinAtopy', searchResult.clinAtopy
		    createAtt control, 'X-ControlExpPercent', searchResult.controlExpPercent
		    createAtt control, 'X-ControlExpNumber', searchResult.controlExpNumber
		    createAtt control, 'X-ControlExpValue', searchResult.controlExpValue
		    createAtt control, 'X-ControlExpSd', searchResult.controlExpSd
		    createAtt control, 'X-ControlExpUnit', searchResult.controlExpUnit
		    createAtt control, 'X-OverExpPercent', searchResult.overExpPercent
		    createAtt control, 'X-OverExpNumber', searchResult.overExpNumber
		    createAtt control, 'X-OverExpValue', searchResult.overExpValue
		    createAtt control, 'X-OverExpSd', searchResult.overExpSd
		    createAtt control, 'X-OverExpUnit', searchResult.overExpUnit
		    createAtt control, 'X-LossExpPercent', searchResult.lossExpPercent
		    createAtt control, 'X-LossExpNumber', searchResult.lossExpNumber
		    createAtt control, 'X-LossExpValue', searchResult.lossExpValue
		    createAtt control, 'X-LossExpSd', searchResult.lossExpSd
		    createAtt control, 'X-LossExpUnit', searchResult.lossExpUnit
		    createAtt control, 'X-TotalExpPercent', searchResult.totalExpPercent
		    createAtt control, 'X-TotalExpNumber', searchResult.totalExpNumber
		    createAtt control, 'X-TotalExpValue', searchResult.totalExpValue
		    createAtt control, 'X-TotalExpSd', searchResult.totalExpSd
		    createAtt control, 'X-TotalExpUnit', searchResult.totalExpUnit
		    createAtt control, 'X-GlcControlPercent', searchResult.glcControlPercent
		    createAtt control, 'X-GlcMolecularChange', searchResult.glcMolecularChange
		    createAtt control, 'X-GlcType', searchResult.glcType
		    createAtt control, 'X-GlcPercent', searchResult.glcPercent
		    createAtt control, 'X-GlcNumber', searchResult.glcNumber
		    createAtt control, 'X-PtmRegion', searchResult.ptmRegion
		    createAtt control, 'X-PtmType', searchResult.ptmType
		    createAtt control, 'X-PtmChange', searchResult.ptmChange
		    createAtt control, 'X-LohLoci', searchResult.lohLoci
		    createAtt control, 'X-MutationType', searchResult.mutationType
		    createAtt control, 'X-MutationChange', searchResult.mutationChange
		    createAtt control, 'X-MutationSites', searchResult.mutationSites
		    createAtt control, 'X-EpigeneticRegion', searchResult.epigeneticRegion
		    createAtt control, 'X-EpigeneticType', searchResult.epigeneticType

		    int refCount
                    if (!refCountMap.containsKey(searchResult.reference.referenceId)) {
                        refCount = 1
			refCountMap[searchResult.reference.referenceId] = refCount
                    }
                    else {
			refCount = refCountMap[searchResult.reference.referenceId]
                        refCount++
			refCountMap[searchResult.reference.referenceId] = refCount
                    }
		    createAtt control, 'mref', searchResult.reference.referenceId, refCount
		    createAtt control, 'msrc', searchResult.reference.referenceTitle, refCount
		    createAtt control, 'Disease', searchResult.reference.disease
		    createAtt control, 'X-Disease Site', searchResult.reference.diseaseSite
		    createAtt control, 'X-Disease Types', searchResult.reference.diseaseTypes
		    createAtt control, 'X-Disease Stage', searchResult.reference.diseaseStage
		    createAtt control, 'X-Disease Description', searchResult.reference.diseaseDescription
		    createAtt control, 'X-Physiology', searchResult.reference.physiology
		    createAtt control, 'X-Clinical Statistics', searchResult.reference.statClinical
		    createAtt control, 'X-Clinical Correlation', searchResult.reference.statClinicalCorrelation
		    createAtt control, 'X-Statistical Tests', searchResult.reference.statTests
		    createAtt control, 'X-Coefficient', searchResult.reference.statCoefficient
		    createAtt control, 'X-P Value', searchResult.reference.statPValue
		    createAtt control, 'X-Statistical Description', searchResult.reference.statDescription

                    if (searchResult.inVivoModel != null) {
			createAtt control, 'X-In Vivo Model Type', searchResult.inVivoModel.modelType
			createAtt control, 'X-In Vivo Description', searchResult.inVivoModel.description
			createAtt control, 'X-In Vivo Stimulation', searchResult.inVivoModel.stimulation
			createAtt control, 'X-In Vivo Control Challenge', searchResult.inVivoModel.controlChallenge
			createAtt control, 'X-In Vivo Challenge', searchResult.inVivoModel.challenge
			createAtt control, 'X-In Vivo Sentization', searchResult.inVivoModel.sentization
			createAtt control, 'X-In Vivo Zygosity', searchResult.inVivoModel.zygosity
			createAtt control, 'X-In Vivo Experimental Model', searchResult.inVivoModel.experimentalModel
			createAtt control, 'X-In Vivo Animal Wild Type', searchResult.inVivoModel.animalWildType
			createAtt control, 'X-In Vivo Tissue', searchResult.inVivoModel.tissue
			createAtt control, 'X-In Vivo Cell Type', searchResult.inVivoModel.cellType

                        if (searchResult.inVivoModel.cellLine != null) {
                            String fullCellLine = searchResult.inVivoModel.cellLine
			    String[] cellLineArray = fullCellLine.split(',')
                            int count = 1
                            for (cellLine in cellLineArray) {
				createAtt control, 'X-In Vivo Cell Line ' + count, cellLine
                                count++
                            }
                        }
			createAtt control, 'X-In Vivo Body Substance', searchResult.inVivoModel.bodySubstance
                    }

                    if (searchResult.inVitroModel != null) {
			createAtt control, 'X-In Vitro Model Type', searchResult.inVitroModel.modelType
			createAtt control, 'X-In Vitro Description', searchResult.inVitroModel.description
			createAtt control, 'X-In Vitro Stimulation', searchResult.inVitroModel.stimulation
			createAtt control, 'X-In Vitro Control Challenge', searchResult.inVitroModel.controlChallenge
			createAtt control, 'X-In Vitro Challenge', searchResult.inVitroModel.challenge
			createAtt control, 'X-In Vitro Sentization', searchResult.inVitroModel.sentization
			createAtt control, 'X-In Vitro Zygosity', searchResult.inVitroModel.zygosity
			createAtt control, 'X-In Vitro Experimental Model', searchResult.inVitroModel.experimentalModel
			createAtt control, 'X-In Vitro Animal Wild Type', searchResult.inVitroModel.animalWildType
			createAtt control, 'X-In Vitro Tissue', searchResult.inVitroModel.tissue
			createAtt control, 'X-In Vitro Cell Type', searchResult.inVitroModel.cellType
                        if (searchResult.inVitroModel.cellLine != null) {
                            String fullCellLine = searchResult.inVitroModel.cellLine
			    String[] cellLineArray = fullCellLine.split(',')
                            int count = 1
                            for (cellLine in cellLineArray) {
				createAtt control, 'X-In Vitro Cell Line ' + count, cellLine
                                count++
                            }
                        }
			createAtt control, 'X-In Vitro Body Substance', searchResult.inVitroModel.bodySubstance
                    }
		    controls.control << control
                }
            }
        }

	misses.addAll altMisses

	pResNet.attr << new Attr(name: 'pResNet.attr', value: altMisses.toString())

	resnet.setProperties pResNet
	resnet.nodes = nodes
	resnet.controls = controls

	resnet
    }

    Resnet processInhibitorData(SearchResult searchResults, String domain) {
	Set<String> inhMisses = [] // Inihibitor misses
	Map<String, Node> nodeMap = [:] // Map of nodes where we have already found the URN
	Map refCountMap = [:] // Map of ref counts for the index attribute
	Random random = new Random()

	Resnet resnet = new Resnet(name: searchFilter.searchText + '-Inh-' + domain + '-' + new Date(), type: 'Pathway')

        Properties pResNet = new Properties()
	pResNet.attr << new Attr(name: 'Source', value: 'Jubilant')

        Nodes nodes = new Nodes()
        Controls controls = new Controls()

        for (searchResult in searchResults.result) {
            if (searchResult.reference != null) {
                if (searchResult.inhibitor != null && searchResult.reference.component != null) {
                    String inhibitor = searchResult.inhibitor.trim()
                    String component = searchResult.reference.component.trim()
                    if (inhMisses.contains(inhibitor) || inhMisses.contains(component)) {
			logger.info 'Inhibitor: {} or component: {} is missing...', inhibitor, component
                        continue
                    }

		    Node inhibitorNode
                    if (nodeMap.containsKey(inhibitor)) {
			inhibitorNode = nodeMap[inhibitor]
                    }
                    else {
			String inhURN = literatureQueryService.findSmallMolURN(inhibitor)
                        if (inhURN == null) {
			    logger.info 'Missed Inhibitor: {}', inhibitor
			    inhMisses << inhibitor
                            continue
                        }

			inhibitorNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: inhURN)
			inhibitorNode.attr << new Attr(name: 'NodeType', value: 'NodeType SmallMol')
			inhibitorNode.attr << new Attr(name: 'Name', value: inhibitor)
			nodeMap[inhibitor] = inhibitorNode
			nodes.node << inhibitorNode
                    }

		    Node componentNode
                    if (nodeMap.containsKey(component)) {
			componentNode = nodeMap[component]
                    }
                    else {
			String cmpURN = literatureQueryService.findGeneURN(component)
                        if (cmpURN == null) {
			    logger.info 'Missed Component: {}', component
			    inhMisses << component
                            continue
                        }

			componentNode = new Node(localId: 'N' + random.nextInt(RLIMIT), urn: cmpURN)
			componentNode.attr << new Attr(name: 'NodeType', value: 'Protein')
			componentNode.attr << new Attr(name: 'Name', value: component)
			nodeMap[component] = componentNode
			nodes.node << componentNode
                    }

		    Control control = new Control(localId: 'L' + +random.nextInt(RLIMIT))

                    if (searchResult.effectMolecular != null) {
			if ('Activation'.equalsIgnoreCase(searchResult.effectMolecular) ||
			    'Upregulation'.equalsIgnoreCase(searchResult.effectMolecular)) {
			    createAtt control, 'ControlType', 'Regulation'
			    createAtt control, 'Effect', 'positive'
			}
			else if ('Cleavage'.equalsIgnoreCase(searchResult.effectMolecular)) {
			    createAtt control, 'ControlType', 'Expression'
			    createAtt control, 'Mechanism', 'Cleavage'
			    createAtt control, 'Effect', 'negative'
			}
			else if ('Decreased'.equalsIgnoreCase(searchResult.effectMolecular)) {
			    createAtt control, 'ControlType', 'Expression'
			    createAtt control, 'Effect', 'negative'
			}
			else if ('Degradation'.equalsIgnoreCase(searchResult.effectMolecular)) {
			    createAtt control, 'ControlType', 'Expression'
			    createAtt control, 'Mechanism', 'Degradation'
			    createAtt control, 'Effect', 'negative'
			}
			else if ('Defective Binding'.equalsIgnoreCase(searchResult.effectMolecular) ||
				 'Downregulation'.equalsIgnoreCase(searchResult.effectMolecular) ||
				 'Downregulation, Inhibition'.equalsIgnoreCase(searchResult.effectMolecular) ||
				 'Inhibition'.equalsIgnoreCase(searchResult.effectMolecular)) {
			    createAtt control, 'ControlType', 'Regulation'
			    createAtt control, 'Effect', 'negative'
			}
			else {
			    logger.warn 'Unknown molecular effect: {}', searchResult.effectMolecular
			    createAtt control, 'ControlType', 'Unknown'
			}
		    }
		    else {
			logger.warn 'Molecular effect is null, mapping relationship type to unknown'
			createAtt control, 'ControlType', 'Unknown'
		    }

		    control.link << new Link(type: 'in', ref: inhibitorNode.localId)
		    control.link << new Link(type: 'out', ref: componentNode.localId)

		    createAtt control, 'X-EffectResponseRate', searchResult.effectResponseRate
		    createAtt control, 'X-EffectDownstream', searchResult.effectDownstream
		    createAtt control, 'X-EffectBeneficial', searchResult.effectBeneficial
		    createAtt control, 'X-EffectAdverse', searchResult.effectAdverse
		    createAtt control, 'X-EffectDescription', searchResult.effectDescription
		    createAtt control, 'X-EffectPharmacos', searchResult.effectPharmacos
		    createAtt control, 'X-EffectPotentials', searchResult.effectPotentials
		    createAtt control, 'X-TrialType', searchResult.trialType
		    createAtt control, 'X-TrialPhase', searchResult.trialPhase
		    createAtt control, 'X-TrialStatus', searchResult.trialStatus
		    createAtt control, 'X-TrialExperimentalModel', searchResult.trialExperimentalModel
		    createAtt control, 'X-TrialTissue', searchResult.trialTissue
		    createAtt control, 'X-TrialBodySubstance', searchResult.trialBodySubstance
		    createAtt control, 'X-TrialDescription', searchResult.trialDescription
		    createAtt control, 'X-TrialDesigns', searchResult.trialDesigns
		    createAtt control, 'X-TrialCellLine', searchResult.trialCellLine
		    createAtt control, 'X-TrialCellType', searchResult.trialCellType
		    createAtt control, 'X-TrialPatientsNumber', searchResult.trialPatientsNumber
		    createAtt control, 'X-TrialInclusionCriteria', searchResult.trialInclusionCriteria
		    createAtt control, 'X-Inhibitor', searchResult.inhibitor
		    createAtt control, 'X-InhibitorStandardName', searchResult.inhibitorStandardName
		    createAtt control, 'X-CasID', searchResult.casid
		    createAtt control, 'X-Description', searchResult.description
		    createAtt control, 'X-Concentration', searchResult.concentration
		    createAtt control, 'X-TimeExposure', searchResult.timeExposure
		    createAtt control, 'X-Administration', searchResult.administration
		    createAtt control, 'X-Treatment', searchResult.treatment
		    createAtt control, 'X-Techniques', searchResult.techniques
		    createAtt control, 'X-EffectMolecular', searchResult.effectMolecular
		    createAtt control, 'X-EffectPercent', searchResult.effectPercent
		    createAtt control, 'X-EffectNumber', searchResult.effectNumber
		    createAtt control, 'X-EffectValue', searchResult.effectValue
		    createAtt control, 'X-EffectSd', searchResult.effectSd
		    createAtt control, 'X-EffectUnit', searchResult.effectUnit

		    int refCount
                    if (!refCountMap.containsKey(searchResult.reference.referenceId)) {
                        refCount = 1
			refCountMap[searchResult.reference.referenceId] = refCount
                    }
                    else {
			refCount = refCountMap[searchResult.reference.referenceId]
                        refCount++
			refCountMap[searchResult.reference.referenceId] = refCount
                    }
		    createAtt control, 'mref', searchResult.reference.referenceId, refCount
		    createAtt control, 'msrc', searchResult.reference.referenceTitle, refCount
		    createAtt control, 'Disease', searchResult.reference.disease
		    createAtt control, 'X-Disease Site', searchResult.reference.diseaseSite
		    createAtt control, 'X-Disease Types', searchResult.reference.diseaseTypes
		    createAtt control, 'X-Disease Stage', searchResult.reference.diseaseStage
		    createAtt control, 'X-Disease Description', searchResult.reference.diseaseDescription
		    createAtt control, 'X-Physiology', searchResult.reference.physiology
		    createAtt control, 'X-Clinical Statistics', searchResult.reference.statClinical
		    createAtt control, 'X-Clinical Correlation', searchResult.reference.statClinicalCorrelation
		    createAtt control, 'X-Statistical Tests', searchResult.reference.statTests
		    createAtt control, 'X-Coefficient', searchResult.reference.statCoefficient
		    createAtt control, 'X-P Value', searchResult.reference.statPValue
		    createAtt control, 'X-Statistical Description', searchResult.reference.statDescription

		    controls.control << control
                }
            }
        }
	misses.addAll inhMisses

	pResNet.attr << new Attr(name: 'Notes', value: inhMisses.toString())

	resnet.setProperties pResNet
	resnet.nodes = nodes
	resnet.controls = controls

	resnet
    }

    // Only create attributes for non-null entries to keep the RNEF file size down
    void createAtt(Control control, String attName, String attValue) {
	if (attValue?.trim()) {
	    control.attr << new Attr(name: attName, value: replaceUnicode(attValue))
        }
    }

    // Only create attributes for non-null entries to keep the RNEF file size down needed for indexed attributes
    void createAtt(Control control, String attName, String attValue, int cnt) {
	if (attValue?.trim()) {
	    control.attr << new Attr(name: attName, value: replaceUnicode(attValue), index: cnt)
        }
    }

    // replace the unicode character for now since Pathway Studio cannot import them
    String replaceUnicode(String input) {
	Matcher m = Pattern.compile('[^\\p{ASCII}]').matcher(input)
        while (m.find()) {
            input = input.replace(m.group(0), '-')
        }
	input
    }
}
