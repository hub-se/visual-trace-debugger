package se.de.hu_berlin.informatik.vtdbg.coverage;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.function.Supplier;

public class MyCoverageBundle extends DynamicBundle {
  @NonNls
  private static final String BUNDLE = "messages.MyCoverageBundle";
  private static final MyCoverageBundle INSTANCE = new MyCoverageBundle();

  private MyCoverageBundle() {
    super(BUNDLE);
  }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object ... params) {
    return INSTANCE.getMessage(key, params);
  }

  @NotNull
  public static Supplier<String> messagePointer(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object ... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}