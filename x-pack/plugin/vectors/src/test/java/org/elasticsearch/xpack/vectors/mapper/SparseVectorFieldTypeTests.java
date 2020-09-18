/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */


package org.elasticsearch.xpack.vectors.mapper;

import org.elasticsearch.index.mapper.FieldTypeTestCase;
import org.elasticsearch.index.mapper.MappedFieldType;

import java.util.Collections;

public class SparseVectorFieldTypeTests extends FieldTypeTestCase {

    public void testDocValuesDisabled() {
        MappedFieldType fieldType = new SparseVectorFieldMapper.SparseVectorFieldType("field", Collections.emptyMap());
        expectThrows(IllegalArgumentException.class, () -> fieldType.fielddataBuilder("index", null));
    }

    public void testDocValueFormatIsNotSupported() {
        MappedFieldType fieldType = new SparseVectorFieldMapper.SparseVectorFieldType("field", Collections.emptyMap());
        UnsupportedOperationException exc = expectThrows(UnsupportedOperationException.class, () -> fieldType.docValueFormat(null, null));
        assertEquals(SparseVectorFieldMapper.ERROR_MESSAGE_7X, exc.getMessage());
    }

    public void testExistsQueryIsNotSupported() {
        MappedFieldType fieldType = new SparseVectorFieldMapper.SparseVectorFieldType("field", Collections.emptyMap());
        UnsupportedOperationException exc = expectThrows(UnsupportedOperationException.class, () -> fieldType.existsQuery(null));
        assertEquals(SparseVectorFieldMapper.ERROR_MESSAGE_7X, exc.getMessage());
    }

    public void testTermQueryIsNotSupported() {
        MappedFieldType fieldType = new SparseVectorFieldMapper.SparseVectorFieldType("field", Collections.emptyMap());
        UnsupportedOperationException exc = expectThrows(UnsupportedOperationException.class, () -> fieldType.termQuery(null, null));
        assertEquals(SparseVectorFieldMapper.ERROR_MESSAGE_7X, exc.getMessage());
    }
}
