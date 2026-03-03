package com.rejowan.pdfreaderpro.presentation.viewmodel

import com.rejowan.pdfreaderpro.domain.repository.PreferencesRepository
import com.rejowan.pdfreaderpro.presentation.screens.onboarding.OnboardingViewModel
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        preferencesRepository = mockk(relaxed = true)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): OnboardingViewModel {
        return OnboardingViewModel(
            preferencesRepository = preferencesRepository
        )
    }

    // region markOnboardingComplete Tests
    @Test
    fun `markOnboardingComplete calls repository with true`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markOnboardingComplete()
        advanceUntilIdle()

        coVerify { preferencesRepository.setOnboardingCompleted(true) }
    }

    @Test
    fun `markOnboardingComplete can be called multiple times`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.markOnboardingComplete()
        viewModel.markOnboardingComplete()
        advanceUntilIdle()

        coVerify(exactly = 2) { preferencesRepository.setOnboardingCompleted(true) }
    }
    // endregion
}
