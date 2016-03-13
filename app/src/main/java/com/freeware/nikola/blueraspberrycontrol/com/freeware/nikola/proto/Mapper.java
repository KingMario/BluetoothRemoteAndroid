package com.freeware.nikola.blueraspberrycontrol.com.freeware.nikola.proto;

import com.freeware.nikola.blueraspberrycontrol.KeyboardEmuView;

import java.util.HashMap;
import java.util.Map;

public class Mapper {

    static enum UInputKeyEvents { // not used. just for reference
        KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6, KEY_7, KEY_8, KEY_9, KEY_0, KEY_MINUS, KEY_EQUAL,

        KEY_TAB, KEY_Q, KEY_W, KEY_E, KEY_R, KEY_T, KEY_Y, KEY_U, KEY_I, KEY_O, KEY_P, KEY_BACKSPACE,

        KEY_CAPSLOCK, KEY_A, KEY_S, KEY_D, KEY_F, KEY_G, KEY_H, KEY_J, KEY_K, KEY_L, KEY_ENTER,

        KEY_LEFTSHIFT, KEY_Z, KEY_X, KEY_C, KEY_V, KEY_B, KEY_N, KEY_M, KEY_LEFTBRACE, KEY_RIGHTBRACE, KEY_COMMA, KEY_DOT,

        KEY_LEFTCTRL, KEY_LEFTALT, KEY_SPACE, KEY_SEMICOLON, KEY_APOSTROPHE, KEY_BACKSLASH, KEY_SLASH
    }

    private static Map<String, Integer> keyEventUinputMap = new HashMap<>();

    static {
        keyEventUinputMap.put("1", 2);
        keyEventUinputMap.put("2", 3);
        keyEventUinputMap.put("3", 4);
        keyEventUinputMap.put("4", 5);
        keyEventUinputMap.put("5", 6);
        keyEventUinputMap.put("6", 7);
        keyEventUinputMap.put("7", 8);
        keyEventUinputMap.put("8", 9);
        keyEventUinputMap.put("9", 10);
        keyEventUinputMap.put("0", 11);
        keyEventUinputMap.put("-", 12);
        keyEventUinputMap.put("=", 13);

        keyEventUinputMap.put(KeyboardEmuView.TAB_SYMBOL, 15);
        keyEventUinputMap.put("Q", 16);
        keyEventUinputMap.put("W", 17);
        keyEventUinputMap.put("E", 18);
        keyEventUinputMap.put("R", 19);
        keyEventUinputMap.put("T", 20);
        keyEventUinputMap.put("Y", 21);
        keyEventUinputMap.put("U", 22);
        keyEventUinputMap.put("I", 23);
        keyEventUinputMap.put("O", 24);
        keyEventUinputMap.put("P", 25);
        keyEventUinputMap.put(KeyboardEmuView.BACKSPACE_SYMBOL, 14);

        keyEventUinputMap.put(KeyboardEmuView.CAPSLOCK_SYMBOL, 58);
        keyEventUinputMap.put("A", 30);
        keyEventUinputMap.put("S", 31);
        keyEventUinputMap.put("D", 32);
        keyEventUinputMap.put("F", 33);
        keyEventUinputMap.put("G", 34);
        keyEventUinputMap.put("H", 35);
        keyEventUinputMap.put("J", 36);
        keyEventUinputMap.put("K", 37);
        keyEventUinputMap.put("L", 38);
        keyEventUinputMap.put(KeyboardEmuView.ENTER_SYMBOL, 28);

        keyEventUinputMap.put(KeyboardEmuView.SHIFT_SYMBOL, 42);
        keyEventUinputMap.put("Z", 44);
        keyEventUinputMap.put("X", 45);
        keyEventUinputMap.put("C", 46);
        keyEventUinputMap.put("V", 47);
        keyEventUinputMap.put("B", 48);
        keyEventUinputMap.put("N", 49);
        keyEventUinputMap.put("M", 50);
        keyEventUinputMap.put("[", 26);
        keyEventUinputMap.put("]", 27);
        keyEventUinputMap.put(",", 51);
        keyEventUinputMap.put(".", 52);

        keyEventUinputMap.put(KeyboardEmuView.CTRL_SYMBOL, 29);
        keyEventUinputMap.put(KeyboardEmuView.ALT_SYMBOL, 56);
        keyEventUinputMap.put(KeyboardEmuView.SPACE_SYMBOL, 57);
        keyEventUinputMap.put(";", 39);
        keyEventUinputMap.put("'", 40);
        keyEventUinputMap.put("\\", 43);
        keyEventUinputMap.put("/", 53);
    }

    public static int mapString(String str) {
        if(keyEventUinputMap.containsKey(str)) {
            return  keyEventUinputMap.get(str);
        } else {
            return -1;
        }
    }

}
