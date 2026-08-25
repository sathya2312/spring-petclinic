/*
 * Copyright 2012-2026 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests that verify actuator endpoint exposure is restricted by default.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ActuatorExposureIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private RestTemplateBuilder restTemplateBuilder;

	@Test
	void healthIsExposedByDefault() {
		RestTemplate template = this.restTemplateBuilder.baseUri("http://localhost:" + this.port).build();
		ResponseEntity<Map> response = template.exchange(RequestEntity.get("/actuator/health").build(), Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).containsKey("status");
	}

	@Test
	void infoIsExposedByDefault() {
		RestTemplate template = this.restTemplateBuilder.baseUri("http://localhost:" + this.port).build();
		ResponseEntity<Map> response = template.exchange(RequestEntity.get("/actuator/info").build(), Map.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	void envIsNotExposedByDefault() {
		RestTemplate template = this.restTemplateBuilder.baseUri("http://localhost:" + this.port).build();
		try {
			template.exchange(RequestEntity.get("/actuator/env").build(), String.class);
		}
		catch (RestClientResponseException ex) {
			// For non-exposed endpoints, Actuator returns 404.
			assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
			return;
		}
		assertThat(false).as("/actuator/env should not be exposed by default").isTrue();
	}

}
