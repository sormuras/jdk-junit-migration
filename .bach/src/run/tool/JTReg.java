/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

package run.tool;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.spi.ToolProvider;
import run.bach.ToolInstaller;
import run.bach.ToolProgram;
import run.bach.ToolRunner;

/**
 * The Regression Test Harness for the OpenJDK platform.
 *
 * @see <a href="https://github.com/openjdk/jtreg">https://github.com/openjdk/jtreg</a>
 */
public record JTReg(String version) implements ToolInstaller {
  public static final String DEFAULT_VERSION = "8+2";

  public static void main(String... args) {
    var version = System.getProperty("version", DEFAULT_VERSION);
    new JTReg(version).install().run(args.length == 0 ? new String[] {"-version"} : args);
  }

  public JTReg() {
    this(DEFAULT_VERSION);
  }

  @Override
  public ToolProvider install(Path into) throws Exception {
    var title = "jtreg-" + version;
    var archive = title + ".zip";
    var target = into.resolve(archive);
    if (!Files.exists(target)) {
      var source = "https://builds.shipilev.net/jtreg/" + archive;
      download(target, URI.create(source));
    }
    var image = into.resolve(title);
    if (!Files.isDirectory(image)) {
      var jar =
          ToolProgram.findJavaDevelopmentKitTool("jar")
              .orElseThrow()
              .withProcessBuilderTweaker(builder -> builder.directory(into.toFile()))
              .withProcessWaiter(process -> process.waitFor(1, TimeUnit.MINUTES) ? 0 : 1)
              .tool();
      ToolRunner.ofSilence().run(jar, "--extract", "--file", archive);
    }
    return ToolProgram.java("-jar", image.resolve("../jtreg/lib/jtreg.jar").normalize().toString());
  }
}
