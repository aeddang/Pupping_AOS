package com.dagger.module

import android.content.Context
import com.lib.page.PageActivityPresenter
import com.lib.page.PagePresenter
import com.raftgroup.pupping.scene.page.viewmodel.ActivityModel
import com.raftgroup.pupping.scene.page.viewmodel.FragmentProvider
import com.raftgroup.pupping.store.provider.DataProvider
import com.raftgroup.pupping.store.PageRepository
import com.raftgroup.pupping.store.ShareManager
import com.raftgroup.pupping.store.Topic
import com.raftgroup.pupping.store.api.ApiInterceptor
import com.raftgroup.pupping.store.api.ApiManager
import com.raftgroup.pupping.store.database.DataBaseManager
import com.raftgroup.pupping.store.mission.MissionGenerator
import com.raftgroup.pupping.store.mission.MissionManager
import com.raftgroup.pupping.store.preference.StoragePreference
import com.skeleton.module.network.NetworkFactory
import com.skeleton.sns.SnsManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoreModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext ctx: Context): Context = ctx

    @Singleton
    @Provides
    fun provideStoragePreference(ctx: Context): StoragePreference = StoragePreference(ctx)

    @Singleton
    @Provides
    fun provideNetworkFactory(@ApplicationContext ctx: Context): NetworkFactory = NetworkFactory(ctx)

    @Singleton
    @Provides
    fun provideSnsManager(@ApplicationContext ctx: Context): SnsManager = SnsManager(ctx)

    @Singleton
    @Provides
    fun provideDataBaseManager(ctx: Context, storage:StoragePreference): DataBaseManager
            = DataBaseManager(ctx, storage)

    @Singleton
    @Provides
    fun provideTopic(storage:StoragePreference, pagePresenter: PagePresenter): Topic
            = Topic(pagePresenter, storage)

    @Singleton
    @Provides
    fun provideApiManager(ctx: Context, networkFactory: NetworkFactory, interceptor: ApiInterceptor): ApiManager
            = ApiManager(ctx, networkFactory, interceptor)

    @Singleton
    @Provides
    fun providePagePresenter(): PagePresenter = PageActivityPresenter ()

    @Singleton
    @Provides
    fun provideFragmentProvider(): FragmentProvider = FragmentProvider ()

    @Singleton
    @Provides
    fun provideActivityModel(): ActivityModel = ActivityModel()

    @Singleton
    @Provides
    fun provideApiInterceptor(): ApiInterceptor = ApiInterceptor()

    @Singleton
    @Provides
    fun provideDataProvider(): DataProvider = DataProvider()

    @Singleton
    @Provides
    fun provideShareManager(): ShareManager = ShareManager()


    @Singleton
    @Provides
    fun provideMissionGenerator(ctx: Context): MissionGenerator = MissionGenerator(ctx)

    @Singleton
    @Provides
    fun provideMissionManager(missionGenerator:MissionGenerator): MissionManager = MissionManager(missionGenerator)

    @Singleton
    @Provides
    fun providePageRepository(
        ctx: Context,
        storage:StoragePreference,
        dataBaseManager: DataBaseManager,
        dataProvider: DataProvider,
        apiManager:ApiManager,
        pageModel: ActivityModel,
        pageProvider: FragmentProvider,
        pagePresenter: PagePresenter,
        shareManager:ShareManager,
        snsManager: SnsManager,
        topic:Topic,
        missionManager:MissionManager,
        interceptor: ApiInterceptor

    ): PageRepository = PageRepository(ctx,
        storage, dataBaseManager,
        dataProvider, apiManager,
        pageModel, pageProvider, pagePresenter,shareManager, snsManager, topic, missionManager, interceptor)
}