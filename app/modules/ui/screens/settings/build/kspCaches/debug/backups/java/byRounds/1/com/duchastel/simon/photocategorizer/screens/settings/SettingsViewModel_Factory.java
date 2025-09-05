package com.duchastel.simon.photocategorizer.screens.settings;

import com.duchastel.simon.photocategorizer.auth.AuthRepository;
import com.duchastel.simon.photocategorizer.storage.LocalStorageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("com.duchastel.simon.photocategorizer.dropbox.di.Dropbox")
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
    "deprecation"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<LocalStorageRepository> localStorageProvider;

  public SettingsViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<LocalStorageRepository> localStorageProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.localStorageProvider = localStorageProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(authRepositoryProvider.get(), localStorageProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<AuthRepository> authRepositoryProvider,
      Provider<LocalStorageRepository> localStorageProvider) {
    return new SettingsViewModel_Factory(authRepositoryProvider, localStorageProvider);
  }

  public static SettingsViewModel newInstance(AuthRepository authRepository,
      LocalStorageRepository localStorage) {
    return new SettingsViewModel(authRepository, localStorage);
  }
}
