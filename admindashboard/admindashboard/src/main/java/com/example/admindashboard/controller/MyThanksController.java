package com.example.admindashboard.controller;

import com.example.admindashboard.model.ThanksTransaction;
import com.example.admindashboard.model.ThanksWallet;
import com.example.admindashboard.model.User;
import com.example.admindashboard.repository.UserRepository;
import com.example.admindashboard.service.ThanksService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class MyThanksController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThanksService thanksService;

    /* ─── HELPER: session guard ─────────────────────────────────── */
    private boolean isThanksAuthenticated(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("thanksAuthenticated"));
    }

    private User getAuthenticatedUser(HttpSession session, Principal principal) {
        String username = (String) session.getAttribute("thanksUser");
        if (username == null && principal != null) {
            username = principal.getName();
        }
        if (username != null) {
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    /* ─── MY-THANKS LOGIN & LOGOUT ──────────────────────────────── */

    @GetMapping("/thanks-login")
    public String showThanksLogin(HttpSession session) {
        if (isThanksAuthenticated(session)) return "redirect:/thanks-dashboard";
        return "mythanks/thanks-login";
    }

    @PostMapping("/thanks-login")
    public String thanksLoginSubmit(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password,
            HttpSession session,
            Model model,
            Principal principal) {

        String usernameToCheck = (loginId != null && !loginId.trim().isEmpty())
                ? loginId.trim() : (principal != null ? principal.getName() : "");

        Optional<User> userOpt = userRepository.findByUsername(usernameToCheck);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String dbPassword = user.getPassword() != null ? user.getPassword().replace("{noop}", "") : "";
            if (dbPassword.equals(password)) {
                session.setAttribute("thanksAuthenticated", true);
                session.setAttribute("thanksUser", usernameToCheck);
                return "redirect:/thanks-dashboard";
            }
        }
        model.addAttribute("error", "Invalid Employee ID or password. Please try again.");
        return "mythanks/thanks-login";
    }

    @GetMapping("/thanks/logout")
    public String thanksLogout(HttpSession session) {
        session.removeAttribute("thanksAuthenticated");
        session.removeAttribute("thanksUser");
        return "redirect:/employee/dashboard";
    }

    /* ─── MY-THANKS DASHBOARD ───────────────────────────────────── */
    @GetMapping("/thanks-dashboard")
    public String showThanksDashboard(HttpSession session, Model model, Principal principal) {
        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getEmployeeProfile());
            model.addAttribute("wallet", thanksService.getOrCreateWallet(user));
            model.addAttribute("transactions", thanksService.getTransactionHistory(user));
        }
        return "mythanks/thanks-dashboard";
    }

    /* ─── MY-THANKS TRANSACTION HISTORY ─────────────────────────── */
    @GetMapping("/thanks-transactions")
    public String showThanksTransactions(HttpSession session, Model model, Principal principal) {
        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getEmployeeProfile());
            model.addAttribute("wallet", thanksService.getOrCreateWallet(user));
            model.addAttribute("transactions", thanksService.getTransactionHistory(user));
        }
        return "mythanks/thanks-transactions";
    }

    /* ─── MY-THANKS SEND THANKS ─────────────────────────────────── */
    @GetMapping("/thanks-send")
    public String showThanksSend(HttpSession session, Model model, Principal principal) {
        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getEmployeeProfile());
            model.addAttribute("wallet", thanksService.getOrCreateWallet(user));
        }
        return "mythanks/thanks-send";
    }

    @PostMapping("/thanks-send")
    public String processSendThanks(
            @RequestParam("receiverUsername") String receiverUsername,
            @RequestParam("category") String category,
            @RequestParam("points") Integer points,
            @RequestParam("message") String message,
            HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {

        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User sender = getAuthenticatedUser(session, principal);
        Optional<User> receiverOpt = userRepository.findByUsername(receiverUsername);

        if (sender != null && receiverOpt.isPresent()) {
            try {
                thanksService.sendThanks(sender, receiverOpt.get(), points, category, message);
                redirectAttributes.addFlashAttribute("successMsg", "Appreciation sent successfully!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Could not find the specified employee.");
        }
        return "redirect:/thanks-send";
    }

    /* ─── MY-THANKS REDEMPTION STORE ────────────────────────────── */
    @GetMapping("/thanks-store")
    public String showThanksStore(HttpSession session, Model model, Principal principal) {
        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getEmployeeProfile());
            model.addAttribute("wallet", thanksService.getOrCreateWallet(user));
        }
        return "mythanks/thanks-store";
    }

    @PostMapping("/thanks-store/redeem")
    public String processRedemption(
            @RequestParam("itemName") String itemName,
            @RequestParam("points") Integer points,
            @RequestParam("productType") String productType,
            HttpSession session, Principal principal, RedirectAttributes redirectAttributes) {

        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            try {
                thanksService.redeemItem(user, itemName, points, productType);
                redirectAttributes.addFlashAttribute("successMsg", "Redemption Successful!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            }
        }
        return "redirect:/thanks-store";
    }

    /* ─── MY-THANKS FAQS ────────────────────────────────────────── */
    @GetMapping("/thanks-faqs")
    public String showThanksFaqs(HttpSession session, Model model, Principal principal) {
        if (!isThanksAuthenticated(session)) return "redirect:/thanks-login";

        User user = getAuthenticatedUser(session, principal);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("profile", user.getEmployeeProfile());
            model.addAttribute("wallet", thanksService.getOrCreateWallet(user));
        }
        return "mythanks/thanks-faqs";
    }
}