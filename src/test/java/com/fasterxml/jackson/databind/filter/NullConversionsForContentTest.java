package com.fasterxml.jackson.databind.filter;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidNullException;

// For [databind#1402]; configurable null handling, for contents of
// Collections, Maps, arrays
public class NullConversionsForContentTest extends BaseMapTest
{
    static class NullContentFail<T> {
        public T nullsOk;

        @JsonSetter(contentNulls=JsonSetter.Nulls.FAIL)
        public T noNulls;
    }

    static class NullContentAsEmpty<T> {
        @JsonSetter(contentNulls=JsonSetter.Nulls.AS_EMPTY)
        public T values;
    }
    
    /*
    /**********************************************************
    /* Test methods
    /**********************************************************
     */

    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testFailOnNull() throws Exception
    {
        TypeReference<?> typeRef = new TypeReference<NullContentFail<List<Integer>>>() { };

        // first, ok if assigning non-null to not-nullable, null for nullable
        NullContentFail<List<Integer>> result = MAPPER.readValue(aposToQuotes("{'nullsOk':[null]}"),
                typeRef);
        assertNotNull(result.nullsOk);
        assertEquals(1, result.nullsOk.size());
        assertNull(result.nullsOk.get(0));

        // and then see that nulls are not ok for non-nullable.
        
        // First: List<Integer>
        final String JSON = aposToQuotes("{'noNulls':[null]}");
        try {
            MAPPER.readValue(JSON, typeRef);
            fail("Should not pass");
        } catch (InvalidNullException e) {
            verifyException(e, "property \"noNulls\"");
        }
        // Then: List<String>
        // Then: Map<String,String>
        // Then: Object[]
        // Then: String[]
    }

    public void testNullsAsEmpty() throws Exception
    {
        final String JSON = aposToQuotes("{'values':[null]}");

        // List<Integer>
        {
            NullContentAsEmpty<List<Integer>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<Integer>>>() { });
            assertEquals(1, result.values.size());
            assertEquals(Integer.valueOf(0), result.values.get(0));
        }

        // List<String>
        {
            NullContentAsEmpty<List<String>> result = MAPPER.readValue(JSON,
                    new TypeReference<NullContentAsEmpty<List<String>>>() { });
            assertEquals(1, result.values.size());
            assertEquals("", result.values.get(0));
        }

        // Then: Map<String,String>
        // Then: Object[]
        // Then: String[]
    }
}
