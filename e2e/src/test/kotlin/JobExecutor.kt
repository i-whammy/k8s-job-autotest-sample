import com.thoughtworks.gauge.Step
import java.io.File
import java.util.ResourceBundle

interface JobExecutor {
    fun execute()
}

class SkaffoldRunner: JobExecutor {
    private val skaffoldYamlDirectory = File("../environments")
    private val imageTag = ResourceBundle.getBundle("e2e").getString("imageTag")

    @Step("Run k8s job with skaffold")
    override fun execute() {
        val installer = ProcessBuilder(listOf("aqua", "i"))
        val remover = ProcessBuilder(listOf("kubectl", "delete", "jobs", "hello-world-timer-job", "--ignore-not-found"))
        val runner = ProcessBuilder(listOf("skaffold", "run", "--tag", imageTag))
        val processBuilders = listOf(
            installer, remover, runner
        )
        processBuilders.forEach {
            it.directory(skaffoldYamlDirectory)
        }
        runner.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        runner.redirectError(ProcessBuilder.Redirect.INHERIT)

        val pipelines = ProcessBuilder.startPipeline(processBuilders)
        val result = pipelines.last().waitFor()
        println("${runner.command().joinToString(" ")} finishes with status code $result")
        if (result > 0) throw RuntimeException("${runner.command().joinToString(" ")} with error.")
    }
}