//# sourceURL=d3Heatmap.js

let animationDuration = 1500
let tmpAnimationDuration = animationDuration

function switchAnimation(checked) { // general purpose callback, this is why it is not inside SmartRHeatmap
    if (! checked) {
        tmpAnimationDuration = animationDuration
        animationDuration = 0
    } else {
        animationDuration = tmpAnimationDuration
    }
}

SmartRHeatmap = (() => {
    let service = {}

    /**
     * Create smart-r heatmap with data
     * data is json formatted object.
     * @param data
     */
    service.create = data => {
        let extraFields = data.extraFields === undefined ? [] : data.extraFields
        let features = data.features === undefined ? [] : data.features
        let fields = data.fields
        let significanceValues = data.significanceValues
        let patientIDs = data.patientIDs
        let probes = data.probes
        let geneSymbols = data.geneSymbols
        let numberOfClusteredColumns = data.numberOfClusteredColumns[0]
        let numberOfClusteredRows = data.numberOfClusteredRows[0]
        let maxRows = 100
        let warning = data.warnings === undefined ? '' : data.warnings

        let rowClustering = false
        let colClustering = false

        let originalPatientIDs = patientIDs.slice()
        let originalProbes = probes.slice()

        function redGreen() {
            let colorSet = []
            const NUM = 100
            ;[...Array(NUM).keys()].reverse().map(i => colorSet.push(d3.rgb(0, (255 * i) / NUM, 0)))
            ;[...Array(NUM).keys()].map(i => colorSet.push(d3.rgb((255 * i) / NUM, 0, 0)))
            return colorSet
        }

        let colorSets = [
            redGreen()
        ]

        const featureColorSetBinary = ['#FF8000', '#FFFF00']
        const featureColorSetSequential = ['rgb(247,252,253)','rgb(224,236,244)','rgb(191,211,230)','rgb(158,188,218)','rgb(140,150,198)','rgb(140,107,177)','rgb(136,65,157)','rgb(129,15,124)','rgb(77,0,75)']

        let gridFieldWidth = 20
        let gridFieldHeight = 20
        let dendrogramHeight = 300
        let histogramHeight = 200

        let margin = { top: gridFieldHeight * 2 + 100 + features.length * gridFieldHeight / 2 + dendrogramHeight,
            right: gridFieldWidth + 300 + dendrogramHeight,
            bottom: 10,
            left: histogramHeight + 250
        }

        let width = gridFieldWidth * patientIDs.length
        let height = gridFieldHeight * probes.length

        let selectedPatientIDs = []

        let histogramScale = d3.scale.linear()
            .domain(d3.extent(significanceValues))
            .range([0, histogramHeight])

        let heatmap = d3.select('#heatmap').append('svg')
            .attr("width", (width + margin.left + margin.right) * 4)
            .attr("height", (height + margin.top + margin.bottom) * 4)
            .append('g')
            .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')')

        function adjustDimensions() {
            // gridFieldWidth/gridFieldHeight are adjusted outside as the zoom changes
            $(heatmap[0]).closest('svg')
                .attr('width', margin.left + margin.right + (gridFieldWidth * patientIDs.length))
                .attr('height', margin.top + margin.bottom + (gridFieldHeight * probes.length))
        }
        adjustDimensions()

        let tooltip = d3.select('#heatmap').append('div')
            .attr('class', 'tooltip text')
            .style('visibility', 'hidden')

        let featureItems = heatmap.append('g')
        let squareItems = heatmap.append('g')
        let colSortItems = heatmap.append('g')
        let patientIDItems = heatmap.append('g')
        let rowSortItems = heatmap.append('g')
        let significanceSortItems = heatmap.append('g')
        let labelItems = heatmap.append('g')
        let barItems = heatmap.append('g')
        let warningDiv = $('#heim-heatmap-warnings').append('strong').text(warning)

        function updateHeatmap() {
            let square = squareItems.selectAll('.square')
                .data(fields, d => `patientID-${d.PATIENTID}-probe-${d.PROBE}`)

            square.enter()
                .append('rect')
                .attr('class', d => `square patientID-${d.PATIENTID} probe-${d.PROBE}`)
                .attr('x', d => patientIDs.indexOf(d.PATIENTID) * gridFieldWidth)
                .attr('y', d => probes.indexOf(d.PROBE) * gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .attr('rx', 0)
                .attr('ry', 0)
                .style('fill', 'white')
                .on('mouseover', d => {
                    d3.select('.patientID.patientID-' + d.PATIENTID).classed('highlight', true)
                    d3.select('.probe.probe-' + d.PROBE).classed('highlight', true)
                    let html = ''
                    for (let key of Object.keys(d)) {
                        html += `${key}: ${d[key]}<br/>`
                    }
                    tooltip
                        .style('visibility', 'visible')
                        .html(html)
                        .style('left', mouseX() + 'px')
                        .style('top', mouseY() + 'px')
                })
                .on('mouseout', () => {
                    d3.selectAll('.patientID').classed('highlight', false)
                    d3.selectAll('.probe').classed('highlight', false)
                    tooltip.style('visibility', 'hidden')
                })
                .on('click', d => window.open('http://www.genecards.org/cgi-bin/carddisp.pl?gene=' + d.GENESYMBOL))

            square.transition()
                .duration(animationDuration)
                .attr('x', d => patientIDs.indexOf(d.PATIENTID) * gridFieldWidth)
                .attr('y', d => probes.indexOf(d.PROBE) * gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)

            let colSortText = colSortItems.selectAll('.colSortText')
                .data(patientIDs, d => d)

            colSortText.enter()
                .append('text')
                .attr('class', 'text colSortText')
                .attr('x', (d, i) => i * gridFieldWidth + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr('text-anchor', 'middle')
                .text('↑↓')
            
            colSortText.transition()
                .duration(animationDuration)
                .attr('x', (d, i) => i * gridFieldWidth + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)

            let colSortBox = colSortItems.selectAll('.colSortBox')
                .data(patientIDs, d => d)

            function getValueForSquareSorting(patientID, probe) {
                const square = d3.select(`.square.patientID-${patientID}.probe-${probe}`)
                return square[0][0] != null ? square.data()[0].ZSCORE : Number.NEGATIVE_INFINITY
            }

            function isSorted(arr) {
                return arr.every((d, i) => i === arr.length - 1 || arr[i][1] <= arr[i+1][1]) ||
                        arr.every((d, i) => i === arr.length - 1 || arr[i][1] >= arr[i+1][1])
            }

            colSortBox.enter()
                .append('rect')
                .attr('class', 'box colSortBox')
                .attr('x', (d, i) => i * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on('click', patientID => {
                    const rowValues = probes.map((probe, idx) => [idx, getValueForSquareSorting(patientID, probe)])
                    isSorted(rowValues) ? rowValues.sort((a, b) => a[1] - b[1]) : rowValues.sort((a, b) => b[1] - a[1])
                    const sortValues = rowValues.map(rowValue => rowValue[0])
                    updateRowOrder(sortValues)
                })

            colSortBox.transition()
                .duration(animationDuration)
                .attr('x', (d, i) => i * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)

            let rowSortText = rowSortItems.selectAll('.rowSortText')
                .data(probes, d => d)

            rowSortText.enter()
                .append('text')
                .attr('class', 'text rowSortText')
                .attr('transform', (d, i) => `translate(${width + 2 + 0.5 * gridFieldWidth}, 0)translate(0, ${i * gridFieldHeight + 0.5 * gridFieldHeight})rotate(-90)`)
                .attr('dy', '0.35em')
                .attr('text-anchor', 'middle')
                .text('↑↓')

            rowSortText.transition()
                .duration(animationDuration)
                .attr('transform', (d, i) => `translate(${width + 2 + 0.5 * gridFieldWidth}, 0)translate(0, ${i * gridFieldHeight + 0.5 * gridFieldHeight})rotate(-90)`)

            let rowSortBox = rowSortItems.selectAll('.rowSortBox')
                .data(probes, d => d)

            rowSortBox.enter()
                .append('rect')
                .attr('class', 'box rowSortBox')
                .attr('x', width + 2)
                .attr('y', (d, i) => i * gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on('click', probe => {
                    const colValues = patientIDs.map((patientID, idx) => [idx, getValueForSquareSorting(patientID, probe)])
                    isSorted(colValues) ? colValues.sort((a, b) => a[1] - b[1]) : colValues.sort((a, b) => b[1] - a[1])
                    const sortValues = colValues.map(colValue => colValue[0])
                    updateColOrder(sortValues)
                })

            rowSortBox.transition()
                .duration(animationDuration)
                .attr('x', width + 2)
                .attr('y', (d, i) => i * gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)

            let significanceSortText = significanceSortItems.selectAll('.significanceSortText')
                .data(['something'], d => d)

            significanceSortText.enter()
                .append('text')
                .attr('class', 'text significanceSortText')
                .attr('x', - gridFieldWidth - 10 + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr('text-anchor', 'middle')
                .text('↑↓')

            significanceSortText.transition()
                .duration(animationDuration)
                .attr('x', - gridFieldWidth - 10 + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight + 0.5 * gridFieldHeight)

            let significanceSortBox = significanceSortItems.selectAll('.significanceSortBox')
                .data(['something'], d => d)

            significanceSortBox.enter()
                .append('rect')
                .attr('class', 'box significanceSortBox')
                .attr('x', - gridFieldWidth - 10)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on('click', () => {
                    const rowValues = significanceValues.map((significanceValue, idx) => [idx, Math.abs(significanceValue)])
                    isSorted(rowValues) ? rowValues.sort((a, b) => a[1] - b[1]) : rowValues.sort((a, b) => b[1] - a[1])
                    const sortValues = rowValues.map(rowValue => rowValue[0])
                    updateRowOrder(sortValues)
                })

            significanceSortBox.transition()
                .duration(animationDuration)
                .attr('x', - gridFieldWidth - 10)
                .attr('y', -2 - gridFieldHeight)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)

            let selectText = heatmap.selectAll('.selectText')
                .data(patientIDs, d => d)

            selectText.enter()
                .append('text')
                .attr('class', d => 'text selectText patientID-' + d)
                .attr('x', (d, i) => i * gridFieldWidth + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .attr('text-anchor', 'middle')
                .text('□')

            selectText.transition()
                .duration(animationDuration)
                .attr('x', (d, i) => i * gridFieldWidth + 0.5 * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight * 2 + 0.5 * gridFieldHeight)

            let selectBox = heatmap.selectAll('.selectBox')
                .data(patientIDs, d => d)

            selectBox.enter()
                .append('rect')
                .attr('class', 'box selectBox')
                .attr('x', (d, i) => i * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight * 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)
                .on('click', patientID => selectCol(patientID))

            selectBox.transition()
                .duration(animationDuration)
                .attr('x', (d, i) => i * gridFieldWidth)
                .attr('y', -2 - gridFieldHeight * 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight)

            let patientID = patientIDItems.selectAll('.patientID')
                .data(patientIDs, d => d)

            patientID.enter()
                .append('text')
                .attr('class', d => 'patientID patientID-' + d)
                .attr('transform', d => `translate(${patientIDs.indexOf(d) * gridFieldWidth}, 0)translate(${gridFieldWidth / 2}, ${-4 - gridFieldHeight * 2})rotate(-45)`)
                .style('text-anchor', 'start')
                .text(d => d)

            patientID.transition()
                .duration(animationDuration)
                .attr('transform', d => `translate(${patientIDs.indexOf(d) * gridFieldWidth}, 0)translate(${gridFieldWidth / 2}, ${-4 - gridFieldHeight * 2})rotate(-45)`)

            let probe = labelItems.selectAll('.probe')
                .data(probes, d => d)

            probe.enter()
                .append('text')
                .attr('class', d => 'probe text probe-' + d)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', d => probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight)
                .attr('dy', '0.35em')
                .style('text-anchor', 'start')
                .text(d => `${d} // ${geneSymbols[probes.indexOf(d)]}`)

            probe.transition()
                .duration(animationDuration)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', d => probes.indexOf(d) * gridFieldHeight + 0.5 * gridFieldHeight)

            let significanceIndexMap = significanceValues.map((significance, idx) => { return {significance, idx}})

            // Visible offset will be effectively _BAR_OFFSET - _MINIMAL_WIDTH
            const _MINIMAL_WIDTH = 10  // This value will be added to the scaled width. So that it is always >0 (visible)
            const _BAR_OFFSET    = 20  // Distance between significance bar and the heatmap.

            let bar = barItems.selectAll('.bar')
                .data(significanceIndexMap, d => d.idx)

            bar.enter()
                .append('rect')
                .attr('class', d => 'bar idx-' + d.idx)
                .attr('width', d => histogramScale(Math.abs(d.significance)) + _MINIMAL_WIDTH)
                .attr('height', gridFieldHeight)
                .attr('x', d => - histogramScale(Math.abs( d.significance)) - _BAR_OFFSET)
                .attr('y', d => gridFieldHeight * d.idx)
                .style('fill', d => d.significance > 0 ? 'steelblue' : '#990000')
                .on('mouseover', d => {
                    let html = 'FEATURE SIGNIFICANCE: ' + d.significance
                    tooltip
                        .style('visibility', 'visible')
                        .html(html)
                        .style('left', mouseX() + 'px')
                        .style('top', mouseY() + 'px')
                    d3.selectAll('.square.probe-' +  probes[d.idx]).classed('squareHighlighted', true)
                    d3.select('.probe.probe-' +  probes[d.idx]).classed('highlight', true)
                })
                .on('mouseout', () => {
                    tooltip.style('visibility', 'hidden')
                    d3.selectAll('.square').classed('squareHighlighted', false)
                    d3.selectAll('.probe').classed('highlight', false)
                })

            bar.transition()
                .duration(animationDuration)
                .attr('height', gridFieldHeight)
                .attr('width', d => histogramScale(Math.abs(d.significance)) + _MINIMAL_WIDTH)
                .attr('x', d => - histogramScale(Math.abs(d.significance)) - _BAR_OFFSET)
                .attr("y", d => gridFieldHeight * d.idx)
                .style('fill', d => d.significance > 0 ? 'steelblue' : '#990000')

            let featurePosY = - gridFieldWidth * 2 - getMaxWidth(d3.selectAll('.patientID')) - features.length * gridFieldWidth / 2 - 20

            let extraSquare = featureItems.selectAll('.extraSquare')
                .data(extraFields, d => `patientID-${d.PATIENTID}-feature-${d.FEATURE}`)

            extraSquare.enter()
                .append('rect')
                .attr('class', d => `extraSquare patientID-${d.PATIENTID} feature-${d.FEATURE}`)
                .attr('x', d => patientIDs.indexOf(d.PATIENTID) * gridFieldWidth)
                .attr('y', d => featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2)
                .attr('rx', 0)
                .attr('ry', 0)
                .style('fill', 'white')
                .on('mouseover', d => {
                    d3.select('.patientID.patientID-' +  d.PATIENTID).classed('highlight', true)
                    d3.select('.feature.feature-' +  d.FEATURE).classed('highlight', true)
                    let html = ''
                    for (let key of Object.keys(d)) {
                        html += `${key}: ${d[key]}<br/>`
                    }
                    tooltip
                        .style('visibility', 'visible')
                        .html(html)
                        .style('left', mouseX() + 'px')
                        .style('top', mouseY() + 'px')
                })
                .on('mouseout', () => {
                    d3.selectAll('.patientID').classed('highlight', false)
                    d3.selectAll('.feature').classed('highlight', false)
                    tooltip.style('visibility', 'hidden')
                })

            extraSquare.transition()
                .duration(animationDuration)
                .attr('x', d => patientIDs.indexOf(d.PATIENTID) * gridFieldWidth)
                .attr('y', d => featurePosY + features.indexOf(d.FEATURE) * gridFieldHeight / 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2)

            let feature = featureItems.selectAll('.feature')
                .data(features, d => d)

            feature.enter()
                .append('text')
                .attr('class', d => 'feature text feature-' + d)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', d => featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4)
                .attr('dy', '0.35em')
                .style('text-anchor', 'start')
                .text(d => d)

            feature.transition()
                .duration(animationDuration)
                .attr('x', width + gridFieldWidth + 7)
                .attr('y', d => featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4)

            let featureSortText = featureItems.selectAll('.featureSortText')
                .data(features, d => d)

            featureSortText.enter()
                .append('text')
                .attr('class', 'text featureSortText')
                .attr('transform', d => `translate(${width + 2 + 0.5 * gridFieldWidth}, 0)translate(0, ${featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4})rotate(-90)`)
                .attr('dy', '0.35em')
                .attr('text-anchor', 'middle')
                .text('↑↓')

            featureSortText.transition()
                .duration(animationDuration)
                .attr('transform', d => `translate(${width + 2 + 0.5 * gridFieldWidth}, 0)translate(0, ${featurePosY + features.indexOf(d) * gridFieldHeight / 2 + gridFieldHeight / 4})rotate(-90)`)

            let featureSortBox = featureItems.selectAll('.featureSortBox')
                .data(features, d => d)

            featureSortBox.enter()
                .append('rect')
                .attr('class', 'box featureSortBox')
                .attr('x', width + 2)
                .attr('y', d => featurePosY + features.indexOf(d) * gridFieldHeight / 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2)
                .on('click', feature => {
                    let missingValues = false
                    let featureValues = patientIDs.map(patientID => {
                        let value = (- Math.pow(2,32)).toString()
                        try {
                            let square = d3.select(`.extraSquare.patientID-${patientID}.feature-${feature}`)
                            value = square.data()[0].VALUE
                        } catch (err) {
                            missingValues = true
                        }
                        return [i, value]
                    })
                    if (isSorted(featureValues)) {
                        featureValues.sort((a, b) => {
                            const diff = a[1] - b[1]
                            return isNaN(diff) ? a[1].localeCompare(b[1]) : diff
                        })
                    } else {
                        featureValues.sort((a, b) => {
                            const diff = b[1] - a[1]
                            return isNaN(diff) ? b[1].localeCompare(a[1]) : diff
                        })
                    }
                    let sortValues = featureValues.map(d => d[0])
                    if (missingValues) alert('Feature is missing for one or more patients.\nEvery missing value will be set to lowest possible value for sorting')
                    updateColOrder(sortValues)
                })

            featureSortBox.transition()
                .duration(animationDuration)
                .attr('x', width + 2)
                .attr('y', (d, i) => featurePosY + features.indexOf(d) * gridFieldHeight / 2)
                .attr('width', gridFieldWidth)
                .attr('height', gridFieldHeight / 2)
        }

        function zoom(zoomLevel) {
            zoomLevel /= 100;
            d3.selectAll('.patientID').style('font-size', Math.ceil(14 * zoomLevel) + 'px');
            d3.selectAll('.selectText').style('font-size', Math.ceil(16 * zoomLevel) + 'px');
            d3.selectAll('.uid').style('font-size', Math.ceil(12 * zoomLevel) + 'px');
            d3.selectAll('.feature').style('font-size', Math.ceil(10 * zoomLevel) + 'px');
            d3.selectAll('.significanceSortText, .rowSortText, .colSortText').style('font-size', Math.ceil(14 * zoomLevel) + 'px');
            d3.selectAll('.featureSortText').style('font-size', Math.ceil(10 * zoomLevel) + 'px');
            gridFieldWidth = 20 * zoomLevel;
            gridFieldHeight = 20 * zoomLevel;
            width = gridFieldWidth * patientIDs.length;
            height = gridFieldHeight * probes.length;
            heatmap
                .attr('width', width + margin.left + margin.right)
                .attr('height', width + margin.top + margin.bottom);
            var temp = animationDuration;
            animationDuration = 0;
            updateHeatmap();
            reloadDendrograms();
            animationDuration = temp;
            adjustDimensions()
        }

        let cutoffLevel = significanceValues[significanceValues.length - 1]
        function animateCutoff(cutoff) {
            cutoffLevel = cutoff
            significanceValues.forEach((significanceValue, i) => {
                d3.selectAll('.square.probe-' + probes[i]).classed('cuttoffHighlight', significanceValue < cutoff)
                d3.select('.bar.idx-' + i).classed('cuttoffHighlight', significanceValue < cutoff)
            })
        }

        function cutoff() {
            cuttoffButton.select('text').text('Loading...')
            const nrows = significanceValues.filter(significanceValue => significanceValue > cutoffLevel).length
            loadRows(nrows)
        }

        function reloadDendrograms() {
            if (colDendrogramVisible) {
                removeColDendrogram()
                createColDendrogram()
            }
            if (rowDendrogramVisible) {
                removeRowDendrogram()
                createRowDendrogram()
            }
        }

        function selectCol(patientID) {
            let colSquares = d3.selectAll('.square.patientID-' + patientID)
            if (colSquares.classed('selected')) {
                let index = selectedPatientIDs.indexOf(patientID)
                selectedPatientIDs.splice(index, 1)
                colSquares.classed('selected', false)
                d3.select('.selectText.patientID-' + patientID).text('□')
            } else {
                selectedPatientIDs.push(patientID)
                colSquares.classed('selected', true)
                d3.select('.selectText.patientID-' + patientID).text('■')
            }
            selectedPatientIDs.length ?
                d3.selectAll('.square:not(.selected)').attr('opacity', 0.4) :
                d3.selectAll('.square').attr('opacity', 1)
        }

        function updateColors(colorIdx) {
            let colorScale = d3.scale.quantile()
                .domain([0, 1])
                .range(colorSets[colorIdx])
            d3.selectAll('.square')
                .transition()
                .duration(animationDuration)
                .style('fill', d => colorScale(1 / (1 + Math.pow(Math.E, - d.ZSCORE))))
            for (let feature of features) {
                let categoricalColorScale = d3.scale.category10()
                d3.selectAll('.extraSquare.feature-' + feature)
                    .style('fill', d => {
                        switch(d.TYPE) {
                            case 'binary':
                                return featureColorSetBinary[d.VALUE]
                            case 'alphabetical':
                                return categoricalColorScale(d.VALUE)
                            default:
                                colorScale.range(featureColorSetSequential)
                                return colorScale(1 / (1 + Math.pow(Math.E, - d.ZSCORE)))
                        }
                    })
            }
        }

        function unselectAll() {
            d3.selectAll('.selectText').text('□')
            d3.selectAll('.square').classed('selected', false).attr('opacity', 1)
            selectedPatientIDs = []
        }

        let colDendrogramVisible = false
        let colDendrogram
        function createColDendrogram() {
            let w = 200
            let colDendrogramWidth = gridFieldWidth * numberOfClusteredColumns
            let spacing = gridFieldWidth * 2 + getMaxWidth(d3.selectAll('.patientID')) + features.length * gridFieldHeight / 2 + 40

            let cluster = d3.layout.cluster()
                .size([colDendrogramWidth, w])
                .separation(() => 1)

            let diagonal = d3.svg.diagonal()
                .projection(d => [d.x, - spacing - w + d.y])

            let colDendrogramNodes = cluster.nodes(colDendrogram)
            let colDendrogramLinks = cluster.links(colDendrogramNodes)

            heatmap.selectAll('.colDendrogramLink')
                .data(colDendrogramLinks).enter()
                .append('path')
                .attr('class', 'colDendrogram link')
                .attr('d', diagonal)

            heatmap.selectAll('.colDendrogramNode')
                .data(colDendrogramNodes).enter()
                .append('circle')
                .attr('class', 'colDendrogram node')
                .attr('r', 4.5)
                .attr('transform', d => `translate(${d.x}, ${- spacing - w + d.y})`)
                .on('click', d => {
                    let previousSelection = selectedPatientIDs.slice()
                    unselectAll()
                    let leafs = d.index.split(' ')
                    leafs.forEach(leaf => selectCol(patientIDs[leaf]))
                    if (arrEqual(previousSelection, selectedPatientIDs)) unselectAll()
                })
                .on('mouseover', d => {
                    tooltip
                        .style('visibility', 'visible')
                        .html('Height: ' + d.height)
                        .style('left', mouseX() + 'px')
                        .style('top', mouseY() + 'px')
                })
                .on('mouseout', () => tooltip.style('visibility', 'hidden'))
            colDendrogramVisible = true
        }

        let rowDendrogramVisible = false
        let rowDendrogram
        function createRowDendrogram() {
            let h = 280
            let rowDendrogramHeight = gridFieldWidth * numberOfClusteredRows
            let spacing = gridFieldWidth + getMaxWidth(d3.selectAll('.probe')) + 20

            let cluster = d3.layout.cluster()
                .size([rowDendrogramHeight, h])
                .separation(() => 1)

            let diagonal = d3.svg.diagonal()
                .projection(d => [width + spacing + h - d.y, d.x])

            let rowDendrogramNodes = cluster.nodes(rowDendrogram)
            let rowDendrogramLinks = cluster.links(rowDendrogramNodes)

            heatmap.selectAll('.rowDendrogramLink')
                .data(rowDendrogramLinks).enter().append('path')
                .attr('class', 'rowDendrogram link')
                .attr('d', diagonal)

            heatmap.selectAll('.rowDendrogramNode')
                .data(rowDendrogramNodes).enter().append('circle')
                .attr('class', 'rowDendrogram node')
                .attr('r', 4.5)
                .attr('transform', d => `translate(${width + spacing + h - d.y}, ${d.x})`)
                .on('click', d => {
                    alert('Under Construction.')
                })
                .on('mouseover', d => {
                    tooltip
                        .style('visibility', 'visible')
                        .html('Height: ' + d.height)
                        .style('left', mouseX() + 'px')
                        .style('top', mouseY() + 'px')
                })
                .on('mouseout', () => tooltip.style('visibility', 'hidden'))
            rowDendrogramVisible = true
        }

        function removeColDendrogram() {
            heatmap.selectAll('.colDendrogram').remove()
            colDendrogramVisible = false
        }

        function removeRowDendrogram() {
            heatmap.selectAll('.rowDendrogram').remove()
            rowDendrogramVisible = false
        }

        function updateColOrder(sortValues) {
            patientIDs = sortValues.map(sortValue => patientIDs[sortValue])
            unselectAll()
            removeColDendrogram()
            updateHeatmap()
        }

        function updateRowOrder(sortValues) {
            let sortedProbes = []
            let sortedGeneSymbols = []
            let sortedSignificanceValues = []
            sortValues.forEach(sortValue => {
                sortedProbes.push(probes[sortValue])
                sortedGeneSymbols.push(geneSymbols[sortValue])
                sortedSignificanceValues.push(significanceValues[sortValue])
            })
            probes = sortedProbes
            geneSymbols = sortedGeneSymbols
            significanceValues = sortedSignificanceValues
            removeRowDendrogram()
            updateHeatmap()
            animateCutoff()
        }

        function transformClusterOrderWRTInitialOrder(clusterOrder, initialOrder) {
            return clusterOrder.map(d => initialOrder.indexOf(d))
        }

        function getInitialRowOrder() {
            return probes.map(probe => originalProbes.indexOf(probe))
        }

        function getInitialColOrder() {
            return patientIDs.map(patientID => originalPatientIDs.indexOf(patientID))
        }

        let lastUsedClustering = null
        function cluster(clustering) {
            // Nothing should be done if clustering switches are turned on without clustering type set.
            if (!lastUsedClustering && typeof clustering === 'undefined') return
            clustering = (typeof clustering === 'undefined') ? lastUsedClustering : clustering
            let clusterData = data[clustering]
            if (rowClustering && numberOfClusteredRows > 0) {
                rowDendrogram = JSON.parse(clusterData[3])
                updateRowOrder(transformClusterOrderWRTInitialOrder(clusterData[1], getInitialRowOrder()))
                createRowDendrogram(rowDendrogram)
            } else {
                removeRowDendrogram()
            }
            if (colClustering && numberOfClusteredColumns > 0) {
                colDendrogram = JSON.parse(clusterData[2])
                updateColOrder(transformClusterOrderWRTInitialOrder(clusterData[0], getInitialColOrder()))
                createColDendrogram(colDendrogram)
            } else {
                removeColDendrogram()
            }
            lastUsedClustering = clustering
        }

        function loadRows(maxRows=probes.length + 100) {
            let data = prepareFormData()
            data = addSettingsToData(data, { maxRows })
            loadFeatureButton.select('text').text('Loading...')
            $.ajax({
                url: pageInfo.basePath + '/SmartR/recomputeOutputDIV',
                type: 'POST',
                timeout: '600000',
                data: data
            }).done(response => {
                $('#outputDIV').html(response)
                loadFeatureButton.select('text').text('Load 100 additional rows')
                cuttoffButton.select('text').text('Apply Cutoff')
            }).fail(() => {
                $('#outputDIV').html('An unexpected error occurred. This should never happen. Ask your administrator for help.')
                loadFeatureButton.select('text').text('Load 100 additional rows')
                cuttoffButton.select('text').text('Apply Cutoff')
            })
        }

        function switchRowClustering() {
            rowClustering = !rowClustering
            cluster()
        }

        function switchColClustering() {
            colClustering = !colClustering
            cluster()
        }

        function init() {
            updateHeatmap()
            reloadDendrograms()
            updateColors(0)
        }
        init()

        let buttonWidth = 200
        let buttonHeight = 40
        let padding = 20

        createD3Switch({
            location: heatmap,
            onlabel: 'Animation ON',
            offlabel: 'Animation OFF',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 0 + padding * 0,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchAnimation,
            checked: true
        })

        createD3Switch({
            location: heatmap,
            onlabel: 'Clustering rows ON',
            offlabel: 'Clustering rows OFF',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 5 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchRowClustering,
            checked: rowClustering
        })

        createD3Switch({
            location: heatmap,
            onlabel: 'Clustering columns ON',
            offlabel: 'Clustering columns OFF',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 5 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: switchColClustering,
            checked: colClustering
        })

        createD3Slider({
            location: heatmap,
            label: 'Zoom in %',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 0 + padding * 0 - 10,
            width: buttonWidth,
            height: buttonHeight,
            min: 1,
            max: 200,
            init: 100,
            callback: zoom,
            trigger: 'dragend'
        })

        let loadFeatureButton = createD3Button({
            location: heatmap,
            label: 'Load 100 add. feat.',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 1 + padding * 1,
            width: buttonWidth,
            height: buttonHeight,
            callback: loadRows
        })

        let cuttoffButton = createD3Button({
            location: heatmap,
            label: 'Apply Cutoff',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 2 + padding * 2,
            width: buttonWidth,
            height: buttonHeight,
            callback: cutoff
        })

        createD3Slider({
            location: heatmap,
            label: 'Cutoff',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 2 + padding * 2 - 10,
            width: buttonWidth,
            height: buttonHeight,
            min: significanceValues[significanceValues.length - 1],
            max: significanceValues[0],
            init: significanceValues[significanceValues.length - 1],
            callback: animateCutoff,
            trigger: 'dragend'
        })

        createD3Dropdown({
            location: heatmap,
            label: 'Heatmap Coloring',
            x: 2 - margin.left + padding * 0 + buttonWidth * 0,
            y: 8 - margin.top + buttonHeight * 3 + padding * 3,
            width: buttonWidth,
            height: buttonHeight,
            items: [
                {
                    callback: () => updateColors(0),
                    label: 'Color Sheme 1'
                },
                {
                    callback: () => updateColors(1),
                    label: 'Color Sheme 2'
                },
                {
                    callback: () => updateColors(2),
                    label: 'Color Sheme 3'
                },
                {
                    callback: () => updateColors(3),
                    label: 'Color Sheme 4'
                },
                {
                    callback: () => updateColors(4),
                    label: 'Color Sheme 5'
                }
            ]
        })

        createD3Dropdown({
            location: heatmap,
            label: 'Heatmap Clustering',
            x: 2 - margin.left + padding * 1 + buttonWidth * 1,
            y: 8 - margin.top + buttonHeight * 3 + padding * 3,
            width: buttonWidth,
            height: buttonHeight,
            items: [
                {
                    callback: () => cluster('hclustEuclideanAverage'),
                    label: 'Hierarch.-Eucl.-Average'
                },
                {
                    callback: () => cluster('hclustEuclideanComplete'),
                    label: 'Hierarch.-Eucl.-Complete'
                },
                {
                    callback: () => cluster('hclustEuclideanSingle'),
                    label: 'Hierarch.-Eucl.-Single'
                },
                {
                    callback: () => cluster('hclustManhattanAverage'),
                    label: 'Hierarch.-Manhat.-Average'
                },
                {
                    callback: () => cluster('hclustManhattanComplete'),
                    label: 'Hierarch.-Manhat.-Complete'
                },
                {
                    callback: () => cluster('hclustManhattanSingle'),
                    label: 'Hierarch.-Manhat.-Single'
                }
            ]
        })

    }

    return service
})()
