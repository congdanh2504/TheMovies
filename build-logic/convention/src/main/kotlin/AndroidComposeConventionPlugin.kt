import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.congdanh.compositebuild.get
import com.congdanh.compositebuild.implementation
import com.congdanh.compositebuild.libs
import com.congdanh.compositebuild.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) = target.run {
        pluginManager.run {
            apply(libs.plugin("kotlin.compose").pluginId)
        }

        when {
            pluginManager.hasPlugin("com.android.application") -> {
                target.extensions.getByType<ApplicationExtension>()
                    .apply {
                        buildFeatures.compose = true
                    }
            }

            pluginManager.hasPlugin("com.android.library") -> {
                target.extensions.getByType<LibraryExtension>().apply {
                    buildFeatures.compose = true
                }
            }
        }

        dependencies {
            implementation(platform(libs["androidx.compose.bom"]))
            implementation(libs["androidx.activity.compose"])
            implementation(libs["androidx.ui.tooling.preview"])
            implementation(libs["androidx.ui"])
            implementation(libs["androidx.ui.tooling"])
            implementation(libs["androidx.ui.graphics"])
            implementation(libs["androidx.material3"])
        }
    }
}
