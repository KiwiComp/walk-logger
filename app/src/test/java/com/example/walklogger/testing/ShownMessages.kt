package com.example.walklogger.testing

import com.example.walklogger.core.ui.message.UserMessages
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/** Collects what [userMessages] shows for the rest of the test, standing in for the app's snackbar. */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.shownMessages(userMessages: UserMessages): List<Int> {
    val shown = mutableListOf<Int>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { userMessages.messages.toList(shown) }
    return shown
}
