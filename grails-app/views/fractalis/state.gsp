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
    const matches = window.location.href.match(/\/fractalis\/state\/(.+)/)
    const stateIDs = matches == null ? [] : matches[1].split('+')
    fetch('${url}', {method: 'GET', redirect: 'follow', credentials: 'same-origin'})
        .then(res => res.text())
        .then(html => {
            const doc = document.open("text/html", "replace")
            doc.write(html)
            doc.close()
            const interval = setInterval(() => {
                if (typeof window.fjs === 'object') {
                    if (stateIDs.length) {
                        fjsService.handleStateIDs(stateIDs)
                    } else {
                        fjsService.resetUrl()
                    }
                    clearInterval(interval)
                }
            }, 100)
        })
</script>
