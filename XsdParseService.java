private void processParticle(XmlSchemaParticle particle, String parentPath) {

    if (particle instanceof XmlSchemaElement element) {
        String elementName = element.getName();
        String path = parentPath.isEmpty() ? elementName : parentPath + "/" + elementName;

        System.out.println("ELEM → " + path);

        // Recursively process inner types
        XmlSchemaType type = element.getSchemaType();
        if (type instanceof XmlSchemaComplexType complexType) {
            processComplexType(complexType, path);
        }
    }

    else if (particle instanceof XmlSchemaSequence sequence) {
        for (XmlSchemaObject obj : sequence.getItems()) {

            if (obj instanceof XmlSchemaElement seqElement) {
                processParticle(seqElement, parentPath);
            }
            else if (obj instanceof XmlSchemaSequence innerSeq) {
                processParticle(innerSeq, parentPath);
            }
            else if (obj instanceof XmlSchemaChoice innerChoice) {
                processParticle(innerChoice, parentPath);
            }
        }
    }

    else if (particle instanceof XmlSchemaChoice choice) {
        for (XmlSchemaObject obj : choice.getItems()) {

            if (obj instanceof XmlSchemaElement choiceElement) {
                processParticle(choiceElement, parentPath);
            }
            else if (obj instanceof XmlSchemaSequence seq) {
                processParticle(seq, parentPath);
            }
            else if (obj instanceof XmlSchemaChoice nestedChoice) {
                processParticle(nestedChoice, parentPath);
            }
        }
    }

    else if (particle instanceof XmlSchemaAny any) {
        System.out.println("ANY → " + parentPath + "/*");
    }
}
