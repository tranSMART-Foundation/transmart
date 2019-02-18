<html>
<head>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'fractalis.css', plugin: 'transmart-fractalis')}"/>
</head>

<body>

<div class="fjs-tm-block">
    <div class="fjs-tm-spinner">
        <span>Looking for patient ids...</span>
        <div class="fjs-tm-rect1"></div>
        <div class="fjs-tm-rect2"></div>
        <div class="fjs-tm-rect3"></div>
        <div class="fjs-tm-rect4"></div>
        <div class="fjs-tm-rect5"></div>
    </div>
</div>

<div class="fjs-transmart">
    <br/>
    <div class="fjs-tm-control-elements">
        <span class="fjs-tm-headline">Add chart to view:</span>
        <div class="fjs-tm-chart-buttons">
            <button class="fjs-tm-button"
                    value="scatterplot"
                    onclick="fjsService.setChart(this.value)">
                Scatter Plot
            </button>
            <button class="fjs-tm-button"
                    value="boxplot"
                    onclick="fjsService.setChart(this.value)">
                Box Plot
            </button>
            <button class="fjs-tm-button"
                    value="pca"
                    onclick="fjsService.setChart(this.value)">
                PCA
            </button>
            <button class="fjs-tm-button"
                    value="histogram"
                    onclick="fjsService.setChart(this.value)">
                Histogram
            </button>
            <button class="fjs-tm-button"
                    value="survivalplot"
                    onclick="fjsService.setChart(this.value)">
                Survival Plot
            </button>
            <button class="fjs-tm-del-button"
                    value=""
                    onclick="fjsService.resetView()">
                Clear View
            </button>
        </div>

        <span class="fjs-tm-headline">Set chart size:</span>
        <div class="fjs-tm-chart-size">
            <button class="fjs-tm-button fjs-chart-size-btn" value="10" onclick="fjsService.setChartSize(this.value)">10%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="20" onclick="fjsService.setChartSize(this.value)">20%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="30" onclick="fjsService.setChartSize(this.value)">30%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="40" onclick="fjsService.setChartSize(this.value)">40%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="50" onclick="fjsService.setChartSize(this.value)">50%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="60" onclick="fjsService.setChartSize(this.value)">60%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="70" onclick="fjsService.setChartSize(this.value)">70%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="80" onclick="fjsService.setChartSize(this.value)">80%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="90" onclick="fjsService.setChartSize(this.value)">90%</button>
            <button class="fjs-tm-button fjs-chart-size-btn" value="100" onclick="fjsService.setChartSize(this.value)">100%</button>
        </div>

        <span class="fjs-tm-headline">Drop variables of interest:</span>
        <div class="fjs-tm-concept-box-container">
            <div class="fjs-tm-concept-box"></div>
            <button class="fjs-tm-del-button"
                    onclick="fjsService.fjs.clearCache()">
                Clear Cache
            </button>
        </div>
    </div>

    <div class="fjs-tm-charts"></div>

</div>
</body>
</html>
