/**
 * User preferences are stored in the file .phageAlign.ini in the default
 * user directory.  It stores variables such as the last path
 */
package com.pfizer.mrbt.genomics.userpref;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.heatmap.HeatmapParameters;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author Peter V. Henstock
 */
public class UserPreferences {

    private String filePath = "";
    /*private boolean useArithmeticMean = true;
    private boolean useRatios = true;
    private boolean useLogXScale = true;*/
    private float minTopNegLogPvalAxis = 0f;
    private float minTopRecombinationRateAxis = 0f;
    private double splitPaneFraction = 0.8;
    public final static Color MUSTARD = new Color(238, 221, 130);
    public final static Color LIGHTISH_GRAY = new Color(235,232,228);
    public final static Color PURPLE3 = new Color(125,38,205);
    public final static Color EMERALD_GREEN = new Color(0, 201, 87);
   // public final static Color ORANGE4 = new Color(139, 90, 0);
    public final static Color RECOMBI_ORANGE = new Color(244, 164, 96);
    public final static Color TEXT_COLOR = new Color(72, 61, 139);
    public final static Color LIGHT_GREEN = new Color(210, 255, 210);
    public final static Color PALE_BLUE = new Color(223, 241, 247);
        public final static Color ORANGE4 = new Color(139, 90, 0);
    
    public final static int DEFAULT_BASE_PAIR_RADIUS = 100000;


    
    private Color backgroundColor   = Color.WHITE;
    private Color pointColor        = Color.BLUE;
    private Color selectionColor    = Color.RED;
    private Color currentColor      = Color.RED;
    private Color frameColor        = LIGHTISH_GRAY;
    private Color frameTextColor    = Color.BLACK;
    private Color recombinationColor = RECOMBI_ORANGE;
    private Color selectionBandColor = Color.LIGHT_GRAY;
    private Color foundSnpColor     = Color.RED;
    private Color axisDragRangeColor = new Color(143, 188, 143);
    private Color borderColor        = Color.BLACK; // border around Manhattan plot and annotation panel
    
    private Color annotationColor   = PALE_BLUE;
    private Color annotationTextColor = PURPLE3;
    private Color currentAnnotationColor = Color.BLUE;
    private Color selectedAnnotationColor = ORANGE4;
    private Color closestAnnotationColor    = Color.MAGENTA;
    private Color interiorAnnotationColor   = Color.RED;
    private Color intronAnnotationColor   = Color.GREEN;

    private Color thumbnailColor        = Color.WHITE;
    private Color thumbnailTextColor    = Color.BLACK;
    private Color thumbnailPointColor   = Color.BLUE;
    private Color thumbnailSelectionBandColor = Color.LIGHT_GRAY;
    private Color thumbnailSelectionColor = Color.GRAY;
    private Color thumbnailHorizontalBandColor = Color.LIGHT_GRAY;
    
    private final int DEFAULT_HEAT_MAP_RADIUS = 10000000;
    private final int DEFAULT_HEAT_MAP_FUNCTION = HeatmapParameters.FUNCTION_MAXIMUM;
    private final int DEFAULT_HEAT_MAP_TOP_N_INDEX   = 0;

    private int heatmapRadius    = DEFAULT_HEAT_MAP_RADIUS;
    private int heatmapFunction  = DEFAULT_HEAT_MAP_FUNCTION;
    private int heatmapTopNindex = DEFAULT_HEAT_MAP_TOP_N_INDEX;
    private int basePairSearchRadius = DEFAULT_BASE_PAIR_RADIUS;
    private Point mainFrameLocation = new Point(30, 17);
    private Point geneModelFrameLocation = new Point(830,17);
    private Dimension mainFrameSize = new Dimension(800,700);
    private Dimension geneModelFrameSize = new Dimension(226, 700);
    private Dimension bufferedPlotSize = null;
    
    public UserPreferences() {
        loadUserPreferences();
    }

    private final ArrayList<UserPrefListener> listeners = new ArrayList<UserPrefListener>();
    
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setWindowPositions(JFrame mainFrame, JFrame geneModelFrame) {
        this.mainFrameLocation = mainFrame.getLocation();
        this.geneModelFrameLocation = geneModelFrame.getLocation();
        this.mainFrameSize = mainFrame.getSize();
        this.geneModelFrameSize = geneModelFrame.getSize();
    }
    
    public void saveUserPreferences() {
        String fileSep = System.getProperty("file.separator");
        String filename = System.getProperty("user.home") + fileSep + ".gwava.ini";
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);

            System.out.println("Exporting data to file " + filename);

            bw.write("filePath" + "\t" + filePath + "\n");
            
            bw.write("backgroundColor"  + "\t" + backgroundColor.getRGB() + "\n");
            bw.write("pointColor"  + "\t" + pointColor.getRGB() + "\n");
            bw.write("selectionColor"  + "\t" + selectionColor.getRGB() + "\n");
            bw.write("currentColor"  + "\t" + currentColor.getRGB() + "\n");
            bw.write("frameColor"  + "\t" + frameColor.getRGB() + "\n");
            bw.write("frameTextColor"  + "\t" + frameTextColor.getRGB() + "\n");
            bw.write("recombinationColor"  + "\t" + recombinationColor.getRGB() + "\n");
            bw.write("selectionBandColor"  + "\t" + selectionBandColor.getRGB() + "\n");
            bw.write("foundSnpColor"  + "\t" + foundSnpColor.getRGB() + "\n");
            bw.write("axisDragRangeColor"  + "\t" + axisDragRangeColor.getRGB() + "\n");
            bw.write("splitPaneFraction" + "\t" + Math.round(splitPaneFraction*100) + "\n");
            
            bw.write("annotationColor"  + "\t" + annotationColor.getRGB() + "\n");
            bw.write("annotationTextColor"  + "\t" + annotationTextColor.getRGB() + "\n");
            bw.write("currentAnnotationColor"  + "\t" + currentAnnotationColor.getRGB() + "\n");
            bw.write("selectedAnnotationColor"  + "\t" + selectedAnnotationColor.getRGB() + "\n");
            bw.write("closestAnnotationColor"  + "\t" + closestAnnotationColor.getRGB() + "\n");
            bw.write("interiorAnnotationColor"  + "\t" + interiorAnnotationColor.getRGB() + "\n");
            bw.write("intronAnnotationColor"  + "\t" + intronAnnotationColor.getRGB() + "\n");
            
            bw.write("thumbnailColor"  + "\t" + thumbnailColor.getRGB() + "\n");
            bw.write("thumbnailTextColor"  + "\t" + thumbnailTextColor.getRGB() + "\n");
            bw.write("thumbnailPointColor"  + "\t" + thumbnailPointColor.getRGB() + "\n");
            bw.write("thumbnailSelectionBandColor"  + "\t" + thumbnailSelectionBandColor.getRGB() + "\n");
            bw.write("thumbnailSelectionColor"  + "\t" + thumbnailSelectionColor.getRGB() + "\n");
            bw.write("thumbnailHorizontalBandColor"  + "\t" + thumbnailHorizontalBandColor.getRGB() + "\n");
            
            bw.write("heatmapRadius" + "\t" + Singleton.getState().getHeatmapParameters().getRadius() + "\n");
            bw.write("heatmapFunction" + "\t" + Singleton.getState().getHeatmapParameters().getFunction() + "\n");
            bw.write("heatmapTopNindex" + "\t" + Singleton.getState().getHeatmapParameters().getTopNindex()+ "\n");
            bw.write("basePairSearchRadius" + "\t" + basePairSearchRadius + "\n");
            
            bw.write("minTopNegLogPvalAxis" + "\t" + minTopNegLogPvalAxis + "\n");
            bw.write("minTopRecombinationRateAxis" + "\t" + minTopRecombinationRateAxis + "\n");
            
            bw.write("mainFrameLocation" + "\t" + toInt(mainFrameLocation.getX()) + "\t" + toInt(mainFrameLocation.getY()) + "\n");
            bw.write("geneModelFrameLocation" + "\t" + toInt(geneModelFrameLocation.getX()) + "\t" + toInt(geneModelFrameLocation.getY()) + "\n");
            bw.write("mainFrameDimension" + "\t" + toInt(mainFrameSize.getWidth())+ "\t" + toInt(mainFrameSize.getHeight()) + "\n");
            bw.write("geneModelFrameDimension" + "\t" + toInt(geneModelFrameSize.getWidth())+ "\t" + toInt(geneModelFrameSize.getHeight()) + "\n");
            //bw.write("bufferedPlotSize" + "\t" + toInt(bufferedPlotSize.getWidth())+ "\t" + toInt(bufferedPlotSize.getHeight()) + "\n");

        } catch (java.io.IOException ex) {
            System.out.println("write exception in saveUserPreferences to file " + filename);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (Exception ex) {
                System.out.println("Write exception in saveUserPreferences to file " + filename);
            }
        }
    }
    
    private int toInt(double value) {
        int output = (int) Math.round(value);
        return output;
    }

    public void loadUserPreferences() {
        String fileSep = System.getProperty("file.separator");
        String filename = System.getProperty("user.home") + fileSep + ".gwava.ini";
        FileReader fr = null;
        BufferedReader br = null;
        boolean loadSuccessful = true;
        HashMap<String,String> activityRangeValues = new HashMap<String,String>();
        boolean inActivityValues = false;
        
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
        } catch (java.io.FileNotFoundException ex) {
            System.out.println("Could not open file " + filename);
            loadSuccessful = false;
        }
        String line;
        if (loadSuccessful) {
            try {
                while ((line = br.readLine()) != null) {
                    String[] splits = line.split("\t");
                    
                    if (splits[0].equalsIgnoreCase("filePath")) {
                        if (splits.length <= 1 || splits[1].equals("null")) {
                            filePath = "";
                        } else {
                            filePath = splits[1].trim();
                        }
                       
                    } else if(splits[0].equalsIgnoreCase("backgroundColor")) {
                       try {
                           backgroundColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid background color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("pointColor")) {
                       try {
                           pointColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid point color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("selectionColor")) {
                       try {
                           selectionColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid selection color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("currentColor")) {
                       try {
                           currentColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid current color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("currentColor")) {
                       try {
                           currentColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid current color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("frameColor")) {
                       try {
                           frameColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid frame/axis color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("frameTextColor")) {
                       try {
                           frameTextColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid frame/axis tick and text color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("recombinationColor")) {
                       try {
                           recombinationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid recombination color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("selectionBandColor")) {
                       try {
                           selectionBandColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid selection band color" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("foundSnpColor")) {
                       try {
                           foundSnpColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid foundSnpColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("axisDragRangeColor")) {
                       try {
                           axisDragRangeColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid axisDragRangeColor " + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("splitPaneFraction")) {
                       try {
                           splitPaneFraction = Integer.parseInt(splits[1])/100.0;
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid splitPaneFraction " + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("annotationColor")) {
                       try {
                           annotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid annotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("annotationTextColor")) {
                       try {
                           annotationTextColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid annotationTextColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("currentAnnotationColor")) {
                       try {
                           currentAnnotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid currentAnnotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("selectedAnnotationColor")) {
                       try {
                           selectedAnnotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid selectedAnnotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("closestAnnotationColor")) {
                       try {
                           closestAnnotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid closestAnnotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("interiorAnnotationColor")) {
                       try {
                           interiorAnnotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid interiorAnnotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("intronAnnotationColor")) {
                       try {
                           intronAnnotationColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid intronAnnotationColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailColor")) {
                       try {
                           thumbnailColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailTextColor")) {
                       try {
                           thumbnailTextColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailTextColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailPointColor")) {
                       try {
                           thumbnailPointColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailPointColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailSelectionBandColor")) {
                       try {
                           thumbnailSelectionBandColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailSelectionBandColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailSelectionColor")) {
                       try {
                           thumbnailSelectionColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailSelectionColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("thumbnailHorizontalBandColor")) {
                       try {
                           thumbnailHorizontalBandColor = new Color(new Integer(splits[1]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid thumbnailHorizontalBandColor" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("minTopNegLogPvalAxis")) {
                       try {
                           minTopNegLogPvalAxis = Float.parseFloat(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid minTopNegLogPvalAxis" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("minTopRecombinationRateAxis")) {
                       try {
                           minTopRecombinationRateAxis = Float.parseFloat(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid minTopRecombinationRateAxis" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("heatmapRadius")) {
                       try {
                           heatmapRadius = Integer.parseInt(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid heatmapRadius" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("heatmapFunction")) {
                       try {
                           heatmapFunction = Integer.parseInt(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid heatmapFunction" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("heatmapTopNindex")) {
                       try {
                           heatmapTopNindex = Integer.parseInt(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid heatmapTopN" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("basePairSearchRadius")) {
                       try {
                           basePairSearchRadius = Integer.parseInt(splits[1]);
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid basePairSearchRadius" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("mainFrameLocation") && splits.length > 2) {
                       try {
                           mainFrameLocation = new Point(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid mainFrameLocation" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("geneModelFrameLocation") && splits.length > 2) {
                       try {
                           geneModelFrameLocation = new Point(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid geneModelFrameLocation" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("mainFrameDimension") && splits.length > 2) {
                       try {
                           mainFrameSize = new Dimension(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid mainFrameDimension" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("geneModelFrameDimension") && splits.length > 2) {
                       try {
                           geneModelFrameSize = new Dimension(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid geneModelFrameDimension" + "\t" + line);
                       }
                    } else if(splits[0].equalsIgnoreCase("bufferedPlotSize") && splits.length > 2) {
                       try {
                           bufferedPlotSize = new Dimension(Integer.parseInt(splits[1]), Integer.parseInt(splits[2]));
                       } catch(NumberFormatException nfe) {
                           System.out.println("Invalid bufferedPlotsize" + "\t" + line);
                       }
                    }
                }
                
            } catch (java.io.IOException ex) {
                System.out.println("Could not load file " + filename);
                ex.printStackTrace();
            } catch (java.lang.NullPointerException npe) {
                System.out.println("Could not load file " + filename);
                npe.printStackTrace();
            } finally {
                try {
                    br.close();
                    fr.close();
                } catch (java.io.IOException ex) {
                    System.out.println("Could not close file " + filename);
                    ex.printStackTrace();
                }
            }
        }
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if(this.backgroundColor != backgroundColor) {
            this.backgroundColor = backgroundColor;
            fireColorChanged();
        }
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        if(this.currentColor != currentColor) {
            this.currentColor = currentColor;
            fireColorChanged();
        }
    }

    public Color getPointColor() {
        return pointColor;
    }

    public void setPointColor(Color pointColor) {
        if(this.pointColor != pointColor) {
            this.pointColor = pointColor;
            fireColorChanged();
        }
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if(this.selectionColor != selectionColor) {
            this.selectionColor = selectionColor;
            fireColorChanged();
        }
    }

    public Color getAnnotationColor() {
        return annotationColor;
    }

    public void setAnnotationColor(Color annotationColor) {
        if(this.annotationColor != annotationColor) {
            this.annotationColor = annotationColor;
            fireColorChanged();
        }
    }

    public Color getAnnotationTextColor() {
        return annotationTextColor;
    }

    public void setAnnotationTextColor(Color annotationTextColor) {
        if(this.annotationTextColor != annotationTextColor) {
            this.annotationTextColor = annotationTextColor;
            fireColorChanged();
        }
    }

    public Color getClosestAnnotationColor() {
        return closestAnnotationColor;
    }

    public void setClosestAnnotationColor(Color closestAnnotationColor) {
        if(this.closestAnnotationColor != closestAnnotationColor) {
            this.closestAnnotationColor = closestAnnotationColor;
            fireColorChanged();
        }
    }

    public Color getCurrentAnnotationColor() {
        return currentAnnotationColor;
    }

    public void setCurrentAnnotationColor(Color currentAnnotationColor) {
        if(this.currentAnnotationColor != currentAnnotationColor) {
            this.currentAnnotationColor = currentAnnotationColor;
            fireColorChanged();
        }
    }

    public Color getFoundSnpColor() {
        return foundSnpColor;
    }

    public void setFoundSnpColor(Color foundSnpColor) {
        if(this.foundSnpColor != foundSnpColor) {
            this.foundSnpColor = foundSnpColor;
            fireColorChanged();
        }
    }

    public Color getFrameColor() {
        return frameColor;
    }

    public void setFrameColor(Color frameColor) {
        if(this.frameColor != frameColor) {
            this.frameColor = frameColor;
            fireColorChanged();
        }
    }

    public Color getFrameTextColor() {
        return frameTextColor;
    }

    public void setFrameTextColor(Color frameTextColor) {
        if(this.frameTextColor != frameTextColor) {
            this.frameTextColor = frameTextColor;
            fireColorChanged();
        }
    }

    public Color getInteriorAnnotationColor() {
        return interiorAnnotationColor;
    }

    public void setInteriorAnnotationColor(Color interiorAnnotationColor) {
        if(this.interiorAnnotationColor != interiorAnnotationColor) {
            this.interiorAnnotationColor = interiorAnnotationColor;
            fireColorChanged();
        }
    }

    public Color getIntronAnnotationColor() {
        return intronAnnotationColor;
    }

    public void setIntronAnnotationColor(Color intronAnnotationColor) {
        if(this.intronAnnotationColor != intronAnnotationColor) {
            this.intronAnnotationColor = intronAnnotationColor;
            fireColorChanged();
        }
    }
    
    public Color getRecombinationColor() {
        return recombinationColor;
    }

    public void setRecombinationColor(Color recombinationColor) {
        if(this.recombinationColor != recombinationColor) {
            this.recombinationColor = recombinationColor;
            fireColorChanged();
        }
    }

    public Color getSelectedAnnotationColor() {
        return selectedAnnotationColor;
    }

    public void setSelectedAnnotationColor(Color selectedAnnotationColor) {
        if(this.selectedAnnotationColor != selectedAnnotationColor) {
            this.selectedAnnotationColor = selectedAnnotationColor;
            fireColorChanged();
        }
    }

    public Color getSelectionBandColor() {
        return selectionBandColor;
    }

    public void setSelectionBandColor(Color selectionBandColor) {
        if(this.selectionBandColor != selectionBandColor) {
            this.selectionBandColor = selectionBandColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailColor() {
        return thumbnailColor;
    }

    public void setThumbnailColor(Color thumbnailColor) {
        if(this.thumbnailColor != thumbnailColor) {
            this.thumbnailColor = annotationTextColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailHorizontalBandColor() {
        return thumbnailHorizontalBandColor;
    }

    public void setThumbnailHorizontalBandColor(Color thumbnailHorizontalBandColor) {
        if(this.thumbnailHorizontalBandColor != thumbnailHorizontalBandColor) {
            this.thumbnailHorizontalBandColor = thumbnailHorizontalBandColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailPointColor() {
        return thumbnailPointColor;
    }

    public void setThumbnailPointColor(Color thumbnailPointColor) {
        if(this.thumbnailPointColor != thumbnailPointColor) {
            this.thumbnailPointColor = thumbnailPointColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailSelectionBandColor() {
        return thumbnailSelectionBandColor;
    }

    public void setThumbnailSelectionBandColor(Color thumbnailSelectionBandColor) {
        if(this.thumbnailSelectionBandColor != thumbnailSelectionBandColor) {
            this.thumbnailSelectionBandColor = thumbnailSelectionBandColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailSelectionColor() {
        return thumbnailSelectionColor;
    }

    public void setThumbnailSelectionColor(Color thumbnailSelectionColor) {
        if(this.thumbnailSelectionColor != thumbnailSelectionColor) {
            this.thumbnailSelectionColor = thumbnailSelectionColor;
            fireColorChanged();
        }
    }

    public Color getThumbnailTextColor() {
        return thumbnailTextColor;
    }

    public void setThumbnailTextColor(Color thumbnailTextColor) {
        if(this.thumbnailTextColor != thumbnailTextColor) {
            this.thumbnailTextColor = thumbnailTextColor;
            fireColorChanged();
        }
    }


    
    
    public void addListener(UserPrefListener upl) {
        if(listeners.contains(upl)) {
            
        } else {
            listeners.add(upl);
        }
    }
    
    public void removeListener(UserPrefListener upl) {
        if(listeners.contains(upl)) {
            listeners.remove(upl);
        }
    }
    
    public void fireColorChanged() {
        ChangeEvent ce = new ChangeEvent(this);
        for(UserPrefListener upl : listeners) {
            upl.colorChanged(ce);
        }
    }

    public Color getAxisDragRangeColor() {
        return axisDragRangeColor;
    }

    public void setAxisDragRangeColor(Color axisDragRangeColor) {
        if(this.axisDragRangeColor != axisDragRangeColor) {
            this.axisDragRangeColor = axisDragRangeColor;
            fireColorChanged();
        }
    }

    public float getMinTopNegLogPvalAxis() {
        return minTopNegLogPvalAxis;
    }

    /**
     * Returns the top preferred RecombinationRate axis value
     * @return 
     */
    public float getMinTopRecombinationRateAxis() {
        return minTopRecombinationRateAxis;
    }

    /**
     * Sets the top preferred RecombinationRate axis value
     * @return 
     */
    public void setMinTopRecombinationRateAxis(float minTopRecombinationRateAxis) {
        this.minTopRecombinationRateAxis = minTopRecombinationRateAxis;
    }
    
    

    /*
     * Sets value for regular top value displayed [0 to minTopNegLogPvalAxis] if
     * the values are less than this.  it is scaled to the max data if this value
     * is not set
     */
    public void setMinTopNegLogPvalAxis(float minTopNegLogPvalAxis) {
        this.minTopNegLogPvalAxis = minTopNegLogPvalAxis;
    }

    /**
     * Returns the color of the border around the full Manhattan plot with axes
     * and the full annotation panel
     * @return 
     */
    public Color getBorderColor() {
        return borderColor;
    }
    
    /**
     * Sets the plot/annotation fraction
     * @param fraction 
     */
    public void setSplitPaneFraction(double fraction) {
        this.splitPaneFraction = fraction;
    }
    
    public double getSplitPaneFraction() {
        return splitPaneFraction;
    }

    public int getHeatmapFunction() {
        return heatmapFunction;
    }

    public int getHeatmapRadius() {
        return heatmapRadius;
    }

    public int getHeatmapTopNindex() {
        return heatmapTopNindex;
    }

    public int getBasePairSearchRadius() {
        return basePairSearchRadius;
    }
    
    public void setBasePairSearchRadius(int searchRadius) {
        this.basePairSearchRadius = searchRadius;
    }

    public Point getMainFrameLocation() {
        return mainFrameLocation;
    }

    public Point getGeneModelFrameLocation() {
        return geneModelFrameLocation;
    }

    public Dimension getMainFrameSize() {
        return mainFrameSize;
    }

    public Dimension getGeneModelFrameSize() {
        return geneModelFrameSize;
    }
    
    public Dimension getBufferedPlotsize() {
        return bufferedPlotSize;
    }
    
    
}
