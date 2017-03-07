package randoop.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import plume.UtilMDE;

/**
 * Constructs a {@code String} containing a method declaration.
 */
public class MethodSourceBuilder extends SourceBuilder {

  private final String modifiers;
  private final String methodName;
  private final String returnTypeName;
  private final List<String> throwsList;
  private final List<String> annotations;
  private final List<String> bodyText;
  private final List<String> parameters;

  public MethodSourceBuilder(
      String modifiers,
      String returnTypeName,
      String methodName,
      List<String> parameters,
      List<String> throwsList) {
    super();
    this.modifiers = modifiers;
    this.methodName = methodName;
    this.returnTypeName = returnTypeName;
    this.parameters = parameters;
    this.throwsList = throwsList;
    this.annotations = new ArrayList<>();
    this.bodyText = new ArrayList<>();
  }

  public void addAnnotation(Collection<String> annotations) {
    if (annotations != null) {
      this.annotations.addAll(annotations);
    }
  }

  public void addBodyText(String bodyText) {
    if (bodyText != null) {
      this.bodyText.add(bodyText);
    }
  }

  public void addBodyText(List<String> bodyText) {
    if (bodyText != null) {
      this.bodyText.addAll(bodyText);
    }
  }

  @Override
  List<String> toLines() {
    List<String> lines = new ArrayList<>();
    for (String annotation : annotations) {
      lines.add(createLine(annotation));
    }
    String paramText = "(" + UtilMDE.join(parameters, ",") + ")";
    String suffix = "";
    if (!throwsList.isEmpty()) {
      suffix = "throws" + " " + UtilMDE.join(throwsList, " ") + " ";
    }
    suffix = suffix + "{";
    lines.add(createLine(modifiers, returnTypeName, methodName, paramText, suffix));
    increaseIndent();
    for (String line : bodyText) {
      lines.add(createLine(line));
    }
    reverseIndent();
    lines.add(createLine("}"));
    return lines;
  }
}
