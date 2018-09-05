/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class GeneModelScore {
    private final String geneName;
    private final String modelName;
    private double score;
    public GeneModelScore(String geneName, String modelName, double score) {
        this.geneName = geneName;
        this.modelName = modelName;
        this.score = Math.round(score * 10.0f) / 10.0f;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getModelName() {
        return modelName;
    }

    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = Math.round(score * 10.0f) / 10.0f;
    }
    
    @Override
    public int hashCode() {
        return (geneName + modelName).hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        System.out.println("Comparing " + (other instanceof GeneModelScore) + "\t[" + this.geneName + "\t" + ((GeneModelScore) other).getGeneName() + "] [" + this.modelName + "< >" +((GeneModelScore) other).getModelName() + "]");
        if(! (other instanceof GeneModelScore)) {
            return false;
        } else {
            GeneModelScore otherGeneModelScore = (GeneModelScore) other;
            return  otherGeneModelScore.getGeneName().equals(geneName) &&
                    otherGeneModelScore.getModelName().equals(modelName);
        }
    }
    
}
