package ca.uhn.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.api.AddProfileTagEnum;
import ca.uhn.fhir.util.TestUtil;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BasicPatientTest {

	private static FhirContext ourCtx = FhirContext.forR4();
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BasicPatientTest.class);

	@Before
	public void before() {
		ourCtx.setAddProfileTagWhenEncoding(AddProfileTagEnum.ONLY_FOR_CUSTOM);
	}

	@AfterClass
	public static void afterClassClearContext() {
		TestUtil.clearAllStaticFieldsForUnitTest();
	}

	@Test
	public void minimumPatient() {
		Patient pat = new Patient();
		pat.setId("pat1");

		IParser parser = ourCtx.newJsonParser().setPrettyPrint(true);
		String jsonStr = parser.encodeResourceToString(pat);

		IParser rdfParser = ourCtx.newRdfParser().setPrettyPrint(true);
		String ttlStr = rdfParser.encodeResourceToString(pat);

		ourLog.info(ttlStr);
	}

	@Test
	public void basicPatient() {
		Patient pat = new Patient();
		pat.setId("pat1");
		pat.addName().setFamily("Simpson");

//		List<Extension> undeclaredExtensions = pat.getExtension();
//		Extension ext = new Extension();
//		ext.setUrl("http://example.com/extensions#someext");
//		ext.setValue(new DateTimeType("2011-01-02T11:13:15"));
//		undeclaredExtensions.add(ext);
//
//		List<String> commentsPre = pat.getFormatCommentsPre();
//		commentsPre.add("Format Comments Pre");
//
//		List<String> commentsPost = pat.getFormatCommentsPost();
//		commentsPost.add("Format Comments Post");

//		Meta patMetaData = new Meta();
//		patMetaData.setVersionId("2");
//		patMetaData.setSource("Fabrica");

//		pat.setMeta(patMetaData);


		IParser rdfParser = ourCtx.newRdfParser().setPrettyPrint(true);
		String ttlStr = rdfParser.encodeResourceToString(pat);

		IParser parser = ourCtx.newJsonParser().setPrettyPrint(true);
		String jsonStr = parser.encodeResourceToString(pat);

		ourLog.info(jsonStr);
	}
}
