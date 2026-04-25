import com.android.build.gradle.LibraryExtension
import com.congdanh.compositebuild.configureKotlinAndroid
import com.congdanh.compositebuild.libs
import com.congdanh.compositebuild.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("kotlin.android").pluginId)
            apply(libs.plugin("android.library").pluginId)
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = ProjectConfigure.TARGET_SDK
        }

        dependencies {
            add("testImplementation", libs.findLibrary("junit").get())
            add("testImplementation", libs.findLibrary("coroutines-test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            add("testImplementation", libs.findLibrary("mockk").get())
        }
    }
}
