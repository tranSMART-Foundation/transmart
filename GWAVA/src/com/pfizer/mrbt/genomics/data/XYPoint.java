package com.pfizer.mrbt.genomics.data;

/**
 * Utility class to store x-y points as integers
 * @author henstockpv
 */
public class XYPoint implements Comparable {
    private int x, y;
    public XYPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
    
    /**
     * Sorts based on X-values, then Y
     * @param otherPoint
     * @return 
     */
    @Override
    public int compareTo(Object otherPoint) {
        int otherX = ((XYPoint) otherPoint).getX();
        if(otherX < x) {
            return 1;
        } else if(otherX > x) {
            return -1;
        } else {
            int otherY = ((XYPoint) otherPoint).getY();
            if(otherY < y) {
                return 1;
            } else if(otherY > y) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
