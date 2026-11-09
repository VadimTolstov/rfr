package io.student.rangiffler.jupiter.condition;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Restricts fixture targets to explicitly enabled EngineTestKit runs. */
public final class ProbeTargetCondition implements ExecutionCondition {
 public static final String PARAMETER = "rfr.probe";

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    return context.getConfigurationParameter(PARAMETER, Boolean::parseBoolean).orElse(false)
        ?ConditionEvaluationResult.enabled("Enabled by EngineTestKit")
        : ConditionEvaluationResult.disabled("Run only by EngineTestKit");
  }
}
