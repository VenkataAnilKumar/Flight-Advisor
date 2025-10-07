package org.siriusxi.htec.fa.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/info")
@Slf4j
public class InfoController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping(value = "/version", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin
    public ResponseEntity<Map<String, Object>> version() {
        Map<String, Object> body = new HashMap<>();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        
        String version = (appVersion != null && !appVersion.isEmpty()) ? appVersion : "v1";
        log.debug("App version injected value: '{}', using: '{}'", appVersion, version);
        log.debug("App version is null: {}", appVersion == null);
        log.debug("App version is empty: {}", appVersion != null && appVersion.isEmpty());
        
        body.put("version", version);
        body.put("uptimeMillis", uptime);
        log.debug("Returning version info: {} (uptime: {} ms)", version, uptime);
        return ResponseEntity.ok(body);
    }
}
