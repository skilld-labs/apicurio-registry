/*
 * Copyright 2021 Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.registry.rest;

import static io.apicurio.registry.AbstractResourceTestBase.CT_JSON;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anything;

import java.util.UUID;

import jakarta.inject.Inject;

import io.apicurio.registry.utils.tests.ApicurioTestTags;
import io.apicurio.registry.utils.tests.TestUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.apicurio.tenantmanager.api.datamodel.ApicurioTenant;
import io.apicurio.tenantmanager.api.datamodel.TenantStatusValue;
import io.apicurio.registry.ccompat.rest.ContentTypes;
import io.apicurio.registry.mt.MockTenantMetadataService;
import io.apicurio.registry.noprofile.ccompat.rest.CCompatTestConstants;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * @author Fabian Martinez
 */
@QuarkusTest
@TestProfile(MultipleRequestFiltersTestProfile.class)
@Tag(ApicurioTestTags.SLOW)
public class MultitenancyAndDisabledApisTest {

    @Inject
    MockTenantMetadataService tenantMetadataService;

    @ConfigProperty(name = "quarkus.http.test-port")
    public int testPort;

    @Test
    public void testRestApi() throws Exception {
        doTestDisabledApis(true);

        var tenant1 = new ApicurioTenant();
        tenant1.setTenantId("abc");
        tenant1.setOrganizationId("aaa");
        tenant1.setStatus(TenantStatusValue.READY);
        tenantMetadataService.createTenant(tenant1);

        //this should return http 404, it's disabled
        given()
            .baseUri("http://localhost:" + testPort)
            .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CCompatTestConstants.SCHEMA_SIMPLE_WRAPPED)
                .post("/t/abc/apis/ccompat/v7/subjects/{subject}/versions", UUID.randomUUID().toString())
            .then()
                .statusCode(404);

        //this should return http 200, it's not disabled
        given()
            .baseUri("http://localhost:" + testPort)
            .when().contentType(CT_JSON).get("/t/abc/apis/ccompat/v7/subjects")
            .then()
            .statusCode(200)
            .body(anything());
    }

    public void doTestDisabledApis(boolean disabledDirectAccess) throws Exception {
        doTestDisabledSubPathRegexp(disabledDirectAccess);

        doTestDisabledChildPathByParentPath(disabledDirectAccess);

        doTestUIDisabled();
    }

    private void doTestUIDisabled() {
        given()
                .baseUri("http://localhost:" + testPort)
                .when()
                .get("/ui")
                .then()
                .statusCode(404);
    }

    private static void doTestDisabledSubPathRegexp(boolean disabledDirectAccess) {
        //this should return http 404, it's disabled
        given()
                .when()
                .contentType(ContentTypes.COMPAT_SCHEMA_REGISTRY_STABLE_LATEST)
                .body(CCompatTestConstants.SCHEMA_SIMPLE_WRAPPED)
                .post("/ccompat/v7/subjects/{subject}/versions", UUID.randomUUID().toString())
                .then()
                .statusCode(404);

        var req = given()
                .when().contentType(CT_JSON).get("/ccompat/v7/subjects")
                .then();
        if (disabledDirectAccess) {
            req.statusCode(404);
        } else {
            //this should return http 200, it's not disabled
            req.statusCode(200)
                    .body(anything());
        }
    }

    private void doTestDisabledChildPathByParentPath(boolean disabledDirectAccess) throws Exception {
        String artifactContent = "{\"type\":\"record\",\"name\":\"myrecord1\",\"fields\":[{\"name\":\"f1\",\"type\":\"string\"}]}";
        String schemaId = TestUtils.generateArtifactId();

        var req = given()
                .when()
                .contentType(CT_JSON + "; artifactType=AVRO")
                .pathParam("groupId", "default")
                .header("X-Registry-ArtifactId", schemaId)
                .body(artifactContent)
                .post("/registry/v2/groups/{groupId}/artifacts")
                .then();

        if (disabledDirectAccess) {
            req.statusCode(404);
        } else {
            req.statusCode(200);
        }
    }

}
