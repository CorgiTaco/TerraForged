/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.util.nbt;

import net.minecraft.nbt.*;

import java.io.*;
import java.util.HashMap;

public class NBTLeavesFixer {

    public static void main(String[] args) {
        RuleSet ruleSet = new RuleSet();
        ruleSet.put("persistent", new Rule("true", "false"));
        String path = "H:\\Projects\\TerraForged\\TerraForgedMod\\src\\main\\resources\\data\\terraforged\\structures\\trees";
        visit(new File(path), ruleSet);
    }

    public static void visit(File file, RuleSet ruleSet) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                visit(f, ruleSet);
            }
        } else if (file.getName().endsWith(".nbt")) {
            try {
                System.out.println("Opening file: " + file);
                CompoundTag value;
                Tag result;
                try (InputStream in = new FileInputStream(file)) {
                    value = NbtIo.readCompressed(in);
                    result = modify("", value, ruleSet);
                    if (value == result) {
                        return;
                    }
                }
                System.out.println("Writing file: " + file);
                try (OutputStream out = new FileOutputStream(file)) {
                    NbtIo.writeCompressed((CompoundTag) result, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Tag modify(String name, Tag nbt, RuleSet ruleSet) {
        if (nbt instanceof CompoundTag) {
            CompoundTag map = (CompoundTag) nbt;
            boolean change = false;
            for (String key : map.getKeys()) {
                Tag value = map.get(key);
                Tag result = modify(key, value, ruleSet);
                map.put(key, result);
                change |= value != result;
            }
            return change ? map.copy() : map;
        } else if (nbt instanceof ListTag) {
            ListTag list = (ListTag) nbt;
            boolean change = false;
            for (int i = 0; i < list.size(); i++) {
                Tag value = list.get(i);
                Tag result = modify(name + "[" + i + "]", value, ruleSet);
                list.set(i, result);
                change |= result != value;
            }
            return change ? list.copy() : list;
        } else {
            Rule rule = ruleSet.get(name);
            if (rule == null) {
                return nbt;
            }
            if (nbt instanceof StringTag) {
                String value = nbt.asString();
                if (value.equals(rule.match)) {
                    System.out.println(" Replaced value for: " + name);
                    return StringTag.of(rule.replace.toString());
                }
            }
            return nbt;
        }
    }

    private static class RuleSet extends HashMap<String, Rule> {}

    private static class Rule {

        private final Object match;
        private final Object replace;

        private Rule(Object match, Object replace) {
            this.match = match;
            this.replace = replace;
        }
    }
}
