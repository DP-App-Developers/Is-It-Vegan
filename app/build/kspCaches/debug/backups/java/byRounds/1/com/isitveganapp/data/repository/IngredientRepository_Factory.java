package com.isitveganapp.data.repository;

import com.isitveganapp.data.local.DatabaseSeeder;
import com.isitveganapp.data.local.IngredientDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class IngredientRepository_Factory implements Factory<IngredientRepository> {
  private final Provider<IngredientDao> daoProvider;

  private final Provider<DatabaseSeeder> seederProvider;

  public IngredientRepository_Factory(Provider<IngredientDao> daoProvider,
      Provider<DatabaseSeeder> seederProvider) {
    this.daoProvider = daoProvider;
    this.seederProvider = seederProvider;
  }

  @Override
  public IngredientRepository get() {
    return newInstance(daoProvider.get(), seederProvider.get());
  }

  public static IngredientRepository_Factory create(Provider<IngredientDao> daoProvider,
      Provider<DatabaseSeeder> seederProvider) {
    return new IngredientRepository_Factory(daoProvider, seederProvider);
  }

  public static IngredientRepository newInstance(IngredientDao dao, DatabaseSeeder seeder) {
    return new IngredientRepository(dao, seeder);
  }
}
