package ca.uhn.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.rest.api.EncodingEnum;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RdfParser extends BaseParser {

    private FhirContext myContext;
    private boolean myPrettyPrint;

    private String fhirUrl = "http://hl7.org/fhir/";

    ModelBuilder builder = new ModelBuilder();

    public RdfParser(FhirContext theContext, IParserErrorHandler theParserErrorHandler) {
        super(theContext, theParserErrorHandler);
        myContext = theContext;
        builder.setNamespace("fhir", "http://hl7.org/fhir/");
    }

    @Override
    protected void doEncodeResourceToWriter(IBaseResource theResource, Writer theWriter, EncodeContext theEncodeContext) throws IOException, DataFormatException {
        TurtleWriter tw = new TurtleWriter(theWriter);
        doEncodeResourceToTurtleLikeWriter(theResource, tw, theEncodeContext);
    }

    @Override
    protected <T extends IBaseResource> T doParseResource(Class<T> theResourceType, Reader theReader) throws DataFormatException {
        return null;
    }

    @Override
    public EncodingEnum getEncoding() {
        return EncodingEnum.RDF;
    }

    @Override
    public IParser setPrettyPrint(boolean thePrettyPrint) {
        myPrettyPrint = thePrettyPrint;
        return this;
    }

    public void doEncodeResourceToTurtleLikeWriter(IBaseResource theResource, TurtleWriter theEventWriter, EncodeContext theEncodeContext) throws IOException {
        RuntimeResourceDefinition resDef = myContext.getResourceDefinition(theResource);
        encodeResourceToTurtleStreamWriter(resDef, theResource, theEventWriter, null, false,  theEncodeContext);

    }

    @SuppressWarnings("Duplicates")
    private void encodeResourceToTurtleStreamWriter(RuntimeResourceDefinition theResDef, IBaseResource theResource, TurtleWriter theEventWriter, String theObjectNameOrNull,
                                                    boolean theContainedResource, EncodeContext theEncodeContext) throws IOException {
        IIdType resourceId = null;

        if (StringUtils.isNotBlank(theResource.getIdElement().getIdPart())) {
            resourceId = theResource.getIdElement();
            if (theResource.getIdElement().getValue().startsWith("urn:")) {
                resourceId = null;
            }
        }

        if (!theContainedResource) {
            if (!super.shouldEncodeResourceId(theResource, theEncodeContext)) {
                resourceId = null;
                // HACK ALERT MAYBE??? - had to change getResourcePath from protected to public
                // JsonParser and XmlParser do not complain, but this class does...
            } else if (theEncodeContext.getResourcePath().size() == 1 && getEncodeForceResourceId() != null) {
                resourceId = getEncodeForceResourceId();
            }
        }

        encodeResourceToTurtleStreamWriter(theResDef, theResource, theEventWriter, theObjectNameOrNull, theContainedResource, resourceId, theEncodeContext);
    }

    @SuppressWarnings("Duplicates")
    private void encodeResourceToTurtleStreamWriter(RuntimeResourceDefinition theResDef, IBaseResource theResource, TurtleWriter theEventWriter, String theObjectNameOrNull,
                                                  boolean theContainedResource, IIdType theResourceId, EncodeContext theEncodeContext) throws IOException {
        if (!super.shouldEncodeResource(theResDef.getName())) {
            return;
        }

        if (!theContainedResource) {
            super.containResourcesForEncoding(theResource);
        }

        RuntimeResourceDefinition resDef = myContext.getResourceDefinition(theResource);
        Map<String, String> fieldClassMap = myContext.getFieldDeclaringClassMap(theResource);

        if (theObjectNameOrNull == null) {
        } else {
        }

        String resId = UUID.randomUUID().toString();
        String resourceUrl = fhirUrl
                                .concat(resDef.getName())
                                .concat("/")
                                .concat(resId);
        if (theResourceId != null && theResourceId.hasIdPart()) {
            resId = theResourceId.getIdPart();
            resourceUrl = fhirUrl
                    .concat(resDef.getName())
                    .concat("/")
                    .concat(resId);
        }

        this.makeIdentifierTriple(resourceUrl, "fhir:" + resDef.getName());
        this.makeResourceIdTriple(resourceUrl, resId, fieldClassMap);

       this.logTurtle(builder);



    }

    private void makeResourceIdTriple(String originalSubject, String id, Map<String, String> fieldClassMap) {
        String key = "id";
        String resourceIdPredicate = "fhir:" + fieldClassMap.get(key) + "." + key;
        ValueFactory vf = SimpleValueFactory.getInstance();
        BNode resId = vf.createBNode();
        builder.subject(originalSubject)
                .add(resourceIdPredicate, resId)
                .subject(resId)			    // switch the subject
                .add("fhir:value", id)
                .subject(originalSubject);
    }

    private void makeIdentifierTriple(String resourceUrl, String resourceType) {
        builder.subject(resourceUrl)
                .add(RDF.TYPE, resourceType)
                .add("fhir:nodeRole", "fhir:treeRoot");
    }

    private void logTurtle(ModelBuilder builder) {
        StringWriter sw = new StringWriter();
        TurtleWriter tw = new TurtleWriter(sw);
        tw.getWriterConfig().set(BasicWriterSettings.PRETTY_PRINT, true).set(BasicWriterSettings.INLINE_BLANK_NODES,
                true);
        Model m = builder.build();

        tw.startRDF();
        tw.handleNamespace("fhir", "http://hl7.org/fhir/");
        Iterator<Statement> statementIterator = m.iterator();
        while (statementIterator.hasNext()) {
            Statement s = statementIterator.next();
            tw.handleStatement(s);
        }
        tw.endRDF();

        System.out.println(sw.toString());
    }

}
