package app.kobuggi.hyuabot.service.database

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase = AppDatabase.getInstance(context)

    @Singleton
    @Provides
    fun provideUserDao(database: AppDatabase): AppDatabaseDao = database.databaseDao()

    @Singleton
    @Provides
    fun provideUserRepository(userDao: AppDatabaseDao): AppDatabaseRepository = AppDatabaseRepository(userDao)
}