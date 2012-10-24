package uk.ac.ebi.mydas.example;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.datasource.CommandExtender;
import uk.ac.ebi.mydas.exceptions.BadCommandException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author 4ndr01d3
 */
public class TestDataSourceSecond implements AnnotationDataSource, CommandExtender {

    ServletContext svCon;
    Map<String, PropertyType> globalParameters;
    DataSourceConfiguration config;

    public void destroy() {
        // In this case, nothing to do.
    }

    public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbeans)
            throws BadReferenceObjectException, DataSourceException {
        try {
            if (segmentId.equals("one")) {
                Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(2);
                DasTarget target = new DasTarget("oneTargetId", 20, 30, "oneTargetName");
                oneFeatures.add(new DasFeature(
                        "oneFeatureIdOne",
                        "one Feature Label One",
                        new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "CV:00001", "one Feature DasType Label One"),
                        new DasMethod("oneFeatureMethodIdOne", "one Feature Method Label One", "ECO:12345"),
                        5,
                        10,
                        123.45,
                        DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                        DasPhase.PHASE_NOT_APPLICABLE,
                        Collections.singleton("This is a note relating to feature one of segment one."),
                        Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                        Collections.singleton(target),
                        null,
                        null
                ));
                return new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures);
            } else throw new BadReferenceObjectException(segmentId, "Not found");
        } catch (MalformedURLException e) {
            throw new DataSourceException("Tried to create an invalid URL for a LINK element.", e);
        }
    }

    public Collection<DasAnnotatedSegment> getFeatures(
            Collection<String> featureIdCollection, Integer maxbins)
            throws UnimplementedFeatureException, DataSourceException {
        Collection<DasAnnotatedSegment> segments = new ArrayList<DasAnnotatedSegment>(featureIdCollection.size());
        for (String featureId : featureIdCollection) {
            if (featureId.equals("oneFeatureIdOne")) {
                Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(1);
                DasTarget target = new DasTarget("oneTargetId", 20, 30, "oneTargetName");
                try {
                    oneFeatures.add(new DasFeature(
                            "oneFeatureIdOne",
                            "one Feature Label One",
                            new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "CV:00001", "one Feature DasType Label One"),
                            new DasMethod("oneFeatureMethodIdOne", "one Feature Method Label One", "ECO:12345"),
                            5,
                            10,
                            123.45,
                            DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                            DasPhase.PHASE_NOT_APPLICABLE,
                            Collections.singleton("This is a note relating to feature one of segment one."),
                            Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                            Collections.singleton(target),
                            null,
                            null
                    ));
                } catch (MalformedURLException e) {
                }
                segments.add(new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures));
            } else {
                segments.add(new DasUnknownFeatureSegment(featureId));
            }
        }
        return segments;
    }

    public URL getLinkURL(String field, String id)
            throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("No implemented");
    }

    public Integer getTotalCountForType(DasType type)
            throws DataSourceException {
        if (type.getId() == "oneFeatureTypeIdTwo")
            return new Integer(1);
        return null;
    }

    public Collection<DasType> getTypes() throws DataSourceException {
        Collection<DasType> types = new ArrayList<DasType>(1);
        types.add(new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "CV:00001", null));
        return types;
    }

    public void init(ServletContext servletContext,
                     Map<String, PropertyType> globalParameters,
                     DataSourceConfiguration dataSourceConfig)
            throws DataSourceException {
        this.svCon = servletContext;
        this.globalParameters = globalParameters;
        this.config = dataSourceConfig;
    }

    public void executeOtherCommand(HttpServletRequest request,
                                    HttpServletResponse response,
                                    DataSourceConfiguration dataSourceConfig,
                                    String command, String queryString)
            throws BadCommandException, DataSourceException {
        System.out.println("Command: {" + command + "}");
        System.out.println("QueryString: {" + queryString + "}");
        if (command.equals("newCommand"))
            try {
                response.setContentType("text/xml");
                response.getWriter().write("<?xml version=\"1.0\" ?>\n<newcommand>RESPONSE</newcommand>");
                return;
            } catch (IOException e) {
                throw new DataSourceException("Problems writing the response", e);
            }
        throw new BadCommandException("The command is not recognised. Query: {" + queryString + "}");
    }

    @Override
    public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins,
                                           Range rows) throws BadReferenceObjectException,
            DataSourceException, UnimplementedFeatureException {
        throw new UnimplementedFeatureException("The rows-for-feature capability has not been implemented");
    }

    @Override
    public Collection<DasAnnotatedSegment> getFeatures(
            Collection<String> featureIdCollection, Integer maxbins, Range rows)
            throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("The rows-for-feature capability has not been implemented");
    }

}
