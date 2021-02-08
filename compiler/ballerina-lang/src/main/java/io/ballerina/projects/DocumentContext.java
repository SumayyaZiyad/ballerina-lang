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
package io.ballerina.projects;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.internal.TransactionImportValidator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.SourceKind;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Maintains the internal state of a {@code Document} instance.
 * <p>
 * Works as a document cache.
 *
 * @since 2.0.0
 */
class DocumentContext {
    // TODO This constant should not be here
    private static final String IDENTIFIER_LITERAL_PREFIX = "'";

    private SyntaxTree syntaxTree;
    private TextDocument textDocument;
    private Set<ModuleLoadRequest> moduleLoadRequests;
    private BLangCompilationUnit compilationUnit;
    private NodeCloner nodeCloner;
    private DocumentId documentId;
    private String name;
    private String content;

    private DocumentContext(DocumentId documentId, String name, String content) {
        this.documentId = documentId;
        this.name = name;
        this.content = content;
    }

    static DocumentContext from(DocumentConfig documentConfig) {
        return new DocumentContext(documentConfig.documentId(), documentConfig.name(), documentConfig.content());
    }

    DocumentId documentId() {
        return this.documentId;
    }

    String name() {
        return this.name;
    }

    void parse() {
        if (syntaxTree != null) {
            return;
        }

        syntaxTree = SyntaxTree.from(this.textDocument(), name);
    }

    SyntaxTree syntaxTree() {
        parse();
        return syntaxTree;
    }

    TextDocument textDocument() {
        if (this.textDocument == null) {
            this.textDocument = TextDocuments.from(this.content);
        }
        return this.textDocument;
    }

    BLangCompilationUnit compilationUnit(CompilerContext compilerContext, PackageID pkgID, SourceKind sourceKind) {
        nodeCloner = NodeCloner.getInstance(compilerContext);
        if (compilationUnit != null) {
            return nodeCloner.cloneCUnit(compilationUnit);
        }
        BLangDiagnosticLog dlog = BLangDiagnosticLog.getInstance(compilerContext);

        SyntaxTree syntaxTree = syntaxTree();
        reportSyntaxDiagnostics(pkgID, syntaxTree, dlog);
        BLangNodeTransformer bLangNodeTransformer = new BLangNodeTransformer(compilerContext, pkgID, this.name);
        compilationUnit = (BLangCompilationUnit) bLangNodeTransformer.accept(syntaxTree.rootNode()).get(0);
        compilationUnit.setSourceKind(sourceKind);
        return nodeCloner.cloneCUnit(compilationUnit);
    }

    Set<ModuleLoadRequest> moduleLoadRequests(ModuleId currentModuleId, PackageDependencyScope scope) {
        if (this.moduleLoadRequests != null) {
            return this.moduleLoadRequests;
        }

        this.moduleLoadRequests = getModuleLoadRequests(currentModuleId, scope);
        return this.moduleLoadRequests;
    }

    private Set<ModuleLoadRequest> getModuleLoadRequests(ModuleId currentModuleId, PackageDependencyScope scope) {
        Set<ModuleLoadRequest> moduleLoadRequests = new LinkedHashSet<>();
        ModulePartNode modulePartNode = syntaxTree().rootNode();
        for (ImportDeclarationNode importDcl : modulePartNode.imports()) {
            moduleLoadRequests.add(getModuleLoadRequest(importDcl, scope));
        }

        // TODO This is a temporary solution for SLP6 release
        // TODO Traverse the syntax tree to see whether to import the ballerinai/transaction package or not
        TransactionImportValidator trxImportValidator = new TransactionImportValidator();
        if (trxImportValidator.shouldImportTransactionPackage(modulePartNode) &&
               !currentModuleId.moduleName().equals(Names.TRANSACTION.value)) {
            PackageName packageName = PackageName.from(Names.TRANSACTION.value);
            ModuleLoadRequest ballerinaiLoadReq =
                    new ModuleLoadRequest(PackageOrg.from(Names.BALLERINA_INTERNAL_ORG.value),
                            packageName, ModuleName.from(packageName), null, scope);
            moduleLoadRequests.add(ballerinaiLoadReq);
        }

        return moduleLoadRequests;
    }

    private ModuleLoadRequest getModuleLoadRequest(ImportDeclarationNode importDcl, PackageDependencyScope scope) {
        // TODO We need to handle syntax errors in importDcl
        // Get organization name
        PackageOrg orgName = importDcl.orgName()
                .map(orgNameNode -> PackageOrg.from(orgNameNode.orgName().text()))
                .orElse(null);

        // Compute package name
        PackageName packageName;
        // Index in identifierTokenList from which the moduleNamePart starts
        int moduleNamePartStartIndex;
        SeparatedNodeList<IdentifierToken> identifierTokenList = importDcl.moduleName();
        String firstModuleNamePart = handleQuotedIdentifier(identifierTokenList.get(0).text());

        // Check for langLib packages
        if (PackageOrg.BALLERINA_ORG.equals(orgName) &&
                PackageName.LANG_LIB_PACKAGE_NAME_PREFIX.equals(firstModuleNamePart)) {
            // This a request to load a lang lib package
            // Lang lib package names take the form lang.{identifier}
            //  e.g, lang.int, lang.boolean lang.stream
            String secondModuleNamePart = handleQuotedIdentifier(identifierTokenList.get(1).text());
            packageName = PackageName.from(firstModuleNamePart + "." + secondModuleNamePart);
            moduleNamePartStartIndex = 2;
        } else {
            packageName = PackageName.from(firstModuleNamePart);
            moduleNamePartStartIndex = 1;
        }

        // Compute the module name
        StringJoiner stringJoiner = new StringJoiner(".");
        for (int i = moduleNamePartStartIndex; i < identifierTokenList.size(); i++) {
            stringJoiner.add(handleQuotedIdentifier(identifierTokenList.get(i).text()));
        }
        String moduleNamePart = stringJoiner.toString();
        ModuleName moduleName = ModuleName.from(packageName, moduleNamePart.isEmpty() ? null : moduleNamePart);

        // Create the module load request
        return new ModuleLoadRequest(orgName, packageName, moduleName, null, scope);
    }

    private String handleQuotedIdentifier(String identifier) {
        if (identifier.startsWith(IDENTIFIER_LITERAL_PREFIX)) {
            return identifier.substring(1);
        } else {
            return identifier;
        }
    }

    private void reportSyntaxDiagnostics(PackageID pkgID, SyntaxTree tree, BLangDiagnosticLog dlog) {
        for (Diagnostic syntaxDiagnostic : tree.diagnostics()) {
            dlog.logDiagnostic(pkgID, syntaxDiagnostic);
        }
    }
}
