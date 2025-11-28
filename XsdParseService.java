@Service
public class XsdParserService {

    private XmlSchema schema;
    private final Map<String, String> elementTree = new LinkedHashMap<>();

    /** NEW: parse directly from uploaded file */
    public Map<String, String> parseXsd(MultipartFile file) throws Exception {

        XmlSchemaCollection collection = new XmlSchemaCollection();
        schema = collection.read(file.getInputStream(), null);

        elementTree.clear();

        for (XmlSchemaObject obj : schema.getItems()) {
            if (obj instanceof XmlSchemaElement rootElement) {
                processElement(rootElement, rootElement.getName());
            }
        }

        return elementTree;
    }

    /** Same method body as your approved version */
    private void processElement(XmlSchemaElement element, String path) {
        element = resolveElementRef(element);

        elementTree.put(path,
                element.getSchemaTypeName() != null ?
                        element.getSchemaTypeName().getLocalPart() :
                        "complex/simple");

        XmlSchemaType type = element.getSchemaType();
        if (type instanceof XmlSchemaComplexType complexType) {
            processComplexType(complexType, path);
        }
    }

    private void processComplexType(XmlSchemaComplexType complexType, String parentPath) {
        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            processParticle(particle, parentPath);
        }
    }

    private void processParticle(XmlSchemaParticle particle, String parentPath) {

        if (particle instanceof XmlSchemaElement element) {
            processElement(element, parentPath + "/" + element.getName());
        }
        else if (particle instanceof XmlSchemaSequence sequence) {
            for (XmlSchemaObject obj : sequence.getItems()) {
                if (obj instanceof XmlSchemaElement el) {
                    processElement(el, parentPath + "/" + el.getName());
                } else if (obj instanceof XmlSchemaSequence seq) {
                    processParticle(seq, parentPath);
                } else if (obj instanceof XmlSchemaChoice choice) {
                    processParticle(choice, parentPath);
                }
            }
        }
        else if (particle instanceof XmlSchemaChoice choice) {
            for (XmlSchemaObject obj : choice.getItems()) {
                if (obj instanceof XmlSchemaElement el) {
                    processElement(el, parentPath + "/" + el.getName());
                } else if (obj instanceof XmlSchemaSequence seq) {
                    processParticle(seq, parentPath);
                } else if (obj instanceof XmlSchemaChoice nestedChoice) {
                    processParticle(nestedChoice, parentPath);
                }
            }
        }
    }

    private XmlSchemaElement resolveElementRef(XmlSchemaElement element) {
        if (element.isRef()) {
            QName qName = element.getRef().getTargetQName();
            XmlSchemaElement resolved = schema.getElementByName(qName);
            if (resolved != null) return resolved;
        }
        return element;
    }
}
