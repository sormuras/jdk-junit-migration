import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import run.bach.ToolCall;
import run.bach.info.MavenCoordinate;
import run.bach.internal.PathSupport;
import run.tool.JTReg;

public class Migrate {
  static final boolean verbose = Boolean.getBoolean("ebug");

  public static void main(String... args) throws Exception {
    // Install JTReg tool
    var jtreg = new JTReg().install();
    if (verbose) jtreg.run("-version");

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
    var lib = Path.of("lib");
    for (var coordinate : coordinates) {
      PathSupport.copy(
          lib.resolve(coordinate.artifact() + '-' + coordinate.version() + ".jar"),
          coordinate.toUri());
    }
    var jars = new ArrayList<String>();
    try (var stream = Files.newDirectoryStream(lib, "*.jar")) {
      stream.forEach(jar -> jars.add(jar.toAbsolutePath().toString()));
    }

    // Always create a new target directory to ensure compilation
    var timestamp =
        OffsetDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX"));
    var temporary = Files.createDirectories(Path.of("tmp", timestamp));

    // Create local argument file for `javac` call
    var compilerArgs =
        List.of(
            "-XDaccessInternalAPI",
            "-XDcompilePolicy=simple",
            "-processorpath " + String.join(File.pathSeparator, jars),
            "'-Xplugin:ErrorProne -XepPatchChecks:TestNGJUnitMigration,Refaster"
                + " -XepPatchLocation:IN_PLACE'");
    var compilerArgsFile = Files.write(temporary.resolve("compiler.args"), compilerArgs);

    // Run `jtreg` with all batteries included
    ToolCall.of(jtreg)
        .when(verbose, "-va")
        .add("-agentvm")
        .add("-r:" + temporary.resolve("JTReport"))
        .add("-w:" + temporary.resolve("JTWork"))
        .add("-vmoption:--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED")
        .add("-javacoption:@" + compilerArgsFile.toAbsolutePath())
        .addAll(args)
        .run();
  }
}
