package ca.uhn.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.BaseParser;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.rest.api.EncodingEnum;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class RdfParser extends BaseParser {

    private FhirContext myContext;
    private boolean myPrettyPrint;

    public RdfParser(FhirContext theContext, IParserErrorHandler theParserErrorHandler) {
        super(theContext, theParserErrorHandler);
        myContext = theContext;
    }

    @Override
    protected void doEncodeResourceToWriter(IBaseResource theResource, Writer theWriter, EncodeContext theEncodeContext) throws IOException, DataFormatException {

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
}
