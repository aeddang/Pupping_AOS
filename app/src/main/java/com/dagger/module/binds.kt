package com.dagger.module
import com.lib.page.PageActivity
import com.raftgroup.pupping.App
import com.raftgroup.pupping.HiltApp
import com.raftgroup.pupping.MainActivity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AppModule  {
    // If you have a scope annotation, see the section on scope aliases
    @Binds
    @Singleton
    abstract fun bindHiltApp(app: App): HiltApp
}

@InstallIn(ActivityComponent::class)
@Module
abstract class ActivityModule {
    // If you have a scope annotation, see the section on scope aliases
    @Binds
    abstract fun bindMainActivity(mainActivity: MainActivity): MainActivity
}
