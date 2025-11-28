package com.example.demo.service;

import org.apache.ws.commons.schema.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class XsdParserService {

    private XmlSchema schema;
    private final Map<String, String> elementTree = new LinkedHashMap<>();

    /**
     * Parse XSD from uploaded MultipartFile with logging
     */
    public Map<String, String> parseXsd(MultipartFile file) throws Exception {
        System.out.println("=== parseXsd START ===");

        if (file == null || file.isEmpty()) {
            System.out.println("File is empty or null!");
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        System.out.println("File received: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize() + " bytes");

        try (InputStream is = file.getInputStream()) {

            // Parse InputStream into W3C Document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);

            System.out.println("Parsed InputStream into Document.");

            XmlSchemaCollection collection = new XmlSchemaCollection();
            schema = collection.read(document, file.getOriginalFilename());

            System.out.println("Schema loaded successfully. Target namespace: " + schema.getTargetNamespace());

            elementTree.clear();

            // Process root elements
            for (XmlSchemaObject obj : schema.getItems()) {
                if (obj instanceof XmlSchemaElement rootElement) {
                    System.out.println("Processing root element: " + rootElement.getName());
                    processElement(rootElement, rootElement.getName());
                } else {
                    System.out.println("Skipping non-element schema object: " + obj.getClass().getSimpleName());
                }
            }

        } catch (Exception e) {
            System.out.println("Exception while reading/parsing schema:");
            e.printStackTrace();
            throw e;
        }

        System.out.println("=== parseXsd END === Total elements: " + elementTree.size());
        return elementTree;
    }

    private void processElement(XmlSchemaElement element, String path) {
        System.out.println("processElement: " + path);
        try {
            element = resolveElementRef(element);
            String typeName = (element.getSchemaTypeName() != null) ? element.getSchemaTypeName().getLocalPart() : "complex/simple";
            elementTree.put(path, typeName);

            XmlSchemaType type = element.getSchemaType();
            if (type instanceof XmlSchemaComplexType complexType) {
                processComplexType(complexType, path);
            }
        } catch (Exception e) {
            System.out.println("Error in processElement at path: " + path);
            e.printStackTrace();
        }
    }

    private void processComplexType(XmlSchemaComplexType complexType, String parentPath) {
        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            processParticle(particle, parentPath);
        } else {
            System.out.println("No particle for complexType at path: " + parentPath);
        }
    }

    private void processParticle(XmlSchemaParticle particle, String parentPath) {
        if (particle == null) {
            System.out.println("Null particle at path: " + parentPath);
            return;
        }

        System.out.println("processParticle: " + particle.getClass().getSimpleName() + " at path: " + parentPath);

        try {
            // ELEMENT
            if (particle instanceof XmlSchemaElement element) {
                processElement(element, parentPath + "/" + element.getName());
            }

            // SEQUENCE
            else if (particle instanceof XmlSchemaSequence sequence) {
                for (XmlSchemaSequenceMember member : sequence.getItems()) {
                    if (member instanceof XmlSchemaElement el) {
                        processElement(el, parentPath + "/" + el.getName());
                    } else if (member instanceof XmlSchemaSequence seq) {
                        processParticle(seq, parentPath);
                    } else if (member instanceof XmlSchemaChoice choice) {
                        processParticle(choice, parentPath);
                    } else {
                        System.out.println("Unknown member in sequence: " + member.getClass().getSimpleName());
                    }
                }
            }

            // CHOICE
            else if (particle instanceof XmlSchemaChoice choice) {
                for (XmlSchemaChoiceMember member : choice.getItems()) {
                    if (member instanceof XmlSchemaElement el) {
                        processElement(el, parentPath + "/" + el.getName());
                    } else if (member instanceof XmlSchemaSequence seq) {
                        processParticle(seq, parentPath);
                    } else if (member instanceof XmlSchemaChoice nestedChoice) {
                        processParticle(nestedChoice, parentPath);
                    } else {
                        System.out.println("Unknown member in choice: " + member.getClass().getSimpleName());
                    }
                }
            }

            // ANY
            else if (particle instanceof XmlSchemaAny) {
                elementTree.put(parentPath + "/*", "ANY");
            }

            else {
                System.out.println("Unknown particle type: " + particle.getClass().getSimpleName() + " at path: " + parentPath);
            }

        } catch (Exception e) {
            System.out.println("Error in processParticle at path: " + parentPath);
            e.printStackTrace();
        }
    }

    private XmlSchemaElement resolveElementRef(XmlSchemaElement element) {
        if (element == null) return null;

        try {
            if (element.isRef() && element.getRef() != null) {
                QName qName = element.getRef().getTargetQName();
                XmlSchemaElement resolved = schema.getElementByName(qName);
                if (resolved != null) {
                    System.out.println("Resolved element ref: " + qName + " -> " + resolved.getName());
                    return resolved;
                } else {
                    System.out.println("Reference not found for QName: " + qName);
                }
            }
        } catch (Exception e) {
            System.out.println("Error resolving element ref: " + element.getName());
            e.printStackTrace();
        }
        return element;
    }
}
