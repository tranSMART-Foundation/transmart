//# sourceURL=svgDownload.js

"use strict";

window.smartR.components.svgDownload = function svgArea(containerId, buttonId) {
    var ret = {};

    ret.init = function SvgDownload_init(containerId, buttonId) {
        this.containerEl = jQuery('#' + containerId);
        this.buttonEl = jQuery('#' + buttonId);
        this.buttonEl.on('click', downloadSVG.bind(this));
        deactivateButton.call(this);
        setupMutationObserver.call(this);
    };

    function downloadSVG() {
        // aux for downloadSVG
        var serializer = new XMLSerializer();
        var clonedSvg = copyWithCollapsedCSS.call(this);
        var xmlString = serializer.serializeToString(clonedSvg[0]);
        var blob = new Blob([xmlString], { type: 'image/svg+xml' });
        var svgBlobUrl = URL.createObjectURL(blob);
        var link = jQuery('<a/>')
            .attr('href', svgBlobUrl)
            .attr('download', 'heatmap.svg')
            .css('display', 'none');
        jQuery('body').append(link);
        link[0].click();
        link.remove();
        URL.revokeObjectURL(svgBlobUrl);
        clonedSvg.remove();
    };

    function setupMutationObserver() {
        var observer = new MutationObserver(function() {
            if (this.containerEl.children('svg').length > 0) {
                activateButton.call(this);
            } else {
                deactivateButton.call(this);
            }
        }.bind(this));

        observer.observe(this.containerEl[0], { childList: true, });
    }

    function deactivateButton() {
        this.buttonEl.prop('disabled', true);
    }

    function activateButton() {
        this.buttonEl.prop('disabled', false);
    }

    // aux for downloadSVG
    function copyWithCollapsedCSS() {
        var heatmapElement = this.containerEl.children('svg');
        var relevantProperties = [
            'fill-opacity', 'fill', 'stroke', 'font-size', 'font-family',
            'shape-rendering', 'stroke-width'
        ];
        var clonedSvg = heatmapElement.clone().attr('display', 'none');

        if (clonedSvg.size() != 1) {
            throw new Error('Expected to find only one svg element');
        }

        clonedSvg.insertAfter(heatmapElement[0]);

        var cachedDefaults = {};
        var scratchSvg = jQuery(document.createElement('svg'))
            .attr('display', 'none')
            .appendTo(jQuery('body'));

        function getDefaultsForElement(jqElement) {
            var nodeName = jqElement.prop('nodeName');
            if (!cachedDefaults[nodeName]) {
                var newElement = jQuery(document.createElement(nodeName))
                    .appendTo(scratchSvg);

                cachedDefaults[nodeName] = window.getComputedStyle(newElement[0]);
            }
            return cachedDefaults[nodeName];
        }

        clonedSvg.find('*').each(function(idx, element) { // for each element in <svg>
            var computedStyle = window.getComputedStyle(element);

            var jqElem = jQuery(element);
            relevantProperties.forEach(function(property) { // for each property
                var effectiveStyle = computedStyle.getPropertyValue(property);
                var defaultStyle = getDefaultsForElement(jqElem).getPropertyValue(property);

                if (effectiveStyle != defaultStyle) {
                    jqElem.attr(property, effectiveStyle);
                }
            })
        });

        scratchSvg.remove();

        return clonedSvg;
    }

    ret.init(containerId, buttonId);

    return ret;
};
