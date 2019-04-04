package ca.uhn.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.parser.BaseParser;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.rest.api.EncodingEnum;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class RdfParser extends BaseParser {

    private FhirContext myContext;
    private boolean myPrettyPrint;

    ModelBuilder builder = new ModelBuilder();

    public RdfParser(FhirContext theContext, IParserErrorHandler theParserErrorHandler) {
        super(theContext, theParserErrorHandler);
        myContext = theContext;
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

    private void encodeResourceToTurtleStreamWriter(RuntimeResourceDefinition theResDef, IBaseResource theResource, TurtleWriter theEventWriter, String theObjectNameOrNull,
                                                  boolean theContainedResource, IIdType theResourceId, EncodeContext theEncodeContext) throws IOException {
        if (!super.shouldEncodeResource(theResDef.getName())) {
            return;
        }

        if (!theContainedResource) {
            super.containResourcesForEncoding(theResource);
        }

        RuntimeResourceDefinition resDef = myContext.getResourceDefinition(theResource);
    }
}
