package com.slimgears.rxrpc.apt;

import com.slimgears.rxrpc.apt.TypeInfoBaseListener;
import com.slimgears.rxrpc.apt.TypeInfoParser;
import com.slimgears.rxrpc.apt.TypeInfoLexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.UnbufferedCharStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import java.io.StringReader;

public class AntlrTest {
    @Test
    public void testAntlrParsing() {
        String typeStr = "java.util.Map<java.lang.Integer, java.util.List<java.lang.String>>";
        TypeInfoLexer lexer = new TypeInfoLexer(CharStreams.fromString(typeStr));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TypeInfoParser parser = new TypeInfoParser(tokenStream);
        TypeInfoBaseListener listener = new TypeInfoBaseListener() {
            @Override
            public void enterType(TypeInfoParser.TypeContext ctx) {
                System.out.println("Entering type: " + ctx.referenceType().getText());
            }

            @Override
            public void exitClassOrInterfaceType(TypeInfoParser.ClassOrInterfaceTypeContext ctx) {
                System.out.println("Exiting classOrInterfaceType: " + ctx.getText());
            }

            @Override
            public void exitType(TypeInfoParser.TypeContext ctx) {
                System.out.println("Exiting type: " + ctx);
            }
        };

        ParseTreeWalker.DEFAULT.walk(listener, parser.type());
    }
}
