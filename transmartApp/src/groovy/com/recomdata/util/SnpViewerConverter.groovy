package com.recomdata.util

import groovy.util.logging.Slf4j

@Slf4j('logger')
class SnpViewerConverter {

    private String gtcFileName
    private String snpFileName
    private String sampleFileName

    void generateSNPFile() {
        File snpFile = new File(snpFileName)
	File sampleFile = new File(sampleFileName)

        int numSet = 0

	for (String line in new File(gtcFileName).readLines()) {
	    if (!line.contains('#')) {
		if (line.indexOf('AFFX-SNP') != 0 && line.lastIndexOf('SNP_') > 0) {
		    logger.error 'Series Error: input file has concatenated lines! : {}', line
                }

		if (line.contains('Probe Set ID')) {
		    String[] colNames = line.split('\\t')
                    numSet = (colNames.length - 1 - 3) / 7
                    String[] setNames = new String[numSet]

                    snpFile << 'SNP\tChromosome\tPhysicalPosition'

                    for (int i = 0; i < numSet; i++) {
                        String setName = colNames[1 + i * 7]
                        setName = setName.substring(0, setName.indexOf('.'))
                        setNames[i] = setName
			snpFile << '\t' + setName + '_Allele_A\t' + setName + '_Allele_B\t' + setName + ' Call'
                    }
                    snpFile << '\n'

                    // Prepare the sampleInfo.text for CopyNumberDevideByNormals
		    sampleFile << 'Array\tSample\tType\tPloidy(numeric)\tGender\tPaired\n'
                    for (int j = 0; j < numSet; j = j + 2) {
                        String normalName = null, tumorName = null
                        for (int k = 0; k < 2; k++) {
                            String setName = setNames[j + k]
			    if (setName.endsWith('N')) {
                                normalName = setName
			    }
			    else if (setName.endsWith('T')) {
                                tumorName = setName
                            }
			}
			sampleFile << normalName + '\t' + normalName + '\tcontrol\t2\tF\tYes\n'
			sampleFile << tumorName + '\t' + tumorName + '\tovarian_tumor\t2\tF\t' + normalName + '\n'
                    }
                }
                else {
                    String[] values = line.split('\\t')
                    if (values.length == (1 + numSet * 7 + 2 + 1)) {
                        // Some line has 'rs11111	---' at the end. Skip this kind of lines
			if (!values[1 + numSet * 7 + 1].equalsIgnoreCase('MT')) {
                            // Affy 6.0 SNP Array has probes for mitochondria DNA. GenePattern SNPFileSorter cannot handle that. Skip
                            String probeId = values[0]
                            String chrom = values[1 + numSet * 7 + 1]
                            String chromPos = values[1 + numSet * 7 + 2]
			    snpFile << probeId + '\t' + chrom + '\t' + chromPos

                            for (int i = 0; i < numSet; i++) {
                                String value_a = values[1 + i * 7 + 3]
                                String value_b = values[1 + i * 7 + 4]
                                String value_call = values[1 + i * 7]
				if (value_call.equalsIgnoreCase('NoCall')) {
                                    value_call = 'No'
				}

				snpFile << '\t' + value_a + '\t' + value_b + '\t' + value_call
                            }
                            snpFile << '\n'
                        }
                    }
                }
            }
        }
    }

    private void checkGTCFile() {
	String gtcFileName = 'C:\\Project\\Transmart\\SNPView\\Background\\GSE19539_Result\\GSE19539_genotype_022-201_10.txt'
	String gtcFileNewName = 'C:\\Project\\Transmart\\SNPView\\Background\\GSE19539_Result\\GSE19539_genotype_022-201_10_new.txt'
        File gtcFileNew = new File(gtcFileNewName)

	for (String line in new File(gtcFileName).readLines()) {
            if (line.indexOf('#') != 0 && line.indexOf('AFFX-SNP') != 0 && line.lastIndexOf('SNP_') > 0) {
		logger.error 'Series Error: input file has concatenated lines! : {}', line
		gtcFileNew << line.replaceAll('SNP_', '\nSNP_').trim() + '\n'
            }
            else {
		gtcFileNew << line + '\n'
            }
        }
    }

    private void loadConfiguration(File file) throws IOException {
        Properties prop = new Properties()
        FileInputStream fis = new FileInputStream(file)
	prop.load fis
        fis.close()

        String destinationDirectoy = prop.getProperty('destination_directory')

	gtcFileName = prop.getProperty('source_directory') + '/' + prop.getProperty('raw_snp_file')
	snpFileName = destinationDirectoy + '/' + prop.getProperty('output_snp_file')
	sampleFileName = destinationDirectoy + '/' + prop.getProperty('sample_file')
    }

    static void main(String[] args) {
        SnpViewerConverter svc = new SnpViewerConverter()
	File path = new File(SnpViewerConverter.protectionDomain.codeSource.location.path)
	svc.loadConfiguration new File(path.parent, 'SnpViewer.properties')
        svc.generateSNPFile()
    }
}
