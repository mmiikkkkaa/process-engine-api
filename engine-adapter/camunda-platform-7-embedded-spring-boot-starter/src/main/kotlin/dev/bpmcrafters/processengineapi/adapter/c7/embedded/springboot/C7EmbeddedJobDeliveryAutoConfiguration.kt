package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.ExternalServiceTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.UserTaskDeliveryStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.event.EventBasedEmbeddedUserTaskDeliveryBinding
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.UserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.event.EmbeddedEventBasedUserTaskDelivery
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryEnginePlugin
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.job.EmbeddedTaskDeliveryJobHandler
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import mu.KLogging
import org.camunda.bpm.engine.TaskService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration for delivery.
 */
@Configuration
@AutoConfigureAfter(C7EmbeddedAdapterAutoConfiguration::class)
@ConditionalOnProperty(prefix = DEFAULT_PREFIX, name = ["enabled"], havingValue = "true", matchIfMissing = true)
class C7EmbeddedJobDeliveryAutoConfiguration {

  companion object : KLogging()

  @Bean("c7embedded-task-delivery-job-handler")
  @ConditionalOnExpression(
    "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${$DEFAULT_PREFIX.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedTaskDeliveryJobHandler(
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): EmbeddedTaskDeliveryJobHandler {
    return EmbeddedTaskDeliveryJobHandler(
      subscriptionRepository = subscriptionRepository,
      workerId = c7AdapterProperties.serviceTasks.workerId,
      lockTimeInSeconds = c7AdapterProperties.serviceTasks.lockTimeInSeconds
    )
  }

  @Bean("c7embedded-task-delivery-engine-plugin")
  @ConditionalOnExpression(
    "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_job')"
      + " or "
      + "'\${$DEFAULT_PREFIX.user-tasks.delivery-strategy}'.equals('embedded_job')"
  )
  fun embeddedTaskDeliveryBinding(
    jobHandler: EmbeddedTaskDeliveryJobHandler,
    c7AdapterProperties: C7EmbeddedAdapterProperties
  ): EmbeddedTaskDeliveryEnginePlugin {
    return EmbeddedTaskDeliveryEnginePlugin(
      jobHandler = jobHandler,
      deliverServiceTasks = c7AdapterProperties.serviceTasks.deliveryStrategy == ExternalServiceTaskDeliveryStrategy.EMBEDDED_JOB,
      deliverUserTasks = c7AdapterProperties.userTasks.deliveryStrategy == UserTaskDeliveryStrategy.EMBEDDED_JOB,
    )
  }
}
