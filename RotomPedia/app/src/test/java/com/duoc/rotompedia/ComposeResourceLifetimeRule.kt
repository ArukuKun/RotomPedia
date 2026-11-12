package com.duoc.rotompedia

import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

/** JUnit @After executes before Compose's rule cleanup. Repositories/loaders must instead
 * outlive that cleanup, including when an assertion or Compose teardown itself fails.
 * Setup can stay in @Before; only resource closing belongs to this outer rule.
 */
internal fun disposeComposeBeforeClosingResources(compose: TestRule, close: () -> Unit): TestRule =
    RuleChain.outerRule(object : ExternalResource() {
        override fun after() = close()
    }).around(compose)
