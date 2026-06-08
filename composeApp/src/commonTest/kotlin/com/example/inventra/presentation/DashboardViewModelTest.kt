package com.example.inventra.presentation

import app.cash.turbine.test
import com.example.inventra.FakeAuthRepository
import com.example.inventra.FakeBorrowRepository
import com.example.inventra.FakeItemRepository
import com.example.inventra.domain.model.BorrowRecord
import com.example.inventra.domain.model.BorrowStatus
import com.example.inventra.domain.model.Item
import com.example.inventra.domain.model.ItemCategory
import com.example.inventra.domain.model.ItemCondition
import com.example.inventra.presentation.screens.dashboard.DashboardUiState
import com.example.inventra.presentation.screens.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Clock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var borrowRepository: FakeBorrowRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: DashboardViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        itemRepository = FakeItemRepository()
        borrowRepository = FakeBorrowRepository()
        authRepository = FakeAuthRepository()
        viewModel = DashboardViewModel(itemRepository, borrowRepository, authRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state should show correct totalItems count`() = runTest {
        itemRepository.insertItem(createTestItem("Item 1"))
        itemRepository.insertItem(createTestItem("Item 2"))

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            assertEquals(2, (state as DashboardUiState.Success).totalItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state should show overdueItems count correctly`() = runTest {
        borrowRepository.addRecord(createOverdueRecord())

        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            assertEquals(1, (state as DashboardUiState.Success).overdueItems)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `state should show empty activeBorrowings when no records`() = runTest {
        viewModel.uiState.test {
            skipItems(1)
            advanceUntilIdle()

            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            assertTrue((state as DashboardUiState.Success).activeBorrowings.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createTestItem(name: String): Item {
        return Item(
            id = 0,
            name = name,
            category = ItemCategory.OTHER,
            location = "Office",
            totalStock = 5,
            availableStock = 5,
            condition = ItemCondition.GOOD,
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
    }

    private fun createOverdueRecord(): BorrowRecord {
        val now = Clock.System.now()
        return BorrowRecord(
            id = 1,
            itemId = 1,
            itemName = "Test Item",
            borrowerName = "Test User",
            borrowDate = now,
            dueDate = now,
            status = BorrowStatus.OVERDUE
        )
    }
}
