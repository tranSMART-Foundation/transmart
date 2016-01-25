function createD3Button(args) {
    var button = args.location.append('g')

    var box = button.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', '#009ac9')
        .style('fill', '#009ac9')
        .style('cursor', 'pointer')
        .on('mouseover', function() {
            box.transition()
                .duration(300)
                .style('fill', '#ffffff')
            text.transition()
                .duration(300)
                .style('fill', '#009ac9')
        })
        .on('mouseout', function() {
            box.transition()
                .duration(300)
                .style('fill', '#009ac9')
            text.transition()
                .duration(300)
                .style('fill', '#ffffff')
        })
        .on('click', function() { return args.callback() })

    var text = button.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(args.label)

    return button
}

function createD3Switch(args) {
    var switcher = args.location.append('g')

    var checked = args.checked
    var color = checked ? 'green' : 'red'

    var box = switcher.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', color)
        .style('fill', color)
        .style('cursor', 'pointer')
        .on('click', function() {
            if (color === 'green') {
                box.transition()
                    .duration(300)
                    .style('stroke', 'red')
                    .style('fill', 'red')
                color = 'red'
                checked = false
            } else {
                box.transition()
                    .duration(300)
                    .style('stroke', 'green')
                    .style('fill', 'green')
                color = 'green'
                checked = true
            }
            text.text(checked ? args.onlabel : args.offlabel)
            args.callback(checked)
        })

    var text = switcher.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(checked ? args.onlabel : args.offlabel)

    return switcher
}

function createD3Dropdown(args) {
    function shrink() {
        dropdown.selectAll('.itemBox')
            .attr('y', args.y + args.height)
            .style('visibility', 'hidden')
        dropdown.selectAll('.itemText')
            .attr('y', args.y + args.height + args.height / 2)
            .style('visibility', 'hidden')
        itemHovered = false
        hovered = false
        itemHovered = false
    }
    var dropdown = args.location.append('g')

    var hovered = false
    var itemHovered = false

    var itemBox = dropdown.selectAll('.itemBox')
        .data(args.items, function(item) { return item.label })

    itemBox.enter()
        .append('rect')
        .attr('class', 'itemBox')
        .attr('x', args.x)
        .attr('y', args.y + args.height)
        .attr('rx', 0)
        .attr('ry', 0)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('cursor', 'pointer')
        .style('stroke-width', '2px')
        .style('stroke', '#ffffff')
        .style('fill', '#E3E3E3')
        .style('visibility', 'hidden')
        .on('mouseover', function() {
            itemHovered = true
            d3.select(this)
                .style('fill', '#009ac9')
        })
        .on('mouseout', function() {
            itemHovered = false
            d3.select(this)
                .style('fill', '#E3E3E3')
            setTimeout(function() {
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 50)
        })
        .on('click', function(d) { d.callback() })

    var itemText = dropdown.selectAll('.itemText')
        .data(args.items, function(item) { return item.label })

    itemText
        .enter()
        .append('text')
        .attr('class', 'itemText')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '14px')
        .style('visibility', 'hidden')
        .text(function(d) { return d.label })

    var box = dropdown.append('rect')
        .attr('x', args.x)
        .attr('y', args.y)
        .attr('rx', 3)
        .attr('ry', 3)
        .attr('width', args.width)
        .attr('height', args.height)
        .style('stroke-width', '1px')
        .style('stroke', '#009ac9')
        .style('fill', '#009ac9')
        .on('mouseover', function() {
            if (hovered) {
                return
            }
            dropdown.selectAll('.itemBox')
                .transition()
                .duration(300)
                .style('visibility', 'visible')
                .attr('y', function(d) {
                    var idx = args.items.findIndex(function(item) { return item.label === d.label })
                    return 2 + args.y + (idx + 1) * args.height
                })

            dropdown.selectAll('.itemText')
                .transition()
                .duration(300)
                .style('visibility', 'visible')
                .attr('y', function(d) {
                    var idx = args.items.findIndex(function(item) { return item.label === d.label })
                    return 2 + args.y + (idx + 1) * args.height + args.height / 2
                })

            hovered = true
        })
        .on('mouseout', function() {
            hovered = false
            setTimeout(function() {
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 50)
            setTimeout(function() { // first check is not enough if animation interrupts it
                if (! hovered && ! itemHovered) {
                    shrink()
                }
            }, 350)
        })

    var text = dropdown.append('text')
        .attr('class', 'buttonText')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height / 2)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#ffffff')
        .style('font-size', '14px')
        .text(args.label)

    return dropdown
}

function createD3Slider(args) {
    var slider = args.location.append('g')

    var lineGen = d3.svg.line()
        .x(function(d) { return d.x })
        .y(function(d) { return d.y })
        .interpolate('linear')

    var lineData = [
        {x: args.x, y: args.y + args.height},
        {x: args.x, y: args.y + 0.75 * args.height},
        {x: args.x + args.width, y: args.y + 0.75 * args.height},
        {x: args.x + args.width, y: args.y + args.height}
    ]

    var sliderScale = d3.scale.linear()
        .domain([args.min, args.max])
        .range([args.x, args.x + args.width])

    slider.append('path')
        .attr('d', lineGen(lineData))
        .style('pointer-events', 'none')
        .style('stroke', '#009ac9')
        .style('stroke-width', '2px')
        .style('shape-rendering', 'crispEdges')
        .style('fill', 'none')

    slider.append('text')
        .attr('x', args.x)
        .attr('y', args.y + args.height + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.min)

    slider.append('text')
        .attr('x', args.x + args.width)
        .attr('y', args.y + args.height + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '9px')
        .text(args.max)

    slider.append('text')
        .attr('x', args.x + args.width / 2)
        .attr('y', args.y + args.height)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'middle')
        .style('fill', '#000000')
        .style('font-size', '14px')
        .text(args.label)

    var currentValue = args.init

    function move() {
        var xPos = d3.event.x
        if (xPos < args.x) {
            xPos = args.x
        } else if (xPos > args.x + args.width) {
            xPos = args.x + args.width
        }

        currentValue = Number(sliderScale.invert(xPos)).toFixed(5)

        dragger
            .attr('x', xPos - 20)
        handle
            .attr('cx', xPos)
        pointer
            .attr('x1', xPos)
            .attr('x2', xPos)
        value
            .attr('x', xPos + 10)
            .text(currentValue)
    }

    var drag = d3.behavior.drag()
        .on('drag', move)
        .on(args.trigger, function() { args.callback(currentValue) })

    var dragger = slider.append('rect')
        .attr('x', sliderScale(args.init) - 20)
        .attr('y', args.y)
        .attr('width', 40)
        .attr('height', args.height)
        .style('opacity', 0)
        .style('cursor', 'pointer')
        .on('mouseover', function() {
            handle
                .style('fill', '#009ac9')
            pointer
                .style('stroke', '#009ac9')
        })
        .on('mouseout', function() {
            handle
                .style('fill', '#000000')
            pointer
                .style('stroke', '#000000')
        })
        .call(drag)

    var handle = slider.append('circle')
        .attr('cx', sliderScale(args.init))
        .attr('cy', args.y + 10)
        .attr('r', 6)
        .style('pointer-events', 'none')
        .style('fill', '#000000')

    var pointer = slider.append('line')
        .attr('x1', sliderScale(args.init))
        .attr('y1', args.y + 10)
        .attr('x2', sliderScale(args.init))
        .attr('y2', args.y + 0.75 * args.height)
        .style('pointer-events', 'none')
        .style('stroke', '#000000')
        .style('stroke-width', '1px')

    var value = slider.append('text')
        .attr('x', sliderScale(args.init) + 10)
        .attr('y', args.y + 10)
        .attr('dy', '0.35em')
        .style('pointer-events', 'none')
        .style('text-anchor', 'start')
        .style('fill', '#000000')
        .style('font-size', '10px')
        .text(args.init)

    return slider
}

function mouseX() {
    var mouseXPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageX : d3.event.clientX
    return mouseXPos + $('#index').parent().scrollLeft() - $('#smartRPanel').offset().left
}

function mouseY() {
    var mouseYPos = typeof d3.event.sourceEvent !== 'undefined' ? d3.event.sourceEvent.pageY : d3.event.clientY
    return mouseYPos + $('#index').parent().scrollTop() - $('#smartRPanel').offset().top
}

function getMaxWidth(selection) {
    return selection[0].map(function(d) { return d.getBBox().width; }).max()
}

function showCohortInfo() {
    var cohortsSummary = ''

    for(var i = 1; i < GLOBAL.NumOfSubsets; i++) {
        var currentQuery = getQuerySummary(i)
        if(currentQuery !== '') {
            cohortsSummary += '<br/>Subset ' + (i) + ': <br/>'
            cohortsSummary += currentQuery
            cohortsSummary += '<br/>'
        }
    }
    if (!cohortsSummary) {
        cohortsSummary = '<br/>WARNING: No subsets have been selected! Please go to the "Comparison" tab and select your subsets.'
    }
    $('#cohortInfo').html(cohortsSummary)
}
//showCohortInfo()

function updateInputView() {
    if (typeof updateOnView === 'function') updateOnView()
}

var panelItem = $('#resultsTabPanel__smartRPanel')
//panelItem.click(showCohortInfo)
//panelItem.click(updateInputView)

function shortenConcept(concept) {
    var splits = concept.split('\\')
    return splits[splits.length - 3] + '/' + splits[splits.length - 2]
}

function activateDragAndDrop(divName) {
    var div = Ext.get(divName)
    var dtgI = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'})
    dtgI.notifyDrop = dropOntoCategorySelection
}

window.addSmartRPanel = function addSmartRPanel(parentPanel, config) {
    var smartRPanel = new Ext.Panel({
        id: 'smartRPanel',
        title: 'SmartR',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        collapsible: true,
        autoScroll: true,
        tbar: new Ext.Toolbar({
            id: 'smartRToolbar',
            title: 'R Scripts',
            items: []
        }),
        autoLoad: {
            url: pageInfo.basePath + '/smartR/index',
            method: 'POST',
            evalScripts: false
        },
        listeners: {
            render: function (panel) {
                panel.body.on('click', function () {
                    if (typeof updateOnView === "function") {
                        updateOnView()
                    }
                })
            }
        }
    })
    parentPanel.add(smartRPanel)
}

function clearVarSelection(divName) {
    $('#' + divName).children().remove()
}

function getConcepts(divName) {
    return $('#' + divName).children().toArray().map(function(childNode) {
        return childNode.getAttribute('conceptid') })
}

function changeInputDIV() {
    jQuery('#outputDIV').empty();
    var request = jQuery.ajax({
        url: pageInfo.basePath + '/SmartR/renderInput',
        type: 'POST',
        timeout: 10000,
        data: {script: jQuery('#scriptSelect').val()}
    });
    request.done(function(response) { jQuery('#inputDIV').html(response) });
    request.fail(function() { alert('Server does not respond. Network connection lost?') });
}
