# Helper function: shift vector number of positions
shiftRow <- function(x, shift=1L) 
{
  	ind        <- (1L + shift):(length(x) + shift)
  	ind[ind<1] <- NA
  	
	return(x[ind])
}

#######################################################################################
# General frequency-plot function. It takes the number of probes as the horizontal axis.
# This might result in so called "accordeon" chromosomes

freqPlot_simple <- function (column, groupnames, phenodata, calls, data.info){
  # Determine 'gain' and 'loss' for each group
  for (group in groupnames)
  {
      group.samples <- which(phenodata[,column] == group & !is.na(phenodata[,column]))
      group.ids     <- phenodata[group.samples, "PATIENT_NUM"]
      highdimColumnsMatchingGroupIds <- match(paste("flag.",group.ids,sep=""), colnames(calls))
      highdimColumnsMatchingGroupIds <- highdimColumnsMatchingGroupIds[which(!is.na(highdimColumnsMatchingGroupIds))]
      group.calls   <- calls[ , highdimColumnsMatchingGroupIds, drop=FALSE]

      # We only use the values we know (hom.del, loss, gain, ampl)
      data.info[, paste('gain.freq.', group, sep='')] <- rowSums(group.calls==1  | group.calls==2 ) / ncol(group.calls)
      data.info[, paste('loss.freq.', group, sep='')] <- rowSums(group.calls==-1 | group.calls==-2) / ncol(group.calls)
  }


  # Helper function to create frequency-plot for 1 group
  FreqPlot <- function(data, group, main = 'Frequency Plot',...)
  {
    par(mar=c(5,4,4,5) + 0.1)
    cols <- c('blue', 'red')
    names(cols) <- c('gain', 'loss')

    chromosomes <- data$chromosome
    a.freq <- data[,paste('gain', '.freq.', group, sep='')]
    b.freq <- data[,paste('loss', '.freq.', group, sep='')]

    if ('num.probes' %in% colnames(data) & !any(is.na(data$num.probes))) {
      chromosomes <- rep(chromosomes, data$num.probes)
      a.freq <- rep(a.freq, data$num.probes)
      b.freq <- rep(b.freq, data$num.probes)
    }

    # Make clear the horizontal axis is based on number of probes
    plot(a.freq, ylim=c(-1,1), type='h', col=cols['gain'], xlab='chromosome (number of probes)', ylab='frequency', xaxt='n', yaxt='n', main=main, ...)
    points(-b.freq, type='h', col=cols['loss'])
    abline(h=0)
    abline(v=0, lty='dashed')
    cs.chr = cumsum(table(chromosomes))
    for(i in cs.chr)
      abline(v=i, lty='dashed')

    ax <- (cs.chr + c(0,cs.chr[-length(cs.chr)])) / 2
    lbl.chr <- unique(chromosomes)
    lbl.chr[lbl.chr==0] <- 'U'
    lbl.chr[lbl.chr==23] <- 'X'
    lbl.chr[lbl.chr==24] <- 'XY'
    lbl.chr[lbl.chr==25] <- 'Y'
    lbl.chr[lbl.chr==26] <- 'M'

    # Check if chromosomes (labels) are loaded properly
    if (length(ax) != length(lbl.chr)) {
        stop("||FRIENDLY||There is an error in the chromosome/region data. It may not be loaded properly."); return()
    }

    axis(side=1, at=ax, labels=lbl.chr, las=2)
    axis(side=2, at=c(-1, -0.5, 0, 0.5, 1), labels=c('100 %', ' 50 %', '0 %', '50 %', '100 %'), las=1)
    labels <- c(0.01, 0.05, 0.025, 0.1, 0.25, 0.5, 1)
    axis(side=4, at=-log10(labels) - 1, labels=labels, las=1)
    mtext('gain', side=2, line=3, at=0.5, col=cols['gain'])
    mtext('loss', side=2, line=3, at=-0.5, col=cols['loss'])
  }

  # Create the Plots.
  filename <- paste('frequency-plot','.png',sep='')
  CairoPNG(file=filename, width=1000, height=length(groupnames) * 400)
  par(mfrow = c(length(groupnames),1))
  for (group in groupnames)
  {
    FreqPlot(data.info, group, paste('Frequency Plot for "', group, '"', sep=''))
  }

  dev.off()
}

#######################################################################################
# frequency-plot function. It uses the package qDNAseq (bioConductor) to do that.
# Restriction: defined chromosomal-regions in "data.info" cannnot overlap.

freqPlot_qdnaseq <- function (column, groupnames, phenodata, calls, data.info){
  library(Biobase)
  library(QDNAseq)

  # We need a "QDNAseqCopyNumbers" structure.
  data(LGG150)
  readCounts <- LGG150
  readCountsFiltered <- applyFilters(readCounts)
  readCountsFiltered <- estimateCorrection(readCountsFiltered)
  copyNumbers <- correctBins(readCountsFiltered)
  copyNumbersNormalized <- normalizeBins(copyNumbers)
  copyNumbersSmooth <- smoothOutlierBins(copyNumbersNormalized)
  copyNumbersSegmented <- segmentBins(copyNumbersSmooth)
  copyNumbersSegmented <- normalizeSegmentedBins(copyNumbersSegmented)
  copyNumbersCalled <- callBins(copyNumbersSegmented)
  unlockBinding("calls", copyNumbersCalled@assayData)
  
  # Determine 'gain' and 'loss' for each group
  filename <- paste('frequency-plot','.png',sep='')
  CairoPNG(file=filename, width=1000, height=length(groupnames) * 400)
  par(mfrow = c(length(groupnames),1))
  for (group in groupnames) 
  {
      group.samples <- which(phenodata[,column] == group & !is.na(phenodata[,column]))
      group.ids     <- phenodata[group.samples, "PATIENT_NUM"]
      highdimColumnsMatchingGroupIds <- match(paste("flag.",group.ids,sep=""), colnames(calls))
      highdimColumnsMatchingGroupIds <- highdimColumnsMatchingGroupIds[which(!is.na(highdimColumnsMatchingGroupIds))]
      group.calls   <- calls[ , highdimColumnsMatchingGroupIds, drop=FALSE]

      temp <- data.frame(data.info[,1:3])
      row.names(temp) <- paste(data.info[,1], ":", data.info[,2], "-", data.info[,3], sep="")
      copyNumbersCalled@featureData@data <- temp
      copyNumbersCalled@assayData$calls <- group.calls
      # Like to use "xlab", but results in error with R 3.2 (OK with R 3.3) ?
      frequencyPlot(copyNumbersCalled, main=paste('Frequency Plot for "', group, '"', sep=''), sub='(base pairs)')
      #frequencyPlot(copyNumbersCalled, main=paste('Frequency Plot for "', group, '"', sep=''), xlab='chromosome (base pairs)')
  }

  dev.off()
}


#######################################################################################

acgh.frequency.plot <- function ( column = 'group') {

  library(reshape2)
  library(Cairo)
  
  # read the data
  dat       <- read.table('outputfile.txt'  , header=TRUE, sep='\t', quote='"', as.is=TRUE      , check.names=FALSE, stringsAsFactors = FALSE)
  phenodata <- read.table('phenodata.tsv', header=TRUE, sep='\t', quote='"', strip.white=TRUE, check.names=FALSE, stringsAsFactors = FALSE)

  # Determine the groups (NA and '' are discarded)
  groupnames <- unique(phenodata[,column])
  groupnames <- groupnames[!is.na(groupnames)]
  groupnames <- groupnames[groupnames!='']

  # Make sure "dat" is properly ordered (chromosome,start)
  dat$chromosome[dat$chromosome=='X'] <- '23'
  dat$chromosome[dat$chromosome=='XY'] <- '24'
  dat$chromosome[dat$chromosome=='Y'] <- '25'
  dat$chromosome[dat$chromosome=='M'] <- '26'
  dat$chromosome[dat$chromosome=='[:alpha:]'] <- '0'
  dat$chromosome[dat$chromosome==''] <- '0'
  dat$chromosome <- as.integer(dat$chromosome)
  # Do the ordering
  dat <- dat[with(dat,order(chromosome,start)),]
  dat$chromosome[dat$chromosome==0] <- '0'
  dat$chromosome[dat$chromosome==23] <- 'X'
  dat$chromosome[dat$chromosome==24] <- 'XY'
  dat$chromosome[dat$chromosome==25] <- 'Y'
  dat$chromosome[dat$chromosome==26] <- 'M'

  # get the data-information columns
  first.data.col <- min(grep('chip', names(dat)), grep('flag', names(dat)))
  data.info      <- dat[,1:(first.data.col-1)]

  # We only need the flag-columns (posible values: [-1,0,1,2] -> [loss,norm,gain,amp])
  calls <- as.matrix(dat[,grep('flag', colnames(dat)), drop=FALSE])

  # Check if we are having overlapping regions (qDANseq cannot handle this)
  overlapping <- data.info[(data.info$start      <  shiftRow(data.info$end, -1)       ) & 
                           (data.info$chromosome == shiftRow(data.info$chromosome, -1)), ]

  if (nrow(overlapping) > 1)
  {
	print("Using simple approach...")
        print(nrow(overlapping))
        freqPlot_simple(column, groupnames, phenodata, calls, data.info)
  }
  else
  {
	print("Using qDNAseq")
        freqPlot_qdnaseq(column, groupnames, phenodata, calls, data.info)
  }

}


