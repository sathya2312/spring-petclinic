/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for actuator endpoint exposure defaults.
 *
 * Story EPMCDMETST-61413:
 * - Default config should expose only non-sensitive actuator endpoints (health, info)
 * - Dev profile can expose broader actuator endpoints for local troubleshooting
 */
class ActuatorExposureSecurityTests {

	@WebMvcTest
	@DisabledInNativeImage
	@DisabledInAotMode
	static class DefaultProfile {

		@Autowired
		private MockMvc mockMvc;

		@Test
		void healthEndpointIsExposed() throws Exception {
			mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
		}

		@Test
		void infoEndpointIsExposed() throws Exception {
			mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
		}

		@Test
		void envEndpointIsNotExposedByDefault() throws Exception {
			// When an actuator endpoint is not exposed, Spring returns 404 (not found)
			mockMvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
		}

		@Test
		void beansEndpointIsNotExposedByDefault() throws Exception {
			mockMvc.perform(get("/actuator/beans")).andExpect(status().isNotFound());
		}

	}

	@WebMvcTest
	@ActiveProfiles("dev")
	@DisabledInNativeImage
	@DisabledInAotMode
	static class DevProfile {

		@Autowired
		private MockMvc mockMvc;

		@Test
		void envEndpointIsExposedInDevProfile() throws Exception {
			// With management.endpoints.web.exposure.include=*, the endpoint should be mapped.
			// Depending on security auto-configuration, this may still be blocked; in that
			// case, we'll see 401/403. The important behavior is "not 404".
			mockMvc.perform(get("/actuator/env")).andExpect(isNotNotFound());
		}

		private static ResultMatcher isNotNotFound() {
			return result -> {
				int code = result.getResponse().getStatus();
				if (code == 404) {
					throw new AssertionError(
							"Expected actuator endpoint '/actuator/env' to be exposed in dev profile, but got 404");
				}
			};
		}

	}

}
