import React, { useState } from "react";
import axios from "axios";

function XsdUploader() {
  const [file, setFile] = useState(null);
  const [result, setResult] = useState(null);

  const uploadFile = async () => {
    if (!file) return alert("Choose a file first");

    const formData = new FormData();
    formData.append("file", file);

    const resp = await axios.post("http://localhost:8080/api/xsd/parse", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    setResult(resp.data);
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Upload ISO XSD File</h2>

      <input
        type="file"
        accept=".xsd"
        onChange={(e) => setFile(e.target.files[0])}
      />

      <button onClick={uploadFile}>Parse</button>

      {result && (
        <div style={{ marginTop: 20 }}>
          <h3>XSD Tree</h3>
          <pre>{JSON.stringify(result, null, 2)}</pre>
        </div>
      )}
    </div>
  );
}

export default XsdUploader;
