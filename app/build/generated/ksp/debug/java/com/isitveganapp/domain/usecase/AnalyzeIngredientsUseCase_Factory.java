package com.isitveganapp.domain.usecase;

import com.isitveganapp.data.repository.IngredientRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class AnalyzeIngredientsUseCase_Factory implements Factory<AnalyzeIngredientsUseCase> {
  private final Provider<IngredientRepository> repositoryProvider;

  private final Provider<ParseIngredientTextUseCase> parseUseCaseProvider;

  public AnalyzeIngredientsUseCase_Factory(Provider<IngredientRepository> repositoryProvider,
      Provider<ParseIngredientTextUseCase> parseUseCaseProvider) {
    this.repositoryProvider = repositoryProvider;
    this.parseUseCaseProvider = parseUseCaseProvider;
  }

  @Override
  public AnalyzeIngredientsUseCase get() {
    return newInstance(repositoryProvider.get(), parseUseCaseProvider.get());
  }

  public static AnalyzeIngredientsUseCase_Factory create(
      Provider<IngredientRepository> repositoryProvider,
      Provider<ParseIngredientTextUseCase> parseUseCaseProvider) {
    return new AnalyzeIngredientsUseCase_Factory(repositoryProvider, parseUseCaseProvider);
  }

  public static AnalyzeIngredientsUseCase newInstance(IngredientRepository repository,
      ParseIngredientTextUseCase parseUseCase) {
    return new AnalyzeIngredientsUseCase(repository, parseUseCase);
  }
}
