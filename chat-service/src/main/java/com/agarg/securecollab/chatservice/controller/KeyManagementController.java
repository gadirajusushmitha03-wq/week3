package com.agarg.securecollab.chatservice.controller;

import com.agarg.securecollab.chatservice.entity.KeyBundleEntity;
import com.agarg.securecollab.chatservice.service.KeyManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/keys")
@CrossOrigin(origins = "*")
public class KeyManagementController {

    private final KeyManagementService keyService;

    public KeyManagementController(KeyManagementService keyService) {
        this.keyService = keyService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String deviceId = body.get("deviceId");
        String identityKey = body.get("identityKey");
        String preKey = body.get("preKey");
        String signedPreKey = body.get("signedPreKey");

        if (userId == null || deviceId == null || identityKey == null) {
            return ResponseEntity.badRequest().body("userId, deviceId and identityKey are required");
        }

        KeyBundleEntity entity = keyService.registerKeyBundle(userId, deviceId, identityKey, preKey, signedPreKey);
        return ResponseEntity.ok(Map.of(
            "userId", entity.getUserId(),
            "deviceId", entity.getDeviceId(),
            "id", entity.getId(),
            "createdAt", entity.getCreatedAt()
        ));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getKeys(@PathVariable String userId) {
        List<KeyBundleEntity> bundles = keyService.getKeyBundlesForUser(userId);
        List<Map<String, Object>> out = new ArrayList<>();
        for (KeyBundleEntity b : bundles) {
            out.add(Map.of(
                "deviceId", b.getDeviceId(),
                "identityKey", b.getIdentityKey(),
                "preKey", b.getPreKey(),
                "signedPreKey", b.getSignedPreKey(),
                "createdAt", b.getCreatedAt()
            ));
        }
        return ResponseEntity.ok(out);
    }

    @DeleteMapping("/{userId}/{deviceId}")
    public ResponseEntity<?> removeKey(@PathVariable String userId, @PathVariable String deviceId) {
        keyService.removeKeyBundle(userId, deviceId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }
}
