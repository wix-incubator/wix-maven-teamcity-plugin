package com.wixpress.ci.teamcity.teamCityAnalyzer;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author yoav
 * @since 3/5/12
 */
public class SimplePatternMatcherTest {

    SimplePatternMatcher matcher = new SimplePatternMatcher();

    @Test
    public void positive() throws Exception {
        String text = "java/lang/StringBuffer.class";
        String pattern = "*Str*Bu*er*class";
        assertTrue(matcher.wildcardMatch(text, pattern));
    }

    @Test
    public void negative() throws Exception {
        String text = "java/lang/StringBuffer.class";
        String pattern = "*Str*Bx*er*class";
        assertFalse(matcher.wildcardMatch(text, pattern));
    }

    @Test
    public void emptyPattern() throws Exception {
        String text = "java/lang/StringBuffer.class";
        String pattern = "";
        assertTrue(matcher.wildcardMatch(text, pattern));
    }

    @Test
    public void emptyText() throws Exception {
        String text = "";
        String pattern = "*Str*Bx*er*class";
        assertFalse(matcher.wildcardMatch(text, pattern));
    }

}
