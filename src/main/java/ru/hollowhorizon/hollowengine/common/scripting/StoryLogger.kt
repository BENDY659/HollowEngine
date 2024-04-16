/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hollowengine.common.scripting

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.appender.ConsoleAppender
import org.apache.logging.log4j.core.appender.FileAppender
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig
import org.apache.logging.log4j.core.layout.PatternLayout


object StoryLogger {
    val LOGGER = LogManager.getLogger("StoryLogger")

    init {
        val context = (LOGGER as org.apache.logging.log4j.core.Logger).context
        val config = context.configuration

        val logPattern = PatternLayout.newBuilder()
            .withPattern("%highlightForge{[%d{HH:mm:ss}] [%t/%level] [%c{2.}/%markerSimpleName]: %minecraftFormatting{%msg{nolookup}}%n%tEx}")
            .build()

        val filePattern = PatternLayout.newBuilder()
            .withPattern("[%d{HH:mm:ss}] [StoryLogger/%level]: %msg%n%throwable")
            .build()

        val fileAppender = FileAppender.newBuilder()
            .withFileName("logs/story-events.log")
            .withAppend(false)
            .setName("StoryLogger")
            .withImmediateFlush(true)
            .setIgnoreExceptions(false)
            .setConfiguration(config)
            .setLayout(filePattern)
            .build()

        val consoleAppender = ConsoleAppender.newBuilder()
            .setName("ConsoleAppender")
            .setLayout(logPattern)
            .build()

        config.addAppender(fileAppender.apply { start() })
        config.addAppender(consoleAppender.apply { start() })

        val loggerConfig = LoggerConfig.createLogger(
            false, Level.INFO, "StoryLogger", "true",
            arrayOf(ref("FileAppender"), ref("ConsoleAppender")), null, config, null
        )

        loggerConfig.addAppender(fileAppender, null, null)
        loggerConfig.addAppender(consoleAppender, null, null)
        config.addLogger("StoryLogger", loggerConfig)
        context.updateLoggers()
    }

    private fun ref(name: String): AppenderRef {
        return AppenderRef.createAppenderRef(name, null, null)
    }
}