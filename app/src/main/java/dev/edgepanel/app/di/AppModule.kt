package dev.edgepanel.app.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.edgepanel.app.core.common.AndroidEdgeLogger
import dev.edgepanel.app.core.common.DefaultDispatcherProvider
import dev.edgepanel.app.core.common.DispatcherProvider
import dev.edgepanel.app.core.common.EdgeLogger
import dev.edgepanel.app.core.permissions.AndroidOverlayPermissionDataSource
import dev.edgepanel.app.core.permissions.OverlayPermissionDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindLogger(impl: AndroidEdgeLogger): EdgeLogger

    @Binds
    @Singleton
    abstract fun bindDispatchers(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindOverlayPermission(
        impl: AndroidOverlayPermissionDataSource,
    ): OverlayPermissionDataSource
}
