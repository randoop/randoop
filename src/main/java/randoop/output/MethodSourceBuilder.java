package randoop.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import plume.UtilMDE;
import randoop.generation.RandoopGenerationError;

/**
 * Created by bjkeller on 3/2/17.
 */
public class MethodSourceBuilder extends SourceBuilder {

  private final String methodName;
  private final String returnTypeName;
  private final List<String> throwsList;
  private final List<String> annotations;
  private final List<String> bodyText;
  private final List<String> parameters;

  public MethodSourceBuilder(
      String methodName, String returnTypeName, List<String> parameters, List<String> throwsList) {
    super();
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

  @Override
  public String toString() {
    for (String annotation : annotations) {
      appendLine(annotation);
    }
    String paramText = "(" + UtilMDE.join(parameters, ",") + ")";
    String suffix = "";
    if (!throwsList.isEmpty()) {
      suffix = "throws" + " " + UtilMDE.join(throwsList, " ") + " ";
    }
    suffix = suffix + "{";

    appendLine("public", returnTypeName, methodName, paramText, suffix);

    return super.toString();
  }
}
