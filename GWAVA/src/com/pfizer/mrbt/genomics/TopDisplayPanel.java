/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.data.DataPointEntry;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.View;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class TopDisplayPanel extends JComponent {
    private AbstractButton leftButton, rightButton;
    private JTextField modelValueField;
    private JTextField rsIdField;
    private JTextField snpLocationField;
    private JTextField pValueField;
    
    private JTextField snpGeneField;
    private JTextField regulomeField;
    private JTextField intronExonField;
    
    private View view;
    private DecimalFormat myFormatter = new DecimalFormat("##.##");
    private String decimalFormat = "###.###";
    
    public TopDisplayPanel(View view) {
        super();
        this.view = view;
        //this.dataSet = dataSet;
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridheight = 20;
        add(getLeftButton(), gbc);

        gbc.gridx = 20;
        gbc.weightx = 0.0;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0,3,0,3);
        add(new JLabel("Model"), gbc);
        
        gbc.gridx = 30;
        add(new JLabel("RsID"), gbc);
        
        gbc.gridx = 40;
        add(new JLabel("SNP Location"), gbc);
        
        gbc.gridx = 50;
        add(new JLabel("In/Exon"), gbc);
        
        gbc.gridx = 60;
        add(new JLabel("Regulome"), gbc);
        
        gbc.gridx = 70;
        add(new JLabel("Gene"), gbc);

        gbc.gridx = 80;
        gbc.weightx = 0.3;
        add(new JLabel("-log p-Val"), gbc);

        
        gbc.gridy = 20;
        gbc.gridx = 20;
        gbc.weightx = 2.0;
        add(getModelValueField(), gbc);

        gbc.gridx = 30;
        gbc.weightx = 0.2;
        add(getRsIdField(), gbc);

        gbc.gridx = 40;
        gbc.weightx = 0.0;
        add(getSNPLocationField(), gbc);

        gbc.gridx = 50;
        gbc.weightx = 0.0;
        add(getIntronExonField(), gbc);

        gbc.gridx = 60;
        gbc.weightx = 0.0;
        add(getRegulomeField(), gbc);

        gbc.gridx = 70;
        gbc.weightx = 0.5;
        add(getSnpGeneField(), gbc);

        gbc.gridx = 80;
        gbc.weightx = 0.0;
        add(getPValueField(), gbc);

        gbc.gridy = 10;
        gbc.gridx = 100;
        gbc.weightx = 0.0;
        gbc.insets = new Insets(5,5,5,5);
        gbc.gridheight = 20;
        add(getRightButton(), gbc);


        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);

    }

    /**
     * Shifts the screen view to the left.
     * @return 
     */
    protected AbstractButton getLeftButton() {
        if (leftButton == null) {
            leftButton = new JButton("Left");
            ImageIcon leftIcon = null;
            java.net.URL imgURL = null;
            try {
                imgURL = this.getClass().getResource("/images/leftArrow20.jpg");
                leftIcon = new ImageIcon(imgURL);
                leftButton.setIcon(leftIcon);
                leftButton.setText(null);
            } catch (NullPointerException npe) {
                System.out.println("Failed to load in the icon [" + imgURL + "]");
            }
            leftButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    view.shiftLeft();
                }
            });
        }
        return leftButton;
    }

    /**
     * Shifts the screen view to the right.
     * @return 
     */
    protected AbstractButton getRightButton() {
        if (rightButton == null) {
            rightButton = new JButton("Right");
            ImageIcon rightIcon = null;
            java.net.URL imgURL = null;
            try {
                imgURL = this.getClass().getResource("/images/rightArrow20.jpg");
                rightIcon = new ImageIcon(imgURL);
                rightButton.setIcon(rightIcon);
                rightButton.setText(null);
            } catch (NullPointerException npe) {
                System.out.println("Failed to load in the icon [" + imgURL + "]");
            }
            rightButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    view.shiftRight();
                }
            });
        }
        return rightButton;
    }
    
    /**
     * Defines the model value field that displays the model name of the current
     * position
     * @return 
     */
    protected JTextField getModelValueField() {
        if( modelValueField == null) {
            modelValueField = new JTextField(33);
            modelValueField.setText("");
            modelValueField.setBackground(Color.WHITE);
            modelValueField.setHorizontalAlignment(JTextField.CENTER);
            modelValueField.setEditable(false);
        }
        return modelValueField;
    }

    /**
     * Defines the model value field that displays the model name of the current
     * position
     * @return 
     */
    protected JTextField getRsIdField() {
        if( rsIdField == null) {
            rsIdField = new JTextField(9);
            rsIdField.setText("");
            rsIdField.setBackground(Color.WHITE);
            rsIdField.setHorizontalAlignment(JTextField.CENTER);
            rsIdField.setEditable(false);
        }
        return rsIdField;
    }

    /**
     * Defines the chromosome and SNP location value field for the current
     * position
     * @return 
     */
    protected JTextField getSNPLocationField() {
        if( snpLocationField == null) {
            snpLocationField = new JTextField(9);
            snpLocationField.setText("");
            snpLocationField.setBackground(Color.WHITE);
            snpLocationField.setHorizontalAlignment(JTextField.CENTER);
            snpLocationField.setEditable(false);
        }
        return snpLocationField;
    }
    
    /**
     * Defines the y-axis -logPvalue
     * @return 
     */
    protected JTextField getPValueField() {
        if( pValueField == null) {
            pValueField = new JTextField(1);
            pValueField.setText("");
            pValueField.setBackground(Color.WHITE);
            pValueField.setHorizontalAlignment(JTextField.CENTER);
            pValueField.setEditable(false);
        }
        return pValueField;
    }
    

    /**
     * Defines the intron/exon field
     * @return 
     */
    protected JTextField getIntronExonField() {
        if( intronExonField == null) {
            intronExonField = new JTextField(3);
            intronExonField.setText("");
            intronExonField.setBackground(Color.WHITE);
            intronExonField.setHorizontalAlignment(JTextField.CENTER);
            intronExonField.setEditable(false);
        }
        return intronExonField;
    }
    

    /**
     * Defines the y-axis -logPvalue
     * @return 
     */
    protected JTextField getRegulomeField() {
        if( regulomeField == null) {
            regulomeField = new JTextField(4);
            regulomeField.setText("");
            regulomeField.setBackground(Color.WHITE);
            regulomeField.setHorizontalAlignment(JTextField.CENTER);
            regulomeField.setEditable(false);
        }
        return regulomeField;
    }
    

    /**
     * Defines the y-axis -logPvalue
     * @return 
     */
    protected JTextField getSnpGeneField() {
        if( snpGeneField == null) {
            snpGeneField = new JTextField(11);
            snpGeneField.setText("");
            snpGeneField.setBackground(Color.WHITE);
            snpGeneField.setHorizontalAlignment(JTextField.CENTER);
            snpGeneField.setEditable(false);
        }
        return snpGeneField;
    }
    

    
    public class StateController implements StateListener {
        @Override
        public void currentChanged(ChangeEvent ce) {
            DataPointEntry currentEntry = Singleton.getState().getCurrenDataEntry();
            if(currentEntry == null) {
                modelValueField.setText("--");
                snpLocationField.setText("--");
                pValueField.setText("--");
                rsIdField.setText("--");
                intronExonField.setText("--");
                regulomeField.setText("--");
                snpGeneField.setText("--");
            } else {
                //System.out.println("currentChanged: " + currentEntry);
                SNP snp             = currentEntry.getSnp();
                Model model            = currentEntry.getModel();
                DataSet dataSet = currentEntry.getDataSet();
                Double negLogPValue = dataSet.getPvalFromSnpModel(snp, model);
                modelValueField.setText(model.toString());
                DecimalFormat formatter = new DecimalFormat("#,###");
                //snpLocationField.setText(snp.getLoc() + "");
                snpLocationField.setText(formatter.format(snp.getLoc()));
                String outputStr = myFormatter.format(negLogPValue);
                pValueField.setText(outputStr);
                rsIdField.setText("rs" + snp.getRsId());
                intronExonField.setText(snp.getIntronExon().toString());
                regulomeField.setText(snp.getRegulome());
                snpGeneField.setText(snp.getAssociatedGene());
            }
        }
        @Override
        public void mainPlotChanged(ChangeEvent ce) { 
            view = Singleton.getState().getMainView();
        }
        
        @Override
        public void thumbnailsChanged(ChangeEvent ce) { }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) { }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) { }

             @Override
        public void averagingWindowChanged(ChangeEvent ce) {    }
             
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { }

        public void heatmapChanged(ChangeEvent ce) { }
             
}

}
