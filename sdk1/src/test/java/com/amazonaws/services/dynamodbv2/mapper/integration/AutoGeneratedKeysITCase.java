/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.mapper.integration;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.mapper.encryption.TestDynamoDBMapperFactory;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ConditionalOperator;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.amazonaws.util.ImmutableMapParameter;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;


/**
 * Tests using auto-generated keys for range keys, hash keys, or both.
 */
public class AutoGeneratedKeysITCase extends DynamoDBMapperCryptoIntegrationTestBase {

    private static final String TABLE_NAME = "aws-java-sdk-string-range-crypto";

    @BeforeClass
    public static void setUp() throws Exception {
        DynamoDBMapperCryptoIntegrationTestBase.setUp();

        // Create a table
        String keyName = DynamoDBMapperCryptoIntegrationTestBase.KEY_NAME;
        String rangeKeyAttributeName = "rangeKey";

        CreateTableRequest createTableRequest = new CreateTableRequest()
                .withTableName(TABLE_NAME)
                .withKeySchema(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH),
                        new KeySchemaElement().withAttributeName(rangeKeyAttributeName).withKeyType(KeyType.RANGE))
                .withAttributeDefinitions(
                        new AttributeDefinition().withAttributeName(keyName).withAttributeType(
                                ScalarAttributeType.S),
                        new AttributeDefinition().withAttributeName(rangeKeyAttributeName).withAttributeType(
                                ScalarAttributeType.S));
        createTableRequest.setProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(10L)
                .withWriteCapacityUnits(5L));

        if (TableUtils.createTableIfNotExists(dynamo, createTableRequest)) {
            TableUtils.waitUntilActive(dynamo, TABLE_NAME);
        }
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class HashKeyRangeKeyBothAutoGenerated {

        private String key;
        private String rangeKey;
        private String otherAttribute;

        @DynamoDBAutoGeneratedKey
        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBAutoGeneratedKey
        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        public String getOtherAttribute() {
            return otherAttribute;
        }

        public void setOtherAttribute(String otherAttribute) {
            this.otherAttribute = otherAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((otherAttribute == null) ? 0 : otherAttribute.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            HashKeyRangeKeyBothAutoGenerated other = (HashKeyRangeKeyBothAutoGenerated) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( otherAttribute == null ) {
                if ( other.otherAttribute != null )
                    return false;
            } else if ( !otherAttribute.equals(other.otherAttribute) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testHashKeyRangeKeyBothAutogenerated() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyRangeKeyBothAutoGenerated obj = new HashKeyRangeKeyBothAutoGenerated();
        obj.setOtherAttribute("blah");

        assertNull(obj.getKey());
        assertNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        HashKeyRangeKeyBothAutoGenerated other = mapper.load(HashKeyRangeKeyBothAutoGenerated.class, obj.getKey(),
                obj.getRangeKey());
        assertEquals(other, obj);
    }

    @Test
    public void testHashKeyRangeKeyBothAutogeneratedBatchWrite() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyRangeKeyBothAutoGenerated obj = new HashKeyRangeKeyBothAutoGenerated();
        obj.setOtherAttribute("blah");
        HashKeyRangeKeyBothAutoGenerated obj2 = new HashKeyRangeKeyBothAutoGenerated();
        obj2.setOtherAttribute("blah");

        assertNull(obj.getKey());
        assertNull(obj.getRangeKey());
        assertNull(obj2.getKey());
        assertNull(obj2.getRangeKey());
        mapper.batchSave(obj, obj2);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());
        assertNotNull(obj2.getKey());
        assertNotNull(obj2.getRangeKey());

        assertEquals(mapper.load(HashKeyRangeKeyBothAutoGenerated.class, obj.getKey(),
                obj.getRangeKey()), obj);
        assertEquals(mapper.load(HashKeyRangeKeyBothAutoGenerated.class, obj2.getKey(),
                obj2.getRangeKey()), obj2);
    }

    /**
     * Tests providing additional expected conditions when saving item with
     * auto-generated keys.
     */
    @Test
    public void testAutogeneratedKeyWithUserProvidedExpectedConditions() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyRangeKeyBothAutoGenerated obj = new HashKeyRangeKeyBothAutoGenerated();
        obj.setOtherAttribute("blah");

        assertNull(obj.getKey());
        assertNull(obj.getRangeKey());

        // Add additional expected conditions via DynamoDBSaveExpression.
        // Expected conditions joined by AND are compatible with the conditions
        // for auto-generated keys.
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        saveExpression
                .withExpected(Collections.singletonMap(
                        "otherAttribute", new ExpectedAttributeValue(false)))
                .withConditionalOperator(ConditionalOperator.AND);
        // The save should succeed since the user provided conditions are joined by AND.
        mapper.save(obj, saveExpression);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        HashKeyRangeKeyBothAutoGenerated other = mapper.load(HashKeyRangeKeyBothAutoGenerated.class, obj.getKey(),
                obj.getRangeKey());
        assertEquals(other, obj);

        // Change the conditional operator to OR.
        // IllegalArgumentException is expected since the additional expected
        // conditions cannot be joined with the conditions for auto-generated
        // keys.
        saveExpression.setConditionalOperator(ConditionalOperator.OR);
        try {
            mapper.save(new HashKeyRangeKeyBothAutoGenerated(), saveExpression);
        } catch (IllegalArgumentException expected) {}

        // User-provided OR conditions should work if they completely override the generated conditions.
        saveExpression
            .withExpected(ImmutableMapParameter.of(
                "otherAttribute", new ExpectedAttributeValue(false),
                "key",            new ExpectedAttributeValue(false),
                "rangeKey",       new ExpectedAttributeValue(false)))
            .withConditionalOperator(ConditionalOperator.OR);
        mapper.save(new HashKeyRangeKeyBothAutoGenerated(), saveExpression);

        saveExpression
            .withExpected(ImmutableMapParameter.of(
                "otherAttribute", new ExpectedAttributeValue(new AttributeValue("non-existent-value")),
                "key",            new ExpectedAttributeValue(new AttributeValue("non-existent-value")),
                "rangeKey",       new ExpectedAttributeValue(new AttributeValue("non-existent-value"))))
            .withConditionalOperator(ConditionalOperator.OR);
        try {
            mapper.save(new HashKeyRangeKeyBothAutoGenerated(), saveExpression);
        } catch (ConditionalCheckFailedException expected) {}
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class HashKeyAutoGenerated {

        private String key;
        private String rangeKey;
        private String otherAttribute;

        @DynamoDBAutoGeneratedKey
        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        public String getOtherAttribute() {
            return otherAttribute;
        }

        public void setOtherAttribute(String otherAttribute) {
            this.otherAttribute = otherAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((otherAttribute == null) ? 0 : otherAttribute.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            HashKeyAutoGenerated other = (HashKeyAutoGenerated) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( otherAttribute == null ) {
                if ( other.otherAttribute != null )
                    return false;
            } else if ( !otherAttribute.equals(other.otherAttribute) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testHashKeyAutogenerated() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyAutoGenerated obj = new HashKeyAutoGenerated();
        obj.setOtherAttribute("blah");
        obj.setRangeKey("" + System.currentTimeMillis());

        assertNull(obj.getKey());
        assertNotNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        HashKeyAutoGenerated other = mapper.load(HashKeyAutoGenerated.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class RangeKeyAutoGenerated {

        private String key;
        private String rangeKey;
        private String otherAttribute;

        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBAutoGeneratedKey
        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        public String getOtherAttribute() {
            return otherAttribute;
        }

        public void setOtherAttribute(String otherAttribute) {
            this.otherAttribute = otherAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((otherAttribute == null) ? 0 : otherAttribute.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            RangeKeyAutoGenerated other = (RangeKeyAutoGenerated) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( otherAttribute == null ) {
                if ( other.otherAttribute != null )
                    return false;
            } else if ( !otherAttribute.equals(other.otherAttribute) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testRangeKeyAutogenerated() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        RangeKeyAutoGenerated obj = new RangeKeyAutoGenerated();
        obj.setOtherAttribute("blah");
        obj.setKey("" + System.currentTimeMillis());

        assertNotNull(obj.getKey());
        assertNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        RangeKeyAutoGenerated other = mapper.load(RangeKeyAutoGenerated.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class NothingAutoGenerated {

        private String key;
        private String rangeKey;
        private String otherAttribute;

        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        public String getOtherAttribute() {
            return otherAttribute;
        }

        public void setOtherAttribute(String otherAttribute) {
            this.otherAttribute = otherAttribute;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((otherAttribute == null) ? 0 : otherAttribute.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            NothingAutoGenerated other = (NothingAutoGenerated) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( otherAttribute == null ) {
                if ( other.otherAttribute != null )
                    return false;
            } else if ( !otherAttribute.equals(other.otherAttribute) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testNothingAutogenerated() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        NothingAutoGenerated obj = new NothingAutoGenerated();
        obj.setOtherAttribute("blah");
        obj.setKey("" + System.currentTimeMillis());
        obj.setRangeKey("" + System.currentTimeMillis());

        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        NothingAutoGenerated other = mapper.load(NothingAutoGenerated.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @Test
    public void testNothingAutogeneratedErrors() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        NothingAutoGenerated obj = new NothingAutoGenerated();

        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setKey("" + System.currentTimeMillis());
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("" + System.currentTimeMillis());
        obj.setKey(null);
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("");
        obj.setKey("" + System.currentTimeMillis());
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("" + System.currentTimeMillis());
        mapper.save(obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class HashKeyRangeKeyBothAutoGeneratedKeyOnly {

        private String key;
        private String rangeKey;

        @DynamoDBAutoGeneratedKey
        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBAutoGeneratedKey
        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            HashKeyRangeKeyBothAutoGeneratedKeyOnly other = (HashKeyRangeKeyBothAutoGeneratedKeyOnly) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testHashKeyRangeKeyBothAutogeneratedKeyOnly() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyRangeKeyBothAutoGeneratedKeyOnly obj = new HashKeyRangeKeyBothAutoGeneratedKeyOnly();

        assertNull(obj.getKey());
        assertNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        HashKeyRangeKeyBothAutoGeneratedKeyOnly other = mapper.load(HashKeyRangeKeyBothAutoGeneratedKeyOnly.class, obj.getKey(),
                obj.getRangeKey());
        assertEquals(other, obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class HashKeyAutoGeneratedKeyOnly {

        private String key;
        private String rangeKey;

        @DynamoDBAutoGeneratedKey
        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            HashKeyAutoGeneratedKeyOnly other = (HashKeyAutoGeneratedKeyOnly) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }

    }

    @Test
    public void testHashKeyAutogeneratedKeyOnly() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        HashKeyAutoGeneratedKeyOnly obj = new HashKeyAutoGeneratedKeyOnly();
        obj.setRangeKey("" + System.currentTimeMillis());

        assertNull(obj.getKey());
        assertNotNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        HashKeyAutoGeneratedKeyOnly other = mapper.load(HashKeyAutoGeneratedKeyOnly.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class RangeKeyAutoGeneratedKeyOnly {

        private String key;
        private String rangeKey;

        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBAutoGeneratedKey
        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            RangeKeyAutoGeneratedKeyOnly other = (RangeKeyAutoGeneratedKeyOnly) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }

    }

    @Test
    public void testRangeKeyAutogeneratedKeyOnly() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        RangeKeyAutoGeneratedKeyOnly obj = new RangeKeyAutoGeneratedKeyOnly();
        obj.setKey("" + System.currentTimeMillis());

        assertNotNull(obj.getKey());
        assertNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        RangeKeyAutoGeneratedKeyOnly other = mapper.load(RangeKeyAutoGeneratedKeyOnly.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @DynamoDBTable(tableName = "aws-java-sdk-string-range-crypto")
    public static class NothingAutoGeneratedKeyOnly {

        private String key;
        private String rangeKey;

        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @DynamoDBRangeKey
        public String getRangeKey() {
            return rangeKey;
        }

        public void setRangeKey(String rangeKey) {
            this.rangeKey = rangeKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((rangeKey == null) ? 0 : rangeKey.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            NothingAutoGeneratedKeyOnly other = (NothingAutoGeneratedKeyOnly) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( rangeKey == null ) {
                if ( other.rangeKey != null )
                    return false;
            } else if ( !rangeKey.equals(other.rangeKey) )
                return false;
            return true;
        }
    }

    @Test
    public void testNothingAutogeneratedKeyOnly() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        NothingAutoGeneratedKeyOnly obj = new NothingAutoGeneratedKeyOnly();
        obj.setKey("" + System.currentTimeMillis());
        obj.setRangeKey("" + System.currentTimeMillis());

        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());
        mapper.save(obj);
        assertNotNull(obj.getKey());
        assertNotNull(obj.getRangeKey());

        NothingAutoGeneratedKeyOnly other = mapper.load(NothingAutoGeneratedKeyOnly.class, obj.getKey(), obj.getRangeKey());
        assertEquals(other, obj);
    }

    @Test
    public void testNothingAutogeneratedKeyOnlyErrors() {
        DynamoDBMapper mapper = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);
        NothingAutoGeneratedKeyOnly obj = new NothingAutoGeneratedKeyOnly();

        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setKey("" + System.currentTimeMillis());
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("" + System.currentTimeMillis());
        obj.setKey(null);
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("");
        obj.setKey("" + System.currentTimeMillis());
        try {
            mapper.save(obj);
            fail("Expected a mapping exception");
        } catch (DynamoDBMappingException expected) {
        }

        obj.setRangeKey("" + System.currentTimeMillis());
        mapper.save(obj);
    }
}