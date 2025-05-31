/*
 * Author: canetizen
 * Created on Sat May 31 2025
 * Description: Example Rest controller for load balancing.
 */

package com.github.canetizen;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/")
    public String home() {
        return "Hello from service running on port: " + port;
    }

    @GetMapping("/status")
    public String status() {
        return "UP";
    }

    @GetMapping("/api/hello")
    public String apiHello() {
        return "API Hello from service running on port: " + port;
    }

    @GetMapping("/auth/hello")
    public String authHello() {
        return "AUTH Hello from service running on port: " + port;
    }

    @GetMapping("/other/hello")
    public String otherHello() {
        return "OTHER Hello from service running on port: " + port;
    }
}
