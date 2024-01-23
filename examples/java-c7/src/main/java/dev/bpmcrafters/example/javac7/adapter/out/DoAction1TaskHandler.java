package dev.bpmcrafters.example.javac7.adapter.out;

import dev.bpmcrafters.example.javac7.infrastructure.AbstractSynchronousTaskHandler;
import dev.bpmcrafters.example.javac7.infrastructure.TaskHandlerException;
import dev.bpmcrafters.processengineapi.task.TaskApi;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DoAction1TaskHandler extends AbstractSynchronousTaskHandler {
  public DoAction1TaskHandler(TaskApi taskApi) {
    super(taskApi, "action1");
  }

  @Override
  public Map<String, Object> execute(String taskId, Map<String, ?> variables) throws TaskHandlerException {
    log.info("Executing action1 task handler");
    val map = new HashMap<String, Object>(variables);
    map.put("action1", "value1");
    return map;
  }
}
