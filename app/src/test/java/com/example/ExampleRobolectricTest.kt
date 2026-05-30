package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.CalculatorRepository
import com.example.ui.viewmodel.CalculatorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: CalculatorRepository
  private lateinit var viewModel: CalculatorViewModel

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = CalculatorRepository(database.historyDao(), database.currencyDao())
    viewModel = CalculatorViewModel(repository)
  }

  @After
  fun tearDown() {
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("All-in-One Calc", appName)
  }

  @Test
  fun `test onCalculate saves standard expression to history`() = runTest {
    viewModel.onNumberPress("5")
    viewModel.onOperatorPress("+")
    viewModel.onNumberPress("3")
    
    val currentExpr = viewModel.expression.value
    println("DEBUG: currentExpr = $currentExpr")
    assertEquals("5+3", currentExpr)
    
    viewModel.onCalculate()
    
    val resultExpr = viewModel.expression.value
    println("DEBUG: resultExpr = $resultExpr")
    assertEquals("8", resultExpr)
    
    // As insert is on Dispatchers.IO, let's wait a moment for propagation
    var history = viewModel.calculationHistory.value
    println("DEBUG: initial history size = ${history.size}")
    var attempts = 0
    while (history.isEmpty() && attempts < 20) {
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        Thread.sleep(50)
        history = viewModel.calculationHistory.value
        attempts++
    }
    
    println("DEBUG: final history size = ${history.size}")
    assertEquals(1, history.size)
    assertEquals("5+3", history[0].expression)
    assertEquals("8", history[0].result)
  }
}
