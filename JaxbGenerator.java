package com.example.isoxsd.util;

import java.io.File;
import java.util.*;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import com.sun.tools.xjc.api.*;
import com.sun.tools.xjc.api.util.*;
import org.xml.sax.InputSource;

public class JaxbGenerator {

    /**
     * Generates Java sources from the XSD file into outDir and tries to compile them into classes/jar.
     * Returns the folder with generated sources and the compiled jar file path.
     */
    public static File generateAndCompile(File xsdFile, String packageName, File outDir) throws Exception {
        // Ensure outDir exists
        outDir.mkdirs();

        // Use XJC to generate sources
        // com.sun.tools.xjc.api.XJC can be used to create a SchemaCompiler
        SchemaCompiler sc = XJC.createSchemaCompiler();
        sc.forcePackageName(packageName);

        // add schema
        String systemId = xsdFile.toURI().toString();
        InputSource is = new InputSource(systemId);
        is.setSystemId(systemId);
        sc.parseSchema(is);

        S2JJAXBModel model = sc.bind();
        JCodeModel codeModel = model.generateCode(null, null);

        // write sources
        File srcOut = new File(outDir, "src");
        codeModel.build(srcOut);

        // compile sources
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("No JavaCompiler available. Run with JDK, not JRE.");
        }
        // collect .java files
        List<String> javaFiles = new ArrayList<>();
        collectJavaFiles(srcOut, javaFiles);
        List<String> options = Arrays.asList("-d", new File(outDir, "classes").getAbsolutePath());
        List<String> compileArgs = new ArrayList<>(options);
        compileArgs.addAll(javaFiles);
        int result = compiler.run(null, null, null, compileArgs.toArray(new String[0]));
        if (result != 0) {
            throw new RuntimeException("Compilation failed, exit code: " + result);
        }

        // Create jar
        File jarFile = new File(outDir, "generated-pojos.jar");
        ProcessBuilder pb = new ProcessBuilder();
        List<String> jarCmd = new ArrayList<>();
        jarCmd.add("jar");
        jarCmd.add("cf");
        jarCmd.add(jarFile.getAbsolutePath());
        jarCmd.add("-C");
        jarCmd.add(new File(outDir, "classes").getAbsolutePath());
        jarCmd.add(".");
        pb.command(jarCmd);
        Process p = pb.start();
        int exit = p.waitFor();
        if (exit != 0) {
            // on Windows or if jar not available, you can fallback to programmatic JarOutputStream packaging
            // For brevity, throw an error here telling the user to use JDK with 'jar' available.
            throw new RuntimeException("Jar creation failed (ensure 'jar' tool available).");
        }

        return jarFile;
    }

    private static void collectJavaFiles(File dir, List<String> out) {
        for (File f : Objects.requireNonNull(dir.listFiles())) {
            if (f.isDirectory()) collectJavaFiles(f, out);
            else if (f.getName().endsWith(".java")) out.add(f.getAbsolutePath());
        }
    }
}
