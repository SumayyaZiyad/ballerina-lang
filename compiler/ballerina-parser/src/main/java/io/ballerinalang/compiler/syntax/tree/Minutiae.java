/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.syntax.tree;

import io.ballerinalang.compiler.internal.parser.tree.STMinutiae;
import io.ballerinalang.compiler.text.LineRange;
import io.ballerinalang.compiler.text.TextDocument;
import io.ballerinalang.compiler.text.TextRange;

/**
 * Represents whitespaces, comments, newline characters attached to a {@code Token}.
 *
 * @since 2.0.0
 */
public class Minutiae {
    private final STMinutiae internalMinutiae;
    private final Token token;
    private final int position;

    private TextRange textRange;
    private LineRange lineRange;

    Minutiae(STMinutiae internalMinutiae, Token token, int position) {
        this.internalMinutiae = internalMinutiae;
        this.token = token;
        this.position = position;
    }

    public SyntaxKind kind() {
        return internalMinutiae.kind;
    }

    public TextRange textRange() {
        if (textRange != null) {
            return textRange;
        }
        textRange = TextRange.from(position, internalMinutiae.width());
        return textRange;
    }

    public LineRange lineRange() {
        if (lineRange != null) {
            return lineRange;
        }

        SyntaxTree syntaxTree = token.syntaxTree();
        TextDocument textDocument = syntaxTree.textDocument();
        lineRange = LineRange.from(syntaxTree.filePath(), textDocument.linePositionFrom(textRange().startOffset()),
                textDocument.linePositionFrom(textRange().endOffset()));
        return lineRange;
    }
}
