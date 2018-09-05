/*
 * This class is responsible for parsing the parameters that come in from a
 * TranSMART query.  The concept is that TranSMART comes up with a query that
 * is then driven using the GWAVA service calls back to TranSMART and/or Bioservices.
 * The parameters come in through a file sent with the webstart that becomes 
 * simply arguments to main.  This routine handles these parameters.
 */
package com.pfizer.mrbt.genomics.data;

import java.util.ArrayList;

/**
 *
 * @author henstockpv
 */
public class StartupInfo {
    private String gene;
    private int range;
    private int snpSourceId;
    private int geneSourceId;
    private String pvalStr;
    private ArrayList<Long> studySetModelIndexList = new ArrayList<Long>();


    public StartupInfo(String gene, int range, int snpSourceId, int geneSourceId, 
                                                                String studySetModelIndexStr, String pvalStr) {
        this.gene = gene;
        this.range = range;
        this.snpSourceId = snpSourceId;
        this.geneSourceId = geneSourceId;
        this.studySetModelIndexList = convertToLongList(studySetModelIndexStr);
        this.pvalStr = pvalStr;
    }
    
    /**
     * Returns a list of integers from a comma-delimited list of strings.  If
     * any are not integers, it removes them from the list and returns the
     * others.
     * @param commaDelimitedStr
     * @return 
     */
    private ArrayList<Long> convertToLongList(String commaDelimitedStr) {
        String[] tokens = commaDelimitedStr.split("\\,");
        ArrayList<Long> longList = new ArrayList<Long>(tokens.length);
        for(int i = 0; i < tokens.length; i++) {
            try {
                longList.add(Long.parseLong(tokens[i]));
            } catch(NumberFormatException nfe) {
                System.err.println("Failed to convert to long" + tokens[i]);
            }
        }
        return longList;
    }
    
    public String getGene() {
        return gene;
    }

    public int getGenesourceId() {
        return geneSourceId;
    }

    public ArrayList<Long> getStudySetModelIndexList() {
        return studySetModelIndexList;
    }
    
    /**
     * Returns comma-separated list of the model indices where the model is
     * the study/set/model.
     * @return 
     */
    public String getCommaSeparatedStudySetModelIndices() {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for(Long studySetModelIndex : studySetModelIndexList) {
            if(index > 0) {
                sb.append(",");
            }
            sb.append(studySetModelIndex);
            index++;
        }
        return sb.toString();
    }
    
    

    public int getRange() {
        return range;
    }

    public int getSnpSourceId() {
        return snpSourceId;
    }

    public int getGeneSourceId() {
        return geneSourceId;
    }

    public String getPvalStr() {
        return pvalStr;
    }

    
    /**
     * Parses the input argv arguments and returns a list of the
     * StartupInfo data that can be used to query in order
     * The argv is in the form of:
     * Argv[0]: A comma-separated list of selected model IDs
     * Argv[1]: GENE,RADIUS pairs. If more than one gene is selected, these will be separated by semicolons
     * Argv[2]: The gene annotation source – we currently only support one, so this will always be GRCh37.
     * Argv[3]: The SNP annotation source – arrives as 19 (=HG19) or 18 (=HG18), matching the IDs given by the GetSnpSources webservice.
     * Argv[4]: The selected p-value cutoff – you can ignore this if the application doesn’t support it.
     * 
     * Note, we have kluged the GeneSourceID so that it is always 0!!!!
     * @param argv 
     */
    public static ArrayList<StartupInfo> parse(String[] argv) {
        ArrayList<StartupInfo> startupList = new ArrayList<StartupInfo>();
        final int KLUGE_TRANSMART_GENE_SRC_ID = 0;
        if(argv.length >= 5) {
            String modelsStr = argv[0];
            String[] genesRadiusEntries = argv[1].split("\\;");
            //int geneSrcId       = Integer.parseInt(argv[2]);
            int geneSrcId = KLUGE_TRANSMART_GENE_SRC_ID;
            int snpSrcId        = Integer.parseInt(argv[3]);
            String pValueStr    = argv[4];
            for (int genei = 0; genei < genesRadiusEntries.length; genei++) {
                String[] geneRadius = genesRadiusEntries[genei].split("\\,");
                int radius = Integer.parseInt(geneRadius[1]);
                StartupInfo startupInfo = new StartupInfo(geneRadius[0], radius, snpSrcId, geneSrcId, modelsStr, pValueStr);
                startupList.add(startupInfo);
            }
        }
        return startupList;
    }

    /**
     * Returns a string of vector entries joined by a comma.
     * @param delimiter
     * @param vector
     * @return 
     */
    private static String join(String delimiter, String[] vector) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < vector.length; i++) {
            if(i > 0) {
                sb.append(delimiter);
            }
            sb.append(vector[i]);
        }
        return sb.toString();
    }
}
