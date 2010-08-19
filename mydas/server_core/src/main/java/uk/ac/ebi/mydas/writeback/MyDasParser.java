package uk.ac.ebi.mydas.writeback;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.WritebackException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasFeatureOrientation;
import uk.ac.ebi.mydas.model.DasMethod;
import uk.ac.ebi.mydas.model.DasPhase;
import uk.ac.ebi.mydas.model.DasTarget;
import uk.ac.ebi.mydas.model.DasType;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class MyDasParser {
	private XmlPullParserFactory XPP_FACTORY = null;
	private String href=null;

	public static final String ELEMENT_DASGFF		="DASGFF";
	public static final String ELEMENT_GFF			="GFF";
	public static final String ELEMENT_SEGMENT		="SEGMENT";
	public static final String ELEMENT_FEATURE		="FEATURE";
	public static final String ELEMENT_TYPE			="TYPE";
	public static final String ELEMENT_METHOD		="METHOD";
	public static final String ELEMENT_START		="START";
	public static final String ELEMENT_END			="END";
	public static final String ELEMENT_SCORE		="SCORE";
	public static final String ELEMENT_ORIENTATION	="ORIENTATION";
	public static final String ELEMENT_PHASE		="PHASE";
	public static final String ELEMENT_NOTE			="NOTE";
	public static final String ELEMENT_LINK			="LINK";
	public static final String ELEMENT_TARGET		="TARGET";
	public static final String ELEMENT_PARENT		="PARENT";
	public static final String ELEMENT_PART			="PART";

	public static final String ATT_version="version";
	public static final String ATT_href="href";
	public static final String ATT_id="id";
	public static final String ATT_start="start";
	public static final String ATT_stop="stop";
	public static final String ATT_label="label";
	public static final String ATT_category="category";
	public static final String ATT_cvid="cvId";
	public static final String ATT_reference="reference";
	public static final String ATT_subparts="subparts";
	public static final String ATT_superparts="suṕerparts";


	public MyDasParser(XmlPullParserFactory xmlFactory){
		this.XPP_FACTORY=xmlFactory;
	}

	public DasAnnotatedSegment parse2MyDasModel(String content) throws WritebackException{
		DasAnnotatedSegment segment=null;
		XmlPullParser xpp;
		try {
			xpp = XPP_FACTORY.newPullParser();
			xpp.setInput(new StringReader (content));
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_DOCUMENT) 
					segment= parse2MyDasModel(xpp);
				eventType = xpp.next();
			}

		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The newPullParser could not be created",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The end of the document were not reached",e);
		}

		return segment;
	}

	private DasAnnotatedSegment parse2MyDasModel(XmlPullParser xpp) throws WritebackException {
		DasAnnotatedSegment segment=null;		
		try {
			while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_DASGFF.equals(xpp.getName()))) {
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					final String tagName = xpp.getName();
					if (ELEMENT_GFF.equals(tagName)) {
						segment=parseGFF(xpp);
					}
				}
			}
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: While reading to look for the DASGFF tag",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The next part of the document could not be readed while looking for DASGFF tag",e);
		}
		return segment;
	}

	private DasAnnotatedSegment parseGFF(XmlPullParser xpp) throws WritebackException {
		DasAnnotatedSegment segment=null;
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_href))
				href=xpp.getAttributeValue(i);
		}
		try {
			while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_GFF.equals(xpp.getName()))) {
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					final String tagName = xpp.getName();
					if (ELEMENT_SEGMENT.equals(tagName)) {
						segment=parseSegment(xpp);
					}
				}
			}
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: While reading to look for the GFF tag",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The next part of the document could not be readed while looking for GFF tag",e);
		}
		return segment;
	}

	private DasAnnotatedSegment parseSegment(XmlPullParser xpp) throws WritebackException {
		DasAnnotatedSegment segment=null;
		String id=null,start=null,stop=null,version=null,label=null;
		List<DasFeature> features=null;
		try{
			for (int i=0;i<xpp.getAttributeCount();i++){
				final String attName = xpp.getAttributeName(i);
				if (attName.equals(ATT_id))
					id=xpp.getAttributeValue(i);
				else if (attName.equals(ATT_start))
					start=xpp.getAttributeValue(i);
				else if (attName.equals(ATT_stop))
					stop=xpp.getAttributeValue(i);
				else if (attName.equals(ATT_version))
					version=xpp.getAttributeValue(i);
				else if (attName.equals(ATT_label))
					label=xpp.getAttributeValue(i);
			}
			features = new ArrayList<DasFeature>();
			while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_SEGMENT.equals(xpp.getName()))) {
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					final String tagName = xpp.getName();
					if (ELEMENT_FEATURE.equals(tagName)) {
						features.add(parseFeature(xpp));
					}
				}
			}
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: While reading to look for the GFF tag",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The next part of the document could not be readed while looking for GFF tag",e);
		}
		try {
			segment= new DasAnnotatedSegment(id, new Integer(start), new Integer(stop), version, label, features);
		} catch (NumberFormatException e) {
			throw new WritebackException("Error parsing the document: The start("+start+") or the stop("+stop+") attribute were not converted to numbers",e);
		} catch (DataSourceException e) {
			throw new WritebackException("Error parsing the document: The segment was not created",e);
		}
		return segment;
	}

	private DasFeature parseFeature(XmlPullParser xpp) throws WritebackException {
		DasFeature feature=null;
		String id=null,label=null;
		DasType type=null;
		DasMethod method=null;
		List<String> notes= new ArrayList<String>();
		if (href!=null)
			notes.add("HREF="+href);
		Integer start=null, end=null;
		Double score=null;
		DasFeatureOrientation orientation=null;
		DasPhase phase=null;
		Map<URL,String> links=new HashMap<URL,String>();
		List<DasTarget> targets= new ArrayList<DasTarget>();;
		List<String> parents= new ArrayList<String>();;
		List<String> parts= new ArrayList<String>();;
		try{
			for (int i=0;i<xpp.getAttributeCount();i++){
				final String attName = xpp.getAttributeName(i);
				if (attName.equals(ATT_id))
					id=xpp.getAttributeValue(i);
				else if (attName.equals(ATT_label))
					label=xpp.getAttributeValue(i);
			}

			while (! (xpp.next() == XmlPullParser.END_TAG && ELEMENT_FEATURE.equals(xpp.getName()))) {
				if (xpp.getEventType() == XmlPullParser.START_TAG) {
					final String tagName = xpp.getName();
					if (ELEMENT_TYPE.equals(tagName)) {
						type = parseType(xpp);
					} else if (ELEMENT_METHOD.equals(tagName)) {
						method = parseMethod(xpp);
					} else if (ELEMENT_START.equals(tagName)) {
						xpp.next();
						try{
							start = Integer.parseInt(xpp.getText());
						}catch(NumberFormatException e){
							throw new WritebackException("Error parsing the document: The start("+start+") attribute of the feature were not converted to numbers",e);
						}

					} else if (ELEMENT_END.equals(tagName)) {
						xpp.next();
						try{
							end = Integer.parseInt(xpp.getText());
						}catch(NumberFormatException e){
							throw new WritebackException("Error parsing the document: The end("+end+") attribute of the feature were not converted to numbers",e);
						}
					} else if (ELEMENT_SCORE.equals(tagName)) {
						xpp.next();
						score = Double.parseDouble(xpp.getText());
					} else if (ELEMENT_ORIENTATION.equals(tagName)) {
						orientation = parseOrientation(xpp);
					} else if (ELEMENT_PHASE.equals(tagName)) {
						phase = parsePhase(xpp);
					} else if (ELEMENT_NOTE.equals(tagName)) {
						xpp.next();
						notes.add(xpp.getText());
					} else if (ELEMENT_LINK.equals(tagName)) {
						addLink(links,xpp);
						//						xpp.next();
						//						links.put(new URL(xpp.getAttributeValue(null, ATT_href)), xpp.getText());
					} else if (ELEMENT_TARGET.equals(tagName)) {
						targets.add(parseTarget(xpp));
					} else if (ELEMENT_PARENT.equals(tagName)) {
						addParent(parents,xpp);
						//						xpp.next();
						//						parents.add(xpp.getAttributeValue(null, ATT_id));
					} else if (ELEMENT_PART.equals(tagName)) {
						addPart(parts,xpp);
						xpp.next();
						parts.add(xpp.getAttributeValue(null, ATT_id));
					}
				}
			}
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: While reading to look for the GFF tag",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The next part of the document could not be readed while looking for GFF tag",e);
		}
		try {
			feature= new DasFeature(id, label, type, method, start, end, score, orientation, phase, notes, links, targets, parents, parts);
		} catch (DataSourceException e) {
			throw new WritebackException("Error parsing the document: The feature was not created",e);
		}
		return feature;
	}



	private DasType parseType(XmlPullParser xpp) throws WritebackException {
		DasType type=null;
		String id=null,category=null,cvId=null,label=null;//,reference=null,subparts=null,superparts=null;
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_id))
				id=xpp.getAttributeValue(i);
			else if (attName.equals(ATT_category))
				category=xpp.getAttributeValue(i);
			else if (attName.equals(ATT_cvid))
				cvId=xpp.getAttributeValue(i);
//			else if (attName.equals(ATT_reference))
//				reference=xpp.getAttributeValue(i);
//			else if (attName.equals(ATT_subparts))
//				subparts=xpp.getAttributeValue(i);
//			else if (attName.equals(ATT_superparts))
//				superparts=xpp.getAttributeValue(i);
		}
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The content of the type could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The content of the type could not be readed",e);
		}
		label=xpp.getText();
		type= new DasType(id, category, cvId, label);
		//TODO: How to manage the parts in the writeback?
		return type;
	}
	private DasMethod parseMethod(XmlPullParser xpp) throws WritebackException {
		DasMethod method=null;
		String id=null,cvId=null,label=null;
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_id))
				id=xpp.getAttributeValue(i);
			else if (attName.equals(ATT_cvid))
				cvId=xpp.getAttributeValue(i);
		}
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The content of the method could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The content of the method could not be readed",e);
		}
		label=xpp.getText();
		try {
			method= new DasMethod(id, label,cvId);
		} catch (DataSourceException e) {
			throw new WritebackException("Error parsing the document: The method was not created",e);
		}
		return method;
	}
	private DasFeatureOrientation parseOrientation(XmlPullParser xpp) throws WritebackException {
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The orientation could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The orientation could not be readed",e);
		}
		String ori=xpp.getText();
		if (ori.equals("+"))
			return DasFeatureOrientation.ORIENTATION_SENSE_STRAND;
		else if (ori.equals("-"))
			return DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND;
		return DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
	}
	private DasPhase parsePhase(XmlPullParser xpp) throws WritebackException {
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The phase could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The phase could not be readed",e);
		}
		String phase=xpp.getText();
		if (phase.equals("0"))
			return DasPhase.PHASE_READING_FRAME_0;
		else if (phase.equals("1"))
			return DasPhase.PHASE_READING_FRAME_1;
		else if (phase.equals("2"))
			return DasPhase.PHASE_READING_FRAME_2;
		return DasPhase.PHASE_NOT_APPLICABLE;
	}
	private void addLink(Map<URL, String> links, XmlPullParser xpp) throws WritebackException {
		URL url=null;
		String text;
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_href))
				try {
					url= new URL(xpp.getAttributeValue(i));
				} catch (MalformedURLException e) {
					throw new WritebackException("Error parsing the document: The href in a link tag was not a valid URL",e);
				}
		}
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The link could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The link could not be readed",e);
		}
		text=xpp.getText();
		links.put(url, text);

	}
	private DasTarget parseTarget(XmlPullParser xpp) throws WritebackException {
		String id=null,label=null;
		int start=0,stop=0;
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_id))
				id= xpp.getAttributeValue(i);
			else if (attName.equals(ATT_start))
				start= Integer.parseInt(xpp.getAttributeValue(i));
			else if (attName.equals(ATT_stop))
				stop= Integer.parseInt(xpp.getAttributeValue(i));
		}
		try {
			xpp.next();
		} catch (XmlPullParserException e) {
			throw new WritebackException("Error parsing the document: The link could not be readed",e);
		} catch (IOException e) {
			throw new WritebackException("Error parsing the document: The link could not be readed",e);
		}
		label=xpp.getText();
		
		DasTarget target=null;
		try {
			target = new DasTarget(id, start, stop, label);
		} catch (DataSourceException e) {
			throw new WritebackException("Error parsing the document: The target was not created",e);
		}
		return target;
	}
	private void addParent(List<String> parents, XmlPullParser xpp) {
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_id))
				parents.add(xpp.getAttributeValue(i));
		}
	}
	private void addPart(List<String> parts, XmlPullParser xpp) {
		for (int i=0;i<xpp.getAttributeCount();i++){
			final String attName = xpp.getAttributeName(i);
			if (attName.equals(ATT_id))
				parts.add(xpp.getAttributeValue(i));
		}
	}


}
