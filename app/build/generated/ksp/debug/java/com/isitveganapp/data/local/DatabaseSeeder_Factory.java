package com.isitveganapp.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DatabaseSeeder_Factory implements Factory<DatabaseSeeder> {
  private final Provider<Context> contextProvider;

  private final Provider<IngredientDao> daoProvider;

  public DatabaseSeeder_Factory(Provider<Context> contextProvider,
      Provider<IngredientDao> daoProvider) {
    this.contextProvider = contextProvider;
    this.daoProvider = daoProvider;
  }

  @Override
  public DatabaseSeeder get() {
    return newInstance(contextProvider.get(), daoProvider.get());
  }

  public static DatabaseSeeder_Factory create(Provider<Context> contextProvider,
      Provider<IngredientDao> daoProvider) {
    return new DatabaseSeeder_Factory(contextProvider, daoProvider);
  }

  public static DatabaseSeeder newInstance(Context context, IngredientDao dao) {
    return new DatabaseSeeder(context, dao);
  }
}
