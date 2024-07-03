/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class Status {
  public static void main(String... args) throws Exception {
    var start = Path.of("github", "openjdk", "jdk", "test");
    System.out.printf("Walking file tree %s...%n", start.toUri());
    if (!Files.isDirectory(start)) {
      throw new Error(
          "Start directory does not exist. Forgot to check out with submodules?\n"
              + "\tgit submodule update --init --recursive");
    }

    var collectors =
        List.of(
            // TestNG
            Collector.of("status-org-testng.md", "org.testng"),
            Collector.of("status-run-testng.md", "@run testng"),
            Collector.of("status-testng-dirs.md", "TestNG.dirs"),
            // JUnit
            Collector.of("status-org-junit.md", "org.junit"),
            Collector.of("status-run-junit.md", "@run junit"),
            Collector.of("status-junit-dirs.md", "JUnit.dirs"));

    try (var files = Files.walk(start)) {
      files.forEach(
          file -> {
            if (Files.isRegularFile(file))
              try {
                var code = Files.readString(file);
                collectors.forEach(collector -> collector.update(code, file));
              } catch (Exception ignore) {
                // ignore binary files
              }
          });
    }

    collectors.forEach(collector -> System.out.println(collector.toHeader()));
  }

  private record Collector(String name, Predicate<String> predicate, List<Path> files) {
    static Collector of(String name, String token) {
      return new Collector(name, code -> code.contains(token), new ArrayList<>());
    }

    void update(String code, Path file) {
      if (predicate.test(code)) files.add(file);
    }

    String toHeader() {
      return "# " + name + " with " + files.size() + " files";
    }
  }
}
