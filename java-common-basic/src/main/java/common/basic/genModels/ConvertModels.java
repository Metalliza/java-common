package common.basic.genModels;

import common.basic.interfaces.RunnableThrowsException;
import common.basic.utils.FileUtil;
import common.basic.utils.Grep;
import common.basic.utils.RegExpUtil;
import common.basic.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class ConvertModels implements RunnableThrowsException {
    public static Pattern patternRemoveBrace = Pattern.compile("\\{[^{]+?\\}", Pattern.DOTALL | Pattern.MULTILINE);
    public static Pattern patternRemoveParenthesis = Pattern.compile("\\([^(]+?\\)", Pattern.DOTALL | Pattern.MULTILINE);


    final File fileFrom;
    final File fileTo;

    public ConvertModels(File fileFrom, File fileTo) {
        this.fileFrom = fileFrom;
        this.fileTo = fileTo;
    }

    @Override
    public void run() throws IOException {
        FileUtil.deleteRecursive(fileTo);
        FileUtil.mkdirs(fileTo);

        for (File file : FileUtil.getArrayFile(fileFrom)) {
            FileUtil.write(new File(fileTo, file.getName()), convert(FileUtil.readString(file)));
        }
    }

    public String convert(String sourceCode) {
        final int start = sourceCode.indexOf("{");
        final int end = sourceCode.lastIndexOf("}");

        final String head = sourceCode.substring(0, start + 1);
        final String body = sourceCode.substring(start + 1, end);
        final String tail = sourceCode.substring(end);

        if(head.contains("public enum "))
            return convertEnum(head, body, tail);

        return convertClass(head, body, tail);
    }

    private String convertEnum(String head, String body, String tail) {

        final String bodyRemoveBrace = removeBrace(body);
        final int semicolon = bodyRemoveBrace.indexOf(";");
        String bodyHead = bodyRemoveBrace.substring(0, semicolon);
        String bodyHeadRemoveParenthesis = removeParenthesis(bodyHead);
        return RegExpUtil.removeRepeatedNewLine(StringUtil.join("\n", convertHead(head), bodyHeadRemoveParenthesis, convertTail(tail)));
    }

    private String convertClass(String head, String body, String tail) {
        return RegExpUtil.removeRepeatedNewLine(StringUtil.join("\n", convertHead(head), convertBody(body), convertTail(tail)));
    }

    private String convertTail(String tail) {
        return tail;
    }

    private String convertBody(String body) {
        return removeLineThatHasAbstract(removeLineThatDoesNotHaveSemicolon(removeBrace(body)));
    }

    private String removeLineThatDoesNotHaveSemicolon(String bodyRemovedBrace) {
        return Grep.execute(bodyRemovedBrace, ";");
    }

    private String removeLineThatHasAbstract(String body) {
        return Grep.executeInvert(body, "abstract");
    }

    private String removeBrace(String bodyCurrent) {
        return RegExpUtil.replaceUntilNoChange(patternRemoveBrace, bodyCurrent, "");
    }

    private String removeParenthesis(String bodyCurrent) {
        return RegExpUtil.replaceUntilNoChange(patternRemoveParenthesis, bodyCurrent, "");
    }

    private String convertHead(String head) {
        return addSuppressWarningsUnusedDeclaration(removeExtendsEnhancedModel(removeImport(head)));
    }

    private String addSuppressWarningsUnusedDeclaration(String head) {
        return head
                .replace("public abstract class", "// This file was generated by common.basic.genModels.Main.\n// DO NOT EDIT THIS FILE MANUALLY.\n@SuppressWarnings(\"UnusedDeclaration\")\npublic abstract class")
                .replace("public class", "// This file was generated by common.basic.genModels.Main.\n// DO NOT EDIT THIS FILE MANUALLY.\n@SuppressWarnings(\"UnusedDeclaration\")\npublic class")
                .replace("public enum", "// This file was generated by common.basic.genModels.Main.\n// DO NOT EDIT THIS FILE MANUALLY.\n@SuppressWarnings(\"UnusedDeclaration\")\npublic enum");
    }

    private String removeImport(String head) {
        Pattern patternRemoveImport = Pattern.compile("import .+;");
        return patternRemoveImport.matcher(head).replaceAll("")
                .replace("public abstract class", "import java.util.Date;\n\npublic abstract class")
                .replace("public class", "import java.util.Date;\n\npublic class");
    }

    private String removeExtendsEnhancedModel(String head) {
        return head.replace(" extends EnhancedModel ", " ");
    }
}
