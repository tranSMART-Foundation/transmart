$j(function() {
    /**
     * init heatmap
     */
    var init = function () {

        // Nothing happens yet. just dummy scripts for testing
        // ==================================================
        $j( '#tabs' ).tabs();

        var availableTags = [
            'ActionScript',
            'AppleScript',
            'Asp',
            'BASIC',
            'C',
            'C++',
            'Clojure',
            'COBOL',
            'ColdFusion',
            'Erlang',
            'Fortran',
            'Groovy',
            'Haskell',
            'Java',
            'JavaScript',
            'Lisp',
            'Perl',
            'PHP',
            'Python',
            'Ruby',
            'Scala',
            'Scheme'
        ];

        $j( '#tags' ).autocomplete({
            source: availableTags
        });

        var _rSession = $j.ajax({
                url: pageInfo.basePath + '/RSession/create',
                type: 'POST',
                timeout: '600000'
            }).done(function(response) {
                 // console.log(response);
                if (response.hasOwnProperty('sessionId')) {
                    GLOBAL.HeimAnalysis = {
                        rSessionId :response.sessionId
                    };
                }
            }).fail(function() {
                // TODO: error displayed in a placeholder somewhere in main heim-analysis page
                console.error('Cannot create r-session');
            });

    };

    // where everything starts
    init();

});




