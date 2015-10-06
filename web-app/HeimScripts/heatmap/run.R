#Generate example heatmap
x <- rnorm(100)
testData <- matrix(x,10,10)
rownames(testData) <- c("Gene1","Gene2","Gene3","Gene4","Gene5","Gene6","Gene7","Gene8","Gene9","Gene10")
colnames(testData) <- c("Probe1","Probe2","Probe3","Probe4","Probe5","Probe6","Probe7","Probe8","Probe9","Probe10")
png(filename="/tmp/last_heatmap.png")
heatmap(testData)
dev.off()

