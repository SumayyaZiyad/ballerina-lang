/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/
package org.ballerinalang.nativeimpl.jvm.methodvisitor;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.nativeimpl.jvm.ASMUtil;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.FUNCTION_DESC;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.JVM_PKG_PATH;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_TYPE_DESC;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.METHOD_VISITOR;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.OBJECT_DESC;
import static org.ballerinalang.nativeimpl.jvm.ASMUtil.STRING_DESC;

/**
 * Code generation for invoke dynamic instruction.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "jvm",
        functionName = "visitInvokeDynamicInsn",
        receiver = @Receiver(type = OBJECT, structType = METHOD_VISITOR, structPackage = JVM_PKG_PATH),
        args = {
                @Argument(name = "className", type = STRING),
                @Argument(name = "lambdaName", type = STRING),
        }
)
public class VisitInvokeDynamicInsn extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

        String className = context.getStringArgument(0);
        String lambdaName = context.getStringArgument(1);


        //Function<Object[], Object> - create a dynamic lambda invocation with object[] param and returns object
        MethodVisitor mv = ASMUtil.getRefArgumentNativeData(context, 0);
        mv.visitInvokeDynamicInsn("apply", "()" + FUNCTION_DESC,
                new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory",
                        "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + STRING_DESC + METHOD_TYPE_DESC + METHOD_TYPE_DESC + "Ljava/lang/invoke/MethodHandle;"
                        + METHOD_TYPE_DESC + ")Ljava/lang/invoke/CallSite;", false),
                new Object[]{Type.getType("(" + OBJECT_DESC + ")" + OBJECT_DESC),
                        new Handle(Opcodes.H_INVOKESTATIC, className, lambdaName,
                                "([" + OBJECT_DESC + ")" + OBJECT_DESC, false),
                        Type.getType("([" + OBJECT_DESC + ")" + OBJECT_DESC)});
    }
}
