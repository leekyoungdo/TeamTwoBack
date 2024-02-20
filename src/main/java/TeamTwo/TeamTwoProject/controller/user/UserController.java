package TeamTwo.TeamTwoProject.controller.user;


import TeamTwo.TeamTwoProject.dto.user.UserDTO;
import TeamTwo.TeamTwoProject.dto.user.UserUpdateDTO;
import TeamTwo.TeamTwoProject.entity.user.UserEntity;
import TeamTwo.TeamTwoProject.security.TokenProvider;
import TeamTwo.TeamTwoProject.service.user.TokenBlacklistService;
import TeamTwo.TeamTwoProject.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.util.Optional;

//@Controller
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;
    @Autowired
    TokenProvider tokenProvider;
    @Autowired
    TokenBlacklistService tokenBlacklistService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserDTO userDTO) {
        try {
            UserEntity user = UserEntity.builder()
                    .userid(userDTO.getUserid())
                    .password(userDTO.getPassword())
                    .email(userDTO.getEmail())
                    .nickname(userDTO.getNickname())
                    .image(userDTO.getImage())
                    .build();

            UserEntity newUser = userService.signup(user);
            return ResponseEntity.ok().body("회원가입 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(HttpSession session, @RequestBody UserDTO userDTO) {
        try {
            UserEntity user = userService.login(userDTO.getUserid(), userDTO.getPassword());
            if (user == null) {
                throw new RuntimeException("로그인 실패");
            }
            String accessToken = tokenProvider.createAccessToken(user);
            String refreshToken = tokenProvider.createRefreshToken(user);


            UserDTO responseUserDTO = UserDTO.builder()
                    .id(user.getId())
                    .userid(user.getUserid())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
            return ResponseEntity.ok().body(responseUserDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value="Authorization") String token,
                                    @RequestHeader(value="Refresh-Token") String refreshToken) {
        try {
            // 토큰의 접두어(Bearer )를 제거합니다.
            String actualToken = token.replace("Bearer ", "");
            String actualRefreshToken = refreshToken.replace("Bearer ", "");

            // 토큰과 리프레시 토큰을 블랙리스트에 추가합니다.
            tokenBlacklistService.addToken(actualToken);
            tokenBlacklistService.addToken(actualRefreshToken);

            return ResponseEntity.ok().body("로그아웃 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 이미지 업로드
    @PostMapping("/image/{id}")
    public ResponseEntity<?> uploadImage(@PathVariable int id, @RequestParam("image") MultipartFile image) {
        try {
            userService.saveImage(id, image);
            return ResponseEntity.ok().body("이미지 업로드 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 회원 정보페이지 조회
    @GetMapping("/profile/{userid}")
    public ResponseEntity<UserEntity> getUser(@PathVariable("userid") String userid) {
        UserEntity user = userService.getUser(userid);
        if (user == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(user);
        }
    }


    // 회원 수정
    @PatchMapping("/profile/{userid}")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable("userid") String userid,
            @RequestBody UserUpdateDTO userUpdateDTO
            ) {
        try {
            UserEntity updateUser = userService.updateUser(userid, userUpdateDTO.getCurrentPassword(), userUpdateDTO.getNewPassword(), Optional.ofNullable(userUpdateDTO.getNewNickName()) );
            if (updateUser == null) {
                return ResponseEntity.badRequest().build();
            } else {
                System.out.println(updateUser);
                return ResponseEntity.ok(updateUser);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 회원 삭제
//    @DeleteMapping("delete/{userid}")
//    public ResponseEntity<String> deleteUser(
//            @PathVariable String userid,
//            @RequestBody String password
//    ) {
//        try {
//            userService.deleteUser(userid, password);
//            return ResponseEntity.ok("회원이 삭제되었스므니다.");
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//
//        }
//
//    }


}