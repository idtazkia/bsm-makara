package id.ac.tazkia.payment.bsm.makara.bsmmakara.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {
    @Autowired private GitProperties gitProperties;

    @GetMapping("/")
    public Map<String, String> home(){
        Map<String, String> info = new LinkedHashMap<>();
        info.put("name", "BSM Makara Gateway");
        info.put("version", gitProperties.getShortCommitId());
        info.put("release", gitProperties.get("closest.tag.name"));
        info.put("branch", gitProperties.getBranch());
        info.put("lastUpdate", gitProperties.getCommitTime().toString());
        return info;
    }
}
