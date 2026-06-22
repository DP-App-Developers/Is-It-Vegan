package com.isitveganapp.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ParseIngredientTextUseCase_Factory implements Factory<ParseIngredientTextUseCase> {
  @Override
  public ParseIngredientTextUseCase get() {
    return newInstance();
  }

  public static ParseIngredientTextUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ParseIngredientTextUseCase newInstance() {
    return new ParseIngredientTextUseCase();
  }

  private static final class InstanceHolder {
    static final ParseIngredientTextUseCase_Factory INSTANCE = new ParseIngredientTextUseCase_Factory();
  }
}
