/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import java.util.ArrayList;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author henstockpv
 */
public class XYPointTest {
    private ArrayList<XYPoint> points = new ArrayList<XYPoint>();
    public XYPointTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        points.clear();
        points.add(new XYPoint(1,3));
        points.add(new XYPoint(2,2));
        points.add(new XYPoint(3,3));
        points.add(new XYPoint(1,1));
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of compareTo method, of class XYPoint.
     */
    @Test
    public void testSort1st() {
        System.out.println("sort check position 1");
        Collections.sort(points);
        int result = points.get(0).getY();
        int expResult = 1;
        assertEquals(expResult, result);
    }

    /**
     * Test of compareTo method, of class XYPoint.
     */
    @Test
    public void testSort1stDuplicate() {
        System.out.println("sort check position 1");
        Collections.sort(points);
        int result = points.get(1).getY();
        int expResult = 3;
        assertEquals(expResult, result);
    }

    /**
     * Test of compareTo method, of class XYPoint.
     */
    @Test
    public void testSortLast() {
        System.out.println("sort check position 1");
        Collections.sort(points);
        int result = points.get(points.size()-1).getX();
        int expResult = 3;
        assertEquals(expResult, result);
    }

}