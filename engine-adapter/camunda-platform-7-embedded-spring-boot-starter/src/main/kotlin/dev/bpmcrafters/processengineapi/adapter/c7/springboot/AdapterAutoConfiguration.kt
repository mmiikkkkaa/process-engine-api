package dev.bpmcrafters.processengineapi.adapter.c7.springboot

import dev.bpmcrafters.processengineapi.adapter.c7.correlation.CorrelationApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.correlation.SignalApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.process.StartProcessApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.task.C7TaskApiImpl
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.ExternalTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.c7.task.completion.UserTaskCompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.CompletionStrategy
import dev.bpmcrafters.processengineapi.adapter.commons.task.InMemSubscriptionRepository
import dev.bpmcrafters.processengineapi.adapter.commons.task.SubscriptionRepository
import dev.bpmcrafters.processengineapi.correlation.CorrelationApi
import dev.bpmcrafters.processengineapi.correlation.SignalApi
import dev.bpmcrafters.processengineapi.process.StartProcessApi
import dev.bpmcrafters.processengineapi.task.TaskApi
import org.camunda.bpm.engine.ExternalTaskService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@EnableConfigurationProperties(value = [C7AdapterProperties::class])
class AdapterAutoConfiguration {

  @Bean
  fun startProcessApi(runtimeService: RuntimeService): StartProcessApi = StartProcessApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun taskApi(subscriptionRepository: SubscriptionRepository, completionStrategies: List<CompletionStrategy>): TaskApi = C7TaskApiImpl(
    completionStrategies = completionStrategies,
    subscriptionRepository = subscriptionRepository
  )

  @Bean
  fun correlationApi(runtimeService: RuntimeService): CorrelationApi = CorrelationApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun signalApi(runtimeService: RuntimeService): SignalApi = SignalApiImpl(
    runtimeService = runtimeService
  )

  @Bean
  fun subscriptionRepository(): SubscriptionRepository = InMemSubscriptionRepository()

  @Bean
  fun externalTaskCompletionStrategy(
    externalTaskService: ExternalTaskService,
    subscriptionRepository: SubscriptionRepository,
    c7AdapterProperties: C7AdapterProperties
  ): CompletionStrategy =
    ExternalTaskCompletionStrategy(
      workerId = c7AdapterProperties.externalServiceTasks.workerId,
      externalTaskService = externalTaskService,
      subscriptionRepository = subscriptionRepository
    )

  @Bean
  fun userTaskCompletionStrategy(
    taskService: TaskService,
    subscriptionRepository: SubscriptionRepository
  ): CompletionStrategy =
    UserTaskCompletionStrategy(
      taskService = taskService,
      subscriptionRepository = subscriptionRepository
    )
}
