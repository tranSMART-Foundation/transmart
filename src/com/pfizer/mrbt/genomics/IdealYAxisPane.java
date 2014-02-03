/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author henstockpv
 */
public class IdealYAxisPane extends JComponent {
    private String typedText = null;
    private JLabel instructionsLabel1;
    private JLabel instructionsLabel2;
    private DecimalFormat decFormat = new DecimalFormat("0.00");
    private JTextField valueTextField;
    private float value;
    
    public IdealYAxisPane(float value) {
        super();
        this.value = value;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 10;
        gbc.fill = GridBagConstraints.NONE;
        instructionsLabel1 = new JLabel("Enter the ideal upper Y-axis value: ");
        add(instructionsLabel1, gbc);
        
        gbc.gridx = 10;
        gbc.gridy = 20;
        instructionsLabel2 = new JLabel("(Numbers <= 0 turn this feature off)");
        add(instructionsLabel2, gbc);
        
        gbc.gridx = 10;
        gbc.gridy = 30;
        valueTextField = new JTextField(8);
        valueTextField.setText(decFormat.format(value));
        add(valueTextField, gbc);
    }
    
    /**
     * Returns the trimmed value of the text field.
     */
    public String getValueTextField() {
        return valueTextField.getText().trim();
    }
}
    
    
    
