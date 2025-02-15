package dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.pull

import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.remote.task.delivery.toTaskInformation
import dev.bpmcrafters.processengineapi.adapter.commons.task.RefreshableDelivery
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.TaskSubscriptionHandle
import dev.bpmcrafters.processengineapi.adapter.commons.task.filterBySubscription
import dev.bpmcrafters.processengineapi.task.TaskType
import mu.KLogging
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.task.TaskQuery
import java.util.concurrent.ExecutorService

/**
 * Delivers user tasks to subscriptions.
 * Uses internal Java API for pulling tasks.
 */
class RemotePullUserTaskDelivery(
  private val taskService: TaskService,
  private val repositoryService: RepositoryService,
  private val subscriptionRepository: SubscriptionRepository,
  private val executorService: ExecutorService
) : UserTaskDelivery, RefreshableDelivery {

  companion object : KLogging()


  /**
   * Delivers all tasks found in user task service to corresponding subscriptions.
   */
  override fun refresh() {
    val subscriptions = subscriptionRepository.getTaskSubscriptions()
    if(subscriptions.isNotEmpty()) {
      logger.trace { "PROCESS-ENGINE-C7-REMOTE-036: pulling user tasks for subscriptions: $subscriptions" }
      taskService
        .createTaskQuery()
        .forSubscriptions(subscriptions)
        .list()
        .parallelStream()
        .forEach { task ->
          subscriptions
            .firstOrNull { subscription -> subscription.matches(task) }
            ?.let { activeSubscription ->
              executorService.submit {  // in another thread
                subscriptionRepository.activateSubscriptionForTask(task.id, activeSubscription)
                val variables = taskService.getVariables(task.id).filterBySubscription(activeSubscription)
                try {
                  logger.debug { "PROCESS-ENGINE-C7-REMOTE-037: delivering user task ${task.id}." }
                  activeSubscription.action.accept(task.toTaskInformation(), variables)
                } catch (e: Exception) {
                  logger.error { "PROCESS-ENGINE-C7-REMOTE-038: error delivering task ${task.id}: ${e.message}" }
                  subscriptionRepository.deactivateSubscriptionForTask(taskId = task.id)
                }
              }.get()
            }
        }
    } else {
      logger.trace { "PROCESS-ENGINE-C7-REMOTE-039: pull user tasks disabled because of no active subscriptions" }
    }
  }

  @Suppress("UNUSED_PARAMETER")
  private fun TaskQuery.forSubscriptions(subscriptions: List<TaskSubscriptionHandle>): TaskQuery {
    // FIXME: narrow down, for the moment take all tasks
    return this
      .active()
  }


  private fun TaskSubscriptionHandle.matches(task: Task): Boolean =
    this.taskType == TaskType.USER && (
      this.taskDescriptionKey == null
        || this.taskDescriptionKey == task.taskDefinitionKey
        || this.taskDescriptionKey == task.id
      )
}

