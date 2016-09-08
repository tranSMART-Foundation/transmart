'use strict';

module.exports = function(config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],

        plugins: [
            'karma-jasmine',
            'karma-phantomjs-launcher',
            'karma-ng-html2js-preprocessor',
            'karma-remote-reporter'
        ],        

        // list of files / patterns to load in the browser
        files: [
            // libraries
            'web-app/js/resource/jquery-2.1.4.min.js',
            'web-app/js/resource/angular.js',
            'web-app/js/resource/angular-route.js',
            'web-app/js/resource/angular-css.js',
            'web-app/js/resource/d3.min.js',

            // stuff for testing only
            'web-app/js/resource/angular-mocks.js',
            'test/unit/javascript/testSetup.js',
            // 'node_modules/karma-ng-html2js-preprocessor/lib/html2js.js',

            // application code
            'web-app/js/smartR/_angular/services/smartRUtils.js',
            'web-app/js/smartR/_angular/services/rServeService.js',
            'web-app/js/smartR/_angular/directives/fetchButton.js',
            'web-app/js/smartR/_angular/directives/runButton.js',

            // templates
            'web-app/js/smartR/_angular/templates/*.html',

            // test files
            'test/unit/javascript/**/*.js'
//      'test/functional/javascript/**/*.js'
        ],

        // list of files to exclude
        exclude: [
        ],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            'web-app/js/smartR/_angular/templates/*.html': 'ng-html2js'
        },

        ngHtml2JsPreprocessor: {
            stripPrefix: 'web-app',
            moduleName: 'smartRTemplates'
        },
        
        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'remote'],

        remoteReporter: {
            host: 'localhost',
            port: '9889'
        },


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: true,

        // Concurrency level
        // how many browser should be started simultaneous
        concurrency: Infinity
    });
};
