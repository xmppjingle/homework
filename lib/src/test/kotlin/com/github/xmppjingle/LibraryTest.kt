/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.github.xmppjingle

import java.lang.Math.log
import java.lang.Math.log10
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class LibraryTest {
    @Test fun someLibraryMethodReturnsTrue() {
        val classUnderTest = Homework()
        assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'")


        for(i in 0 .. 1439){

            println("${Math.cos(i*(Math.PI/720))*100} , ${Math.sin(i*(Math.PI/720))*100}")

        }

    }
}