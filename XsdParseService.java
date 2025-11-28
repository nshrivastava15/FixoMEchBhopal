package com.example.isoxsd.service;

import com.example.isoxsd.model.XsdNode;
import org.apache.ws.commons.schema.*;
import org.springframework.stereotype.Service;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.List;

@Service
public class XsdParseService {

    public XsdNode parseRoot(InputStream xsdIn) throws Exception {
        XmlSchemaCollection col = new XmlSchemaCollection();
        XmlSchema schema = col.read(xsdIn, null);

        // find the Document root element (ISO)
        for (XmlSchemaElement el : schema.getElements().values()) {
            if ("Document".equalsIgnoreCase(el.getName())) {
                XsdNode root = new XsdNode(el.getName(), el.getName());
                processElement(resolveElementRef(el, col), root, col);
                return root;
            }
        }

        // fallback: return first global element
        for (XmlSchemaElement el : schema.getElements().values()) {
            XsdNode root = new XsdNode(el.getName(), el.getName());
            processElement(resolveElementRef(el, col), root, col);
            return root;
        }
        return null;
    }

    private void processElement(XmlSchemaElement element, XsdNode node, XmlSchemaCollection col) {
        if (element == null) return;

        // mark repeating
        if (element.getMaxOccurs() > 1 || element.getMaxOccurs() == XmlSchemaElement.UNBOUNDED) {
            node.setRepeating(true);
        }

        XmlSchemaType type = element.getSchemaType();
        if (type == null && element.getSchemaTypeName() != null) {
            type = col.getTypeByQName(element.getSchemaTypeName());
        }

        if (type instanceof XmlSchemaComplexType) {
            XmlSchemaComplexType complex = (XmlSchemaComplexType) type;
            XmlSchemaParticle particle = complex.getParticle();
            processParticle(particle, node, col);
        } else {
            // simple type or unresolved — set type name if available
            if (element.getSchemaTypeName() != null) {
                node.setType(element.getSchemaTypeName().getLocalPart());
            }
        }
    }

    private void processParticle(XmlSchemaParticle particle, XsdNode parent, XmlSchemaCollection col) {
        if (particle == null) return;

        if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence seq = (XmlSchemaSequence) particle;
            for (XmlSchemaObject obj : seq.getItems()) {
                handleParticleItem(obj, parent, col);
            }
        } else if (particle instanceof XmlSchemaChoice) {
            XmlSchemaChoice choice = (XmlSchemaChoice) particle;
            for (XmlSchemaObject obj : choice.getItems()) {
                handleParticleItem(obj, parent, col);
            }
        } else if (particle instanceof XmlSchemaAll) {
            XmlSchemaAll all = (XmlSchemaAll) particle;
            for (XmlSchemaObject obj : all.getItems()) {
                handleParticleItem(obj, parent, col);
            }
        } else if (particle instanceof XmlSchemaGroupRef) {
            // handle groupRef: resolve the group's particle and process it
            XmlSchemaGroupRef grpRef = (XmlSchemaGroupRef) particle;
            XmlSchemaGroup group = col.getGroupByQName(grpRef.getRef());
            if (group != null && group.getParticle() != null) {
                processParticle(group.getParticle(), parent, col);
            }
        }
    }

    private void handleParticleItem(XmlSchemaObject item, XsdNode parent, XmlSchemaCollection col) {
        if (item instanceof XmlSchemaElement) {
            XmlSchemaElement childEl = (XmlSchemaElement) item;
            XmlSchemaElement resolved = resolveElementRef(childEl, col);
            String childName = resolved.getName();
            String childPath = parent.getPath() + "/" + childName;
            XsdNode childNode = new XsdNode(childName, childPath);
            // repeating
            if (resolved.getMaxOccurs() > 1 || resolved.getMaxOccurs() == XmlSchemaElement.UNBOUNDED) {
                childNode.setRepeating(true);
            }
            parent.getChildren().add(childNode);
            // recurse into child's type
            processElement(resolved, childNode, col);
        } else if (item instanceof XmlSchemaSequence) {
            processParticle((XmlSchemaSequence) item, parent, col);
        } else if (item instanceof XmlSchemaChoice) {
            processParticle((XmlSchemaChoice) item, parent, col);
        } else if (item instanceof XmlSchemaGroupRef) {
            XmlSchemaGroupRef ref = (XmlSchemaGroupRef) item;
            XmlSchemaGroup group = col.getGroupByQName(ref.getRef());
            if (group != null && group.getParticle() != null) {
                processParticle(group.getParticle(), parent, col);
            }
        } else {
            // other types (e.g. annotations) — ignore
        }
    }

    /**
     * Resolve element reference (if 'ref' used) to the actual global element.
     */
    private XmlSchemaElement resolveElementRef(XmlSchemaElement el, XmlSchemaCollection col) {
        if (el == null) return null;

        // If it has a local ref (getRef() returns XmlSchemaRef), use it to find the global element
        if (el.getRef() != null) {
            QName refQName = el.getRef().getTargetQName();
            XmlSchemaElement resolved = col.getElementByQName(refQName);
            if (resolved != null) return resolved;
        }

        // if it has a 'refName' style in certain versions:
        if (el.getRefName() != null) {
            XmlSchemaElement resolved = col.getElementByQName(el.getRefName());
            if (resolved != null) return resolved;
        }

        return el;
    }
}
