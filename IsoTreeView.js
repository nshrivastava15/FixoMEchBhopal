import React from "react";
import { Tree } from "antd";

export default function IsoTreeView({ data }) {

  // Convert "Document/GrpHdr/MsgId" → nested Tree nodes
  const buildTree = () => {
    const root = {};

    data.forEach((path) => {
      const parts = path.split("/");
      let current = root;

      parts.forEach((p) => {
        if (!current[p]) current[p] = {};
        current = current[p];
      });
    });

    const convert = (obj) =>
      Object.keys(obj).map((key) => ({
        title: key,
        key: key + Math.random(),
        children: convert(obj[key]),
      }));

    return convert(root);
  };

  return <Tree treeData={buildTree()} defaultExpandAll />;
}
Analyze the uploaded mapping document.

Tasks:
1. Detect sheet purpose automatically
2. Detect header rows
3. Infer mapping columns even if names differ
4. Extract transformation rules from English
5. Identify source and target schemas
6. Normalize all mappings into canonical JSON
7. Generate implementation-ready transformation expressions
8. Identify invalid or incomplete mappings
9. Detect country-specific logic
10. Suggest reusable mapping templates

Important:
- Do not assume missing XPaths
- Flag ambiguity explicitly
- Keep original business text alongside normalized logic
- Preserve row references for traceability

Output JSON schema:

{
  "documentType": "",
  "sourceStandard": "",
  "targetStandard": "",
  "country": "",
  "mappings": [
    {
      "sheet": "",
      "row": "",
      "sourceField": "",
      "targetField": "",
      "businessRule": "",
      "normalizedRule": "",
      "condition": "",
      "defaultValue": "",
      "transformationType": "",
      "confidence": "",
      "ambiguities": []
    }
  ]
}
