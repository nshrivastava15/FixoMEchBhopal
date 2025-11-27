package com.example.isoxsd.model;

import java.util.ArrayList;
import java.util.List;

public class XsdNode {
    private String name;         // element name (e.g. GrpHdr)
    private String path;         // full path: Document/GrpHdr/MsgId
    private boolean repeating;   // true if maxOccurs > 1 or unbounded
    private String type;         // optional primitive type (xs:string etc.)
    private List<XsdNode> children = new ArrayList<>();

    // constructors, getters, setters
    public XsdNode() {}
    public XsdNode(String name, String path) {
        this.name = name; this.path = path;
    }
    // getters & setters omitted for brevity
}
