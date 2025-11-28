package com.example.isoxsd.controller;

import com.example.isoxsd.model.XsdNode;
import com.example.isoxsd.service.XsdParseService;
import com.example.isoxsd.util.JaxbGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

@RestController
@RequestMapping("/api/xsd")
public class XsdController {

    @Autowired
    private XsdParseService parseService;

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<XsdNode>> parseXsd(@RequestParam("file") MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            List<XsdNode> roots = parseService.parse(is);
            return ResponseEntity.ok(roots);
        }
    }

    @PostMapping(value = "/generate-pojos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generatePojos(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value="package", defaultValue="com.generated.iso") String packageName) throws Exception {
        // Save to temp file
        File tmp = File.createTempFile("schema-", ".xsd");
        try (OutputStream os = new FileOutputStream(tmp)) {
            os.write(file.getBytes());
        }
        File outDir = new File(System.getProperty("java.io.tmpdir"), "xsd-gen-" + System.currentTimeMillis());
        File jar = JaxbGenerator.generateAndCompile(tmp, packageName, outDir);

        byte[] jarBytes = java.nio.file.Files.readAllBytes(jar.toPath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated-pojos.jar\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(jarBytes);
    }
    @RestController
@RequestMapping("/api/xsd")
public class XsdController {

    private final XsdParserService xsdParserService;

    public XsdController(XsdParserService xsdParserService) {
        this.xsdParserService = xsdParserService;
    }

    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> parseXsd(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = xsdParserService.parseXsd(file);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
}
