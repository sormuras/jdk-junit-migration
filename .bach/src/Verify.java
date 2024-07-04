/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import run.bach.ToolCall;
import run.tool.JTReg;

public class Verify {
  public static void main(String... args) throws Exception {
    // Install JTReg tool
    var jtreg = new JTReg().install();

    // Always create a new target directory to ensure compilation
    var temporary = PathHelper.createLocalTemporaryDirectoryWithTimestamp();

    // Run `jtreg`
    ToolCall.of(jtreg)
        .add("-agentvm")
        .add("-r:" + temporary.resolve("JTReport"))
        .add("-w:" + temporary.resolve("JTWork"))
        .addAll(args)
        .run();
  }
}
