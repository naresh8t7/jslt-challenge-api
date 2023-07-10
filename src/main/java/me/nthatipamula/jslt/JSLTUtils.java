package me.nthatipamula.jslt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schibsted.spt.data.jslt.Expression;
import com.schibsted.spt.data.jslt.Parser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class JSLTUtils {
    /**
     * transforms json input string to given jslt expression( Ignore exceptions , Returns empty string when exception)
     * @param jsltExp   jslt expression
     * @param input  json String input parameter
     * @return
     */
    public static String transformJSON(String jsltExp, String input) {
        try {
            return transformJSON(jsltExp, input, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * transforms json input string to given jslt expression
     * @param jsltExp   jslt expression
     * @param input  json String input parameter
     * @param ignoreException   Ignore exceptions   If ture, Return after exception null
     * @return
     */
    public static String transformJSON(String jsltExp, String input, boolean ignoreException) throws IOException {
        if (StringUtils.isBlank(jsltExp) || StringUtils.isBlank(input)) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode ip = mapper.readTree(input);
            Expression jslt = Parser.compileString(jsltExp);
            JsonNode output = jslt.apply(ip);
            return output.toString();
        } catch (Exception e) {
            if (!ignoreException) {
                throw e;
            }
        }
        return "";
    }
}
