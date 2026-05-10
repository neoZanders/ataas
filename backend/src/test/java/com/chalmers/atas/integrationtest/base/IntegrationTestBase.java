package com.chalmers.atas.integrationtest.base;

import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestBase extends ScheduleIntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockMvcRequest asCurrentUser(User user) {
        return new MockMvcRequest(user);
    }

    protected MockMvcRequest asUnauthenticated() {
        return new MockMvcRequest(null).withoutAuth();
    }

    protected class MockMvcRequest {

        private final User user;
        private ResultActions lastResult;
        private String prefix = "$.";

        private boolean includeAuth = true;
        private boolean includeCsrf = true;

        public MockMvcRequest(User user) {
            this.user = user;
        }

        public MockMvcRequest withoutAuth() {
            this.includeAuth = false;
            return this;
        }

        public MockMvcRequest withoutCsrf() {
            this.includeCsrf = false;
            return this;
        }

        protected RequestPostProcessor asCurrentUser(User user) {

            UserPrincipal principal = new UserPrincipal(user.getUserId(), user.getUsername(), user.getAuthorities());

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.authorities()
            );

            return authentication(auth);
        }

        private ResultActions perform(MockHttpServletRequestBuilder builder) {
            try {
                MockHttpServletRequestBuilder requestBuilder = builder;

                if (includeAuth && user != null) {
                    requestBuilder = requestBuilder.with(asCurrentUser(user));
                }
                if (includeCsrf) {
                    requestBuilder = requestBuilder.with(csrf());
                }

                lastResult = mockMvc.perform(requestBuilder);
                return lastResult;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public MockMvcRequest get(String urlTemplate, Object... uriVars) {
            perform(MockMvcRequestBuilders.get(urlTemplate, uriVars));
            return this;
        }

        public MockMvcRequest post(String urlTemplate, Object body, Object... uriVars) {
            perform(MockMvcRequestBuilders.post(urlTemplate, uriVars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(body)));
            return this;
        }

        public MockMvcRequest put(String urlTemplate, Object body, Object... uriVars) {
            perform(MockMvcRequestBuilders.put(urlTemplate, uriVars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(body)));
            return this;
        }

        public MockMvcRequest patch(String urlTemplate, Object body, Object... uriVars) {
            perform(MockMvcRequestBuilders.patch(urlTemplate, uriVars)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJson(body)));
            return this;
        }

        public MockMvcRequest delete(String urlTemplate, Object... uriVars) {
            perform(MockMvcRequestBuilders.delete(urlTemplate, uriVars));
            return this;
        }

        public MockMvcRequest expect(ResultMatcher matcher) {
            try {
                lastResult.andExpect(matcher);
                return this;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public MockMvcRequest body(Matcher<?> matcher) {
            expect(jsonPath(fullPath(null)).value(matcher));
            return this;
        }

        public MockMvcRequest body(String path, Matcher<?> matcher) {
            expect(jsonPath(fullPath(path)).value(matcher));
            return this;
        }

        public MockMvcRequest body(String path, Object expectedValue) {
            expect(jsonPath(fullPath(path)).value(expectedValue));
            return this;
        }

        public MockMvcRequest root(String root) {
            if (root == null || root.isBlank()) {
                prefix = "$.";
            } else {
                prefix = root.startsWith("$.") ? root : "$." + root;
                if (!prefix.endsWith(".")) prefix += ".";
            }
            return this;
        }

        private String fullPath(String path) {
            if (path == null || path.isBlank()) return prefix.substring(0, prefix.length() - 1);
            if (path.startsWith("$")) return path;
            if (path.startsWith(".")) return prefix.substring(0, prefix.length() - 1) + path;
            return prefix + path;
        }

        private String asJson(Object obj) {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
