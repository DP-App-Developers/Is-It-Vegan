package com.isitveganapp.ocr;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class MlKitOcrEngine_Factory implements Factory<MlKitOcrEngine> {
  @Override
  public MlKitOcrEngine get() {
    return newInstance();
  }

  public static MlKitOcrEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MlKitOcrEngine newInstance() {
    return new MlKitOcrEngine();
  }

  private static final class InstanceHolder {
    static final MlKitOcrEngine_Factory INSTANCE = new MlKitOcrEngine_Factory();
  }
}
