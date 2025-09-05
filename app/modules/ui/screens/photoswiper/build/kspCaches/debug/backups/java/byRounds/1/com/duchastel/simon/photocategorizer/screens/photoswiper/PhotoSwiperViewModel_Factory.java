package com.duchastel.simon.photocategorizer.screens.photoswiper;

import com.duchastel.simon.photocategorizer.filemanager.PhotoRepository;
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
public final class PhotoSwiperViewModel_Factory implements Factory<PhotoSwiperViewModel> {
  private final Provider<PhotoRepository> photoRepositoryProvider;

  public PhotoSwiperViewModel_Factory(Provider<PhotoRepository> photoRepositoryProvider) {
    this.photoRepositoryProvider = photoRepositoryProvider;
  }

  @Override
  public PhotoSwiperViewModel get() {
    return newInstance(photoRepositoryProvider.get());
  }

  public static PhotoSwiperViewModel_Factory create(
      Provider<PhotoRepository> photoRepositoryProvider) {
    return new PhotoSwiperViewModel_Factory(photoRepositoryProvider);
  }

  public static PhotoSwiperViewModel newInstance(PhotoRepository photoRepository) {
    return new PhotoSwiperViewModel(photoRepository);
  }
}
