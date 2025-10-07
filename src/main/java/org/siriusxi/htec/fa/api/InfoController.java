package org.siriusxi.htec.fa.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${app.version}/info")
@Slf4j
public class InfoController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/version")
    public ResponseEntity<Map<String, Object>> version() {
        Map<String, Object> body = new HashMap<>();
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        body.put("version", appVersion);
        body.put("uptimeMillis", uptime);
        log.debug("Returning version info: {} (uptime: {} ms)", appVersion, uptime);
        return ResponseEntity.ok(body);
    }
}
