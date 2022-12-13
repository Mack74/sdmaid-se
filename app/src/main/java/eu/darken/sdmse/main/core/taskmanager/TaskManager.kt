package eu.darken.sdmse.main.core.taskmanager

import eu.darken.sdmse.common.coroutine.AppScope
import eu.darken.sdmse.common.coroutine.DispatcherProvider
import eu.darken.sdmse.common.debug.logging.log
import eu.darken.sdmse.common.debug.logging.logTag
import eu.darken.sdmse.common.sharedresource.HasSharedResource
import eu.darken.sdmse.common.sharedresource.SharedResource
import eu.darken.sdmse.main.core.SDMTool
import eu.darken.sdmse.stats.StatsRepo
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskManager @Inject constructor(
    @AppScope private val appScope: CoroutineScope,
    private val dispatcherProvider: DispatcherProvider,
    private val tools: Set<@JvmSuppressWildcards SDMTool>,
    private val statsRepo: StatsRepo,
) : HasSharedResource<Any> {

    override val sharedResource = SharedResource.createKeepAlive(TAG, appScope)

    suspend fun submit(task: SDMTool.Task): SDMTool.Task.Result = useSharedResource {
        log(TAG) { "submit(task=$task)..." }
        val start = System.currentTimeMillis()

        val tool = tools.single { it.type == task.type }
        tool.addParent(this@TaskManager)
        val result = tool.submit(task)

        val stop = System.currentTimeMillis()
        log(TAG) { "submit(task=$task) after ${stop - start}ms: $result" }
        result
    }

    companion object {
        private val TAG = logTag("TaskManager")
    }
}