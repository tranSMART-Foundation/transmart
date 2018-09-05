/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import java.io.StringReader;
import org.apache.commons.digester3.Digester;

/**
 *
 * @author henstockpv
 */
public class XMLRuleDigester {
    private Digester digester;
    public XMLRuleDigester() {
        //String defineRule = "<?xml version=\"1.0\"?><digester-rules>
    }
    
    /**
     * Parses the xml string into Rows
     * @param xmlString
     * @return 
     */
    public Rows digest(String xmlString) throws Exception {
        System.out.println("digest call: [" + xmlString + "]");
        
        //URL rules = this.getClass().getClassLoader().getResource("/XMLRules");
        //digester = DigesterLoader.createDigester(rules);
        
        
        digester.setValidating(false);
        digester.push(this);
        
        digester.addObjectCreate("rows", Rows.class);
        digester.addSetProperties("rows");
        
        digester.addObjectCreate("rows/row", Row.class);
        digester.addSetProperties("rows/row");
        
        digester.addCallMethod("rows/row/data", "addData", 1);
        digester.addCallParam("rows/row/data", 0);
        digester.addSetNext("rows/row", "addRow");
        //digester.addSetNext("rows", addRow);

        Rows rows = (Rows) digester.parse(new StringReader(xmlString));
        return rows;
    }
}
