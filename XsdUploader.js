import React, { useState } from "react";
import { Input, Button, Card } from "antd";
import axios from "axios";

export default function XsdUploader({ onDataLoaded }) {
  const [path, setPath] = useState("");

  const processXsd = async () => {
    const res = await axios.get("http://localhost:8080/api/xsd/process", {
      params: { xsdPath: path },
    });

    onDataLoaded(res.data);
  };

  return (
    <Card title="Load XSD" style={{ marginBottom: 20 }}>
      <Input
        placeholder="Enter XSD file path (e.g. /Users/nikhil/pacs008.xsd)"
        value={path}
        onChange={(e) => setPath(e.target.value)}
        style={{ marginBottom: 10 }}
      />

      <Button type="primary" onClick={processXsd}>
        Process XSD
      </Button>
    </Card>
  );
}
