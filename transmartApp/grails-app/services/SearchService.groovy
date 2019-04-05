import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.SearchFilter
import org.transmart.SearchResult

/**
 * @author mmcduffie
 */
@CompileStatic
@Slf4j('logger')
class SearchService {

    static transactional = false

    @Autowired private ClinicalTrialAnalysisTEAService clinicalTrialAnalysisTEAService
    @Autowired private DocumentService documentService
    @Autowired private ExperimentAnalysisQueryService experimentAnalysisQueryService
    @Autowired private ExpressionProfileQueryService expressionProfileQueryService
    @Autowired private LiteratureQueryService literatureQueryService
    @Autowired private TrialQueryService trialQueryService

    def doResultCount(SearchResult sResult, SearchFilter searchFilter) {

        // Closure to measure the time performance
	Closure<TimeDuration> benchmark = { Closure closure ->
	    Date start = new Date()
	    closure()
	    TimeCategory.minus new Date(), start
	}

	TimeDuration duration = benchmark {
	    sResult.litJubOncAltCount = literatureQueryService.litJubOncAltCount(searchFilter)
        }
	logger.info 'Literature Oncology Alteration Count Duration: {}', duration

	duration = benchmark {
	    sResult.litJubOncInhCount = literatureQueryService.litJubOncInhCount(searchFilter)
	}
	logger.info 'Literature Oncology Inhibitor Count Duration: {}', duration

	duration = benchmark {
	    sResult.litJubOncIntCount = literatureQueryService.litJubOncIntCount(searchFilter)
	}
	logger.info 'Literature Oncology Interaction Count Duration: {}', duration

        duration = benchmark {
            sResult.litJubAsthmaAltCount = literatureQueryService.litJubAsthmaAltCount(searchFilter)
        }
	logger.info 'Literature Asthma Alteration Count Duration: {}', duration

        duration = benchmark {
            sResult.litJubAsthmaInhCount = literatureQueryService.litJubAsthmaInhCount(searchFilter)
        }
	logger.info 'Literature Asthma Inhibitor Count Duration: {}', duration

        duration = benchmark {
            sResult.litJubAsthmaIntCount = literatureQueryService.litJubAsthmaIntCount(searchFilter)
        }
	logger.info 'Literature Asthma Interaction Count Duration: ${}', duration

	duration = benchmark {
	    sResult.litJubAsthmaPECount = literatureQueryService.litJubAsthmaPECount(searchFilter)
	}
	logger.info 'Literature Asthma Protein Effect Count Duration: {}', duration

        duration = benchmark {
            sResult.experimentCount = experimentAnalysisQueryService.countExperimentMV(searchFilter)
        }
	logger.info 'Expression Analysis Count Duration: {}', duration

	duration = benchmark {
	    sResult.trialCount = trialQueryService.countAnalysis(searchFilter)
	}
	logger.info 'Trial Count Duration: {}', duration

        duration = benchmark {
            sResult.analysisCount = clinicalTrialAnalysisTEAService.queryExpAnalysisCount(searchFilter)
        }
	logger.info 'Analysis count and duration: {} and {}', sResult.analysisCount, duration

        duration = benchmark {
            sResult.mRNAAnalysisCount = experimentAnalysisQueryService.countTEAAnalysis(searchFilter)
        }
	logger.info 'mRNA Analysis count and duration: {} and {}', sResult.mRNAAnalysisCount, duration

	duration = benchmark {
	    sResult.allAnalysiCount = experimentAnalysisQueryService.countAnalysisMV(searchFilter)
	}
	logger.info 'All Analysis count and duration: {} and {}', sResult.allAnalysiCount, duration

	duration = benchmark {
	    sResult.documentCount = documentService.documentCount(searchFilter)
	}
	logger.info 'Document Count Duration: {}', duration

	duration = benchmark {
	    sResult.profileCount = expressionProfileQueryService.countExperiment(searchFilter)
	}
	logger.info 'Profile Count Duration: {}', duration
    }

    Map<String, ?> createPagingParamMap(GrailsParameterMap params, int defaultmax, int defaultoffset) {
	[max   : params.int('max', defaultmax),
	 offset: params.int('offset', defaultoffset)]
    }
}
