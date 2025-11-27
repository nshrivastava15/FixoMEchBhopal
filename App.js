import React, { useState } from "react";
import { Layout, Tabs } from "antd";
import XsdUploader from "./components/XsdUploader";
import IsoTreeView from "./components/IsoTreeView";
import XPathList from "./components/XPathList";
import PojoViewer from "./components/PojoViewer";

const { Header, Content } = Layout;

function App() {
  const [output, setOutput] = useState(null);

  return (
    <Layout>
      <Header style={{ color: "#fff", fontSize: 20 }}>
        ISO 20022 XSD Analyzer
      </Header>

      <Content style={{ padding: "20px" }}>
        <XsdUploader onDataLoaded={setOutput} />

        {output && (
          <Tabs defaultActiveKey="1" style={{ marginTop: 20 }}>
            <Tabs.TabPane tab="ISO Message Tree" key="1">
              <IsoTreeView data={output.isoTree} />
            </Tabs.TabPane>

            <Tabs.TabPane tab="XPaths" key="2">
              <XPathList data={output.xpaths} />
            </Tabs.TabPane>

            <Tabs.TabPane tab="POJO Classes" key="3">
              <PojoViewer data={output.pojos} />
            </Tabs.TabPane>
          </Tabs>
        )}
      </Content>
    </Layout>
  );
}

export default App;
