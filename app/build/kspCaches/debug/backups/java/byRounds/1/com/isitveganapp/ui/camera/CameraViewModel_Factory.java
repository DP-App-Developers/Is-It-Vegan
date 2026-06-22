package com.isitveganapp.ui.camera;

import com.isitveganapp.data.repository.IngredientRepository;
import com.isitveganapp.domain.usecase.AnalyzeIngredientsUseCase;
import com.isitveganapp.ocr.MlKitOcrEngine;
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
public final class CameraViewModel_Factory implements Factory<CameraViewModel> {
  private final Provider<MlKitOcrEngine> ocrEngineProvider;

  private final Provider<AnalyzeIngredientsUseCase> analyzeUseCaseProvider;

  private final Provider<IngredientRepository> repositoryProvider;

  public CameraViewModel_Factory(Provider<MlKitOcrEngine> ocrEngineProvider,
      Provider<AnalyzeIngredientsUseCase> analyzeUseCaseProvider,
      Provider<IngredientRepository> repositoryProvider) {
    this.ocrEngineProvider = ocrEngineProvider;
    this.analyzeUseCaseProvider = analyzeUseCaseProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public CameraViewModel get() {
    return newInstance(ocrEngineProvider.get(), analyzeUseCaseProvider.get(), repositoryProvider.get());
  }

  public static CameraViewModel_Factory create(Provider<MlKitOcrEngine> ocrEngineProvider,
      Provider<AnalyzeIngredientsUseCase> analyzeUseCaseProvider,
      Provider<IngredientRepository> repositoryProvider) {
    return new CameraViewModel_Factory(ocrEngineProvider, analyzeUseCaseProvider, repositoryProvider);
  }

  public static CameraViewModel newInstance(MlKitOcrEngine ocrEngine,
      AnalyzeIngredientsUseCase analyzeUseCase, IngredientRepository repository) {
    return new CameraViewModel(ocrEngine, analyzeUseCase, repository);
  }
}
