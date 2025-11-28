package com.example.xsdparser;

import org.apache.ws.commons.schema.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XsdParserService {

    private XmlSchema schema;
    private final Map<String, String> elementTree = new HashMap<>();

    /** Entry method */
    public Map<String, String> parseXsd(String xsdPath) throws Exception {

        XmlSchemaCollection collection = new XmlSchemaCollection();
        schema = collection.read(new File(xsdPath).toURI().toURL());

        System.out.println("Loaded schema targetNamespace: " + schema.getTargetNamespace());

        for (XmlSchemaObject obj : schema.getItems()) {
            if (obj instanceof XmlSchemaElement rootElement) {
                processElement(rootElement, rootElement.getName());
            }
        }

        return elementTree;
    }

    /** Process a single element */
    private void processElement(XmlSchemaElement element, String path) {

        element = resolveElementRef(element);

        elementTree.put(path, element.getSchemaTypeName() != null
                ? element.getSchemaTypeName().getLocalPart() : "complex/simple");

        XmlSchemaType type = element.getSchemaType();

        if (type instanceof XmlSchemaComplexType complexType) {
            processComplexType(complexType, path);
        }
    }

    /** Process complex types (sequence, choice, all) */
    private void processComplexType(XmlSchemaComplexType complexType, String parentPath) {

        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            processParticle(particle, parentPath);
        }
    }

    /** Handles Sequence, Choice, Element, Any */
    private void processParticle(XmlSchemaParticle particle, String parentPath) {

        // ELEMENT
        if (particle instanceof XmlSchemaElement element) {
            String name = element.getName();
            String newPath = parentPath + "/" + name;
            processElement(element, newPath);
        }

        // SEQUENCE
        else if (particle instanceof XmlSchemaSequence sequence) {
            for (XmlSchemaObject obj : sequence.getItems()) {

                if (obj instanceof XmlSchemaElement el) {
                    processElement(el, parentPath + "/" + el.getName());
                }
                else if (obj instanceof XmlSchemaSequence innerSeq) {
                    processParticle(innerSeq, parentPath);
                }
                else if (obj instanceof XmlSchemaChoice innerChoice) {
                    processParticle(innerChoice, parentPath);
                }
            }
        }

        // CHOICE (ISO messages use this in many places)
        else if (particle instanceof XmlSchemaChoice choice) {
            for (XmlSchemaObject obj : choice.getItems()) {

                if (obj instanceof XmlSchemaElement el) {
                    processElement(el, parentPath + "/" + el.getName());
                }
                else if (obj instanceof XmlSchemaSequence seq) {
                    processParticle(seq, parentPath);
                }
                else if (obj instanceof XmlSchemaChoice nestedChoice) {
                    processParticle(nestedChoice, parentPath);
                }
            }
        }

        // ANY
        else if (particle instanceof XmlSchemaAny any) {
            elementTree.put(parentPath + "/*", "ANY");
        }
    }

    /** Resolve xs:element ref="abc:SomeElement" */
    private XmlSchemaElement resolveElementRef(XmlSchemaElement element) {

        if (element.isRef()) {
            QName refName = element.getRef().getTargetQName();
            XmlSchemaElement resolved = schema.getElementByName(refName);

            if (resolved != null) {
                return resolved;
            }
        }
        return element;
    }

    /** Test driver */
    public static void main(String[] args) throws Exception {
        XsdParserService service = new XsdParserService();

        Map<String, String> output = service.parseXsd(
                "src/main/resources/pain.001.001.09.xsd"
        );

        System.out.println("\n=== XSD ELEMENT TREE ===");
        output.forEach((k, v) -> System.out.println(k + " : " + v));
    }
}
