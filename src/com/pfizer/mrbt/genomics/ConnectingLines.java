/*
 * Based on AverageVec that takes the data/model/snps and allows access to a
 * sorted list of x-y points connecting the SNPS
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.data.XYPoint;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author henstockpv
 */
public class ConnectingLines {

    private DataSet dataSet;
    private Model model;
    private ArrayList<SNP> snps;
    private AxisScale xAxis;
    private AxisScale yAxis;
    private BufferedImage bufferedImage;

    public ConnectingLines(DataSet dataSet, Model model, ArrayList<SNP> snps, AxisScale xAxis, AxisScale yAxis, BufferedImage bufferedImage) {
        this.model = model;
        this.snps = snps;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        this.dataSet = dataSet;
        this.bufferedImage = bufferedImage;

    }

    /**
     * Computes the filterWidth and fills minXVal, maxXval and avgVecby
     * averaging over all the SNP values
     */
    public ArrayList<XYPoint> computeConnectingLines() {
        ArrayList<XYPoint> points = new ArrayList<XYPoint>();
        // assume that the snps are in order
        for (SNP snp : snps) {
            Double logpval = dataSet.getPvalFromSnpModel(snp, model);
            if (logpval != null) {
                int imagex = (int) Math.round(xAxis.getImagePixelFromValue(snp.getLoc(), bufferedImage));
                int imagey = (int) Math.round(yAxis.getImagePixelFromValue(logpval, bufferedImage));
                points.add(new XYPoint(imagex, imagey));
            }
        }
        Collections.sort(points);
        return points;
    }
}
