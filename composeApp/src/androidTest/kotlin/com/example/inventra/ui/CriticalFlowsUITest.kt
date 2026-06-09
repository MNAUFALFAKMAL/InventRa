package com.example.inventra.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventra.presentation.screens.auth.LoginScreen
import com.example.inventra.presentation.theme.InventRaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Critical Flow UI Test for Login.
 * Sprint 4 fix.
 */
@RunWith(AndroidJUnit4::class)
class CriticalFlowsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Critical Flow Test 1: Tombol login tampil dengan teks yang benar
    @Test
    fun loginScreen_authorizeButtonIsDisplayed() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Authorize Access").assertIsDisplayed()
    }

    // Critical Flow Test 2: Error muncul saat login dengan field kosong
    @Test
    fun loginScreen_showsErrorWhenFieldsAreEmpty() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        // Tap login tanpa mengisi apapun
        composeTestRule.onNodeWithText("Authorize Access").performClick()

        // Error message dari LoginViewModel: "Email dan password harus diisi"
        composeTestRule.onNodeWithText("Email dan password harus diisi").assertIsDisplayed()
    }

    // Critical Flow Test 3: User bisa input email
    @Test
    fun loginScreen_canTypeEmail() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Email").performTextInput("admin@hmif.itera.ac.id")
        // Verifikasi field email menerima input
        composeTestRule.onNodeWithText("admin@hmif.itera.ac.id").assertExists()
    }

    // Critical Flow Test 4: Access Key field tampil
    @Test
    fun loginScreen_accessKeyFieldIsDisplayed() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("Access Key").assertIsDisplayed()
    }

    // Critical Flow Test 5: Header branding InventRa tampil
    @Test
    fun loginScreen_showsInventRaBranding() {
        composeTestRule.setContent {
            InventRaTheme {
                LoginScreen(onLoginSuccess = {})
            }
        }

        composeTestRule.onNodeWithText("InventRa").assertIsDisplayed()
    }
}
