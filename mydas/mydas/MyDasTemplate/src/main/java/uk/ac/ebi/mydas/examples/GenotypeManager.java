package uk.ac.ebi.mydas.examples;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class GenotypeManager {
	/**
	 * List of the types used in this data source
	 */
	private ArrayList<DasType> types;
	/**
	 * Types to be used in the data source: chromosome, gene, transcript and
	 * exon
	 */
	private DasType geneType;
	/**
	 * As this data source just have one method, it can be defined as a
	 * parameter to facilitate its use
	 */
	private DasMethod method;

	private Connection connection;

	private String database = "genotype1";
	private Collection<DasEntryPoint> entryPoints = null;

	public GenotypeManager(String databaseUrl, String databaseUser, String databasePass) throws DataSourceException {

		method = new DasMethod("not_recorded", "not_recorded", "ECO:0000037");

		connection = null;


		//String url = "jdbc:mysql://localhost:3306/" + database;
		// String url = "jdbc:mysql://ensembldb.ensembl.org:5306/"+database;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(databaseUrl, databaseUser, databasePass);
		} catch (InstantiationException e) {
			throw new DataSourceException("Problems loading the MySql driver",
					e);
		} catch (IllegalAccessException e) {
			throw new DataSourceException("Problems loading the MySql driver",
					e);
		} catch (ClassNotFoundException e) {
			throw new DataSourceException("Problems loading the MySql driver",
					e);
		} catch (SQLException e) {
			throw new DataSourceException(
					"Problems connecting to the ensembl database", e);
		}
		// Initialize types
		types = new ArrayList<DasType>();
		try {
			Statement s = connection.createStatement();
			s.executeQuery("select distinct bases from genotype;");
			ResultSet rs = s.getResultSet();
			while (rs.next()) {
				String typeId = rs.getString("bases");
				System.out.println("typeid=" + typeId);
				types.add(new DasType(typeId, "", "SO:0000694", ""));

			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query", e);
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (Exception e) { /* ignore close errors */
		}

	}



	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId,
			int start, int stop) throws DataSourceException,
			BadReferenceObjectException {
		return getSubmodelBySegmentId(segmentId, start, stop, -1);
	}

	public DasAnnotatedSegment getSubmodelBySegmentId(String segmentId,
			int start, int stop, int maxbins) throws DataSourceException,
			BadReferenceObjectException {
	
		
		
		String sql = "select * from genotype where chromosome="+segmentId+" and position > "+start+" and position < "+stop+";";
		System.out.println(sql);
		Collection<DasFeature> features = getGenotypeFeatures( sql);
		DasAnnotatedSegment segment = null;
		try {
			segment = new DasAnnotatedSegment(segmentId,new Integer(start),new Integer(stop),"1.0", segmentId, features);
		} catch (DataSourceException e) {
			//  Auto-generated catch block
			e.printStackTrace();
		}
		return segment;
	}
	

	private Collection<DasFeature> getGenotypeFeatures(
			String sql) {
			
		Collection <DasFeature> features=new ArrayList();
		try {
			Statement s = connection.createStatement();
			s.executeQuery(sql);
			ResultSet rs = s.getResultSet();
			while (rs.next()) {
				String typeId = rs.getString("bases");
				String id=rs.getString("rs_id");
				int position=rs.getInt("position");
				//rs.getInt("chromosome");
				DasType type=new DasType(typeId, "", "SO:0000694", "");
				DasMethod method = null;
				try {
					method = new DasMethod("23AndMe", "microarray", "");
				} catch (DataSourceException e1) {
					//  Auto-generated catch block
					e1.printStackTrace();
				}
				
				DasFeature feature = null;
				try {
					feature = new DasFeature(id,id,type, method,position, position,new Double(1),DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE, DasPhase.PHASE_NOT_APPLICABLE,null, null, null, null, null  );
				} catch (DataSourceException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
				features.add(feature);
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			try {
				throw new DataSourceException("Problems executing the sql query", e);
			} catch (DataSourceException e1) {
				//  Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		return features;
	}

	public ArrayList<DasType> getTypes() {
		System.out.println("Getting types from genotype manager");
		for (DasType type : types) {
			System.out.println(type.getCvId());
		}
		return types;
	}

	public Integer getTotalCountForType(String typeId)
			throws DataSourceException {

		String sql = "SELECT count(bases) as num  from genotype where bases='"
				+ typeId + "'";
		int count = 0;
		try {
			Statement s = connection.createStatement();
			s.executeQuery(sql);
			ResultSet rs = s.getResultSet();
			if (rs.next()) {
				count = rs.getInt("num");
			}
			rs.close();
			s.close();
		} catch (SQLException e) {
			throw new DataSourceException("Problems executing the sql query", e);
		}

		return count;
	}


	public String getDatabase() {
		return database;
	}


}
