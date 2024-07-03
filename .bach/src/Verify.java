/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import run.bach.ToolCall;
import run.tool.JTReg;

public class Verify {
  public static void main(String... args) throws Exception {
    // Install JTReg tool
    var jtreg = new JTReg().install();

    // Always create a new target directory to ensure compilation
    var timestamp =
        OffsetDateTime.now(ZoneOffset.UTC)
            .format(DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX"));
    var temporary = Files.createDirectories(Path.of("tmp", timestamp));

    // Run `jtreg`
    ToolCall.of(jtreg)
        .add("-agentvm")
        .add("-r:" + temporary.resolve("JTReport"))
        .add("-w:" + temporary.resolve("JTWork"))
        .addAll(args)
        .run();
  }
}
