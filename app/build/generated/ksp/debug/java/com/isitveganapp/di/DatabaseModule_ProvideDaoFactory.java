package com.isitveganapp.di;

import com.isitveganapp.data.local.IngredientDao;
import com.isitveganapp.data.local.IngredientDatabase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDaoFactory implements Factory<IngredientDao> {
  private final Provider<IngredientDatabase> dbProvider;

  public DatabaseModule_ProvideDaoFactory(Provider<IngredientDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public IngredientDao get() {
    return provideDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDaoFactory create(Provider<IngredientDatabase> dbProvider) {
    return new DatabaseModule_ProvideDaoFactory(dbProvider);
  }

  public static IngredientDao provideDao(IngredientDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDao(db));
  }
}
