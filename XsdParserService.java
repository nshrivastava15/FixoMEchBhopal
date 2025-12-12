@Service
public class XsdParserService {

    private static final Logger log = LoggerFactory.getLogger(XsdParserService.class);

    public XsdDetailedNode parseXsd(InputStream inputStream, String fileName) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(inputStream);

        XmlSchemaCollection collection = new XmlSchemaCollection();
        XmlSchema schema = collection.read(doc, fileName);

        log.info("XSD Parsed. Target NS = {}", schema.getTargetNamespace());

        XmlSchemaElement root = schema.getElements().values().iterator().next();
        return processElement(root, "/" + root.getName());
    }

    private XsdDetailedNode processElement(XmlSchemaElement element, String path) {

        element = resolve(element);

        XsdDetailedNode node = new XsdDetailedNode();
        node.setXpath(path);
        node.setName(element.getName());
        node.setMinOccurs(element.getMinOccurs());

        node.setMaxOccurs(element.getMaxOccurs() == Long.MAX_VALUE
                ? "unbounded"
                : String.valueOf(element.getMaxOccurs()));

        // Extract type
        if (element.getSchemaTypeName() != null)
            node.setType(element.getSchemaTypeName().getLocalPart());

        XmlSchemaType schemaType = element.getSchemaType();

        if (schemaType instanceof XmlSchemaSimpleType)
            fillSimpleType(node, (XmlSchemaSimpleType) schemaType);

        if (schemaType instanceof XmlSchemaComplexType)
            fillComplexType(node, (XmlSchemaComplexType) schemaType, path);

        return node;
    }

    private void fillSimpleType(XsdDetailedNode node, XmlSchemaSimpleType simpleType) {
        node.setSimpleType(true);

        XmlSchemaSimpleTypeRestriction r = (XmlSchemaSimpleTypeRestriction) simpleType.getContent();
        if (r == null) return;

        node.setBaseType(r.getBaseTypeName().getLocalPart());

        for (XmlSchemaFacet facet : r.getFacets()) {
            if (facet instanceof XmlSchemaLengthFacet lf) node.setLength(Integer.parseInt(lf.getValue().toString()));
            if (facet instanceof XmlSchemaMinLengthFacet mf) node.setMinLength(Integer.parseInt(mf.getValue().toString()));
            if (facet instanceof XmlSchemaMaxLengthFacet mf) node.setMaxLength(Integer.parseInt(mf.getValue().toString()));
            if (facet instanceof XmlSchemaFractionDigitsFacet ff) node.setFractionDigits(Integer.parseInt(ff.getValue().toString()));
            if (facet instanceof XmlSchemaTotalDigitsFacet tf) node.setTotalDigits(Integer.parseInt(tf.getValue().toString()));
            if (facet instanceof XmlSchemaEnumerationFacet ef) {
                if (node.getEnumeration() == null) node.setEnumeration(new ArrayList<>());
                node.getEnumeration().add(ef.getValue().toString());
            }
        }
    }

    private void fillComplexType(XsdDetailedNode node, XmlSchemaComplexType complexType, String parentPath) {

        XmlSchemaAnnotation ann = complexType.getAnnotation();
        if (ann != null && ann.getItems().size() > 0) {
            XmlSchemaAppInfoOrDocumentation info = ann.getItems().get(0);
            if (info instanceof XmlSchemaDocumentation doc && doc.getMarkup() != null) {
                node.setDocumentation(doc.getMarkup().item(0).getNodeValue());
            }
        }

        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            List<XsdDetailedNode> kids = processParticle(particle, parentPath);
            if (kids != null) node.getChildren().addAll(kids);
        }
    }

    private List<XsdDetailedNode> processParticle(XmlSchemaParticle particle, String parentPath) {

        List<XsdDetailedNode> list = new ArrayList<>();

        if (particle instanceof XmlSchemaElement el) {
            list.add(processElement(el, parentPath + "/" + el.getName()));
        }

        if (particle instanceof XmlSchemaSequence seq) {
            for (XmlSchemaSequenceMember m : seq.getItems()) {
                if (m instanceof XmlSchemaElement el)
                    list.add(processElement(el, parentPath + "/" + el.getName()));
                if (m instanceof XmlSchemaSequence nested)
                    list.addAll(processParticle(nested, parentPath));
                if (m instanceof XmlSchemaChoice choice)
                    list.addAll(processParticle(choice, parentPath));
            }
        }

        if (particle instanceof XmlSchemaChoice choice) {
            for (XmlSchemaChoiceMember m : choice.getItems()) {
                if (m instanceof XmlSchemaElement el)
                    list.add(processElement(el, parentPath + "/" + el.getName()));
                if (m instanceof XmlSchemaSequence nested)
                    list.addAll(processParticle(nested, parentPath));
                if (m instanceof XmlSchemaChoice nestedChoice)
                    list.addAll(processParticle(nestedChoice, parentPath));
            }
        }

        return list;
    }

    private XmlSchemaElement resolve(XmlSchemaElement el) {
        return el.getRefName() != null ? el.getRef().getTarget() : el;
    }
}
