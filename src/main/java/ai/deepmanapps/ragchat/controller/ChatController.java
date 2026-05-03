package ai.deepmanapps.ragchat.controller;

import ai.deepmanapps.ragchat.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @PostMapping("/chat")
    public String chat(@RequestParam("message") String message, Model model) {
        try {
            String response = chatService.chat(message);
            model.addAttribute("message", message);
            model.addAttribute("response", response);
        } catch (Exception e) {
            model.addAttribute("message", message);
            model.addAttribute("response", "Error: " + e.getMessage() + (e.getCause() != null ? " Caused by: " + e.getCause().getMessage() : ""));
            e.printStackTrace();
        }
        return "index :: chat-message";
    }

    @PostMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return "<div class='text-red-500'>Please select a file to upload.</div>";
        }

        try {
            chatService.processFile(file);
            return "<div class='text-green-500'>File uploaded and processed successfully!</div>";
        } catch (Exception e) {
            e.printStackTrace();
            return "<div class='text-red-500'>Failed to process file: " + e.getMessage() + "</div>";
        }
    }
}
