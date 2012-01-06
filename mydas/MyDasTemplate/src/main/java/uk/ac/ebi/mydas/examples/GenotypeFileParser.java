package uk.ac.ebi.mydas.examples;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

import org.apache.jasper.logging.Logger;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasComponentFeature;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasType;

public class GenotypeFileParser {

	/**
	 * Object used to process the file line by line.
	 */
	private Scanner scanner;
	/**
	 * List of the parsed segments.
	 */
	private ArrayList<DasAnnotatedSegment> segments;
	/**
	 * List of the types used in this data source
	 */
	private ArrayList<DasType> types;
	/**
	 * Types to be used in the data source:  gene, transcript and exon
	 */
	private DasType snpType;
	/**
	 * As this data source just have one method, it can be defined as a parameter to facilitate its use
	 */
	private DasMethod method;

	/**
	 * Instantiate the scanner with the stream, creates the empty lists for types and segments and creates the types and method to use through the source.
	 * @param gffdoc Stream with the content of the file to process 
	 * @param fileInputStream 
	 * @throws Exception 
	 */
	public GenotypeFileParser(InputStream gffdoc, InputStream fileInputStream) throws Exception{
		scanner= new Scanner(gffdoc);
		segments= new ArrayList<DasAnnotatedSegment>();
		this.processSegments(fileInputStream);
		types= new ArrayList<DasType>();
		snpType= new DasType("AA", "", "SO:0000694", "");
		method = new DasMethod("not_recorded","not_recorded","ECO:0000037");
	}
	
	/**
	 * @param fileInputStream
	 * @throws Exception 
	 */
	private void processSegments(InputStream fileInputStream) throws Exception {
		Scanner entryPoinstsScanner= new Scanner(fileInputStream);
		try {
			//first use a Scanner to get each line
			while ( entryPoinstsScanner.hasNextLine() ){
			
				processEntryPointsLine( entryPoinstsScanner.nextLine() );
				
			}
		} finally {
			//ensure the underlying stream is always closed
			entryPoinstsScanner.close();
		}
		
	}

	/**
	 * @param nextLine
	 * @throws Exception 
	 */
	private void processEntryPointsLine(String nextLine) throws Exception {
		//System.out.println(nextLine);
		String[] parts = nextLine.split("\\|");
		if (parts.length<6)
			throw new Exception("Parsing Error: A line doesn't have the right number of fields ["+nextLine+"]");
		this.getSegment(parts[1], parts[2], parts[3]);
		
	}

	/**
	 * Go through the whole file line by line to process its content  
	 * @return a set of the segments with its features in the file
	 * @throws Exception in cases where the parsing has errors
	 */
	public Collection<DasAnnotatedSegment> parse() throws Exception{
		try {
			//first use a Scanner to get each line
			while ( scanner.hasNextLine() ){

				processLine( scanner.nextLine() );
				
			}
		} finally {
			//ensure the underlying stream is always closed
			scanner.close();
		}
		return this.segments;
	}

	/**
	 * Processes a line by splitting it by pipes. 
	 * The acquired data is used then, to call the methods to get/create the segment(chromosome) and features(gene, transcript or exon). 
	 * @param aLine
	 * @throws Exception
	 */
	private void processLine(String aLine) throws Exception{
		//System.out.println(aLine);
		String[] parts = aLine.split("\t");
		if (parts.length<4)
			throw new Exception("Parsing Error: A line doesn't have the right number of fields ["+aLine+"]");
		DasAnnotatedSegment segment = this.getSegment(parts[1],parts[2],parts[3]);
		snpType= new DasType(parts[3], "", "SO:0000694", "");
		types.add(snpType);
		DasFeature feature=new DasFeature(parts[0],parts[0],snpType, method, Integer.parseInt(parts[2]),Integer.parseInt(parts[2]), null,null,null,null, null, null, null, null);
		segment.getFeatures().add(feature);
		//DasComponentFeature gene= this.getGene(parts[0],parts[2],parts[2],segment);
		
		
	}

	
	/**
	 * To get a segment we start by looking in the current list of segments in case this segment has been created already. 
	 * If is found is returned. If not a new segment is created using the id of the chromosome. 
	 * And then, added to the list and returned. the file is not giving us too much information about the chromosome, 
	 * so we are using default values in most of its fields.
	 * @param segmentId the id to recover/create a segment
	 * @param stop 
	 * @param start 
	 * @return The segment with that ID
	 * @throws DataSourceException in case there is a problem creating a DAS object.
	 */
	private DasAnnotatedSegment getSegment(String segmentId, String start, String stop) throws DataSourceException {
		for (DasAnnotatedSegment segment:segments)
			if (segment.getSegmentId().equals(segmentId))
				return segment;
		DasAnnotatedSegment newSegment = new DasAnnotatedSegment(segmentId,Integer.parseInt(start),Integer.parseInt(stop),"FROM_PIPE_FILE",segmentId, new ArrayList<DasFeature>());
		//System.out.println("adding new seg="+newSegment.getSegmentId());
		segments.add(newSegment);
		return newSegment;
	}
	public static void main(String[] a){
//		try {
//			GenotypeFileParser parser = new GenotypeFileParser((new FileInputStream("/Users/4ndr01d3/Downloads/test16genes.txt")));
//			parser.parse();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public Collection<DasType> getTypes() {
		return types;
	}

}
