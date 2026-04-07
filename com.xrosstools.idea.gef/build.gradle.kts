plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.10.2"
}

group = "com.xrosstools"
version = "1.9.2"

val sandbox  : String by project

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaUltimate("2025.3.3")
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    instrumentCode = true
    buildSearchableOptions = false

    pluginConfiguration {
        name = "Xross Tools Graphic Edit Framework"

        ideaVersion {
            sinceBuild = "183.6156.11"
        }

        changeNotes = """
            <em>1.9.2</em> Optimize AbstractCodeGenerator and fix model reference.<br>
            <em>1.9.1</em> Fix new model dialog display bug when extension is not available.<br>
            <em>1.9.0</em> Support stream mode for AI conversation.<br>
            <em>1.8.0</em> Support template code generator and optimize package selecting.<br>
            <em>1.7.0</em> Support export PNG and search.<br>
            <em>1.6.0</em> Support create model extension.<br>
            <em>1.5.0</em> Support toolbar extension.<br>
            <em>1.4.1</em> Fix rename property bug.<br>
            <em>1.4.0</em> Support rhombus figure, reference utility.<br>
            <em>1.3.0</em> Support OrderedContainerEditPolicy and optimize tree part creation and GenerateFactoryAction.<br>
            <em>1.2.3</em> Optimize undo/redo, toolbar layout, insertion feedback, connection selection feedback and connection refresh.<br>
            <em>1.2.2</em> Fix NPE in palette, optimize assign/change/open implementation and code display.<br>
            <em>1.2.0</em> Support common model, action, command, parts, properties, policies and reference.<br>
            <em>1.1.2</em> Fix NPE for DiagramEditor.selectNotify.<br>
            <em>1.1.1</em> Provide default constructor for ContextMenuProvider.<br>
            <em>1.1.0</em> Support undo/redo.<br>
            <em>1.0.12</em> Fix visual inconsistency between toolbar and main window, and minor optimization when comparing file extension.<br>
            <em>1.0.11</em> optimize interface to support backward compatibility.<br>
            <em>1.0.10</em> Fix load resource file bug and UI interaction logic.<br>
            <em>1.0.9</em> Fix name issue.<br>
            <em>1.0.8</em> Remove dom4j dependency.<br>
            <em>1.0.7</em> Explicitly depends on certain modules.<br>
            <em>1.0.6</em> Add isConnectToSameNode to Connection.<br>
            <em>1.0.5</em> Fix refresh edit part children<br>
            <em>1.0.4</em> Validate xml node before get its children<br>
            <em>1.0.3</em> Fix set color for Label<br>
            <em>1.0.2</em> Fix NPE when selecting non visual element<br>
            <em>1.0.1</em> Fix empty popup menu and duplicate separator<br>
            <em>1.0</em> Inital version
        """.trimIndent()
    }

    pluginVerification {
        ides {
            // 验证最老和最新的目标版本
            ide("IC-2018.3.6")
            ide("IC-2020.3.4")
            ide("IC-2025.3.3")
        }
    }
}

intellijPlatformTesting {
    runIde {
        register("runWithLocalPlugins") {
            plugins {
                val pluginFiles = file(sandbox).listFiles()
                pluginFiles.forEach { file ->
                    if (!file.name.contains(project.name)) {
                        localPlugin(file.absolutePath)
                    }
                }
            }
        }
    }
}

tasks.named("runIde") {
    dependsOn("runWithLocalPlugins")
}

tasks {
    buildPlugin {
        archiveFileName.set("${project.name}.zip")
    }
}