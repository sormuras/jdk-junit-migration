/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import run.bach.ToolCall;
import run.bach.info.MavenCoordinate;
import run.bach.internal.PathSupport;
import run.tool.JTReg;

public class Migrate {
  public static void main(String... args) throws Exception {
    // Install JTReg tool
    var jtreg = new JTReg().install();

    // Always create a new target directory to ensure compilation
    var temporary = PathHelper.createLocalTemporaryDirectoryWithTimestamp();
    var lib = Path.of("lib");

    // Build project-local javac plugin
    ToolCall.of("javac").add("-d", temporary.resolve("classes")).addFiles("src/**/*.java").run();
    ToolCall.of("jar", "--create")
        .add("--file", lib.resolve("plugin.jar"))
        .add("-C", "src", ".")
        .add("-C", temporary.resolve("classes"), ".")
        .run();

    // Grab required libraries
    var coordinates =
        List.of(
            MavenCoordinate.ofCentral(
                "com.google.errorprone", "error_prone_core", "2.27.1", "with-dependencies"),
            new MavenCoordinate(
                "jitpack.io/com/github/PicnicSupermarket",
                "error-prone-support",
                "refaster-runner",
                "gdejong~testng-migrator-SNAPSHOT",
                "",
                "jar"),
            new MavenCoordinate(
                "jitpack.io/com/github/PicnicSupermarket",
                "error-prone-support",
                "refaster-support",
                "gdejong~testng-migrator-SNAPSHOT",
                "",
                "jar"),
            new MavenCoordinate(
                "jitpack.io/com/github/PicnicSupermarket",
                "error-prone-support",
                "testng-junit-migrator",
                "gdejong~testng-migrator-SNAPSHOT",
                "",
                "jar"));

    for (var coordinate : coordinates) {
      PathSupport.copy(
          lib.resolve(coordinate.artifact() + '-' + coordinate.version() + ".jar"),
          coordinate.toUri());
    }
    var jars = new ArrayList<String>();
    try (var stream = Files.newDirectoryStream(lib, "*.jar")) {
      stream.forEach(jar -> jars.add(jar.toAbsolutePath().toString()));
    }

    // Create local argument file for `javac` call
    var compilerArgsFile =
        Files.write(
            temporary.resolve("compiler.args"),
            List.of(
                "-XDaccessInternalAPI",
                "-XDcompilePolicy=simple",
                "-processorpath " + String.join(File.pathSeparator, jars),
                "'-Xplugin:ErrorProne"
                    + " -XepPatchChecks:TestNGJUnitMigration,Refaster"
                    + " -XepPatchLocation:IN_PLACE"
                    + "'",
                "'-Xplugin:LineEditor'"));

    // Run `jtreg` with all batteries included
    ToolCall.of(jtreg)
        .add("-agentvm")
        .add("-r:" + temporary.resolve("JTReport"))
        .add("-w:" + temporary.resolve("JTWork"))
        .add("-vmoption:--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
        .add("-javacoption:@" + compilerArgsFile.toAbsolutePath())
        .addAll(args)
        .run();
  }
}
