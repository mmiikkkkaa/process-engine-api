package dev.bpmcrafters.processengineapi.adapter.commons.task

import dev.bpmcrafters.processengineapi.task.TaskTerminationHandler

/**
 * Composite task termination handler.
 * @since 0.1.2
 * @param handlers handlers to include.
 */
class CompositeTaskTerminationHandler(
  private val handlers: MutableList<TaskTerminationHandler> = mutableListOf(),
) : TaskTerminationHandler {

  /**
   * Creates a new composite task termination handler out of current adding the given task termination handler.
   * @param handler handler to add.
   * @return composite handler.
   */
  fun withHandler(handler: TaskTerminationHandler): CompositeTaskTerminationHandler {
    return CompositeTaskTerminationHandler(handlers.plus(handler).toMutableList())
  }

  /**
   * Adds handler.
   * @param handler handler to add.
   */
  fun addHandler(handler: TaskTerminationHandler) {
    this.handlers.add(handler)
  }

  override fun accept(taskId: String) {
    handlers.forEach { it.accept(taskId) }
  }
}
