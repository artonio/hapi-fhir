package ca.uhn.fhir.parser;

import ca.uhn.fhir.context.*;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.base.composite.BaseContainedDt;
import ca.uhn.fhir.parser.json.JsonLikeWriter;
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
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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

        // JsonParser line 617
        if (theObjectNameOrNull == null) {
        } else {
        }

        String resId = "";
        if (theResourceId != null && theResourceId.hasIdPart()) {
        		// What is extensions and modifierExtensions JsonParser line 626
            resId = theResourceId.getIdPart();
        } else {
        		resId = UUID.randomUUID().toString();
		  }
		 String resourceUrl = fhirUrl
			 .concat(resDef.getName())
			 .concat("/")
			 .concat(resId);

        this.makeIdentifierTriple(resourceUrl, "fhir:" + resDef.getName());
        this.makeResourceIdTriple(resourceUrl, resId, fieldClassMap);

		 if (theResource instanceof IResource) {
			 IResource resource = (IResource) theResource;
		 	// TODO:  JsonParser line 647
			 System.out.println("I am an instance of IResource");
		 }

		 encodeCompositeElementToStreamWriter(theResDef, theResource, theResource, theEventWriter, theContainedResource, new CompositeChildElement(resDef, theEncodeContext), theEncodeContext);

        this.makeFakeFamilyNameTriple(resourceUrl, "fhir:" + resDef.getName(), 0, fieldClassMap);
        this.makeFakeFamilyNameTriple(resourceUrl, "fhir:" + resDef.getName(), 1, fieldClassMap);

       this.logTurtle(builder);



    }

	private void encodeCompositeElementToStreamWriter(RuntimeResourceDefinition theResDef, IBaseResource theResource, IBase theNextValue, TurtleWriter theEventWriter, boolean theContainedResource, CompositeChildElement theParent, EncodeContext theEncodeContext) throws IOException, DataFormatException {

    	// TODO: How do we write comments in RDF Format???
//		writeCommentsPreAndPost(theNextValue, theEventWriter);
		encodeCompositeElementChildrenToStreamWriter(theResDef, theResource, theNextValue, theEventWriter, theContainedResource, theParent, theEncodeContext);
	}

	private void encodeCompositeElementChildrenToStreamWriter(RuntimeResourceDefinition theResDef, IBaseResource theResource, IBase theElement, TurtleWriter theEventWriter,
																				 boolean theContainedResource, CompositeChildElement theParent, EncodeContext theEncodeContext) throws IOException {
		{
			String elementId = getCompositeElementId(theElement);
			if (isNotBlank(elementId)) {
				System.out.println("ElementId: " + elementId);
			}
		}

		// TODO: What is a writtenExtension?
		boolean haveWrittenExtensions = false;
		for (CompositeChildElement nextChildElem : super.compositeChildIterator(theElement, theContainedResource, theParent, theEncodeContext)) {
			BaseRuntimeChildDefinition nextChild = nextChildElem.getDef();
			if (nextChildElem.getDef().getElementName().equals("extension") || nextChildElem.getDef().getElementName().equals("modifierExtension")
				|| nextChild instanceof RuntimeChildDeclaredExtensionDefinition) {
				if (!haveWrittenExtensions) {
//					extractAndWriteExtensionsAsDirectChild(theElement, theEventWriter, myContext.getElementDefinition(theElement.getClass()), theResDef, theResource, nextChildElem, theParent, theEncodeContext);
					haveWrittenExtensions = true;
				}
				continue;
			}

			// TODO: JsonParser line 351
			// if (nextChild instanceof RuntimeChildNarrativeDefinition)
			// TODO: What is RuntimeChildNarrativeDefinition?
			// else if (nextChild instanceof RuntimeChildContainedResources)
			// TODO: What is RuntimeChildContainedResources?


			List<? extends IBase> values = nextChild.getAccessor().getValues(theElement);
			values = super.preProcessValues(nextChild, theResource, values, nextChildElem, theEncodeContext);

			if (values == null || values.isEmpty()) {
				continue;
			}

			String currentChildName = null;
			boolean inArray = false;

			// TODO: JsonParser line 390
//			ArrayList<ArrayList<JsonParser.HeldExtension>> extensions = new ArrayList<ArrayList<JsonParser.HeldExtension>>(0);
//			ArrayList<ArrayList<JsonParser.HeldExtension>> modifierExtensions = new ArrayList<ArrayList<JsonParser.HeldExtension>>(0);
//			ArrayList<ArrayList<String>> comments = new ArrayList<ArrayList<String>>(0);
//			ArrayList<String> ids = new ArrayList<String>(0);

			int valueIdx = 0;
			for (IBase nextValue : values) {
				IBase v = nextValue;
				if (nextValue == null || nextValue.isEmpty()) {
					if (nextValue instanceof BaseContainedDt) {
						if (theContainedResource || getContainedResources().isEmpty()) {
							continue;
						}
					} else {
						continue;
					}
				}

				BaseParser.ChildNameAndDef childNameAndDef = super.getChildNameAndDef(nextChild, nextValue);
				if (childNameAndDef == null) {
					continue;
				}

				/*
				 * Often the two values below will be the same thing. There are cases though
				 * where they will not be. An example would be Observation.value, which is
				 * a choice type. If the value contains a Quantity, then:
				 * nextChildGenericName = "value"
				 * nextChildSpecificName = "valueQuantity"
				 */
				String nextChildSpecificName = childNameAndDef.getChildName();
				String nextChildGenericName = nextChild.getElementName();

				System.out.println("nextChildSpecificName: " + nextChildSpecificName);
				System.out.println("nextChildGenericName: " + nextChildGenericName);

				theEncodeContext.pushPath(nextChildGenericName, false);

				BaseRuntimeElementDefinition<?> childDef = childNameAndDef.getChildDef();
				boolean primitive = childDef.getChildType() == BaseRuntimeElementDefinition.ChildTypeEnum.PRIMITIVE_DATATYPE;

				if ((childDef.getChildType() == BaseRuntimeElementDefinition.ChildTypeEnum.CONTAINED_RESOURCES || childDef.getChildType() == BaseRuntimeElementDefinition.ChildTypeEnum.CONTAINED_RESOURCE_LIST) && theContainedResource) {
					continue;
				}

				boolean force = false;
				if (primitive) {
					// TODO: Code dealing with if nextValue instance of Some Extension | JsonParser line 434
					String elementId = getCompositeElementId(nextValue);
					if (isNotBlank(elementId)) {
						force = true;
						System.out.println("elementId: " + elementId);
						// TODO: What does this do? | JsonParser line: 459
//						addToHeldIds(valueIdx, ids, elementId);
					}
				}

				if (currentChildName == null || !currentChildName.equals(nextChildSpecificName)) {
					if (inArray) {
						System.out.println("inArray: " + inArray);
						// TODO: End array
					}
					if (nextChild.getMax() > 1 || nextChild.getMax() == Child.MAX_UNLIMITED) {
						// TODO: Begin Array
						inArray = true;
					}
				}  else if (nextChild instanceof RuntimeChildNarrativeDefinition && theContainedResource) {
					// suppress narratives from contained resources
				} else {

				}
			}

		}


	}

	private void makeFakeFamilyNameTriple(String originalSubject, String resourceType, int index, Map<String, String> fieldClassMap) {
		String key = "id";
		String resourceIdPredicate = "fhir:" + fieldClassMap.get(key) + "." + key + ".name";
		ValueFactory vf = SimpleValueFactory.getInstance();
		BNode bNode = vf.createBNode();
		builder.add(resourceType, bNode)
					.subject(bNode)
					.add("fhir:index", index)
					.subject(originalSubject);

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

    // This is equivalent of resourceType in JsonParser
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
