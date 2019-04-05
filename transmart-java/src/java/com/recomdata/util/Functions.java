/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/*
Copyright (C) 2001-2004  Kyle Siegrist, Dawn Duehring
Department of Mathematical Sciences
University of Alabama in Huntsville

This program is part of Virtual Laboratories in Probability and Statistics,
http://www.math.uah.edu/stat/.

This program is licensed under a Creative Commons License. Basically, you are free to copy,
distribute, and modify this program, and to make commercial use of the program.
However you must give proper attribution.
See http://creativecommons.org/licenses/by/2.0/ for more information.
*/

package com.recomdata.util;

/**
 * Methods that are useful in probability.
 * @author Kyle Siegrist
 * @author Dawn Duehring
 */
public final class Functions {

    private static final double[] coef = {76.18009173, -86.50532033, 24.01409822, -1.231739516, 0.00120858003, -0.00000536382};
    private static final int maxit = 100;
    private static final double eps = 0.0000003;

    public static final int WITHOUT_REPLACEMENT = 0;
    public static final int WITH_REPLACEMENT = 1;
    public static final double EULER = 0.57721566490153286061;

    /**
     * Takes an array of numbers and returns a probability array (nonnegative numbers that sum to 1).
     * Negative numbers in the original array are converted to zeros, and the
     * remaining numbers scaled by the sum of the numbers, assuming that not
     * all of the original numbers are 0. In the last case, the method returns
     * the uniform probabiltiy array.
     * @param a the array of numbers
     * @return the probability array
     */
    public static double[] getProbabilities(double[] a){
        int n = a.length;
        double[] p = new double[n];
        double sum = 0;
        for (int i = 0; i < n; i++){
            if (a[i] < 0) a[i] = 0;
            else sum = sum + a[i];
        }
        if (sum == 0) for (int i = 0; i < n; i++) p[i] = 1.0 / n;
        else for (int i = 0; i < n; i++) p[i] = a[i] / sum;
        return p;
    }

    /**
     * Takes a real number and converts it to a probability (a number between 0 and 1).
     * @param p the given number
     * @return the probability.
     */
    public static double getProbability(double p){
        if (p < 0) return 0;
        else if (p > 1) return 1;
        else return p;
    }

    /**
     * Returns an index between 0 and n - 1, for use in error correcting in arrays.
     * @param i the given index
     * @param n the specified size of the array
     * @return the verified index between 0 and n - 1
     */
    public static int getIndex(int i, int n){
        if (i < 0) return 0;
        else if (i > n - 1) return n - 1;
        else return i;
    }

    /**
     * Computes the number of permuatations of a specified number of objects
     * chosen from a population of a specified number of objects.
     * @param n the population size
     * @param k the sample size
     * @return the number of ordered samples
     */
    public static double perm(double n, int k){
        if (k > n | k < 0) return 0;

        double prod = 1;
        for (int i = 1; i <= k; i++) prod = prod * (n - i + 1);
        return prod;
    }

    /**
     * Computes the factorial function, the number of permutations of a specified number of objects.
     * @param k  the number of objects
     * @return the number of permutations of the objects
     */
    public static double factorial(int k){
        return perm(k, k);
    }

    /**
     * Computes the number of combinations of a specified number of objects
     * chosen from a population of a specified size.
     * @param n the population size
     * @param k the sample size
     * @return the number of unordered samples
     */
    public static double comb(double n, int k){
        return perm(n, k) / factorial(k);
    }

    /**
     * Computes the log of the gamma function.
     * @param x a positive number
     * @return the log of the gamma function at 
     */
    public static double logGamma(double x){
        double step = 2.50662827465;
        double fpf = 5.5;
        double t = x - 1;
        double tmp = t + fpf;
        tmp = (t + 0.5) * Math.log(tmp) - tmp;
        double ser = 1;
        for (int i = 1; i <= 6; i++){
            t = t + 1;
            ser = ser + coef[i - 1] / t;
        }
        return tmp + Math.log(step * ser);
    }

    /**
     * Computes the gamma function.
     * @param x a positive number
     * @return the gamma function at x
     */
    public static double gamma(double x){
        return Math.exp(logGamma(x));
    }

    /**
     * Computes the cumulative distribution function of the gamma distribution
     * with a specified shape parameter and scale parameter 1.
     * @param x a positive number
     * @param a the shape parameter
     * @return the cumulative probability at x
     */
    public static double gammaCDF(double x, double a){
        if (x <= 0) return 0;
        if (x < a + 1) return gammaSeries(x, a);
        return 1 - gammaCF(x, a);
    }

    /**
     * Computes a gamma series that is used in the gamma cumulative
     * distribution function.
     * @param x a postive number
     * @param a the shape parameter
     * @return the gamma series at x
     */
    private static double gammaSeries(double x, double a){
        double sum = 1.0 / a;
        double ap = a;
        double gln = logGamma(a);
        double del = sum;
        for (int n = 1; n <= maxit; n++){
            ap++;
            del = del * x / ap;
            sum = sum + del;
            if (Math.abs(del) < Math.abs(sum) * eps) break;
        }
        return sum * Math.exp(-x + a * Math.log(x) - gln);
    }

    /**
     * Computes a gamma continued fraction function function that is used in
     * the gamma cumulative distribution function.
     * @param x a positive number
     * @param a the shape parameter
     * @return the gamma continued fraction function at x
     */
    private static double gammaCF(double x, double a){
        double gln = logGamma(a);
        double g = 0;
        double gOld = 0;
        double a0 = 1;
        double a1 = x;
        double b0 = 0;
        double b1 = 1;
        double fac = 1;
        for (int n = 1; n <= maxit; n++){
            double an = 1.0 * n;
            double ana = an - a;
            a0 = (a1 + a0 * ana) * fac;
            b0 = (b1 + b0 * ana) * fac;
            double anf = an * fac;
            a1 = x * a0 + anf * a1;
            b1 = x * b0 + anf * b1;
            if (a1 != 0){
                fac = 1.0 / a1;
                g = b1 * fac;
                if (Math.abs((g - gOld) / g) < eps) break;
                gOld = g;
            }
        }
        return Math.exp(-x + a * Math.log(x) - gln) * g;
    }

    /**
     * Computes the beta cumulative distribution function.
     * @param x a number between 0 and 1
     * @param a the left paramter
     * @param b the right parameter
     * @return the beta cumulative probability at x
     */
    public static double betaCDF(double x, double a, double b){
        double bt;
        if ((x == 0) | (x == 1)) bt = 0;
        else bt = Math.exp(logGamma(a + b) - logGamma(a) - logGamma(b) + a * Math.log(x) + b * Math.log(1 - x));
        if (x < (a + 1) / (a + b + 2)) return bt * betaCF(x, a, b) / a;
        return 1 - bt * betaCF(1 - x, b, a) / b;
    }

    /**
     * Computes a beta continued fractions function that is used in the beta
     * cumulative distribution function.
     * @param x a number between 0 and 1
     * @param a the left parameter
     * @param b the right parameter
     * @return the beta continued fraction function at x
     */
    private static double betaCF(double x, double a, double b){
        double am = 1;
        double bm = 1;
        double az = 1;
        double qab = a + b;
        double qap = a + 1;
        double qam = a - 1;
        double bz = 1 - qab * x / qap;
        for (int m = 1; m <= maxit; m++){
            double em = m;
            double tem = em + em;
            double d = em * (b - m) * x / ((qam + tem) * (a + tem));
            double ap = az + d * am;
            double bp = bz + d * bm;
            d = -(a + em) *(qab + em) * x / ((a + tem) * (qap + tem));
            double app = ap + d * az;
            double bpp = bp + d * bz;
            double aOld = az;
            am = ap / bpp;
            bm = bp / bpp;
            az = app / bpp;
            bz = 1;
            if (Math.abs(az - aOld) < eps * Math.abs(az)) break;
        }
        return az;
    }

    /**
     * An approximation to the beta function.
     * @param a the left value
     * @param b the right value
     * @return the beta function at (a, b)
     */
    public static double beta(double a, double b){
        return gamma(a) * gamma(b) / gamma(a + b);
    }

    /**
     * Computes a sample of a specified size from a specified population
     * and of a specified type (with or without replacement).
     * @param p the population
     * @param n the sample size
     * @param t the type (0 without replacement, 1 with replacemen);
     */
    public static int[] getSample(int[] p, int n, int t){
        int m = p.length;
        int temp, k, u;
        if (n < 1) n = 1;
        else if (n > m && t == WITHOUT_REPLACEMENT) n = m;
        // Ivo changed this to fix a bug with the Sampling With replacement case (04/03/08)
        // else if (n > m) n = m;
        //Define the sample
        int[] s = new int[n];
        if (t == WITH_REPLACEMENT){
            for (int i = 0; i < n; i++){
                u = (int)(m * Math.random());
                s[i] = p[u];
            }
        }
        else{
            for (int i = 0; i < n; i++){
                //Select a random index from 0 to m - i - 1;
                k = m - i;
                u = (int)(k * Math.random());
                //Define the sample element
                s[i] = p[u];
                //Interchange the sampled element p[u] with p[k - 1], at the end of the
                // population so that it will not be sampled again.
                temp = p[k - 1];
                p[k - 1] = p[u];
                p[u] = temp;
            }
        }
        return s;
    }

    /**
     * Computes a sample of a specified size from a population of
     * the form 1, 2, ..., m
     * @param m the population size
     * @param n the sample size
     * @param t the type (0 without replacement, 1 with replacement)
     */
    public static int[] getSample(int m, int n, int t){
        if (m < 1) m = 1;
        if (n < 1) n = 1;
        // Ivo changed this to fix a bug with the Sampling With replacement case (04/03/08)
        // else if (n > m) n = m;
        else if (n > m && t == WITHOUT_REPLACEMENT) n = m;
        int [] p = new int[m];
        //Define the population
        for (int i = 0; i < m; i++) p[i] = i + 1;
        return getSample(p, n, t);
    }

    /**
     * Sorts an array of doubles.
     * @param a the array
     * @return the sorted array
     */
    public static double[] sort(double[] a){
        boolean smallest;
        int n = a.length;
        double[] b = new double[n];
        for (int i = 0; i < n; i++){
            smallest = true;
            for (int j = i - 1; j >= 0; j--){
                if (b[j] <= a[i]){
                    b[j + 1] = a[i];
                    smallest = false;
                    break;
                }
                b[j + 1] = b[j];
            }
            if (smallest) b[0] = a[i];
        }
        return b;
    }

    /**
     * Sorts an array of integers.
     * @param a the array
     * @return the sorted array
     */
    public static int[] sort(int[] a){
        int n = a.length;
        int[] b = new int[n];
        for (int i = 0; i < n; i++){
            boolean smallest = true;
            for (int j = i - 1; j >= 0; j--){
                if (b[j] <= a[i]){
                    b[j + 1] = a[i];
                    smallest = false;
                    break;
                }
                b[j + 1] = b[j];
            }
            if (smallest) b[0] = a[i];
        }
        return b;
    }

    /**
     * Tests to see if a specified number is real.
     * @param x the number
     * @return true if the number is real and finite
     */
    public static boolean isReal(double x){
        return !Double.isInfinite(x) && !Double.isNaN(x);
    }
}
