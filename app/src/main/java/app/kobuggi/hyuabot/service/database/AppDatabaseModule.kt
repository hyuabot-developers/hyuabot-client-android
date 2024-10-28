package app.kobuggi.hyuabot.service.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppDatabaseModule {
    companion object {
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext applicationContext: Context): AppDatabase {
            val database = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "hyuabot"
            ).build()
            return database
        }
    }
}
