/*
 * Table model to support the markers for the given plot.  It is used in the
 * MarkerModificationPane as the primary display.
 */
package com.pfizer.mrbt.genomics.resultstable;

import com.pfizer.mrbt.genomics.hline.*;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Peter V. Henstock
 */
public class ResultsTableModel extends AbstractTableModel {

    public final static int INDEX_COL = 0;
    public final static int GENE_COL = 1;
    public final static int MODEL_COL = 2;
    public final static int RSID_COL = 3;
    public final static int SNP_GENE_COL = 4;
    public final static int INTRON_EXON_COL = 5;
    public final static int REGULOME_COL = 6;
    public final static int CHR_COL = 7;
    public final static int LOC_COL = 8;
    public final static int PVAL_COL = 9;
    /*private ArrayList<String> genes = new ArrayList<String>();
    private HashMap<String, ArrayList<ModelSnp>> gene2modelSnpList = new HashMap<String, ArrayList<ModelSnp>>();
    private HashMap<String, Integer> gene2len = new HashMap<String, Integer>();*/
    private ArrayList<GeneModelSnp> geneModelSnps = new ArrayList<GeneModelSnp>();
    private DecimalFormat decimalFormat = new DecimalFormat("0.000");
    private String[] headers = {"Index", "Search", "Model", "RsID", "Gene","In/Ex", "Regulome", "Chr", "SNP Loc", "-Log10(P-Value)"};

    @Override
    public Object getValueAt(int row, int col) {
        //HLine line = Singleton.getState().getLines().get(row);
        GeneModelSnpOffset geneModelSnpOffset = computeGeneModelSnpOffset(row);
        String gene = geneModelSnpOffset.getGene();
        Model model = geneModelSnpOffset.getModel();
        //GeneModelSnp geneModelSnp = geneModelSnpOffset.getGeneModelIndex();
        GeneModelSnp geneModelSnp = geneModelSnps.get(geneModelSnpOffset.getGeneModelIndex());
        int offset = geneModelSnpOffset.getOffset();
        int geneModelIndex = geneModelSnpOffset.getGeneModelIndex();
        switch (col) {
            case INDEX_COL:
                return (row+1);
            case GENE_COL:
                return gene;
            case MODEL_COL:
                return model;
            case RSID_COL:
                return geneModelSnp.getSnp(offset).getRsId();
            case SNP_GENE_COL:
                return geneModelSnp.getSnp(offset).getAssociatedGene();
            case INTRON_EXON_COL:
                return geneModelSnp.getSnp(offset).getIntronExon().getTwoLetterString();
            case REGULOME_COL:
                return geneModelSnp.getSnp(offset).getRegulome();
            case CHR_COL:
                //return Singleton.getDataModel().getDataSet(gene).getSnps().get(offset).getLoc();
                int chromosomeNumber = Singleton.getDataModel().getDataSet(geneModelSnp.getGene()).getChromosome();
                return DataModel.getChromosomeString(chromosomeNumber);
            case LOC_COL:
                return geneModelSnp.getSnp(offset).getLoc();
                //return Singleton.getDataModel().getDataSet(gene).getSnps().get(offset).getLoc();
            case PVAL_COL:
                SNP snp = geneModelSnp.getSnp(offset);
                return Singleton.getDataModel().getDataSet(gene).getPvalFromSnpModel(snp, model);
            default:
                return "Unknown";
        }
    }

    /**
     * @deprecated
     */
    public Object getValueAtOld(int row, int col) {
        HLine line = Singleton.getState().getLines().get(row);
        GeneModelSnpOffset geneModelSnpOffset = computeGeneModelSnpOffset(row);
        String gene = geneModelSnpOffset.getGene();
        Model model = geneModelSnpOffset.getModel();
        int offset = geneModelSnpOffset.getOffset();
        int geneModelIndex = geneModelSnpOffset.getGeneModelIndex();
        switch (col) {
            case GENE_COL:
                return gene;
            case MODEL_COL:
                return model;
            case RSID_COL:
                return Singleton.getDataModel().getDataSet(gene).getSnps().get(offset).getRsId();
            case LOC_COL:
                return Singleton.getDataModel().getDataSet(gene).getSnps().get(offset).getLoc();
            case PVAL_COL:
                SNP snp = Singleton.getDataModel().getDataSet(gene).getSnps().get(offset);
                return Singleton.getDataModel().getDataSet(gene).getPvalFromSnpModel(snp, model);
            default:
                return "Unknown";
        }
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }
    
    public String getHeader(int col) {
        return headers[col];
    }

    @Override
    public int getRowCount() {
        int cnt = 0;
        for (GeneModelSnp geneModelSnp : geneModelSnps) {
            cnt += geneModelSnp.getNumSnpEntries();
        }
        return cnt;
    }

    @Override
    public Class getColumnClass(int column) {
        switch(column) {
            case INDEX_COL: 
                return Integer.class;
            case GENE_COL:
                return String.class;
            case MODEL_COL:
                return String.class;
            case RSID_COL:
                return Integer.class;
            case INTRON_EXON_COL:
                return String.class;
            case REGULOME_COL:
                return String.class;
            case CHR_COL:
                return String.class;
            case LOC_COL:
                return Integer.class;
            case PVAL_COL:
                return Double.class;
            default:
                return String.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        return headers[column];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * This routine will add all the snps from the gene and model if they 
     * are not already present.  The case where they may be present is if
     * there is a gene-model in the geneModelSnps in which case we may be
     * adding a more complete set potentially so it checks
     * @param gene
     * @param model 
     */
    public void addIfNotPresent(String gene, Model model) {
        int incomingModelId = model.getId();
        boolean found = false;
        CopyOnWriteArrayList<SNP> incomingSnps = Singleton.getDataModel().getDataSet(gene).getSnps();
        DataSet dataSet = Singleton.getDataModel().getDataSet(gene);
        for(GeneModelSnp geneModelSnp : geneModelSnps) {
            if(geneModelSnp.getModel().getId() == incomingModelId &&
               geneModelSnp.getGene().equals(gene)) {
                found = true;
                for(SNP incomingSnp : incomingSnps) {
                    if(! geneModelSnp.getSnps().contains(incomingSnp) &&
                       dataSet.getPvalFromSnpModel(incomingSnp, model) != null) {
                            geneModelSnp.getSnps().add(incomingSnp);
                    }
                }
            }
        }
        if(! found) {
            GeneModelSnp geneModelSnp = new GeneModelSnp(gene, model);
            for(SNP incomingSnp : incomingSnps) {
                if(dataSet.getPvalFromSnpModel(incomingSnp, model) != null) {
                    geneModelSnp.addSnp(incomingSnp);
                }
            }
            geneModelSnps.add(geneModelSnp);
        }
        
        System.out.println("Get new row count: " + getRowCount());
        this.fireTableDataChanged();
    }

    public GeneModelSnpOffset computeGeneModelSnpOffset(int row) {
        int index = 0;
        int geneModelSnpIndex = 0;
        for (GeneModelSnp geneModelSnp : geneModelSnps) {
            int len = geneModelSnp.getNumSnpEntries();
            if (row < index + len) {
                return new GeneModelSnpOffset(geneModelSnp.getGene(), 
                        geneModelSnp.getModel(), 
                        geneModelSnpIndex,
                        row - index);
            }
            index += len;
            geneModelSnpIndex++;
        }
        return null;
    }
    
    /**
     * Removes the selected set of rows from the table.  It has an assumption
     * that the last rows will be moved before the earlier ones.  This may
     * be violated if the data is resorted
     */
    public void removeSelectedRows(int[] selectedRows) {
        int numSelectedRows = selectedRows.length;
        for(int rowi = numSelectedRows - 1; rowi >= 0; rowi--) {
            GeneModelSnpOffset geneModelSnpOffset = computeGeneModelSnpOffset(selectedRows[rowi]);
            String gene = geneModelSnpOffset.getGene();
            Model model = geneModelSnpOffset.getModel();
            GeneModelSnp geneModelSnp = geneModelSnps.get(geneModelSnpOffset.getGeneModelIndex());
            int offset = geneModelSnpOffset.getOffset();
            geneModelSnp.removeSnp(offset);
            if(geneModelSnp.getNumSnpEntries() == 0) {
                geneModelSnps.remove(geneModelSnp);
            }
        }
        fireTableDataChanged();
    }
    
    public void removeAll() {
        geneModelSnps.clear();
        fireTableDataChanged();
    }
    
    /**
     * Removes all the rows that are below the p-value threshold
     */
    public void removeRowsBelowPvalThreshold(double pval) {
        int numRows = getRowCount();
        ArrayList<Integer> rowsToRemove = new ArrayList<Integer>();
        for(int rowi = 0; rowi < numRows; rowi++) {
            if((Double) getValueAt(rowi, PVAL_COL) < pval) {
                rowsToRemove.add(rowi);
            }
        }
        int len = rowsToRemove.size();
        int[] selectedRows = new int[len];
        int index = 0;
        for(Integer entry : rowsToRemove) {
            selectedRows[index++] = entry;
        }
        removeSelectedRows(selectedRows);
    }
    
    /**
     * Keep the selected set of rows from the table.  It has an assumption
     * that the last rows will be moved before the earlier ones.  This may
     * be violated if the data is resorted
     */
    public void keepSelectedRows(int[] selectedRows) {
        int numSelectedRows = selectedRows.length;
        for(int rowi = getRowCount() - 1; rowi >= 0; rowi--) {
            System.out.print("Keeping row " + rowi);
            if(! foundInArray(rowi, selectedRows)) {
                GeneModelSnpOffset geneModelSnpOffset = computeGeneModelSnpOffset(rowi);
                String gene = geneModelSnpOffset.getGene();
                Model model = geneModelSnpOffset.getModel();
                GeneModelSnp geneModelSnp = geneModelSnps.get(geneModelSnpOffset.getGeneModelIndex());
                System.out.println(" Killing off snp index " + geneModelSnpOffset.getOffset() + " of geneModelIndex " + geneModelSnpOffset.getGeneModelIndex());
                int offset = geneModelSnpOffset.getOffset();
                geneModelSnp.removeSnp(offset);
                if(geneModelSnp.getNumSnpEntries() == 0) {
                    geneModelSnps.remove(geneModelSnp);
                }
            } else {
                System.out.println(" Keeping it since in list of selected");
            }
        }
        fireTableDataChanged();
    }
    
    
    /**
     * Returns true if value is in the int[] array else returns false.
     * @param value
     * @param array
     * @return 
     */
    private boolean foundInArray(int value, int[] array) {
        boolean found = false;
        int len = array.length;
        for(int i = 0; i < len; i++) {
            if(array[i] == value) {
                return true;
            }
        }
        return false;
    }   
    
    /**
     * Stroage class
     *
     * @author henstock
     */
    protected class GeneOffset {
        private String gene;
        private int offset;

        public GeneOffset(String gene, int offset) {
            this.gene = gene;
            this.offset = offset;
        }

        public int getOffset() {
            return offset;
        }

        public String getGene() {
            return gene;
        }
    }

    protected class ModelSnp {

        private final Model model;
        private final SNP snp;

        public ModelSnp(Model model, SNP snp) {
            this.model = model;
            this.snp = snp;
        }

        public Model getModel() {
            return model;
        }

        public SNP getSnp() {
            return snp;
        }
    }
    
}