package net.devtopia.demo.security;

import net.devtopia.demo.exception.NotExistingUserException;
import net.devtopia.demo.exception.WrongPasswordException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * 1. 테스트 클래스 만들기
 * 2. 객체 생성하기(쉬운)
 * 3. ID 값이 비정상인 경우(쉬운, 정상에서 벗어난)
 * 4. PW 값이 비정상인 경우(쉬운, 정상에서 벗어난)
 * 5. User가 존재하지 않는 경우(정상에서 벗어난)
 * 6. ID에 해당하는 User가 존재하는데, PW가 일치하지 않는 경우(정상에서 벗어난)
 * 7. ID와 PW가 일치하는 경우(정상)
 *    - 인증 정보를 리턴
 *
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthServiceTest20 {

    public static final String USER_PASSWORD = "userPassword";
    public static final String USER_ID = "userId";
    public static final String NO_USER_ID = "noUserId";
    public static final String USER_WRONG_PASSWORD = "userWrongPassword";

    private AuthService authService;
    private UserRepository mockUserRepository;

    @Before
    public void setUp() {
        mockUserRepository = mock(UserRepository.class);
        // 중복 코드 제거
        authService = new AuthService();

        authService.setUserRepository(mockUserRepository);
    }

    @Test
    public void canCreate() {
        // 1. 코드 작성
    }

    @Test
    // 3. ID 값이 비정상인 경우(쉬운, 정상에서 벗어난)
    // 4. PW 값이 비정상인 경우(쉬운, 정상에서 벗어난)
    public void givenInvalidId_throwIllegalArgEx() {
        // 3. 예외처리 로직

        // 중복되는 코드는 메소드 추출을 하게 되는데 다른 부분을 로컯 변수로 만들고 추출하면 됨
        // "userPassword" 은 매직 넘버이므로 상수로 교체 가능

        // 아이디 오류 로직
        assertIllegalArgExThrown(null, USER_PASSWORD);
        assertIllegalArgExThrown("", USER_PASSWORD);

        // 패스워드 오류 로직
        assertIllegalArgExThrown(USER_ID, null);
        assertIllegalArgExThrown(USER_ID, "");
    }

    private void assertIllegalArgExThrown(String id, String password) {
        assertExceptionthrown(id, password, IllegalArgumentException.class);
    }

    @Test
    // 5. User가 존재하지 않는 경우(정상에서 벗어난)
    public void whenUserNotFound_throwNotExistingUserEx() {
        // 삼각측량: 다른 값으로 테스트를 여러게 수행
        assertExceptionthrown(NO_USER_ID, USER_PASSWORD, NotExistingUserException.class);
        assertExceptionthrown(NO_USER_ID + 2, USER_PASSWORD, NotExistingUserException.class);

        for (int i = 1; i <= 100; i++) {
            assertExceptionthrown(NO_USER_ID + i, USER_PASSWORD, NotExistingUserException.class);
        }
    }

    private void assertExceptionthrown(String id, String password, Class<? extends Exception> type) {
        Exception thrownEx = null;

        try {
            authService.authenticate(id, password);
        } catch (Exception e) {
            //e.printStackTrace();

            thrownEx = e;
        }

        assertThat(thrownEx).isInstanceOf(type);
    }

    @Test
    // 6. ID에 해당하는 User가 존재하는데, PW가 일치하지 않는 경우(정상에서 벗어난)
    public void whenUserFoundButWrongPw_throwWrongPasswordEx() {
        // ID와 PW가 일치하는 경우
        givenUserExists(USER_ID, USER_PASSWORD);

        // ID에 해당하는 User가 존재하는데, PW가 일치하지 않는 경우
        assertExceptionthrown(USER_ID, USER_WRONG_PASSWORD, WrongPasswordException.class);

        // 실제로 ID가 존재하는지 유무
        verifyUserFound(USER_ID);
    }

    private void verifyUserFound(String id) {
        verify(mockUserRepository).findById(id);
    }

    private void givenUserExists(String id, String password) {
        when(mockUserRepository.findById(id)).thenReturn(new User(id, password));
    }

    @Test
    public void whenUserFoundAndRightPw_returnAuth() {
        givenUserExists(USER_ID, USER_PASSWORD);
        Authentication auth = authService.authenticate(USER_ID, USER_PASSWORD);
        assertThat(auth.getId()).isEqualTo(USER_ID);
    }

    // 2. 객체 생성
    private class AuthService {
        private UserRepository userRepository;

        public void setUserRepository(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        public Authentication authenticate(String id, String password) {
            assertIdAndPassword(id, password);

            User user = findUserOrThrowEx(id);

            throwExIfPasswordIsWrong(password, user);

            return createAuthentication(user);
        }

        private void assertIdAndPassword(String id, String password) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException();
            }

            if (password == null || password.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }

        private User findUserOrThrowEx(String id) {
            User user = getUserbyId(id);

            if (user == null) {
                throw new NotExistingUserException();
            }

            return user;
        }

        private void throwExIfPasswordIsWrong(String password, User user) {
            if (!user.matchPassword(password)) {
                throw new WrongPasswordException();
            }
        }

        private Authentication createAuthentication(User user) {
            return new Authentication(user.getId());
        }

        private User getUserbyId(String id) {
            return userRepository.findById(id);
        }
    }

    private class User {
        private String id;
        private String password;

        public User() {
        }

        public User(String id, String password) {
            this.id = id;
            this.password = password;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean matchPassword(String password) {
            return this.password.equals(password);
        }
    }

    private interface UserRepository {
        User findById(String id);
    }

    private class Authentication {
        private String id;

        public Authentication(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }
}
