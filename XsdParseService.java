package com.example.isoxsd.service;

import com.example.isoxsd.model.XsdNode;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl; // Not used; we use xmlschema-core classes
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class XsdParseService {

    /**
     * Parse input XSD stream and return root XsdNode(s).
     */
    public List<XsdNode> parse(InputStream xsdInputStream) throws Exception {
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(xsdInputStream, null);

        // find global elements named Document or root elements
        List<XsdNode> roots = new ArrayList<>();
        for (XmlSchemaElement el : schema.getElements().values()) {
            String elName = el.getName();
            // Many ISO schemas have root named "Document"
            if (elName != null && (elName.equals("Document") || elName.equals("document"))) {
                XsdNode rootNode = new XsdNode(elName, elName);
                processElement(el, rootNode, schemaCol);
                roots.add(rootNode);
            }
        }

        // fallback: if no Document element found, add all global elements as roots
        if (roots.isEmpty()) {
            for (XmlSchemaElement el : schema.getElements().values()) {
                String elName = el.getName();
                XsdNode rootNode = new XsdNode(elName, elName);
                processElement(el, rootNode, schemaCol);
                roots.add(rootNode);
            }
        }

        return roots;
    }

    private void processElement(XmlSchemaElement element, XsdNode node, XmlSchemaCollection schemaCol) {
        // set repeating if maxOccurs > 1
        if (element.getMaxOccurs() > 1 || element.getMaxOccurs() == Long.MAX_VALUE) {
            node.setRepeating(true);
        }

        XmlSchemaType schemaType = element.getSchemaType();
        if (schemaType == null && element.getSchemaTypeName() != null) {
            // named type reference; try to resolve
            schemaType = schemaCol.getTypeByQName(element.getSchemaTypeName());
        }

        if (schemaType instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complex = (XmlSchemaComplexType) schemaType;
            XmlSchemaParticle particle = complex.getParticle();
            if (particle instanceof XmlSchemaSequence) {
                XmlSchemaSequence seq = (XmlSchemaSequence) particle;
                for (XmlSchemaSequenceMember member : seq.getItems()) {
                    if (member instanceof XmlSchemaElement) {
                        XmlSchemaElement childEl = (XmlSchemaElement) member;
                        String childPath = node.getPath() + "/" + childEl.getName();
                        XsdNode childNode = new XsdNode(childEl.getName(), childPath);
                        // mark repeating
                        if (childEl.getMaxOccurs() > 1 || childEl.getMaxOccurs() == Long.MAX_VALUE) {
                            childNode.setRepeating(true);
                        }
                        node.getChildren().add(childNode);
                        // recursive — resolve child element's type definition (may be ref)
                        XmlSchemaElement resolved = resolveElementRef(childEl, schemaCol);
                        processElement(resolved, childNode, schemaCol);
                    }
                }
            } else if (particle instanceof XmlSchemaChoice) {
                XmlSchemaChoice choice = (XmlSchemaChoice) particle;
                for (XmlSchemaSequenceMember member : choice.getItems()) {
                    if (member instanceof XmlSchemaElement) {
                        XmlSchemaElement childEl = (XmlSchemaElement) member;
                        String childPath = node.getPath() + "/" + childEl.getName();
                        XsdNode childNode = new XsdNode(childEl.getName(), childPath);
                        node.getChildren().add(childNode);
                        XmlSchemaElement resolved = resolveElementRef(childEl, schemaCol);
                        processElement(resolved, childNode, schemaCol);
                    }
                }
            }
            // attributes and other constructs are ignored for ISO message tree
        } else if (schemaType instanceof XmlSchemaSimpleType) {
            // simple type: determine base XSD primitive
            node.setType(schemaType.getName());
        } else {
            // If type is null or not resolved, attempt to check element schemaTypeName (primitive)
            if (element.getSchemaTypeName() != null) {
                node.setType(element.getSchemaTypeName().getLocalPart());
            }
        }
    }

    private XmlSchemaElement resolveElementRef(XmlSchemaElement el, XmlSchemaCollection coll) {
        if (el.getRefName() != null) {
            XmlSchemaElement resolved = coll.getElementByQName(el.getRefName());
            return resolved != null ? resolved : el;
        }
        return el;
    }
}
