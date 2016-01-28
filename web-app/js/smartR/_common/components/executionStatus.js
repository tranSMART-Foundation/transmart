//# sourceURL=executionStatus.js

"use strict";

window.smartR.components.executionStatus = function executionStatus() {

    var CLASS_FOR_PARENT_DIV = 'sr-status-and-output-container';
    var CLASS_FOR_STATUS_DIV = 'sr-tab-status';

    var CLASS_FOR_PROGRESS = 'sr-status-progress';
    var CLASS_FOR_ERROR = 'sr-status-error';

    function getCurrentStage() {
        var currentTabId = jQuery('#heim-tabs')
            .find('.ui-tabs-active')
            .attr('aria-controls');

        return currentTabId.replace(/fragment-/, '');
    }

    function getContainerOfTab(stage) {
        stage = stage || getCurrentStage();
        return jQuery('div#' + 'fragment-' + stage)
            .children('.' + CLASS_FOR_PARENT_DIV);
    }

    function getCurrentStatusDiv(container) {
        return container.children('.' + CLASS_FOR_STATUS_DIV);
    }

    function buildErrorMessage(error) {
        var ret;
        
        if (!error) {
            ret = 'Failure with no details.';
        } else if (error.statusText !== undefined) {
            if (error.status) { // for the 0, undefined cases
                 ret = 'Error ' + error.status + ': ' + error.statusText + '.';
            } else {
                ret = error.statusText;
            }

            if (error.response && error.response.type && error.response.message) {
                ret += ' ' + error.response.type + ': ' + error.response.message;
            }
        } else {
            ret = error;
        }

        return ret;
    }

    return {
        clear: function StatusArea_clear(stage) {
            // clear everything if stage is not given
            if (!stage) {
                jQuery('.' + CLASS_FOR_STATUS_DIV).remove();
            } else {
                getContainerOfTab(stage)
                    .children('.' + CLASS_FOR_STATUS_DIV).remove();
            }
        },

        set: function StatusArea_set(message, extraClasses, stage) {
            var container = getContainerOfTab(stage);
            var statusDiv = getCurrentStatusDiv(container);

            extraClasses = extraClasses || [];

            if (statusDiv.size() == 0) {
                statusDiv = jQuery('<div>');
                container.prepend(statusDiv);
            }

            statusDiv.attr('class', CLASS_FOR_STATUS_DIV);

            extraClasses.forEach(function(c) {
                statusDiv.addClass(c);
            });

            statusDiv.html(message);
        },

        bindPromise: function StatusArea_bindPromise(promise, progressMessage) {
            var finalContent = progressMessage + ', please wait<span class="blink_me">_</span>';
            this.set(finalContent, [CLASS_FOR_PROGRESS]);

            promise
                .done(function() { this.clear(); }.bind(this))
                .fail(function(error) {
                    this.set(buildErrorMessage(error), [CLASS_FOR_ERROR]);
                }.bind(this));
        },
    };
};
