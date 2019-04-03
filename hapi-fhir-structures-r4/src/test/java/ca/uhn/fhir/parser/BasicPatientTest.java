package ca.uhn.fhir.parser;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.api.AddProfileTagEnum;
import ca.uhn.fhir.util.TestUtil;
import org.hl7.fhir.r4.model.Patient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

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
	public void basicPatient() {
		Patient pat = new Patient();
		pat.addName().setFamily("Simpson");

		IParser parser = ourCtx.newJsonParser().setPrettyPrint(true);
		String jsonStr = parser.encodeResourceToString(pat);

		ourLog.info(jsonStr);
	}
}
