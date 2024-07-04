/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

class PathHelper {
  static Path createLocalTemporaryDirectoryWithTimestamp() {
    var pattern = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssX");
    var timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(pattern);
    try {
      return Files.createDirectories(Path.of("tmp", timestamp));
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }
}
