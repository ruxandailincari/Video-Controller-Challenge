package org.example.prototype;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootApplication
@RestController
public class PrototypeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrototypeApplication.class, args);
    }

    private enum State {IDLE, PLAYING, ERROR};

    private final AtomicReference<State> currentState = new AtomicReference<>(State.IDLE);
    private final AtomicReference<String> currentScenario = new AtomicReference<>("");
    private final String VIDEO_DIRECTORY = "./scenarios/";

    @PostMapping("/play")
    public Map<String, String> play(@RequestBody Map<String, String> request){
        Map<String, String> response = new HashMap<>();

        if(request.isEmpty() || request.get("scenario") == null){
            response.put("error", "Missing scenario");
            return response;
        }

        String scenario = request.get("scenario");
        File file = new File(VIDEO_DIRECTORY + scenario);

        if(!file.exists()){
            currentState.set(State.ERROR);
            response.put("error", "File not found");
            return response;
        }

        stopCurrentVideo();

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if(os.contains("win")){
                ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", file.getAbsolutePath());
                pb.start();
            } else if(os.contains("mac")) {
                ProcessBuilder pb = new ProcessBuilder("open", file.getAbsolutePath());
                pb.start();
            }

            currentScenario.set(scenario);
            currentState.set(State.PLAYING);

            response.put("status", "started");
            response.put("scenario", scenario);
            return response;
        } catch (IOException e) {
            currentState.set(State.ERROR);
            response.put("error", "Failed playing video");
            return response;
        }
    }

    @GetMapping("/status")
    public Map<String, String> getStatus(){
        Map<String, String> response = new HashMap<>();
        response.put("state", currentState.get().toString());
        response.put("scenario", currentScenario.get());
        return response;
    }

    @PostMapping("/stop")
    public Map<String, String> stop(){
        stopCurrentVideo();
        Map<String, String> response = new HashMap<>();
        response.put("status", "stopped");
        return response;
    }

    private void stopCurrentVideo(){
        currentState.set(State.IDLE);
        currentScenario.set("");
    }

}
