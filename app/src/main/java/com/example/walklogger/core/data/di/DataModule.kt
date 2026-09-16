package com.example.walklogger.core.data.di

import com.example.walklogger.core.data.BehaviourIssueRepository
import com.example.walklogger.core.data.DefaultBehaviourIssueRepository
import com.example.walklogger.core.data.DefaultDogRepository
import com.example.walklogger.core.data.DefaultHandlerRepository
import com.example.walklogger.core.data.DefaultMedicalConcernRepository
import com.example.walklogger.core.data.DefaultWalkRepository
import com.example.walklogger.core.data.DogRepository
import com.example.walklogger.core.data.HandlerRepository
import com.example.walklogger.core.data.MedicalConcernRepository
import com.example.walklogger.core.data.WalkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindsWalkRepository(impl: DefaultWalkRepository): WalkRepository

    @Binds
    abstract fun bindsDogRepository(impl: DefaultDogRepository): DogRepository

    @Binds
    abstract fun bindsHandlerRepository(impl: DefaultHandlerRepository): HandlerRepository

    @Binds
    abstract fun bindsBehaviourIssueRepository(impl: DefaultBehaviourIssueRepository): BehaviourIssueRepository

    @Binds
    abstract fun bindsMedicalConcernRepository(impl: DefaultMedicalConcernRepository): MedicalConcernRepository
}
