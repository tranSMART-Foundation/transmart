<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="${resource(dir: 'css/XNAT', file: 'ng-table.css')}"  />
    <link rel="stylesheet" href="${resource(dir: 'css/XNAT', file: 'session.css')}" />
    <link rel="stylesheet" href="${resource(dir: 'css/XNAT', file: 'bootstrap.min.css')}" />
    <title>Sessions</title>
</head>

<body>
<div id="main" ng-app="main">
    <div ng-controller="SessionCtrl">
        <div id="scanPanel">
            <img id="scanThumbnail" ng-src="{{imageURL}}"/>
        </div>

        <table ng-table="tableParams" class="table">
            <tbody ng-repeat="session in getSessionData()">
            <tr ng-repeat="scan in session.scans">
                <td data-title="'Session'">{{showSessionLabel($index,session.name)}}</td>
                <td data-title="'Series Type'">{{session.scans[$index].series}}</td>

                <td data-title="'Info'"><a href={{session.scans[$index].info}} target="_blank">Dicom</a></td>

                <td data-title="'Thumbnail'"><a href="#" ng-click="setImage(session.scans[$index].thumbnail)">{{session.scans[$index].id}}</a></td>
                <td data-title="'Download'"><a href={{session.scans[$index].download}} target="_blank">Download</a></td>
            </tr>
            </tbody>
        </table>
    </div>
</div>
<script src="${resource(dir: 'js/XNAT', file: 'angular.min.js')}" type="text/javascript"></script>
<script src="${resource(dir: 'js/XNAT', file: 'ng-table.min.js')}" type="text/javascript"></script>
<script src="${resource(dir: 'js/XNAT', file: 'script.js')}"></script>
<!--<script src="src/extern/jquery-2.1.0.min.js" type="text/javascript"></script>-->
</body>
</html>