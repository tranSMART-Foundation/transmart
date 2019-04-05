class NewsUpdateController {

    def listDetailed(NewsUpdate newsUpdate) {
	render template: 'listDetailed', model: [thisUpdate: newsUpdate]
    }

}
