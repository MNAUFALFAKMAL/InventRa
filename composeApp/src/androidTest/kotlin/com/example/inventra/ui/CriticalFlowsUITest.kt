package com.example.inventra.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventra.presentation.screens.auth.LoginScreen
import com.example.inventra.presentation.theme.InventRaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CriticalFlowsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginFlow_showsErrorOnEmptyFields() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        // Tap login without credentials
        composeTestRule.onNodeWithText("MASUK SEKARANG").performClick()
        
        // Wait for error message (this depends on how the UI handles errors)
        // Usually it shows a Snackbar or error text
        composeTestRule.onNodeWithText("Email dan password tidak boleh kosong").assertExists()
    }

    @Test
    fun loginFlow_canInputCredentials() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("admin@hmif.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        
        composeTestRule.onNodeWithText("admin@hmif.com").assertExists()
        // Password shouldn't be visible in clear text depending on implementation, 
        // but it's in the text field.
    }
}
