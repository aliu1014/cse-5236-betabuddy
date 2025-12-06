package com.example.betabuddy

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf

private const val TEST_EMAIL = "test@example.com"
private const val TEST_PASSWORD = "password123"

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    /** 1) Login screen views are visible on launch */
    @Test
    fun loginScreenShowsOnLaunch() {
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
    }

    /** 2) Typing into email/password keeps the text */
    @Test
    fun typingIntoLoginFields_keepsText() {
        onView(withId(R.id.etEmail))
            .perform(typeText("test@example.com"), closeSoftKeyboard())

        onView(withId(R.id.etPassword))
            .perform(typeText("password123"), closeSoftKeyboard())

        onView(withId(R.id.etEmail))
            .check(matches(withText("test@example.com")))

        onView(withId(R.id.etPassword))
            .check(matches(withText("password123")))
    }

    /** 3) Clicking login with empty fields shows some error text */
    @Test
    fun signupOrLogin_navigatesToHome() {
        onView(withId(R.id.etEmail))
            .perform(replaceText(TEST_EMAIL), closeSoftKeyboard())
        onView(withId(R.id.etPassword))
            .perform(replaceText(TEST_PASSWORD), closeSoftKeyboard())

        // Try Sign Up first
        onView(withId(R.id.btnSignup)).perform(click())

        Thread.sleep(5000) // Allow Firebase + navigation

        val homeVisibleAfterSignup = try {
            onView(withId(R.id.btnFindFriends)).check(matches(isDisplayed()))
            true
        } catch (e: NoMatchingViewException) {
            false
        }

        if (!homeVisibleAfterSignup) {
            // Account probably exists → login instead
            onView(withId(R.id.btnLogin)).perform(click())
            Thread.sleep(5000) // Again wait for Firebase + navigation
            onView(withId(R.id.btnFindFriends)).check(matches(isDisplayed()))
        }
    }

}
