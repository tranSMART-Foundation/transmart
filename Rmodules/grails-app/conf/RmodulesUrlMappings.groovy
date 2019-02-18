class RmodulesUrlMappings {
    static mappings = {
	def analysisFilesClosure = {
	    controller = 'analysisFiles'
	    action     = 'download'
	    constraints {
		analysisName matches: /.+-[a-zA-Z]+-\d+/
	    }
	}

	"/analysisFiles/$analysisName/$path**"        analysisFilesClosure
	"/images/analysisFiles/$analysisName/$path**" analysisFilesClosure

	// see also the exclusion of images/analysisFiles in doWithSpring
    }
}
