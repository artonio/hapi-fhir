package org.hl7.fhir.r4.utils.transform;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for the generation of a FHIR StructureDefinition of type Extension and
 * a definition of elements referencing this definition in a given profile.
 *
 * @author Claude Nanjo
 */
public class FhirExtensionGenerator {

  /**
   * @param name
   * @param contexts
   * @param shortDescription
   * @param definition
   * @param min
   * @param max
   * @param type
   * @return
   */
  public StructureDefinition generateExtensionStructureDefinition(String name, List<StringType> contexts, String shortDescription, String definition, int min, String max, String type) {
    StructureDefinition extensionStructureDefinition = new StructureDefinition();
    extensionStructureDefinition.setStatus(Enumerations.PublicationStatus.ACTIVE);
    extensionStructureDefinition.setFhirVersion("3.0.1");//TODO Find out from Grahame or James See Constants
    extensionStructureDefinition.setKind(StructureDefinition.StructureDefinitionKind.COMPLEXTYPE);
    extensionStructureDefinition.setType("Extension");
    extensionStructureDefinition.setContextType(StructureDefinition.ExtensionContext.RESOURCE);
    extensionStructureDefinition.setContext(contexts);
    extensionStructureDefinition.setBaseDefinition("http://hl7.org/fhir/StructureDefinition/Extension");
    extensionStructureDefinition.setId(name);
    extensionStructureDefinition.setName(name);
    extensionStructureDefinition.setUrl("StructureDefinition/" + name);
    extensionStructureDefinition.setDerivation(StructureDefinition.TypeDerivationRule.CONSTRAINT);
    populateDifferential(extensionStructureDefinition, name, shortDescription, definition, min, max, type);
    return extensionStructureDefinition;
  }

  /**
   * @param addExtensionSlicingDefinition
   * @param rootPath
   * @param extensionId
   * @param shortDescription
   * @param definition
   * @param min
   * @param max
   * @param extensionUrl
   * @return
   */
  public List<ElementDefinition> generateExtensionElementDefinitions(boolean addExtensionSlicingDefinition, String rootPath, String extensionId, String shortDescription, String definition, int min, String max, String extensionUrl) {
    List<ElementDefinition> extensionElementDefinitions = new ArrayList<>();
    if (addExtensionSlicingDefinition) {
      ElementDefinition extensionSliceDefinition = new ElementDefinition(new StringType(rootPath + ".extension"));
      extensionSliceDefinition.getSlicing().addDiscriminator().setType(ElementDefinition.DiscriminatorType.VALUE).setPath("url");
      extensionSliceDefinition.getSlicing().setRules(ElementDefinition.SlicingRules.OPEN);
      extensionElementDefinitions.add(extensionSliceDefinition);
    }
    ElementDefinition extensionReference = new ElementDefinition(new StringType(rootPath + ".extension"));
    extensionReference.setId(rootPath + ".extension:" + extensionId);
    extensionReference.setSliceName(extensionId);
    extensionReference.setLabel(extensionId);
    extensionReference.setShort(shortDescription);
    extensionReference.setDefinition(definition);
    extensionReference.setMin(min);
    extensionReference.setMax(max);
    extensionReference.addType().setCode("Extension").setProfile(extensionUrl);
    extensionElementDefinitions.add(extensionReference);
    return extensionElementDefinitions;
  }

  /**
   * @param extensionElementDefinition
   * @param extensionName
   * @param shortDescription
   * @param definition
   * @param min
   * @param max
   * @param type
   */
  protected void populateDifferential(StructureDefinition extensionElementDefinition, String extensionName, String shortDescription, String definition, int min, String max, String type) {
    populateExtensionElement(extensionElementDefinition, shortDescription, definition, min, max);
    populateExtensionUrlElement(extensionElementDefinition, extensionName);
    populateExtensionValueElement(extensionElementDefinition, type, type, shortDescription, definition);
  }

  /**
   * @param extensionStructureDefinition
   * @param shortDescription
   * @param definition
   * @param min
   * @param max
   */
  private void populateExtensionElement(StructureDefinition extensionStructureDefinition, String shortDescription, String definition, int min, String max) {
    ElementDefinition extension = new ElementDefinition(new StringType("Extension"));
    extension.setId("Extension");
    extension.setShort(shortDescription);
    extension.setDefinition(definition);
    extension.setMin(min);
    extension.setMax(max);
    extensionStructureDefinition.getDifferential().addElement(extension);
  }

  /**
   * @param extensionStructureDefinition
   * @param extensionName
   */
  private void populateExtensionUrlElement(StructureDefinition extensionStructureDefinition, String extensionName) {
    ElementDefinition extension = new ElementDefinition(new StringType("Extension.url"));
    extension.setId("Extension.url");
    UriType uri = new UriType("StructureDefinition" + "/" + extensionName);
    extension.setFixed(uri);
    extensionStructureDefinition.getDifferential().addElement(extension);
  }

  /**
   * @param extensionStructureDefinition
   * @param type
   * @param typeSuffix
   * @param shortDescription
   * @param definition
   */
  private void populateExtensionValueElement(StructureDefinition extensionStructureDefinition, String type, String typeSuffix, String shortDescription, String definition) {
    ElementDefinition extension = new ElementDefinition(new StringType("Extension.value" + typeSuffix));
    extension.setId("Extension.value[x]:value" + typeSuffix);
    extension.setSliceName("value" + typeSuffix);
    extension.setShort(shortDescription);
    extension.setDefinition(definition);
    extension.addType().setCode(type);
    extensionStructureDefinition.getDifferential().addElement(extension);
  }

}