package softuniBlog.controller;


import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import softuniBlog.bindingModel.UserBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.Role;
import softuniBlog.entity.User;
import softuniBlog.repository.RoleRepository;
import softuniBlog.repository.UserRepository;
import sun.misc.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Set;
import java.lang.String;

@Controller
public class UserController {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("view", "user/register");
        return "base-layout";
    }

    @PostMapping("/register")
 //   public String registerProcess(@RequestParam("email") String email,@RequestParam("fullName") String name, @RequestParam("password") String pass, @RequestParam("confirmPassword") String pass2, @RequestParam("profile_picture") MultipartFile file) throws IOException {
    public String registerProcess(UserBindingModel userBindingModel,@RequestParam("profile_picture") MultipartFile file ) throws IOException {

        //if(pass!=pass2) return "redirect:/register";

      /*  BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User user = new User(email, name, bCryptPasswordEncoder.encode(pass));
        if(file!=null){
            byte[] imageFile =file.getBytes() ;
            user.setProfilePicture(imageFile);
        }
*/
        if (!userBindingModel.getPassword().equals(userBindingModel.getConfirmPassword())) {
            return "redirect:/register";
        }
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        User user = new User(userBindingModel.getEmail(), userBindingModel.getFullName(), bCryptPasswordEncoder.encode(userBindingModel.getPassword()));

        Role userRole = this.roleRepository.findByName("ROLE_USER");

        user.addRole(userRole);
        this.userRepository.saveAndFlush(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("view", "user/login");
        return "base-layout";
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login?logout";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage(Model model) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());
        Set<Article> articles = user.getArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("user", user);
        model.addAttribute("view", "user/profile");

        return "base-layout";
    }
   /* public byte[] doPost(HttpServletRequest request, HttpServletResponse response)throws IOException, ServletException {
    Part filePart = request.getPart("pic");
    InputStream fileInputStream = filePart.getInputStream();
        byte[] buffer =new byte[500000];
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        int len;
        while((len= fileInputStream.read(buffer))!=-1)
        {os.write(buffer,0,len);}
        return os.toByteArray();*/
    }

