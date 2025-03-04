package com.sesac.carematching.user;

import com.sesac.carematching.user.dto.UserSignupDTO;
import com.sesac.carematching.user.dto.UserUpdateDTO;
import com.sesac.carematching.user.dto.UsernameDTO;
import com.sesac.carematching.util.TokenAuth;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final TokenAuth tokenAuth;

    @PostMapping("/signup")
    public ResponseEntity<Void> join(@RequestBody UserSignupDTO user) {
        System.out.println("회원가입 컨트롤러 실행" + user);
        userService.registerUser(user);
        System.out.println("회원가입 완료");
        return ResponseEntity.ok().build();
    }

    private void checkAdminPrivileges(HttpServletRequest request) {
        User requestedUser = userService.getUserInfo(tokenAuth.extractUsernameFromToken(request));
        if (requestedUser == null || !requestedUser.getRole().getRname().equals("ROLE_ADMIN")) {
            throw new IllegalArgumentException("관리자 전용 기능입니다.");
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        try {
            String username = tokenAuth.extractUsernameFromToken(request);
            userService.deleteUser(username);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request) {
        try {
            String username = tokenAuth.extractUsernameFromToken(request);
            userService.updateUser(username, userUpdateDTO);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("회원 정보 수정 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert")
    public ResponseEntity<?> createAdminCert(HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.getCertList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("목록을 불러오는 도중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert/approve")
    public ResponseEntity<?> createAdminCertApprove(@RequestBody UsernameDTO usernameDTO, HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.updatePending(usernameDTO, false));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("처리하는 도중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/admin/cert/revoke")
    public ResponseEntity<?> createAdminCertRevoke(@RequestBody UsernameDTO usernameDTO, HttpServletRequest request) {
        try {
            checkAdminPrivileges(request);
            return ResponseEntity.ok(userService.updatePending(usernameDTO, true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("처리하는 도중 오류가 발생했습니다.");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
