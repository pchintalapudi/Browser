/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

/**
 *
 * @author prem
 */
public enum CSSInheritedProperties {
    AZIMUTH, BORDER_COLLAPSE, BORDER_SPACING, CAPTION_SIDE, COLOR, CURSOR, DIRECTION,
    ELEVATION, EMPTY_CELLS, FONT_FAMILY, FONT_SIZE, FONT_STYLE, FONT_VARIANT,
    FONT_WEIGHT, FONT, LETTER_SPACING, LINE_HEIGHT, LIST_STYLE_IMAGE, LIST_STYLE_POSITION,
    LIST_STYLE_TYPE, LIST_STYLE, ORPHANS, PITCH_RANGE, PITCH, QUOTES, RICHNESS, SPEAK_HEADER,
    SPEAK_NUMERAL, SPEAK_PUNCTUATION, SPEAK, SPEECH_RATE, STRESS, TEXT_ALIGN, TEXT_INDENT,
    TEXT_TRANSFORM, VISIBILITY, VOICE_FAMILY, VOLUME, WHITE_SPACE, WIDOWS, WORD_SPACING;

    public String toCSSProperty() {
        return this.name().replace("_", "-").toLowerCase();
    }

    public CSSInheritedProperties fromCSSProperty(String cssProperty) {
        return Styler.toEnum(cssProperty, CSSInheritedProperties.class);
    }
}