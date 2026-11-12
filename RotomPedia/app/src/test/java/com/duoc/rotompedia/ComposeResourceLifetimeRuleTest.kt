package com.duoc.rotompedia

import org.junit.Assert.*
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.runner.Description
import org.junit.runners.model.Statement

class ComposeResourceLifetimeRuleTest {
    private fun exercise(fail: Boolean): List<String> {
        val events = mutableListOf<String>()
        val compose = object : ExternalResource() {
            override fun before() { events += "compose setup" }
            override fun after() { events += "compose dispose" }
        }
        val rule = disposeComposeBeforeClosingResources(compose) { events += "resource close" }
        val test = object : Statement() {
            override fun evaluate() { events += "test"; if (fail) throw AssertionError("fixture assertion") }
        }
        try { rule.apply(test, Description.EMPTY).evaluate(); assertFalse(fail) }
        catch (e: AssertionError) { assertTrue(fail); assertEquals("fixture assertion", e.message) }
        return events
    }
    @Test fun `resources outlive composition on success`() {
        assertEquals(listOf("compose setup", "test", "compose dispose", "resource close"), exercise(false))
    }
    @Test fun `resources outlive composition on assertion failure too`() {
        assertEquals(listOf("compose setup", "test", "compose dispose", "resource close"), exercise(true))
    }
}
