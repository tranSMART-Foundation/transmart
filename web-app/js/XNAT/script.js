var app = angular.module('main', ['ngTable']).
    controller('SessionCtrl',

    function($scope, $http, $rootScope, ngTableParams) {

        $scope.data = null;
        var subjectID = null;

        $scope.setImage = function(newImageURL)
        {
            $scope.imageURL = newImageURL;
        }
        //$scope.imageURL = data[0].image;

        $scope.setSubjectID = function(value) {
            //console.log(value);
            subjectID = value;
        }

        $scope.getSessionData = function() {
            return $scope.data;
        }

        $scope.showSessionLabel = function (index, sessionName)
        {
            if (index == 0)
                return sessionName;
            else
                return ' ';
        }

        $scope.showSessionInfo = function (index)
        {
            if (index == 0)
                return 'DICOM';
            else
                return ' ';
        }

        $scope.getData = function () {

            var parameter = window.location.search.replace( "?", "" ); // will return the GET parameter

            var value = parameter.split("=");
            var basePath = window.location.href;

            var arr = basePath.split("/");

            //var url = arr[0] + "//" + arr[2] + '/' + arr[3] + '/Scan/getListData?subjectID='+ value[1];
            var url = arr[0] + "//" + arr[2] + '/' + arr[3] + '/Scan/getAPIListData?subjectID='+ value[1];

            console.log(url);

            $http.get(url).
                success(function (newdata) {
                    console.log("data " + newdata);
                    data = newdata;
                    $scope.setImage("http://www.placehold.it/325x325/e8e8e8/999999&text=no+image+selected");
                    $scope.setSubjectID(value[1]);

                    $scope.tableParams = new ngTableParams({
                        page: 1,            // show first page
                        count: 10           // count per page
                    }, {
                        total: data.length, // length of data
                        getData: function($defer, params) {
                            $defer.resolve(data.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                        }
                    });

                }).error(function (newdata) {
                    console.log("error: " + newdata);
                });
        }

        $scope.getAPIData = function () {

            var parameter = window.location.search.replace( "?", "" ); // will return the GET parameter

            var value = parameter.split("=");
            var basePath = window.location.href;

            var arr = basePath.split("/");

            var url = arr[0] + "//" + arr[2] + '/' + arr[3] + '/Scan/getAPIListData?subjectID='+ value[1];

            $http.get(url).
                success(function (newdata) {
                    $scope.data = newdata;
                    $scope.setImage("http://www.placehold.it/325x325/e8e8e8/999999&text=no+image+selected");
                    $scope.setSubjectID(value[1]);
                    //console.log("here "+ $scope.data);
                    $scope.tableParams = new ngTableParams({
                        page: 1,            // show first page
                        count: 10           // count per page
                    }, {
                        total: $scope.data.length, // length of data
                        getAPIData: function($defer, params) {
                            $defer.resolve($scope.data.slice((params.page() - 1) * params.count(), params.page() * params.count()));
                        }
                    });
                }).error(function (newdata) {
                    console.log('test2');
                    console.log("error: " + newdata);
                });
        }

        //$scope.getData();
        $scope.getAPIData();
    });




