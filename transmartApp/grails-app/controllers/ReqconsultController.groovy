class ReqconsultController {

    static defaultAction = 'newrequest'

    def newrequest() {}

    def saverequest() {
	[reqtext: params.consulttext]
    }
}
