/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.runtime.entity;

import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.analysis.ISourceFileCoverage;

import java.util.List;

/**
 * Represents a source file containing lines modified to consider partially covered coverage info as fully covered.
 * @since 2.0.0
 */
public class PartialCoverageModifiedSourceFile implements ISourceFileCoverage {

    private final ISourceFileCoverage oldSourceFile;
    private final List<ILine> modifiedLines;

    public PartialCoverageModifiedSourceFile(ISourceFileCoverage oldSourcefile, List<ILine> modifiedLines) {
        this.oldSourceFile = oldSourcefile;
        this.modifiedLines = modifiedLines;
    }

    @Override
    public int getFirstLine() {
        return oldSourceFile.getFirstLine();
    }

    @Override
    public int getLastLine() {
        return oldSourceFile.getLastLine();
    }

    /**
     * Returns the modified lines instead of lines stored in the original source file.
     */
    @Override
    public ILine getLine(int nr) {
        if (modifiedLines.size() == 0 || nr < getFirstLine() || nr > getLastLine()) {
            return oldSourceFile.getLine(nr);
        }
        ILine reqLine = modifiedLines.get(nr - getFirstLine());
        return reqLine == null ? oldSourceFile.getLine(nr) : reqLine;
    }

    @Override
    public String getPackageName() {
        return oldSourceFile.getPackageName();
    }

    @Override
    public ElementType getElementType() {
        return oldSourceFile.getElementType();
    }

    @Override
    public String getName() {
        return oldSourceFile.getName();
    }

    @Override
    public ICounter getInstructionCounter() {
        return oldSourceFile.getInstructionCounter();
    }

    @Override
    public ICounter getBranchCounter() {
        return oldSourceFile.getBranchCounter();
    }

    @Override
    public ICounter getLineCounter() {
        return oldSourceFile.getLineCounter();
    }

    @Override
    public ICounter getComplexityCounter() {
        return oldSourceFile.getComplexityCounter();
    }

    @Override
    public ICounter getMethodCounter() {
        return oldSourceFile.getMethodCounter();
    }

    @Override
    public ICounter getClassCounter() {
        return oldSourceFile.getClassCounter();
    }

    @Override
    public ICounter getCounter(CounterEntity entity) {
        return oldSourceFile.getCounter(entity);
    }

    @Override
    public boolean containsCode() {
        return oldSourceFile.containsCode();
    }

    @Override
    public ICoverageNode getPlainCopy() {
        return oldSourceFile.getPlainCopy();
    }
}
