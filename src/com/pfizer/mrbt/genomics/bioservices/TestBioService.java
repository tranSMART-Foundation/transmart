/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.cbr.remote.BioServicesRemoteException;
import com.pfizer.mrbt.genomics.tou.bioservices.GeneGoGenericRowData;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class TestBioService {
    public TestBioService() {
        BioServicesDataGetter bsgetter = BioServicesDataGetter.getInstance();
        try {
            List<GeneGoGenericRowData> metReactions = bsgetter.getMetabolicReactions();
            for(GeneGoGenericRowData row : metReactions) {
                System.out.println("Value\t" + row);
            }
        } catch (BioServicesRemoteException bsre) {
            System.out.println("BioSerivce remote exception " + bsre.getMessage());
        } catch (IOException ioe) {
            System.out.println("IOException ioe: " + ioe.getMessage());
        }
    }

    public static void main(String[] argv) {
        TestBioService testBioService = new TestBioService();
    }

}