/**
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.morethan.jmhreport.gradle.task

import io.morethan.jmhreport.*
import io.morethan.jmhreport.gradle.JmhReportExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Task for generating a jmh-report from the default report location (configured through JmhReportExtension)
 **/
open class JmhReportTask : DefaultTask() {

    //TODO declare input as input
    @Suppress("UNCHECKED_CAST")
    @TaskAction
    fun generateReport() {
        val extension: JmhReportExtension = project.extensions.getByType(JmhReportExtension::class.java);
        val reportFile = File(extension.jmhResultPath)
        val outputFolder = File(extension.jmhReportOutput)

        check(reportFile.exists(), { "Input '${reportFile.canonicalFile}' does not exists!" })

        val jmhVisualizerZip: InputStream = this.javaClass.getResourceAsStream("/jmh-visualizer.zip");
        ZipInputStream(jmhVisualizerZip).use { zipStream ->
            zipStream.extract(outputFolder);
        };

        val runName = reportFile.nameWithoutExtension;
        val providedJsFile: File = File(outputFolder, "provided.js");
        providedJsFile.printWriter().use { writer ->
            writer.println("// provided.js - generated by gradle-jmh-report, " + (SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())));
            writer.println();
            writer.println("var providedBenchmarks = ['${runName}'];")
            writer.println();
            writer.println("var providedBenchmarkStore = {");
            writer.println("'${runName}': ");
            reportFile.forEachLine() { line -> writer.println(line) }
            writer.println("};");
        }
        println("JMH Report generated, please open: file://$outputFolder/index.html")
    }
}