package dev.bpmcrafters.example.common.application.usecase;

import dev.bpmcrafters.example.common.application.port.in.SignalInPort;
import dev.bpmcrafters.processengineapi.correlation.Correlation;
import dev.bpmcrafters.processengineapi.correlation.SendSignalCmd;
import dev.bpmcrafters.processengineapi.correlation.SignalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
public class SignalUseCase implements SignalInPort {

  private final SignalApi signalApi;

  @Override
  public Future<Void> deliverSignal(String variableValue) {
    CompletableFuture<Void> completableFuture = new CompletableFuture<>();
    Executors.newCachedThreadPool().submit(() -> {
      try {
        signalApi.sendSignal(
          new SendSignalCmd(
            "signal1",
            () -> Map.of(
              "signal-delivered-value", variableValue
            ),
            Correlation.Companion::getEMPTY
          )
        ).get();
        completableFuture.complete(null); // FIXME -> chain instead of sync get
      } catch (Exception e) {
        completableFuture.completeExceptionally(e);
      }
    });
    return completableFuture;
  }

}
