package com.example.changeadvisor;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
class SessionController {
  @GetMapping("/")
  String home(Authentication user) {
    boolean developer =
        user.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals("ROLE_DEVELOPER"));
    return developer ? "redirect:/developer.html" : "forward:/index.html";
  }

  @GetMapping("/api/csrf")
  @ResponseBody
  CsrfToken csrf(CsrfToken token) {
    return token;
  }
}
