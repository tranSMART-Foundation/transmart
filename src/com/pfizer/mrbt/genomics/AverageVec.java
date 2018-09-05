/*
 * This is an attempt at generating average lines representing the weighted sum
 * of positions throughout the displayed region of the chromosome for a given
 * model and set of snp
 * 
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.axis.AxisScale;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author henstockpv
 */ 
public class AverageVec {
    private DataSet dataSet;
    private Model model;
    private CopyOnWriteArrayList<SNP> snps;
    private AxisScale xAxis;
    private AxisScale yAxis;
    private BufferedImage bufferedImage;
    private int minXVal = 0;
    private int maxXVal = 0;
    private int[] avgVec;
    private int[] cntVec;
    private ArrayList<Point2D> line = new ArrayList<Point2D>();
    public AverageVec(DataSet dataSet, Model model, CopyOnWriteArrayList<SNP> snps, AxisScale xAxis, AxisScale yAxis, BufferedImage bufferedImage) {
        this.model = model;
        this.snps = snps;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.dataSet = dataSet;
        this.bufferedImage = bufferedImage;
        
    }
    
    /**
     * Computes the filterWidth and fills minXVal, maxXval and avgVecby averaging over all the SNP values
     * @param filterWidth 
     */
    public void computeAverageVec(int filterWidth) {
        minXVal = (int) Math.round(xAxis.getImagePixelFromValue(xAxis.getMinDisplayValue(), bufferedImage));
        maxXVal = (int) Math.round(xAxis.getImagePixelFromValue(xAxis.getMaxDisplayValue(), bufferedImage));
        int numPix = maxXVal - minXVal + 1;
        avgVec = new int[numPix];
        cntVec = new int[numPix];
     
        int filterRadius = filterWidth/2;
        int lowerpixX = xAxis.getImagePixelFromValue(0,  bufferedImage);
        int upperpixX = xAxis.getImagePixelFromValue(filterRadius, bufferedImage);
        int filterWidthInPix = upperpixX - lowerpixX;
        for(SNP snp : snps) {
                Double logpval = dataSet.getPvalFromSnpModel(snp, model);
                if (logpval != null) {
                    double imagex = xAxis.getImagePixelFromValue(snp.getLoc(), bufferedImage);
                    double imagey = yAxis.getImagePixelFromValue(logpval, bufferedImage);
                    int lowerX = xAxis.getImagePixelFromValue(snp.getLoc() - filterRadius, bufferedImage);
                    int upperX = xAxis.getImagePixelFromValue(snp.getLoc() + filterRadius, bufferedImage);
                    for(int x = Math.max(0,lowerX-minXVal); x <Math.min(upperX-minXVal, numPix-1); x++) {
                        avgVec[x] += imagey;
                        cntVec[x] += 1;
                       // System.out.println("[" + x + "\t" + imagey + "]");
                    }
                }
        }
        float filterRadiusFloat = filterWidthInPix/2f;
        float filterWidthFloat = filterWidthInPix * 1f;
        for(int i = 0; i < avgVec.length; i++) {
            if(cntVec[i]==0) {
                avgVec[i] = bufferedImage.getHeight();
            } else {
                avgVec[i] = (int) Math.round(avgVec[i] * 1f / cntVec[i]);
            }
        }
    }

    /**
     * This is the x value where the display starts
     * @return 
     */
    public int getMinXVal() {
        return minXVal;
    }
    
    public int[] getYPixVector() {
        return avgVec;
    }
}
