package com.mussonindustrial.ignition.embr.periscope.component

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

class ProjectComponentDelegateScriptCacheTest :
    DescribeSpec(
        {
            describe("ProjectComponentDelegateScriptCache") {
                it("compiles a script once until it is cleared") {
                    val compiles = AtomicInteger()
                    val cache = ProjectComponentDelegateScriptCache {
                        compiles.incrementAndGet()
                        null
                    }

                    cache.find("a")
                    cache.find("a")
                    compiles.get() shouldBe 1

                    cache.clear()
                    cache.find("a")
                    compiles.get() shouldBe 2
                }

                it("lets a project change through while a session waits to compile") {
                    // Compiling reads the project's resources, which a project change holds locked
                    // while it clears the cache and looks scripts up again.
                    val resources = ReentrantLock()
                    val compiling = CountDownLatch(1)
                    val cache = ProjectComponentDelegateScriptCache {
                        compiling.countDown()
                        resources.withLock { null }
                    }

                    val changed = CountDownLatch(1)
                    val projectChange =
                        thread(isDaemon = true) {
                            resources.withLock {
                                compiling.await()
                                cache.clear()
                                cache.find("a")
                            }
                            changed.countDown()
                        }
                    val session = thread(isDaemon = true) { cache.find("a") }

                    changed.await(5, TimeUnit.SECONDS) shouldBe true
                    projectChange.join(5000)
                    session.join(5000)
                    session.isAlive shouldBe false
                }
            }
        },
    )
