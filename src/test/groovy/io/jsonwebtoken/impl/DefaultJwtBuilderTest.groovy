/*
 * Copyright (C) 2015 jsonwebtoken.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jsonwebtoken.impl

import io.jsonwebtoken.Jwts

//import com.fasterxml.jackson.core.JsonProcessingException
//import com.fasterxml.jackson.databind.JsonMappingException
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.impl.compression.CompressionCodecs
import io.jsonwebtoken.impl.crypto.MacProvider
import io.jsonwebtoken.impl.json.JacksonObjectMapper
import io.jsonwebtoken.impl.json.ObjectMapperFactory
import org.junit.Test

import static org.junit.Assert.*

class DefaultJwtBuilderTest {

    DefaultJwtBuilder createBuilder() {
        return new DefaultJwtBuilder(ObjectMapperFactory.INSTANCE.defaultObjectMapper())
    }

    @Test
    void testSetHeader() {
        def h = Jwts.header()
        def b = createBuilder()
        b.setHeader(h)
        assertSame b.header, h
    }

    @Test
    void testSetHeaderFromMap() {
        def m = [foo: 'bar']
        def b = createBuilder()
        b.setHeader(m)
        assertNotNull b.header
        assertEquals b.header.size(), 1
        assertEquals b.header.foo, 'bar'
    }

    @Test
    void testSetHeaderParams() {
        def m = [a: 'b', c: 'd']
        def b = createBuilder()
        b.setHeaderParams(m)
        assertNotNull b.header
        assertEquals b.header.size(), 2
        assertEquals b.header.a, 'b'
        assertEquals b.header.c, 'd'
    }

    @Test
    void testSetHeaderParam() {
        def b = createBuilder()
        b.setHeaderParam('foo', 'bar')
        assertNotNull b.header
        assertEquals b.header.size(), 1
        assertEquals b.header.foo, 'bar'
    }

    @Test
    void testSetClaims() {
        def b = createBuilder()
        def c = Jwts.claims()
        b.setClaims(c)
        assertNotNull b.claims
        assertSame b.claims, c
    }

    @Test
    void testClaim() {
        def b = createBuilder()
        b.claim('foo', 'bar')
        assertNotNull b.claims
        assertEquals b.claims.size(), 1
        assertEquals b.claims.foo, 'bar'
    }

    @Test
    void testExistingClaimsAndSetClaim() {
        def b = createBuilder()
        def c = Jwts.claims()
        b.setClaims(c)
        b.claim('foo', 'bar')
        assertSame b.claims, c
        assertEquals b.claims.size(), 1
        assertEquals c.size(), 1
        assertEquals b.claims.foo, 'bar'
        assertEquals c.foo, 'bar'
    }

    @Test
    void testRemoveClaimBySettingNullValue() {
        def b = createBuilder()
        b.claim('foo', 'bar')
        assertNotNull b.claims
        assertEquals b.claims.size(), 1
        assertEquals b.claims.foo, 'bar'

        b.claim('foo', null)
        assertNotNull b.claims
        assertNull b.claims.foo
    }

    @Test
    void testCompactWithoutBody() {
        def b = createBuilder()
        try {
            b.compact()
            fail()
        } catch (IllegalStateException ise) {
            assertEquals ise.message, "Either 'payload' or 'claims' must be specified."
        }
    }

    @Test
    void testCompactWithoutPayloadOrClaims() {
        def b = createBuilder()
        try {
            b.compact()
            fail()
        } catch (IllegalStateException ise) {
            assertEquals ise.message, "Either 'payload' or 'claims' must be specified."
        }
    }

    @Test
    void testCompactWithBothPayloadAndClaims() {
        def b = createBuilder()
        b.setPayload('foo')
        b.claim('a', 'b')
        try {
            b.compact()
            fail()
        } catch (IllegalStateException ise) {
            assertEquals ise.message, "Both 'payload' and 'claims' cannot both be specified. Choose either one."
        }
    }

    @Test
    void testCompactWithBothKeyAndKeyBytes() {
        def b = createBuilder()
        b.setPayload('foo')
        def key = MacProvider.generateKey()
        b.signWith(SignatureAlgorithm.HS256, key)
        b.signWith(SignatureAlgorithm.HS256, key.encoded)
        try {
            b.compact()
            fail()
        } catch (IllegalStateException ise) {
            assertEquals ise.message, "A key object and key bytes cannot both be specified. Choose either one."
        }
    }

    @Test
    void testCompactWithJwsHeader() {
        def b = createBuilder()
        b.setHeader(Jwts.jwsHeader().setKeyId('a'))
        b.setPayload('foo')
        def key = MacProvider.generateKey()
        b.signWith(SignatureAlgorithm.HS256, key)
        b.compact()
    }

    @Test
    void testBase64UrlEncodeError() {
        def b = new DefaultJwtBuilder(new JacksonObjectMapper() {
            @Override
            byte[] toJsonBytes(Object object) {
                throw new Exception("foo")
            }
        });

        try {
            b.setPayload('foo').compact()
            fail()
        } catch (IllegalStateException ise) {
            assertEquals ise.cause.message, 'foo'
        }

    }

    @Test
    void testCompactCompressionCodecJsonProcessingException() {
        def b = new DefaultJwtBuilder(new JacksonObjectMapper() {
            @Override
            byte[] toJsonBytes(Object object) {
                if (object instanceof DefaultJwsHeader) { return super.toJsonBytes(object) }
                throw new Exception('simulate json processing exception on claims')
            }
        })

        def c = Jwts.claims().setSubject("Joe");

        try {
            b.setClaims(c).compressWith(CompressionCodecs.DEFLATE).compact()
            fail()
        } catch (IllegalArgumentException iae) {
            assertEquals iae.message, 'Unable to serialize claims object to json.'
        }
    }

    @Test
    void testSignWithBytesWithoutHmac() {
        def bytes = new byte[16];
        try {
            createBuilder().signWith(SignatureAlgorithm.ES256, bytes);
            fail()
        } catch (IllegalArgumentException iae) {
            assertEquals "Key bytes may only be specified for HMAC signatures.  If using RSA or Elliptic Curve, use the signWith(SignatureAlgorithm, Key) method instead.", iae.message
        }
    }

    @Test
    void testSignWithBase64EncodedBytesWithoutHmac() {
        try {
            createBuilder().signWith(SignatureAlgorithm.ES256, 'foo');
            fail()
        } catch (IllegalArgumentException iae) {
            assertEquals "Base64-encoded key bytes may only be specified for HMAC signatures.  If using RSA or Elliptic Curve, use the signWith(SignatureAlgorithm, Key) method instead.", iae.message
        }

    }

    @Test
    void testSetHeaderParamsWithNullMap() {
        def b = createBuilder()
        b.setHeaderParams(null)
        assertNull b.header
    }

    @Test
    void testSetHeaderParamsWithEmptyMap() {
        def b = createBuilder()
        b.setHeaderParams([:])
        assertNull b.header
    }

    @Test
    void testSetIssuerWithNull() {
        def b = createBuilder()
        b.setIssuer(null)
        assertNull b.claims
    }

    @Test
    void testSetSubjectWithNull() {
        def b = createBuilder()
        b.setSubject(null)
        assertNull b.claims
    }

    @Test
    void testSetAudienceWithNull() {
        def b = createBuilder()
        b.setAudience(null)
        assertNull b.claims
    }

    @Test
    void testSetIdWithNull() {
        def b = createBuilder()
        b.setId(null)
        assertNull b.claims
    }

    @Test
    void testClaimNullValue() {
        def b = createBuilder()
        b.claim('foo', null)
        assertNull b.claims
    }

    @Test
    void testSetNullExpirationWithNullClaims() {
        def b = createBuilder()
        b.setExpiration(null)
        assertNull b.claims
    }

    @Test
    void testSetNullNotBeforeWithNullClaims() {
        def b = createBuilder()
        b.setNotBefore(null)
        assertNull b.claims
    }

    @Test
    void testSetNullIssuedAtWithNullClaims() {
        def b = createBuilder()
        b.setIssuedAt(null)
        assertNull b.claims
    }

}
