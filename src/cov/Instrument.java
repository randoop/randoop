package cov;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import randoop.util.Files;
import plume.Option;
import plume.Options;
import plume.Pair;
import plume.Options.ArgException;

/**
 * The cov.Instrument program implements a basic branch coverage
 * instrumenter that we use for the branch-directed test generation
 * research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 *
 * This class contains the main method for the source code coverage
 * instrumenter.
 *
 * Usage:
 *
 * java cov.Instrument [OPTIONS] [dir]*
 *
 * Notes:
 *
 * Currently, we do not instrument enumeration classes.
 *
 * Currently, method-level info is only available for top-level methods.
 * Top-level methods are methods directly declared in a top-level class. This is
 * for no important reason. If a method is declared within another method (via a
 * class declaration), the branches of the inner will be assigned to the
 * outermost containing method declaration.
 *
 * We do not instrument branches inside annotation declarations.
 */
@SuppressWarnings("unchecked")
public class Instrument extends ASTVisitor {

  @Option("Destination directory for instrumented files (REQUIRED OPTION)")
  public static String destination = null;

  @Option("Overwrite contents of instrumented directory, if present")
  public static boolean overwrite = false;

  @Option("Specify instrumentation type")
  public static boolean printout = false;

  @Option("The name of a file containing a list of source files to instrument")
  public static List<String> files = new ArrayList<String>();

  static AST ast;

  private static CompilationUnit unit;

  private static String packageName = null;
  private static String covFieldsCls = null;
  private static int branchNumber;
  private static int classDepth = 0;
  private static int methodDepth = 0;
  private static int methodId = 0;
  private static Map<String,Set<Integer>> allMethodIndices = null;
  private static Map<String,Pair<Integer,Integer>> allMethodLineSpans = null;

  private static Set<Integer> oneMethodIndices = null;
  private static List<Integer> branchToLine = null;

  public static String sourceFileName = null;

  public static void main(String[] args) throws ArgException, IOException {

    Options options = new Options(Instrument.class);
    options.parse(args);
    if (destination == null) {
      System.out.println("Missing required option --destination=<dir>");
      System.exit(1);
    }
    File destinationDir = new File(destination);
    if (destinationDir.exists() && !overwrite) {
      System.out.println("Destination directory \"" + destinationDir +
      "\" exists but --overwrite option was not given. Will not proceed.");
      System.exit(1);
    }

    List<String> javaFileNames = new ArrayList<String>();
    if (!files.isEmpty())
      javaFileNames.addAll(readJavaFileNames(files));
    javaFileNames.addAll(FilesUtil.getJavaFileNames(Arrays.asList(args)));

    for (String oneFileStr : javaFileNames) {

      File oneFile = new File(oneFileStr);

      sourceFileName = oneFile.getName();

      // Workaround for thesis experiments.
      // Don't instrument enums...
      if (oneFile.getAbsolutePath().endsWith("java3/math3/RoundingMode.java")) {
        continue;
      }

      System.out.println("Instrumenting " + oneFile);

      ASTParser parser = ASTParser.newParser(AST.JLS3);
      Map<String,String> compilerOptions = new HashMap<String,String>();
      compilerOptions.put(JavaCore.COMPILER_SOURCE, "1.5");
      parser.setCompilerOptions(compilerOptions);
      String fileContents = Files.getFileContents(oneFile);
      parser.setSource(fileContents.toCharArray());
      unit = (CompilationUnit) parser.createAST(null);
      unit.recordModifications();
      ast = unit.getAST();

      assert classDepth == 0;
      assert methodDepth == 0;
      assert allMethodIndices == null;
      assert allMethodLineSpans == null;
      assert oneMethodIndices == null;
      assert branchToLine == null;

      packageName = "";
      PackageDeclaration packageDecl = unit.getPackage();
      if (packageDecl != null)
        packageName = unit.getPackage().getName().toString();

      unit.accept(new Instrument());

      try {

        File instrumentedFileDir = new File(destinationDir.getPath()
            + File.separator
            + packageName.replace(".", File.separator));

        if (!instrumentedFileDir.exists()) {
          instrumentedFileDir.mkdirs();
        }

        // Output instrumented class
        String instrumentedFileName = oneFile.getName();
        File instrumentedFile = new File(instrumentedFileDir, instrumentedFileName);
        System.out.println("Writing " + instrumentedFile);
        Writer output = new FileWriter(instrumentedFile);

        char[] contents = null;
        try {
          Document doc = new Document(fileContents);
          TextEdit edits = unit.rewrite(doc,null);
          edits.apply(doc);
          String sourceCode = doc.get();
          if (sourceCode != null)
            contents = sourceCode.toCharArray();
        }
        catch (BadLocationException e) {
          throw new RuntimeException(e);
        }

        output.append(new String(contents));
        output.close();

        // Output original source file with .java.orig suffix.
        File origFile = new File(instrumentedFileDir, instrumentedFileName + ".orig");
        System.out.println("Writing " + origFile);
        output = new FileWriter(origFile);
        output.append(fileContents);
        output.close();

      } catch (IOException e) {
        System.err.println("Exception while instrumenting " + oneFile.getAbsolutePath());
        System.err.println(e.getMessage());
        e.printStackTrace();
        System.exit(1);
      }
    }
  }




  private static List<String> readJavaFileNames(List<String> fileListings) throws IOException {
    assert fileListings != null;
    List<String> ret = new ArrayList<String>();
    for (String oneListing : fileListings) {
      for (String oneLine : Files.readWhole(oneListing)) {
        oneLine = oneLine.trim();
        if (!oneLine.endsWith(".java")) {
          throw new RuntimeException("File " + oneListing + " contains a line not ending in .java: " + oneLine);
        }
        ret.add(oneLine);
      }
    }
    return ret;
  }




  private boolean preProcessClassDeclaration(TypeDeclaration type) {
    // If this is a top-level class, reset state.
    if (classDepth == 0) {
      assert allMethodIndices == null;
      assert allMethodLineSpans == null;
      assert branchToLine == null;
      assert packageName != null;
      assert covFieldsCls == null;
      covFieldsCls =
        (packageName.length()==0 ? "" : packageName + ".")
        + type.getName().toString();
      branchNumber = 0;
      methodDepth = 0;
      methodId = 0;
      allMethodIndices = new LinkedHashMap<String, Set<Integer>>();
      allMethodLineSpans = new LinkedHashMap<String, Pair<Integer,Integer>>();
      branchToLine = new ArrayList<Integer>();
    }
    classDepth++;
    return true;
  }

  private void postProcessClassOrEnumDeclaration(List<BodyDeclaration> bodyDeclarations) {
    // If this is a top-level class, output instrumentation declarations.
    if (classDepth == 1) {
      assert allMethodIndices != null;
      assert allMethodLineSpans != null;
      assert branchToLine != null;
      assert methodDepth == 0;
      assert covFieldsCls != null;
      // We must insert coverage-related fields before other declarations,
      // so that they are initialized before everything else (otherwise we
      // may get forward-reference error.
      bodyDeclarations.addAll(0, getCovIndicesMethod(allMethodIndices, branchToLine, branchNumber, allMethodLineSpans));
      covFieldsCls = null;
      allMethodIndices = null;
      allMethodLineSpans = null;
      branchToLine = null;
    }
    classDepth--;
  }

  @Override
  public boolean visit(TypeDeclaration type) {
    System.out.println("Visiting class " + type.getName().toString());
    // Do nothing with interfaces.
    if (type.isInterface())
      return false;
    if (type.isLocalTypeDeclaration()) {
      return false;
    }
    return preProcessClassDeclaration(type);
  }

  @Override
  public void endVisit(TypeDeclaration type) {
    // Do nothing with interfaces.
    if (type.isInterface())
      return;
    if (type.isLocalTypeDeclaration())
      return;
    postProcessClassOrEnumDeclaration(type.bodyDeclarations());
  }

// We don't handle enums currently (we'll do so at some point, first writing
// tests to make sure we do things correctly).
//
//  @Override
//  public boolean visit(EnumDeclaration en) {
//    return preProcessClassOrEnumDeclaration(en);
//  }
//
//  @Override
//  public void endVisit(EnumDeclaration en) {
//    postProcessClassOrEnumDeclaration(en.bodyDeclarations());
//  }

  @Override
  public boolean visit(MethodDeclaration method) {

    if (methodDepth == 0) {
      assert oneMethodIndices == null;
      oneMethodIndices = new LinkedHashSet<Integer>();
      // add annotation @SimpleCovMethodId("X") where X is methodId.
      SingleMemberAnnotation anno = ast.newSingleMemberAnnotation();
      anno.setTypeName(ast.newName(Constants.MethodIdAnnotation));
      StringLiteral uniqueId = ast.newStringLiteral();
      uniqueId.setLiteralValue(Integer.toString(methodId));
      anno.setValue(uniqueId);
      method.modifiers().add(0, anno);
    }
    methodDepth++;
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration method) {

    if (methodDepth == 1) {
      assert oneMethodIndices != null;
      try {
        allMethodIndices.put(Integer.toString(methodId), oneMethodIndices);
        int startLine = unit.getLineNumber(method.getStartPosition());
        int endLine = unit.getLineNumber(method.getStartPosition() + method.getLength());
        allMethodLineSpans.put(Integer.toString(methodId), new Pair<Integer,Integer>(startLine, endLine));
      } catch (Exception e ) {
        System.out.println(method.toString());
        throw new RuntimeException(e);
      }
      oneMethodIndices = null;
      methodId++;
    }
    methodDepth--;
  }

  /**
   * Augments boolean expression e to also record whether it evaluates to
   * true or false. Each expression is assigned a unique id.
   *
   * If instrumentation==ARRAYS,
   * e becomes ((e && ++branchTrue[13]!=0) || ++branchFalse[13]==0)
   * which is equivalent to ((e && true) || false), because the array elements are
   * initialized to zero and are only incremented.
   *
   * Then:
   *
   * If e==true,  ((true  && true) || false) == true
   * If e==false, ((false && true) || false) == false
   *
   * If instrumentation==PRINTOUT
   * e becomes ((e && branchTrue(13)) || branchFalse(13))
   * which works similar to ARRAYS but the branchTrue and branchFalse methods
   * print out new coverage to System.out.
   *
   */
  @Override
  public boolean visit(IfStatement ifst) {
    ifst.setExpression(coverageAugmentedExpr(ifst.getExpression(),
        unit.getLineNumber(ifst.getStartPosition())));
    return true;
  }

  @Override
  public boolean visit(ForStatement forst) {
    Expression exp = forst.getExpression();
    if (exp == null) {
      // Expression equivalent to true, so we'll never follow the other branch.
      // Thus no sense measuring branch coverage for this branch.
      return true;
    }
    forst.setExpression(coverageAugmentedExpr(exp, unit.getLineNumber(forst.getStartPosition())));
    return true;
  }

  @Override
  public boolean visit(WhileStatement whilest) {
    Expression exp = whilest.getExpression();
    whilest.setExpression(coverageAugmentedExpr(exp, unit.getLineNumber(whilest.getStartPosition())));
    return true;
  }

  private static List<BodyDeclaration> getCovIndicesMethod(Map<String,Set<Integer>> methodIndices,
      List<Integer> branchToLine, int totBranches, Map<String, Pair<Integer, Integer>> allMethodLineSpans) {

    assert totBranches == branchToLine.size() : "totBranches:" + totBranches + ",branchToLine.size()=" + branchToLine.size();
    StringBuilder code = new StringBuilder();

    code.append("public static final String " + Constants.sourceFileNameField + " = \"" + sourceFileName + "\";");
    code.append("public static final boolean " + Constants.isInstrumentedField + " = true;");

    code.append("public static int[] " + Constants.trueBranches + " = new int[" + totBranches + "];");
    code.append("public static int[] " + Constants.falseBranches + " = new int[" + totBranches + "];");
    code.append("public static java.util.Map<String,int[]> " + Constants.methodIdToBranches +  " = new java.util.LinkedHashMap<String,int[]>();");
    code.append("public static java.util.Map<String,int[]> " + Constants.methodLineSpansField + " = new java.util.LinkedHashMap<String,int[]>();");
    code.append("static {  ");
    for (Map.Entry<String,Set<Integer>> entry : methodIndices.entrySet()) {
      String methodSig = entry.getKey();
      code.append(Constants.methodIdToBranches + ".put(\"" + methodSig + "\", new int[]{");
      List<Integer> intList = new ArrayList<Integer>(entry.getValue());
      for (int i = 0 ; i < intList.size() ; i++) {
        if (i > 0) code.append(",");
        code.append(intList.get(i));
      }
      code.append("});");
    }
    for (Map.Entry<String,Pair<Integer,Integer>> entry : allMethodLineSpans.entrySet()) {
      String methodSig = entry.getKey();
      code.append(Constants.methodLineSpansField + ".put(\"" + methodSig + "\", new int[]{");
      code.append(entry.getValue().a + "," + entry.getValue().b + "});");
    }
    code.append("}");

    code.append("public static int " + Constants.branchLines + "[] = new int[]{");
    for (int i = 0 ; i < branchToLine.size() ; i++) {
      if (i > 0) code.append(",");
      code.append(branchToLine.get(i));
    }
    code.append("};");

    code.append("@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
    code.append("@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD,java.lang.annotation.ElementType.CONSTRUCTOR})");
    code.append("public static @interface " + Constants.MethodIdAnnotation + " {");
    code.append("String value();");
    code.append("};");

    List<BodyDeclaration> decls = ASTUtil.parseBodyDeclarations(code.toString(), ast);
    return decls;
  }


  private static Expression coverageAugmentedExpr(Expression exp, int lineNumber) {

    if (exp.toString().trim().equals("true") || exp.toString().trim().equals("false")) {
      return (Expression) ASTNode.copySubtree(ast, exp);
    }

    assert branchToLine.size() == branchNumber;
    branchToLine.add(lineNumber);
    if (oneMethodIndices != null) {
      oneMethodIndices.add(branchNumber);
    }

    StringBuilder code = new StringBuilder();
    code.append("(((");
    code.append(exp.toString());
    code.append(") && ++");
    code.append(covFieldsCls + "." + Constants.trueBranches);
    code.append("[");
    code.append(branchNumber);
    code.append("]!=0) || ++");
    code.append(covFieldsCls + "." + Constants.falseBranches);
    code.append("[");
    code.append(branchNumber);
    code.append("]==0)");

    branchNumber++;
    return ASTUtil.parseExpression(code.toString(), ast);
  }
}
