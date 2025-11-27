import React from "react";
import { Collapse, Card } from "antd";

const { Panel } = Collapse;

export default function PojoViewer({ data }) {
  return (
    <Card>
      <Collapse accordion>
        {Object.keys(data).map((className) => (
          <Panel header={className} key={className}>
            <pre>{data[className]}</pre>
          </Panel>
        ))}
      </Collapse>
    </Card>
  );
}
