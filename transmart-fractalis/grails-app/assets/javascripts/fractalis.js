// # sourceURL=fractalis.js

// eslint-disable-next-line no-unused-vars
window.addFractalisPanel = parentPanel => parentPanel.insert(4, fractalisPanel)

const fractalisPanel = new Ext.Panel({
    id: 'fractalisPanel',
    title: 'Data Analysis (Fractalis)',
    region: 'center',
    split: true,
    height: 90,
    layout: 'fit',
    collapsible: true,
    autoScroll: true,
    autoLoad: {
	url: window.pageInfo.basePath + '/fractalis/index',
	method: 'POST',
	scripts: false,
	callback: () => {
	    if (fjsService.fjs == null) {
		fjsService.initFractalis().catch(() => {
		    Ext.Msg.alert('Could not initialize Fractalis. '+
				  'This is likely due to a misconfigured setup. '+
				  'Please contact your tranSMART support.')
		})
	    }
	}
    },
    listeners: {
	activate: () => { fjsService.activate() }
    }
})

const fjsService = {
    fjs: null,
    settings: null,
    chartWidth: '30vw',
    chartHeight: '30vw',
    conceptBoxObserver: null,

    async initFractalis () {
	console.log("initFractalis");
	this.settings = await this.fetchAsync('settings', 'GET')
	console.log("settings "+this.settings);
	this.fjs = window.fractalis.init({
	    // The service in which this library is used. Example: 'ada', 'tranSMART', 'variantDB'
	    handler: 'tranSMART',
	    // The base URL of the service in which this library is used. Example: 'https://my.service.org/'
	    dataSource: this.settings.dataSource,
	    // The base URL of the fractalis back end that you want to use. 'http://fractalis.uni.lu/'
	    fractalisNode: this.settings.node,
	    // This MUST be a function that can be called at any time to retrieve credentials to authenticate with
	    // on the local tranSMART
	    getAuth: () => {
		return { token: this.settings.token }
	    },
	    // Optional object to configure fractalis.js within the target UI.
	    options: {
		controlPanelPosition: 'right',
		controlPanelExpanded: true
	    }
	})
    },

    activate () {
	let conceptBox, spinner
	console.log("fractalis activate");
	if ((conceptBox = document.querySelector('.fjs-tm-concept-box')) == null
            || (spinner = document.querySelector('.fjs-tm-spinner')) == null
            || window.dropOntoCategorySelection == null) {
	    setTimeout(this.activate, 1000)
	    return
	}
	fjsService.getPatientIDs()
	fjsService.activateDragAndDrop(conceptBox)
	fjsService.observeConceptBox(conceptBox)
    },

    resetView () {
	this.fjs.removeAllCharts()
	this.initFractalis()
	document.querySelector('.fjs-tm-charts').innerHTML = ''
    },

    setChartSize (value) {
	this.chartWidth = value + 'vw'
	this.chartHeight = value + 'vw'
	Array.prototype.forEach.call(document.querySelectorAll('.fjs-tm-charts > div'), div => {
	    div.style.width = this.chartWidth
	    div.style.height = this.chartHeight
	})
    },

    async fetchAsync (action, method, data) {
	console.log("fetchAsync action '"+action+"' method '"+method+"' data '"+data+"'");
	console.log("fetching "+pageInfo.basePath+"/fractalis/"+action);
	return (await window.fetch(window.pageInfo.basePath + '/fractalis/' + action, {
	    method: method,
	    redirect: 'follow',
	    credentials: 'same-origin',
	    body: typeof data !== 'object' || method !== 'POST' ? undefined : JSON.stringify(data)
	})).json()
    },

    activateDragAndDrop (conceptBox) {
	const extObj = Ext.get(conceptBox)
	const dtgI = new Ext.dd.DropTarget(extObj, {ddGroup: 'makeQuery'})
	dtgI.notifyDrop = window.dropOntoCategorySelection
    },

    observeConceptBox (conceptBox) {
	console.log("fractalis observeConceptBox")
	if (this.conceptBoxObserver != null) {
	    this.conceptBoxObserver.disconnect()
	}
	this.conceptBoxObserver = new MutationObserver(targets => {
	    targets.forEach(target => {
		Array.prototype.forEach.call(target.addedNodes, node => {
		    const attr = this.getConceptAttributes(node)
		    if(this.settings.resourceName == 'transmart') {
			const descriptor = {query: this.buildTransmartQuery(attr.path, attr.dataType), dataType: attr.dataType}
			console.log("calling loadData");
			this.fjs.loadData([descriptor]).then(() => {
			    node.innerHTML = '<span>Request has been submitted!</span>'
			    node.style.background = '#82ff69'
			    node.className = 'fjs-tm-fade-out'
			    window.setTimeout(() => node.remove(), 2000)
			})
		    }
		    else {
			const descriptor = {query: this.buildPicSureQuery(attr.path, attr.dataType), dataType: attr.dataType}
			console.log("calling loadData");
			this.fjs.loadData([descriptor]).then(() => {
			    node.innerHTML = '<span>Request has been submitted!</span>'
			    node.style.background = '#82ff69'
			    node.className = 'fjs-tm-fade-out'
			    window.setTimeout(() => node.remove(), 2000)
			})
		    }
		})
	    })
	})
	this.conceptBoxObserver.observe(conceptBox, {childList: true})
    },

    getConceptAttributes (node) {
	console.log("fractalis getConceptAttributes conceptId: '"+node.getAttribute('conceptid')+"' type '"+node.getAttribute('setnodetype')+"'");
	return {
	    path: node.getAttribute('conceptid'),
	    dataType: node.getAttribute('setnodetype') === 'valueicon' ? 'numerical' : 'categorical'
	}
    },

    buildPicSureQuery (path, type) {
	console.log("buildPicSureQuery type '"+type+"' path '"+path+"'");
	const alias = this.shortenConcept(path)
	console.log("alias '"+alias+"'");
	console.log("settings.resourceName '"+this.settings.resourceName+"'");
	path = path.replace(/\\+/g, '/')
	path = this.settings.resourceName + path
	console.log("building query using path '"+path+"'");
	console.log("PicSure looks for query with ENCOUNTER defined");
	return {
	    'select': [
		{'field': {'pui': path}, 'alias': alias}
	    ],
	    'where': [
		{
		    'field': {'pui': path, 'dataType': 'STRING'}, // FIXME: dataType should be attr.dataType but PIC-SURE only knows STRING
		    'predicate': 'CONTAINS',
		    'fields': {'ENCOUNTER': 'YES'}
		}
	    ]
	}
    },

    buildTransmartQuery (path, type) {
	console.log("buildTransmartQuery type '"+type+"' path '"+path+"'");
	const alias = this.shortenConcept(path)
	console.log("alias '"+alias+"'");
	console.log("settings.resourceName '"+this.settings.resourceName+"'");
	path = path.replace(/\\+/g, '/')
	path = this.settings.resourceName + path
	console.log("building transmart query using path '"+path+"'");
	console.log("PicSure looks for query with ENCOUNTER defined");
	return {
	    'select': [
		{'field': {'pui': path}, 'alias': alias}
	    ],
	    'where': [
		{
		    'field': {'pui': path, 'dataType': 'STRING'}, // FIXME: dataType should be attr.dataType but PIC-SURE only knows STRING
		    'predicate': 'CONTAINS',
		    'fields': {'ENCOUNTER': 'YES'}
		}
	    ]
	}
    },

    shortenConcept (concept) {
	let split = concept.split('\\')
	split = split.filter(s => s !== '')
	return split[split.length - 2] + '/' + split[split.length - 1]
    },

    addChartContainer () {
	const chart = document.createElement('div')
	const container = document.querySelector('.fjs-tm-charts')
	chart.id = 'fjs-tm-chart-' + container.children.length
	chart.style.width = this.chartWidth
	chart.style.height = this.chartHeight
	container.appendChild(chart)
	return chart.id
    },

    setChart (chartName) {
	const chartID = this.addChartContainer()
	this.fjs.setChart(chartName, '#' + chartID)
    },

    clearCache () {
	this.fjs.clearCache()
	document.querySelector('.fjs-tm-concept-box').innerHTML = ''
    },

    getPatientIDs () {
	console.log("getPatientIDs");
	this.showLoadingScreen(true)
	window.runAllQueries(() => {
	    this.fetchAsync('patients', 'POST', {
		result_instance_id1: window.GLOBAL.CurrentSubsetIDs[1],
		result_instance_id2: window.GLOBAL.CurrentSubsetIDs[2]
	    })
		.then(ids => {
		    const subset1 = ids.subjectIDs1.split(',')
		    const subset2 = ids.subjectIDs2.split(',')
		    fjsService.fjs.setSubsets([subset1, subset2])
		})
		.then(() => {
		    this.showLoadingScreen(false)
		})
	})
    },

    showLoadingScreen (bb) {
	console.log("showLoadingScreen");
	const container = document.querySelector('.fjs-tm-block')
	if (bb) {
	    container.style.display = 'block'
	} else {
	    container.style.display = 'none'
	}
    }
}
