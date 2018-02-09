<!DOCTYPE html>
<html>
<head>
    <r:require module="jquery"/>
    <r:layoutResources/>
</head>
<body>
    <r:layoutResources/>
</body>
</html>

<script type="text/javascript">
    var matches = window.location.href.match(/\/fractalis\/state\/(.+)/);
    var stateIDs = matches == null ? [] : matches[1].split('+');
    fetch('${url}', {method: 'GET', redirect: 'follow', credentials: 'same-origin'})
        .then(function(res) { return res.text(); })
        .then(function(html) {
            var doc = document.open("text/html", "replace");
            doc.write(html);
            doc.close();
            var interval = setInterval(function() {
                if (typeof window.fjs === 'object') {
                    if (stateIDs.length) {
                        fjs_handleStateIDs(stateIDs);
                    } else {
                        fjs_resetUrl();
                    }
                    clearInterval(interval);
                }
            }, 100);
        });
</script>