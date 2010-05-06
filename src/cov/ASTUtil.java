package cov;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * ASTUtil contains AST-related utility methods used by the instrumenter.
 *
 * The cov package implements a basic branch coverage instrumenter
 * that we use for the branch-directed test generation research.
 *
 * This tool is prototype-quality, not for production use. In
 * particular, it is missing a number of features including tracking
 * coverage for switch statements, and lack of support for
 * generics.
 */
public class ASTUtil {

  static List<BodyDeclaration> parseBodyDeclarations(String str, AST owner) {
    if (str == null || owner == null) throw new IllegalArgumentException();
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
    parser.setSource(str.toCharArray());
    ASTNode node = parser.createAST(null);
    if (node instanceof CompilationUnit) // This means something went wrong.
      throw new RuntimeException(Arrays.toString(((CompilationUnit)node).getProblems()));
    TypeDeclaration decl = (TypeDeclaration) node;
    List<BodyDeclaration> bds = new ArrayList<BodyDeclaration>();
    for (Object o : decl.bodyDeclarations()) {
      BodyDeclaration bd = (BodyDeclaration)o;
      bds.add((BodyDeclaration) ASTNode.copySubtree(owner, bd));
    }
    return bds;
  }

  static List<Statement> parseStatements(String str, AST owner) {
    if (str == null || owner == null) throw new IllegalArgumentException();
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_STATEMENTS);
    parser.setSource(str.toCharArray());
    ASTNode node = parser.createAST(null);
    if (node instanceof CompilationUnit) // This means something went wrong.
      throw new RuntimeException(Arrays.toString(((CompilationUnit)node).getProblems()));
    Block bl = (Block) node;
    List<Statement> sts = new ArrayList<Statement>();
    for (Object o : bl.statements()) {
      Statement bd = (Statement)o;
      sts.add((Statement) ASTNode.copySubtree(owner, bd));
    }
    return sts;
  }

  static Expression parseExpression(String str, AST owner) {
    if (str == null || owner == null) throw new IllegalArgumentException();
    ASTParser parser = ASTParser.newParser(AST.JLS3);
    parser.setKind(ASTParser.K_EXPRESSION);
    parser.setSource(str.toCharArray());
    ASTNode exp = parser.createAST(null);
    if (exp instanceof CompilationUnit) // This means something went wrong.
      throw new RuntimeException(Arrays.toString(((CompilationUnit)exp).getProblems()));
    return (Expression) ASTNode.copySubtree(owner, exp);
  }

}
