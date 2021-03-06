/*
 * Copyright (C) 2012-2019 DuyHai DOAN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.archinnov.achilles.internals.codegen.dsl.update.cassandra2_2;

import com.squareup.javapoet.TypeName;

import info.archinnov.achilles.internals.codegen.dsl.JSONFunctionCallSupport;
import info.archinnov.achilles.internals.codegen.dsl.update.UpdateDSLCodeGen;
import info.archinnov.achilles.internals.parser.FieldParser.FieldMetaSignature;

public class UpdateDSLCodeGen2_2 extends UpdateDSLCodeGen implements JSONFunctionCallSupport {

    @Override
    protected void augmentUpdateRelationClass(ParentSignature parentSignature, FieldMetaSignature fieldMeta,
                                              TypeName newTypeName, ReturnType returnType) {
        buildSetFromJSONToRelationClass(parentSignature, fieldMeta, newTypeName, returnType);
    }
}
