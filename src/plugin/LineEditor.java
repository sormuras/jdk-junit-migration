/*
 * Copyright (c) 2024 Christian Stein
 * Licensed under the Universal Permissive License v 1.0 -> https://opensource.org/license/upl
 */

package plugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.tools.JavaFileObject;

public class LineEditor implements Plugin, TaskListener {
  private PrintWriter out;
  private ArrayList<Path> sourceFilePaths;

  @Override
  public String getName() {
    return getClass().getSimpleName();
  }

  @Override
  public void init(JavacTask task, String... args) {
    this.out = new PrintWriter(System.out, true);
    this.sourceFilePaths = new ArrayList<>();
    task.addTaskListener(this);
  }

  @Override
  public void finished(TaskEvent event) {
    if (event.getKind().equals(TaskEvent.Kind.PARSE)) {
      var sourceFile = event.getSourceFile();
      if (sourceFile == null) return;
      if (sourceFile.getKind() != JavaFileObject.Kind.SOURCE) return;
      sourceFilePaths.add(Path.of(sourceFile.toUri()));
      return;
    }
    if (event.getKind().equals(TaskEvent.Kind.COMPILATION)) {
      try {
        for (var sourceFilePath : sourceFilePaths) {
          var sourceLines = new ArrayList<>(Files.readAllLines(sourceFilePath));
          var iterator = sourceLines.listIterator();
          while (iterator.hasNext()) {
            var line = iterator.next();
            remove(iterator, line, "org.testng."); // dangling imports
            replace(iterator, line, "@run testng", "@run junit"); // jtreg action
          }
          Files.writeString(sourceFilePath, String.join("\n", sourceLines) + '\n');
        }
      } catch (Exception exception) {
        throw new RuntimeException(exception);
      }
    }
  }

  void replace(ListIterator<String> iterator, String line, String target, String replacement) {
    if (!line.contains(target)) return;
    out.printf("Replace in-line `%s` substring with `%s`...%n", target, replacement);
    iterator.remove();
    iterator.add(line.replace(target, replacement));
  }

  void remove(ListIterator<String> iterator, String line, String target) {
    if (!line.contains(target)) return;
    out.println("Remove line: " + line);
    iterator.remove();
  }
}
