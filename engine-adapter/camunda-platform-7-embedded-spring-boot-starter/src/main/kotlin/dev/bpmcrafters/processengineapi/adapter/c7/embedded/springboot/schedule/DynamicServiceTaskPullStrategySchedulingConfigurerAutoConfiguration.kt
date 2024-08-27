package dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.schedule

import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.springboot.C7EmbeddedAdapterProperties.Companion.DEFAULT_PREFIX
import dev.bpmcrafters.processengineapi.adapter.c7.embedded.task.delivery.pull.EmbeddedPullServiceTaskDelivery
import mu.KLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Dynamic / imperative scheduling configuration using own task scheduler for service tasks.
 */
@EnableScheduling
@Configuration
@ConditionalOnExpression(
  "'\${$DEFAULT_PREFIX.enabled}'.equals('true')"
    + " and "
    + "'\${$DEFAULT_PREFIX.service-tasks.delivery-strategy}'.equals('embedded_scheduled')"
)
@AutoConfigureAfter(C7SchedulingAutoConfiguration::class)
class DynamicServiceTaskPullStrategySchedulingConfigurerAutoConfiguration(
  private val embeddedPullServiceTaskDelivery: EmbeddedPullServiceTaskDelivery,
  private val c7EmbeddedAdapterProperties: C7EmbeddedAdapterProperties,
  @Qualifier("c7embedded-task-scheduler")
  private val c7taskScheduler: TaskScheduler
) : SchedulingConfigurer {

  companion object : KLogging()

  override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
    taskRegistrar.setScheduler(c7taskScheduler)
    taskRegistrar.addFixedRateTask(
      {
        logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-105: Delivering external tasks..." }
        embeddedPullServiceTaskDelivery.refresh()
        logger.trace { "PROCESS-ENGINE-C7-EMBEDDED-106: Delivered external tasks." }
      },
      Duration.of(c7EmbeddedAdapterProperties.serviceTasks.scheduleDeliveryFixedRateInSeconds, ChronoUnit.SECONDS)
    )
  }

}
