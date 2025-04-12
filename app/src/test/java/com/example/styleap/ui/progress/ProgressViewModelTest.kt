package com.example.styleap.ui.progress

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.styleap.data.User
import com.example.styleap.data.UserRepository
import com.example.styleap.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ProgressViewModelTest {

    // Rule to execute LiveData operations synchronously
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Mock the dependency
    @Mock
    private lateinit var userRepository: UserRepository

    // Subject under test
    private lateinit var viewModel: ProgressViewModel

    // Coroutine test dispatcher
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for ViewModelScope
        // Simulate successful user data loading by default
        runTest {
             whenever(userRepository.getCurrentUserData()) doReturn Resource.Success(
                 User(points = 50, level = 2) // Sample data
             )
        }
        viewModel = ProgressViewModel(userRepository)
        // Advance dispatcher after VM init to allow loadUserData coroutine to run
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher
    }

    @Test
    fun `loadUserData success updates LiveData`() = runTest {
        // Arrange: Setup already done in @Before

        // Act: ViewModel initialized in @Before

        // Assert
        assertEquals(ProgressUiState.Success, viewModel.uiState.value)
        assertEquals(50, viewModel.userPoints.value)
        assertEquals(2, viewModel.userLevel.value)
    }

    @Test
    fun `loadUserData error updates LiveData`() = runTest {
        // Arrange
        val errorMessage = "Failed to load"
        whenever(userRepository.getCurrentUserData()) doReturn Resource.Error(errorMessage)

        // Act: Re-initialize ViewModel to trigger loading again
        viewModel = ProgressViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertIs<ProgressUiState.Error>(viewModel.uiState.value)
        assertEquals(errorMessage, (viewModel.uiState.value as ProgressUiState.Error).message)
        // Default values should remain
        assertEquals(0, viewModel.userPoints.value)
        assertEquals(1, viewModel.userLevel.value)
    }

    @Test
    fun `incrementPoints updates points LiveData`() {
        // Arrange (initial state from setUp)
        val initialPoints = viewModel.userPoints.value ?: 0
        val incrementAmount = 15

        // Act
        viewModel.incrementPoints(incrementAmount)

        // Assert
        assertEquals(initialPoints + incrementAmount, viewModel.userPoints.value)
    }

    @Test
    fun `incrementProgress updates progress and level correctly`() = runTest {
        // Arrange
        viewModel.incrementProgress(80) // Progress is 80
        assertEquals(80, viewModel.progressValue.value)
        assertEquals(2, viewModel.userLevel.value) // Initial level from setUp

        // Act: Increment progress enough to level up
        viewModel.incrementProgress(30) // Total becomes 110 -> level up, progress reset
        testDispatcher.scheduler.advanceUntilIdle() // Allow level update coroutine

        // Assert
        assertEquals(10, viewModel.progressValue.value) // Progress resets (110-100)
        assertEquals(3, viewModel.userLevel.value) // Level increments
        verify(userRepository).updateUserLevel(3) // Verify repository interaction
    }

     @Test
    fun `attemptWithdrawal success when sufficient points`() = runTest {
        // Arrange: Ensure sufficient points (setup has 50)
        whenever(userRepository.withdrawPoints(any())).thenReturn(Unit) // Mock successful withdrawal

        // Act
        viewModel.attemptWithdrawal()
        testDispatcher.scheduler.advanceUntilIdle() // Allow withdrawal coroutine

        // Assert
        assertIs<Resource.Success<Unit>>(viewModel.withdrawalState.value)
        assertEquals(0, viewModel.userPoints.value) // Points reset
        assertEquals(1, viewModel.totalWithdrawals.value) // Withdrawals incremented
        verify(userRepository).withdrawPoints(50) // Verify repository called with correct points
    }

    @Test
    fun `attemptWithdrawal error when insufficient points`() {
        // Arrange: Set points lower than minimum
        runTest {
            whenever(userRepository.getCurrentUserData()) doReturn Resource.Success(User(points = 10))
        }
        viewModel = ProgressViewModel(userRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(10, viewModel.userPoints.value)

        // Act
        viewModel.attemptWithdrawal()
        // No coroutine needed here as it's a synchronous check

        // Assert
        assertIs<Resource.Error<Unit>>(viewModel.withdrawalState.value)
        assertEquals("Insufficient points for withdrawal", (viewModel.withdrawalState.value as Resource.Error).message)
        assertEquals(10, viewModel.userPoints.value) // Points unchanged
    }

    @Test
    fun `attemptWithdrawal handles repository error`() = runTest {
         // Arrange: Ensure sufficient points
        val errorMessage = "Firestore error"
        whenever(userRepository.withdrawPoints(any())) doThrow RuntimeException(errorMessage)

         // Act
        viewModel.attemptWithdrawal()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertIs<Resource.Error<Unit>>(viewModel.withdrawalState.value)
        assertEquals(errorMessage, (viewModel.withdrawalState.value as Resource.Error).message)
        assertEquals(50, viewModel.userPoints.value) // Points unchanged on error
    }
} 