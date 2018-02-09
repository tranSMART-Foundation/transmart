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
    const stateIDs = window.location.href.match(/\/fractalis\/state\/(.+)/)[1].split('+');
    fetch('${url}', {method: 'GET', redirect: 'follow', credentials: 'same-origin'})
        .then(function(res) { return res.text(); })
        .then(function(html) {
            const doc = document.open("text/html", "replace");
            doc.write(html);
            doc.close();
            const interval = setInterval(function() {
                if (typeof window.fjs === 'object') {
                    clearInterval(interval);
                    fjs_handleStateIDs(stateIDs);
                }
            }, 100);
        });
</script>