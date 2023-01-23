package randoop.output;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.commons.io.FilenameUtils;
import randoop.main.Minimize;
import randoop.main.RandoopBug;
import randoop.main.RandoopUsageError;

/** JavaParser visitor to rename classes. */
public class ClassRenamingVisitor extends VoidVisitorAdapter<Void> {
  /** The new name. */
  private final String oldName;
  /** The new name. */
  private final String newName;

  /**
   * Create a ClassRenamingVisitor.
   *
   * @param oldName the old name
   * @param newName the new name
   */
  public ClassRenamingVisitor(String oldName, String newName) {
    this.oldName = oldName;
    this.newName = newName;
  }

  // TODO: should rename constructors and uses too, not just the declaration.
  /**
   * Rename the class.
   *
   * @param arg the old and new class names
   */
  @Override
  public void visit(ClassOrInterfaceDeclaration n, Void arg) {
    if (n.getName().toString().equals(oldName)) {
      n.setName(newName);
    }
  }

  ///
  /// Static methods
  ///

  /** An instance of a Java parser. */
  private static final JavaParser javaParser = new JavaParser();

  /**
   * Copies a file to a new name, renaming the class. Does not affect the original file.
   *
   * @param file the original Java file
   * @param newClassName the new class name
   * @return the newly-created file
   */
  public static Path copyAndRename(Path file, String newClassName) {

    CompilationUnit compilationUnit;
    try (FileInputStream inputStream = new FileInputStream(file.toFile())) {
      ParseResult<CompilationUnit> parseCompilationUnit = javaParser.parse(inputStream);
      if (parseCompilationUnit.isSuccessful()) {
        compilationUnit = parseCompilationUnit.getResult().get();
      } else {
        StringBuilder sb = new StringBuilder("Error parsing Java file: ");
        sb.append(file);
        for (Problem problem : parseCompilationUnit.getProblems()) {
          sb.append(problem);
        }
        throw new RandoopBug(sb.toString());
      }
    } catch (IOException e) {
      throw new RandoopBug("Error reading Java file: " + file, e);
    }
    return copyAndRename(
        file,
        compilationUnit,
        FilenameUtils.removeExtension(file.getFileName().toString()),
        newClassName);
  }

  /**
   * Copies a file to a new name, renaming the class. Does not affect the original file.
   *
   * @param file the original Java file
   * @param compilationUnit the original Java file, parsed
   * @param oldClassName the old class name
   * @param newClassName the new class name
   * @return the newly-created file
   */
  public static Path copyAndRename(
      Path file, CompilationUnit compilationUnit, String oldClassName, String newClassName) {

    // Rename the overall class to [original class name][suffix].
    new ClassRenamingVisitor(oldClassName, newClassName).visit(compilationUnit, null);

    // Write the compilation unit to the minimized file.
    Path minimizedFile = file.resolveSibling(newClassName + ".java");
    try {
      Minimize.writeToFile(compilationUnit, minimizedFile);
    } catch (IOException e) {
      throw new RandoopUsageError("Problem while writing file " + minimizedFile, e);
    }

    return minimizedFile;
  }
}
