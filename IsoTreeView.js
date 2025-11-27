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
