public class XsdDetailedNode {

    private String xpath;
    private String name;
    private String type;
    private String baseType;
    private boolean simpleType;
    private int minOccurs;
    private String maxOccurs;
    private String documentation;

    private Integer length;
    private Integer minLength;
    private Integer maxLength;
    private Integer fractionDigits;
    private Integer totalDigits;
    private List<String> enumeration;

    private List<XsdDetailedNode> children = new ArrayList<>();

    // getters + setters
}
